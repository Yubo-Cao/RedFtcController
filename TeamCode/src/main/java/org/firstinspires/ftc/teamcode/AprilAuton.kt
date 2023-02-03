/*
 * Copyright (c) 2021 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.openftc.apriltag.AprilTagDetection
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation

@Autonomous(name = "AprilAuton", group = "Autonomous")
class AprilAuton : LinearOpMode() {
    val camera: OpenCvCamera by lazy {
        val cameraMonitorViewId = hardwareMap.appContext
            .resources
            .getIdentifier(
                "cameraMonitorViewId",
                "id",
                hardwareMap.appContext.packageName
            )
        OpenCvCameraFactory.getInstance()
            .createWebcam(
                hardwareMap.get(WebcamName::class.java, "Webcam 1"), cameraMonitorViewId
            )
    }
    var aprilTagDetectionPipeline: AprilTagDetectionPipeline? = null

    // Pixel calibration for camera
    var fx = 578.272
    var fy = 578.272
    var cx = 402.145
    var cy = 221.506

    // Units are meters
    var tagSize = 0.166
    var ID_TAG_OF_INTEREST = 18 // Tag ID 18 from the 36h11 family
    var tagOfInterest: AprilTagDetection? = null
    override fun runOpMode() {
        aprilTagDetectionPipeline = AprilTagDetectionPipeline(tagSize, fx, fy, cx, cy)
        camera.setPipeline(aprilTagDetectionPipeline)
        camera.openCameraDeviceAsync(
            object : AsyncCameraOpenListener {
                override fun onOpened() {
                    camera.startStreaming(800, 448, OpenCvCameraRotation.UPRIGHT)
                }

                override fun onError(errorCode: Int) {}
            })
        telemetry.msTransmissionInterval = 50

        /*
         * The INIT-loop:
         * This REPLACES waitForStart!
         */
        while (!isStarted && !isStopRequested) {
            val currentDetections = aprilTagDetectionPipeline!!.latestDetections
            if (currentDetections.size != 0) {
                var tagFound = false
                for (tag in currentDetections) {
                    if (tag.id == ID_TAG_OF_INTEREST) {
                        tagOfInterest = tag
                        tagFound = true
                        break
                    }
                }
                if (tagFound) {
                    telemetry.addLine("Tag of interest is in sight!\n\nLocation data:")
                    tagToTelemetry(tagOfInterest)
                } else {
                    telemetry.addLine("Don't see tag of interest :(")
                    if (tagOfInterest == null) {
                        telemetry.addLine("(The tag has never been seen)")
                    } else {
                        telemetry.addLine("\nBut we HAVE seen the tag before; last seen at:")
                        tagToTelemetry(tagOfInterest)
                    }
                }
            } else {
                telemetry.addLine("Don't see tag of interest :(")
                if (tagOfInterest == null) {
                    telemetry.addLine("(The tag has never been seen)")
                } else {
                    telemetry.addLine("\nBut we HAVE seen the tag before; last seen at:")
                    tagToTelemetry(tagOfInterest)
                }
            }
            telemetry.update()
            sleep(20)
        }

        /*
         * The START command just came in: now work off the latest snapshot acquired
         * during the init loop.
         */

        /* Update the telemetry */
        if (tagOfInterest != null) {
            telemetry.addLine("Tag snapshot:\n")
            tagToTelemetry(tagOfInterest)
            telemetry.update()
        } else {
            telemetry.addLine(
                "No tag snapshot available, it was never sighted during the init loop :("
            )
            telemetry.update()
        }

        /* Actually do something useful */
        if (tagOfInterest == null) {
            /*
             * Insert your autonomous code here, presumably running some default configuration
             * since the tag was never sighted during INIT
             */
        } else {
            /*
             * Insert your autonomous code here, probably using the tag pose to decide your configuration.
             */

            // e.g.
            if (tagOfInterest!!.pose.x <= 20) {
                // do something
            } else if (tagOfInterest!!.pose.x >= 20 && tagOfInterest!!.pose.x <= 50) {
                // do something else
            } else if (tagOfInterest!!.pose.x >= 50) {
                // do something else
            }
        }

        /* You wouldn't have this in your autonomous, this is just to prevent the sample from ending */while (opModeIsActive()) {
            sleep(20)
        }
    }

    fun tagToTelemetry(detection: AprilTagDetection?) {
        telemetry.addLine(String.format("\nDetected tag ID=%d", detection!!.id))
        telemetry.addLine(
            String.format(
                "Translation X: %.2f feet",
                detection.pose.x * FEET_PER_METER
            )
        )
        telemetry.addLine(
            String.format(
                "Translation Y: %.2f feet",
                detection.pose.y * FEET_PER_METER
            )
        )
        telemetry.addLine(
            String.format(
                "Translation Z: %.2f feet",
                detection.pose.z * FEET_PER_METER
            )
        )
        telemetry.addLine(
            String.format(
                "Rotation Yaw: %.2f degrees", Math.toDegrees(
                    detection.pose.yaw
                )
            )
        )
        telemetry.addLine(
            String.format(
                "Rotation Pitch: %.2f degrees", Math.toDegrees(detection.pose.pitch)
            )
        )
        telemetry.addLine(
            String.format(
                "Rotation Roll: %.2f degrees", Math.toDegrees(
                    detection.pose.roll
                )
            )
        )
    }

    companion object {
        const val FEET_PER_METER = 3.28084
    }
}