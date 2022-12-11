package org.firstinspires.ftc.teamcode.extension

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap

class LinearSlide(
    val hardwareMap: HardwareMap
) {
    val motor: DcMotor by lazy {
        hardwareMap[DcMotor::class.java, "linearSlide"].also {
            it.direction = DcMotorSimple.Direction.REVERSE
        }
    }

    var power: Double
        get() = motor.power
        set(value) {
            motor.power = value
        }

    /**
     * Move the linear slide to the given position.
     * TODO: A linear model would be necessary to make this work.
     */
    var height: Double
        get() = motor.currentPosition.toDouble()
        set(value) {
            motor.targetPosition = value.toInt()
        }

    /**
     * Move the linear slide up.
     */
    fun up(power: Double = 1.0) {
        this.power = power
    }

    /**
     * Move the linear slide down.
     */
    fun down(power: Double = 1.0) {
        this.power = -power
    }
}