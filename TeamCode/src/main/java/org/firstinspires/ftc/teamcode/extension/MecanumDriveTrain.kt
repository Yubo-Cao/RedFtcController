package org.firstinspires.ftc.teamcode.extension

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.utils.Logger.debug
import org.firstinspires.ftc.teamcode.utils.Velocity

/**
 * A drive train that uses mecanum wheels.
 *
 * @param hardwareMap The hardware map to use.
 * @param width The distance between the left and right wheels.
 * @param height The distance between the front and back wheels.
 */
class MecanumDriveTrain(
    val hardwareMap: HardwareMap,
    val width: Double = 0.0,
    val height: Double = 0.0
) : DriveTrain {
    private val backLeft: DcMotor by lazy { hardwareMap[DcMotor::class.java, "backLeft"] }
    private val backRight: DcMotor by lazy { hardwareMap[DcMotor::class.java, "backRight"] }
    private val frontLeft: DcMotor by lazy { hardwareMap[DcMotor::class.java, "frontLeft"] }
    private val frontRight: DcMotor by lazy { hardwareMap[DcMotor::class.java, "frontRight"] }


    init {
        debug("DriveTrain", "Initialized")
    }

    override fun run(velocity: Velocity) {
        var (v, ω) = velocity
        v = v.with(angle = v.angle - Math.PI / 4)
        val (x, y) = v

        frontLeft.power = x + ω
        frontRight.power = y - ω
        backLeft.power = y + ω
        backRight.power = x - ω

        debug(
            "DriveTrain",
            "Running at $velocity with ${backLeft.power}, ${backRight.power}, ${frontLeft.power}, ${frontRight.power}"
        )
    }
}