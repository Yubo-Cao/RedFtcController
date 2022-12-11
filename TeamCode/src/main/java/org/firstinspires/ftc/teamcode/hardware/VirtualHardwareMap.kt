package org.firstinspires.ftc.teamcode.hardware

import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap

object VirtualHardwareMap : HardwareMap(null, null) {
    // appContext is required param. However, this property is never used in the SDK.
    // So, we can pass null.
    private val map = mutableMapOf<String, HardwareDevice>()

    override fun get(name: String?): HardwareDevice {
        return map[name]!!
    }

    operator fun set(name: String, device: HardwareDevice?) {
        map[name] = device!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> get(classOrInterface: Class<out T>?, name: String?): T {
        return map[name] as T
    }
}