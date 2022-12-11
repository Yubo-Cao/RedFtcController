package org.firstinspires.ftc.teamcode.utils

import org.firstinspires.ftc.robotcore.external.Telemetry

interface Handler {
    fun log(type: LogLevel, tag: String, message: String)
}

class HistoryHandler : Handler {
    val history = mutableListOf<String>()

    override fun log(type: LogLevel, tag: String, message: String) {
        history.add("$type/$tag: $message")
    }
}

class ConsoleHandler : Handler {
    override fun log(type: LogLevel, tag: String, message: String) {
        println("$type/$tag: $message")
    }
}

class TelemetryHandler(val telemetry: Telemetry) : Handler {
    override fun log(type: LogLevel, tag: String, message: String) {
        telemetry.addData("$type/$tag", message)
        telemetry.update()
    }
}

class LogcatHandler : Handler {
    override fun log(type: LogLevel, tag: String, message: String) {
        when (type) {
            LogLevel.DEBUG -> android.util.Log.d(tag, message)
            LogLevel.INFO -> android.util.Log.i(tag, message)
            LogLevel.WARN -> android.util.Log.w(tag, message)
            LogLevel.ERROR -> android.util.Log.e(tag, message)
        }
    }
}

class Handlers(vararg val handlers: Handler) : Handler {
    override fun log(type: LogLevel, tag: String, message: String) {
        handlers.forEach { it.log(type, tag, message) }
    }
}