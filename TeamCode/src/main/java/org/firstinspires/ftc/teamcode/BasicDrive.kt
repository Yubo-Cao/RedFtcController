package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Servo
import kotlin.math.*

/**
 * Basic remote operation program for PowerPlay.
 *
 *  - This class supports the following operations: - Drive train. Left stick controls forward
 * and backward motion. Right stick controls self-rotation. Mecanum wheels.
 *  - Intake. When left trigger is pressed, linear slide moves down. When right trigger is
 * pressed, linear slide moves up. When b is pressed, servo opens. When a is pressed, servo
 * closes.
 */
@TeleOp(name = "Basic Drive")
class BasicDrive : OpMode() {
    /**
     * Initialize. Because of by lazy delegate, this function is empty and is only used to
     * suffice all the AbstractOpMode requirements.
     */
    override fun init() {

    }

    /**
     * Run continuously. This function is called repeatedly in a loop.
     */
    override fun loop() {
        intakeArm.power = if (pad.right_trigger == 1f) .5 else .05

        if (pad.b) {
            gripServerLeft.position = 0.25
            gripServoRight.position = 0.25
        }
        if (pad.a) {
            gripServerLeft.position = 0.75
            gripServoRight.position = 0.75
        }

        if (pad.left_trigger >= 0.05)
            linearSlide.power = -1.0
        else if (gamepad1.right_trigger >= 0.05)
            linearSlide.power = 0.4
        else
            linearSlide.power = -0.16

        mecanumWheel()
    }

    /**
     * Mecanum wheel drive train. This function is called in loop() to control the drive
     * train.
     */
    private fun mecanumWheel() {
        val dx = if (abs(pad.left_stick_x) < 0.05) 0.0 else -pad.left_stick_x.toDouble()
        val dy = if (abs(pad.right_stick_x) < 0.05) 0.0 else -pad.right_stick_x.toDouble()
        val dr = if (abs(pad.left_stick_y) < 0.05) 0.0 else pad.left_stick_y.toDouble()
        val r = hypot(dx, -dy)
        val robotAngle = atan2(-dy, dx) - Math.PI / 4
        val rightX = dr / 1.25

        frontRight.power = (r * cos(robotAngle) + rightX) * .75
        frontLeft.power = (r * cos(robotAngle) - rightX) * .75
        backRight.power = (r * sin(robotAngle) + rightX) * .75
        backLeft.power = (r * sin(robotAngle) - rightX) * .75
    }

    override fun stop() {}

    /**
     * Drive train motors. These motors are used to control the drive train.
     */
    private val backLeft by lazy {
        hardwareMap[DcMotor::class.java, "backLeft"]
            .apply { direction = DcMotorSimple.Direction.FORWARD }
    }
    private val backRight by lazy {
        hardwareMap[DcMotor::class.java, "backRight"]
            .apply { direction = DcMotorSimple.Direction.REVERSE }
    }
    private val frontLeft by lazy {
        hardwareMap[DcMotor::class.java, "frontLeft"]
            .apply { direction = DcMotorSimple.Direction.FORWARD }
    }
    private val frontRight by lazy {
        hardwareMap[DcMotor::class.java, "frontRight"]
            .apply { direction = DcMotorSimple.Direction.REVERSE }
    }

    /**
     * Intake motors. These motors are used to control the intake.
     */
    private val intakeArm by lazy {
        hardwareMap[DcMotor::class.java, "baseArm"]
    }
    private val gripServerLeft by lazy {
        hardwareMap[Servo::class.java, "gripServo1"]
    }
    private val gripServoRight by lazy {
        hardwareMap[Servo::class.java, "gripServo2"]
            .apply { direction = Servo.Direction.REVERSE }
    }
    private val linearSlide by lazy {
        hardwareMap[DcMotor::class.java, "linearSlide"]
    }

    /**
     * The default gamepad. This is the gamepad that is used to control the robot.
     */
    private val pad by lazy { gamepad1 }
}