package org.firstinspires.ftc.teamcode.extension

import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * Use 2 Servo to grip some object
 */
class Gripper(
    val hardwareMap: HardwareMap,
    val leftServoName: String = "leftGripper",
    val rightServoName: String = "rightGripper",
    val closedPosition: Double = 1.0,
) {
    private val leftServo by lazy { hardwareMap.servo[leftServoName] }
    private val rightServo by lazy { hardwareMap.servo[rightServoName] }

    var leftPosition: Double
        get() = leftServo.position
        set(value) {
            leftServo.position = value
        }

    var rightPosition: Double
        get() = rightServo.position
        set(value) {
            rightServo.position = value
        }

    /**
     * Open the gripper
     */
    fun open() {
        leftPosition = 0.0
        rightPosition = 0.0
    }

    /**
     * Close the gripper
     */
    fun close() {
        leftPosition = closedPosition
        rightPosition = closedPosition
    }
}