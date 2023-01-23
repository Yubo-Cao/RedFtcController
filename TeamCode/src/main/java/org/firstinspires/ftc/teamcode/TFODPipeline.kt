package org.firstinspires.ftc.teamcode

import android.content.res.AssetManager
import android.util.Log
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvPipeline
import org.openftc.easyopencv.OpenCvWebcam
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.util.concurrent.TimeUnit


class TFODPipeline @JvmOverloads constructor(
    private val webcam: OpenCvWebcam,
    assetManager: AssetManager,
    path: String,
    private val model: Interpreter = loadInterpreter(assetManager, path),
    private val minConfidence: Float = 0.5f,
    private val iouThreshold: Float = 0.5f,
) : OpenCvPipeline() {

    companion object {
        /**
         * Tag for logging
         */
        const val TAG = "TFODPipeline"

        /**
         * Expect 640x640x3 input
         */
        const val INPUT_SIZE = 640

        /**
         * Executor for running inference
         */
        private val executor = java.util.concurrent.Executors.newSingleThreadExecutor()

        /**
         * Model initializer
         */
        fun loadInterpreter(assetManager: AssetManager, path: String): Interpreter {
            val fileDescriptor = assetManager.openFd(path)
            val modelBuffer = FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                val fileChannel = inputStream.channel
                val startOffset = fileDescriptor.startOffset
                val declaredLength = fileDescriptor.declaredLength
                fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            }
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(true)
            }
            return Interpreter(modelBuffer, options)
        }
    }

    /**
     * Analyzed results. Return the class with the largest bounding box
     */
    val classification: String
        get() = _detections.maxByOrNull { it.bbox.area }?.label ?: Detection.LABELS[1]

    /**
     * All the detections
     */
    val detections: List<Detection>
        get() = _detections

    /**
     * Get image, run model, draw boxes and update results
     */
    override fun processFrame(input: Mat): Mat {
        val resized = Mat()
        Imgproc.resize(input, resized, Size(INPUT_SIZE.toDouble(), INPUT_SIZE.toDouble()))
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val pixel = resized[i, j]
                inputArray[0][i][j][0] = pixel[0].toFloat()
                inputArray[0][i][j][1] = pixel[1].toFloat()
                inputArray[0][i][j][2] = pixel[2].toFloat()
            }
        }
        val infer = Runnable { model.run(inputArray, outputArray) }
        val future = executor.submit(infer)
        try {
            future.get(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            Log.e(TAG, "Inference timed out")
            val width = input.cols()
            val height = input.rows()
            Imgproc.putText(
                input, "Inference timed out", Point(
                    width / 2.0, height / 2.0
                ), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(0.0, 0.0, 255.0), 2
            )
            return input
        }

        val width = input.cols()
        val height = input.rows()

        _detections = nms(outputArray[0].filter { it[4] > minConfidence }
            .map { Detection.fromArray(it, width.toFloat(), height.toFloat()) }
            .toMutableList()).map {
            Log.d(TAG, it.toString())
            Imgproc.rectangle(input, it.bbox.toRect(), Scalar(0.0, 255.0, 0.0), 2)
            val size = Imgproc.getTextSize(it.label, Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, 2, null)
            Imgproc.putText(
                input, it.label + " " + it.confidence, Point(
                    it.bbox.x, it.bbox.y - size.height
                ), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0.0, 255.0, 0.0), 2
            )
            Imgproc.rectangle(
                input,
                Point(it.bbox.x, it.bbox.y),
                Point(it.bbox.x + size.width, it.bbox.y - size.height),
                Scalar(0.0, 255.0, 0.0),
                -1
            )
            it
        }

        return input
    }

    /**
     * The viewport/camera stream
     */
    private var viewportPaused = false

    /**
     * Infer input and output tensor buffer
     */
    private val inputArray = Array(1) { Array(INPUT_SIZE) { Array(INPUT_SIZE) { FloatArray(3) } } }
    private val outputArray = Array(1) { Array(25200) { FloatArray(8) } }

    /**
     * All the detections
     */
    private var _detections = listOf<Detection>()


    /**
     * Toggle the viewport
     */
    override fun onViewportTapped() {
        viewportPaused = !viewportPaused
        if (viewportPaused) webcam.pauseViewport() else webcam.resumeViewport()
    }

    /**
     * Perform non-maximum suppression
     */

    private fun nms(detections: MutableList<Detection>): List<Detection> {
        detections.sortWith { (_, _, ca), (_, _, b) -> -ca.compareTo(b) }
        val result: MutableList<Detection> = ArrayList()
        while (detections.isNotEmpty()) {
            val detection = detections.removeAt(0)
            result.add(detection)
            val removeList: MutableList<Detection> = ArrayList()
            for (d in detections) {
                if (BBox.iou(detection.bbox, d.bbox) > iouThreshold) {
                    removeList.add(d)
                }
            }
            detections.removeAll(removeList)
        }
        return result
    }
}