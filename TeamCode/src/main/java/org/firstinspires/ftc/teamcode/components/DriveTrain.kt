package org.firstinspires.ftc.teamcode.components

import android.util.Log
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class DriveTrain(map: HardwareMap) {
    private val backLeft: DcMotor
    private val backRight: DcMotor
    private val frontLeft: DcMotor
    private val frontRight: DcMotor
    private val time = ElapsedTime()

    init {
        backLeft = map.get(DcMotor::class.java, "backLeft")
        backRight = map.get(DcMotor::class.java, "backRight")
        frontLeft = map.get(DcMotor::class.java, "frontLeft")
        frontRight = map.get(DcMotor::class.java, "frontRight")
        backLeft.direction = DcMotorSimple.Direction.FORWARD
        backRight.direction = DcMotorSimple.Direction.REVERSE
        frontLeft.direction = DcMotorSimple.Direction.FORWARD
        frontRight.direction = DcMotorSimple.Direction.REVERSE
    }

    /**
     * Waits for a certain amount of time
     *
     * @param ms the amount of time to wait in milliseconds
     */
    fun wait(ms: Double) {
        time.reset()
        while (time.milliseconds() < ms) {
            Log.d("DriveTrain", "Waiting for " + (ms - time.milliseconds()) + "ms")
        }
    }

    /**
     * Sets the power of all motors
     *
     * @param pfr the power of the front right motor
     * @param pfl the power of the front left motor
     * @param pbr the power of the back right motor
     * @param pbl the power of the back left motor
     */
    fun powers(pfr: Double, pfl: Double, pbr: Double, pbl: Double) {
        backRight.power = pbr
        backLeft.power = pbl
        frontRight.power = pfr
        frontLeft.power = pfl
    }

    /**
     * Moves the robot
     *
     * @param x the x component of the vector
     * @param y the y component of the vector
     * @param rotate the rotation component of the vector
     */
    @JvmOverloads
    fun move(x: Double, y: Double, rotate: Double = 0.0) {
        val r = hypot(x, -y)
        val robotAngle = atan2(-y, x) - Math.PI / 4
        val rightX = rotate / 1.25
        val v1 = r * cos(robotAngle) + rightX
        val v2 = r * sin(robotAngle) - rightX
        val v3 = r * sin(robotAngle) + rightX
        val v4 = r * cos(robotAngle) - rightX
        powers(v1, v4, v3, v2)
    }

    /** Moves the robot forward for a block  */
    fun blockForward() {
        move(0.0, 0.75)
        wait(1000.0)
        stop()
    }

    /** Moves the robot backward for a block  */
    fun blockBackward() {
        move(0.0, -0.75)
        wait(1000.0)
        stop()
    }

    /** Moves the robot left for a block  */
    fun blockLeft() {
        move(-0.75, 0.0)
        wait(1000.0)
        stop()
    }

    /** Moves the robot right for a block  */
    fun blockRight() {
        move(0.75, 0.0)
        wait(1000.0)
        stop()
    }

    /** Stops the robot  */
    fun stop() {
        backLeft.power = 0.0
        backRight.power = 0.0
        frontLeft.power = 0.0
        frontRight.power = 0.0
        time.reset()
    }
}