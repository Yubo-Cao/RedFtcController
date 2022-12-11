package org.firstinspires.ftc.teamcode.extension

import android.graphics.Bitmap
import android.graphics.ImageFormat
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.android.util.Size
import org.firstinspires.ftc.robotcore.external.function.Continuation
import org.firstinspires.ftc.robotcore.external.hardware.camera.*
import org.firstinspires.ftc.robotcore.external.hardware.camera.Camera
import org.firstinspires.ftc.robotcore.external.hardware.camera.Camera.StateCallback
import org.firstinspires.ftc.robotcore.internal.system.Deadline
import org.firstinspires.ftc.teamcode.utils.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Camera(
    idx: Int? = null,
    val name: String = "",
    cameraName: CameraName? = null,
    hardwareMap: HardwareMap? = null,
    val format: Int = ImageFormat.YUY2,
    size: Size? = null,
    fps: Int = -1
) : AutoCloseable {
    /**
     * Camera manager
     */
    private val cameraManager by lazy { ClassFactory.getInstance().cameraManager }

    /**
     * Camera name
     */
    private val cameraName by lazy {
        when {
            cameraName != null -> cameraName
            idx != null -> cameraManager.allWebcams[idx]
            name != "" -> {
                if (hardwareMap == null) {
                    throw IllegalArgumentException("hardwareMap must be provided if name is provided")
                }
                hardwareMap[WebcamName::class.java, name]
            }
            else -> throw IllegalArgumentException("Must provide idx, name, or cameraName")
        }
    }

    /**
     * Has camera been used?
     */
    private var used = false

    /**
     * The real camera device
     */
    private val camera by lazy {
        used = true
        val callback by lazy {
            object : StateCallback {
                override fun onOpened(camera: Camera) {
                    Logger.debug("$this opened")
                }

                override fun onOpenFailed(cameraName: CameraName, reason: Camera.OpenFailure) {
                    Logger.debug("$this open failed: $reason")
                }

                override fun onClosed(camera: Camera) {
                    Logger.debug("$this closed")
                }

                override fun onError(camera: Camera, error: Camera.Error?) {
                    Logger.debug("$this error: $error")
                }
            }
        }

        val timeout by lazy {
            Deadline(5, TimeUnit.SECONDS)
        }
        cameraManager.requestPermissionAndOpenCamera(
            timeout, this.cameraName, cont(callback)
        )
    }

    /**
     * Characterize the camera
     */
    val characteristics: CameraCharacteristics = this.cameraName.cameraCharacteristics

    /**
     * Width, height
     */
    val size: Size = size ?: characteristics.getDefaultSize(this.format)
    val width: Int = this.size.width
    val height: Int = this.size.height

    /**
     * FPS
     */
    val fps: Int =
        if (fps == -1) characteristics.getMaxFramesPerSecond(this.format, this.size) else fps

    /**
     * Current frame
     */
    val currentFrame: Bitmap
        get() {
            val request = camera.createCaptureRequest(this.format, this.size, this.fps)
            val result = request.createEmptyBitmap()
            val session = camera.createCaptureSession(cont(errorCallback))
            var obtained = false
            val sequenceId = session.startCapture(
                request,
                cont(object : CameraCaptureSession.CaptureCallback {
                    override fun onNewFrame(
                        session: CameraCaptureSession,
                        request: CameraCaptureRequest,
                        cameraFrame: CameraFrame
                    ) {
                        cameraFrame.copyToBitmap(result)
                        Logger.debug("Got frame")
                        obtained = true
                    }
                }),
                cont(finishCallback)
            )
            Logger.debug("Sequence ID: $sequenceId")
            while (!obtained) Thread.sleep(100)
            session.apply {
                stopCapture()
                close()
            }
            return result
        }


    override fun toString() =
        "${if (cameraName is WebcamName) "Webcam" else "Camera"}x${if (name != "") " $name" else ""}"

    /**
     * Close the camera. Must be called when done
     */
    override fun close() {
        if (!used) Logger.debug("Camera $this was never used")
        Logger.debug("Closing $this")
        camera.close()
    }


    fun <T> cont(callback: T) = Continuation.create(exe, callback)

    // create continuation
    private val exe by lazy { Executors.newSingleThreadExecutor() }

    // capture session status callback
    private val finishCallback by lazy {
        object : CameraCaptureSession.StatusCallback {
            override fun onCaptureSequenceCompleted(
                session: CameraCaptureSession,
                cameraCaptureSequenceId: CameraCaptureSequenceId?,
                lastFrameNumber: Long
            ) {
                Logger.debug("$this capture sequence completed (id: $cameraCaptureSequenceId, last frame number: $lastFrameNumber)")
            }
        }
    }

    // capture session status callback
    private val errorCallback by lazy {
        object : CameraCaptureSession.StateCallback {
            override fun onConfigured(session: CameraCaptureSession) {
                Logger.debug("$this capture session is configured")
            }

            override fun onClosed(session: CameraCaptureSession) {
                Logger.debug("$this capture session is closed")
            }
        }
    }
}