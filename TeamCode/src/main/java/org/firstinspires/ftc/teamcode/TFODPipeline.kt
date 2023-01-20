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
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
    val fileDescriptor = assetManager.openFd(modelPath)
    var fileChannel: FileChannel
    FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
        fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}

class TFODPipeline @JvmOverloads constructor(
    modelPath: String = "model.tflite",
    assetManager: AssetManager,
    val webcam: OpenCvWebcam,
    val model: Interpreter = Interpreter(loadModelFile(assetManager, modelPath)),
    val minConfidence: Float = 0.5f,
    val iouThreshold: Float = 0.5f,
) : OpenCvPipeline() {
    var viewportPaused = false

    override fun processFrame(input: Mat): Mat {
        val inputArray = Array(1) { Array(INPUT_SIZE) { Array(INPUT_SIZE) { FloatArray(3) } } }
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
        val outputArray = Array(1) { Array(25200) { FloatArray(8) } }
        model.run(inputArray, outputArray)
        val width = input.cols()
        val height = input.rows()

        val result = nms(outputArray[0].filter { it[4] > minConfidence }
            .map { Detection.fromArray(it, width.toFloat(), height.toFloat()) }.toMutableList())

        for (r in result) {
            Log.d(TAG, r.toString())
            Imgproc.rectangle(input, r.bbox.toRect(), Scalar(0.0, 255.0, 0.0), 2)
            Imgproc.putText(
                input, r.label + " " + r.confidence, Point(
                    r.bbox.x, r.bbox.y - Imgproc.getTextSize(
                        r.label, Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, 2, null
                    ).height
                ), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0.0, 255.0, 0.0), 2
            )
        }
        return input
    }

    override fun onViewportTapped() {
        viewportPaused = !viewportPaused
        if (viewportPaused) webcam.pauseViewport() else webcam.resumeViewport()
    }


    fun nms(detections: MutableList<Detection>): List<Detection> {
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

    companion object {
        private const val TAG = "TFODPipeline"
        private const val INPUT_SIZE = 640
    }
}