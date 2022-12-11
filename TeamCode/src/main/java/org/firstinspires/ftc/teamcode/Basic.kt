package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.teamcode.extension.Gripper
import org.firstinspires.ftc.teamcode.extension.LinearSlide
import org.firstinspires.ftc.teamcode.extension.MecanumDriveTrain
import org.firstinspires.ftc.teamcode.utils.*
import kotlin.math.sqrt

@TeleOp(name = "Basic Drive", group = "Interactive")
class Basic : OpMode() {
    private val drivetrain by lazy { MecanumDriveTrain(hardwareMap) }
    private val linearSlide by lazy { LinearSlide(hardwareMap) }
    private val gripper by lazy { Gripper(hardwareMap) }
    private val pad: Gamepad by lazy { gamepad1 }

    override fun init() {
        Logger.handler = Handlers(
            TelemetryHandler(telemetry),
            LogcatHandler()
        )
        Logger.logLevel = LogLevel.WARN
    }

    override fun loop() {
        val x = -pad.left_stick_x.toDouble()
        val y = -pad.left_stick_y.toDouble()

        drivetrain.run(
            x = x,
            y = y,
            speed = sqrt(x * x + y * y),
            rotate = pad.right_stick_x.toDouble(),
        )

        if (pad.left_trigger > 0.05) {
            linearSlide.up(1.0)
        } else if (pad.right_trigger > 0.05) {
            linearSlide.down(1.0)
        }

        if (pad.left_bumper) {
            gripper.open()
        } else if (pad.right_bumper) {
            gripper.close()
        }
    }
}