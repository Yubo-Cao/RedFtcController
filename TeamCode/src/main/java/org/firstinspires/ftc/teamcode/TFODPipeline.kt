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


/**
 * A pipeline for running Tensorflow Object Detection
 *
 * @param webcam The webcam to use
 * @param assetManager The asset manager
 * @param path The path to the model
 * @constructor Creates a new TFODPipeline
 */
class TFODPipeline constructor(
    private val webcam: OpenCvWebcam,
    assetManager: AssetManager,
    path: String
) : OpenCvPipeline() {
    private val model: Interpreter = loadInterpreter(assetManager, path)
    private val minConfidence: Float = 0.5f
    private val iouThreshold: Float = 0.5f

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
         * The color of the bounding box
         */
        val COLOR = Scalar(0.0, 255.0, 0.0)

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
     * @param input The input image
     * @return The image with boxes drawn, or drawn "Inference timed out" if the model takes too long to run
     */
    override fun processFrame(input: Mat): Mat {
        val width = input.cols()
        val height = input.rows()

        preprocess(input)
        val result = withTimeout(5, TimeUnit.SECONDS) {
            model.run(inputArray, outputArray)
            true
        }


        if (result != true) {
            Imgproc.putText(
                input, "Inference timed out", Point(0.0, 0.0),
                Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, COLOR, 2
            )
            return input
        }


        _detections = nms(outputArray[0].filter { it[4] > minConfidence }
            .map { Detection.from(it, width.toDouble(), height.toDouble()) }
            .toMutableList())

        for (det in detections) {
            Log.d(TAG, det.toString())
            Imgproc.rectangle(input, det.bbox.toRect(), COLOR, 2)
            val size = Imgproc.getTextSize(det.label, Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, 2, null)
            Imgproc.putText(
                input, det.label + " " + det.confidence, Point(
                    det.bbox.x, det.bbox.y - size.height
                ), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, COLOR, 2
            )
            Imgproc.rectangle(
                input,
                Point(det.bbox.x, det.bbox.y),
                Point(det.bbox.x + size.width, det.bbox.y - size.height),
                COLOR,
                -1
            )
        }

        return input
    }

    /**
     * Preprocess the image, resize to INPUT_SIZE and copy to inputArray
     */
    private fun preprocess(input: Mat) {
        Imgproc.resize(input, resized, Size(INPUT_SIZE.toDouble(), INPUT_SIZE.toDouble()))
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val pixel = resized[i, j]
                inputArray[0][i][j][0] = pixel[0].toFloat()
                inputArray[0][i][j][1] = pixel[1].toFloat()
                inputArray[0][i][j][2] = pixel[2].toFloat()
            }
        }
    }

    /**
     * The viewport/camera stream
     */
    private var viewportPaused = false

    /**
     * Infer input, process, and output tensor buffer
     */
    private val inputArray = Array(1) { Array(INPUT_SIZE) { Array(INPUT_SIZE) { FloatArray(3) } } }
    private val outputArray = Array(1) { Array(25200) { FloatArray(8) } }
    private val resized = Mat()

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
     * @param detections the detections to perform NMS on
     * @return the detections after NMS
     */
    private fun nms(detections: MutableList<Detection>): List<Detection> {
        detections.sortWith { (_, _, ca), (_, _, b) -> -ca.compareTo(b) }
        val result: MutableList<Detection> = ArrayList()
        while (detections.isNotEmpty()) {
            val detection = detections.removeAt(0)
            result.add(detection)
            detections.removeAll { detection.bbox.iou(it.bbox) > iouThreshold }
        }
        return result
    }

    /**
     * Run a specific function with a timeout
     * @param timeout the timeout
     * @param timeUnit the time unit
     * @param block the function to run
     * @param message the message to log if the function times out
     * @return the result of the function
     */
    private fun <T> withTimeout(
        timeout: Long,
        timeUnit: TimeUnit,
        message: String? = null,
        block: () -> T
    ): T? {
        val future = executor.submit(block)
        return try {
            future.get(timeout, timeUnit)
        } catch (e: Exception) {
            Log.e(TAG, message ?: "Execution timed out")
            null
        }
    }
}