package org.firstinspires.ftc.teamcode.hardware

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorController
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import org.firstinspires.ftc.teamcode.utils.Logger

class VirtualDcMotor(private var name: String = "") : DcMotor {
    private var _direction = DcMotorSimple.Direction.FORWARD
    private var _power = 0.0
    private var _mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

    init {
        name = if (name == "") "VirtualDcMotor" else name
        if (name == "VirtualDcMotor") Logger.debug("VirtualDcMotor", "Initialized")
        else Logger.debug("VirtualDcMotor", "Initialized $name")
    }


    override fun getManufacturer(): HardwareDevice.Manufacturer {
        TODO("Not yet implemented")
    }

    override fun getDeviceName(): String {
        TODO("Not yet implemented")
    }

    override fun getConnectionInfo(): String {
        TODO("Not yet implemented")
    }

    override fun getVersion(): Int {
        TODO("Not yet implemented")
    }

    override fun resetDeviceConfigurationForOpMode() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun setDirection(direction: DcMotorSimple.Direction) {
        Logger.debug(name, "setDirection($direction)")
        _direction = direction
    }

    override fun getDirection(): DcMotorSimple.Direction {
        Logger.debug(name, "getDirection()")
        return _direction
    }

    override fun setPower(power: Double) {
        Logger.debug(name, "setPower($power)")
        _power = power
    }

    override fun getPower(): Double {
        Logger.debug(name, "getPower()")
        return _power
    }

    override fun getMotorType(): MotorConfigurationType {
        TODO("Not yet implemented")
    }

    override fun setMotorType(motorType: MotorConfigurationType?) {
        TODO("Not yet implemented")
    }

    override fun getController(): DcMotorController {
        TODO("Not yet implemented")
    }

    override fun getPortNumber(): Int {
        TODO("Not yet implemented")
    }

    override fun setZeroPowerBehavior(zeroPowerBehavior: DcMotor.ZeroPowerBehavior?) {
        TODO("Not yet implemented")
    }

    override fun getZeroPowerBehavior(): DcMotor.ZeroPowerBehavior {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java", ReplaceWith("TODO(\"Not yet implemented\")"))
    override fun setPowerFloat() {
        TODO("Not yet implemented")
    }

    override fun getPowerFloat(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setTargetPosition(position: Int) {
        TODO("Not yet implemented")
    }

    override fun getTargetPosition(): Int {
        TODO("Not yet implemented")
    }

    override fun isBusy(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCurrentPosition(): Int {
        Logger.debug(name, "getCurrentPosition()")
        return 0
    }

    override fun setMode(mode: DcMotor.RunMode) {
        Logger.debug(name, "setMode($mode)")
        _mode = mode
    }

    override fun getMode(): DcMotor.RunMode {
        Logger.debug(name, "getMode()")
        return _mode
    }
}