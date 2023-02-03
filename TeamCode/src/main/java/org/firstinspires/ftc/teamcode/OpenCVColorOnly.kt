package org.firstinspires.ftc.teamcode

import android.util.Log
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.opencv.core.Mat
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation
import org.openftc.easyopencv.OpenCvPipeline

@Autonomous(name = "OpenCV Color Only")
class OpenCVColorOnly : LinearOpMode() {
    companion object {
        const val TAG = "OpenCVColorOnly"
        const val CAMERA_NAME = "Webcam 1"

        class ColorPipeline : OpenCvPipeline() {
            override fun processFrame(input: Mat): Mat {
                // detect rect
                val rects = mutableListOf<Mat>()

                return input
            }
        }
    }

    override fun runOpMode() {
        val ctx = hardwareMap.appContext
        val cameraMonitorViewId = ctx.resources.getIdentifier(
            "cameraMonitorViewId",
            "id",
            ctx.packageName,
        )
        val webcam = OpenCvCameraFactory.getInstance().createWebcam(
            hardwareMap[WebcamName::class.java, CAMERA_NAME],
            cameraMonitorViewId
        )
        log("Request permission"); webcam.setMillisecondsPermissionTimeout(5000)
        val pipeline = ColorPipeline()
        webcam.setPipeline(pipeline)
        log("Ready to start"); waitForStart()
        webcam.openCameraDeviceAsync(object : OpenCvCamera.AsyncCameraOpenListener {
            override fun onOpened() = webcam.startStreaming(1280, 720, OpenCvCameraRotation.UPRIGHT)
            override fun onError(errorCode: Int) = log("Error: $errorCode")
        })
    }

    private fun log(msg: String?) {
        telemetry.addData("Status", msg)
        telemetry.update()
        Log.d(TAG, msg ?: "null")
    }
}