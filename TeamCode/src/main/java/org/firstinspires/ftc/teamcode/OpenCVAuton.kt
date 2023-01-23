package org.firstinspires.ftc.teamcode

import android.util.Log
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation

@Autonomous(name = "Open CV Powered Auton")
class OpenCVAuton : LinearOpMode() {
    companion object {
        const val TFOD_MODEL_ASSET = "model.tflite"
        const val TAG = "OpenCVAuton"
        const val CAMERA_NAME = "Webcam 1"
    }

    private val driveTrain by lazy { DriveTrain(hardwareMap) }

    override fun runOpMode() {
        park()
    }

    private fun park() {
        val classification = classify()
        log("Classification: $classification")
        when (classification) {
            "Dragon" -> driveTrain.apply {
                blockBackward()
                blockLeft()
            }
            "Robot" -> driveTrain.apply {
                blockBackward()
            }
            "Console" -> driveTrain.apply {
                blockForward()
                blockRight()
            }
        }
        driveTrain.stop()
    }

    private fun classify(): String {
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
        val pipeline = TFODPipeline(webcam, ctx.assets, TFOD_MODEL_ASSET)
        webcam.setPipeline(pipeline)
        log("Request permission"); webcam.setMillisecondsPermissionTimeout(5000)

        webcam.openCameraDeviceAsync(object : AsyncCameraOpenListener {
            override fun onOpened() = webcam.startStreaming(1280, 720, OpenCvCameraRotation.UPRIGHT)
            override fun onError(errorCode: Int) = log("Error: $errorCode")
        })

        log("Ready to start"); waitForStart()
        telemetry.clearAll()

        var classification: String? = null
        while (classification == null) {
            classification = pipeline.classification
            safeSleep(1000)
        }
        webcam.stopStreaming()
        return classification
    }

    private fun safeSleep(millis: Long) = try {
        sleep(millis)
    } catch (e: InterruptedException) {
        log("Interrupted")
    }

    private fun log(msg: String): Unit = with(telemetry) {
        Log.d(TAG, msg)
        addLine(msg)
        update()
    }
}