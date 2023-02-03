package org.firstinspires.ftc.teamcode

import android.util.Log
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.ColorSensor

@Autonomous(name = "Color Sensor Autonomous")
class ColorSensorAuton : LinearOpMode() {
    companion object {
        const val TAG = "ColorSensorAuton"
    }

    private val colorSensor by lazy {
        hardwareMap.get(ColorSensor::class.java, "colorSensor")
    }

    override fun runOpMode() {
        log("Ready to start"); waitForStart()
        telemetry.clearAll()
        while (true) {
            log("Color: ${colorSensor.red()}, ${colorSensor.green()}, ${colorSensor.blue()}, ${colorSensor.alpha()}")
        }
    }

    private fun log(msg: String?) {
        telemetry.addData("Status", msg)
        telemetry.update()
        Log.d(TAG, msg ?: "null")
    }
}