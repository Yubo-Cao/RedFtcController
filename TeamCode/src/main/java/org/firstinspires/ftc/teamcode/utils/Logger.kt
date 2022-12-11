package org.firstinspires.ftc.teamcode.utils

object Logger {
    var logLevel = LogLevel.INFO
    var handler: Handler = Handlers()

    fun log(type: LogLevel, tag: String, message: String) {
        val tag = if (tag == "") Thread.currentThread().stackTrace[3].className.split(".").last() else tag

        if (type >= logLevel) {
            if (message.length > 4000) {
                message.chunked(4000).forEach { log(type, tag, it) }
            } else {
                handler.log(type, tag, message)
            }
        }
    }

    fun debug(title: String, message: Any) {
        log(LogLevel.DEBUG, title, message.toString())
    }

    fun debug(message: Any) {
        log(LogLevel.DEBUG, "", message.toString())
    }

    fun info(title: String, message: Any) {
        log(LogLevel.INFO, title, message.toString())
    }

    fun info(message: Any) {
        log(LogLevel.INFO, "", message.toString())
    }

    fun warn(title: String, message: Any) {
        log(LogLevel.WARN, title, message.toString())
    }

    fun warn(message: Any) {
        log(LogLevel.WARN, "", message.toString())
    }

    fun error(title: String, message: Any) {
        log(LogLevel.ERROR, title, message.toString())
    }

    fun error(message: Any) {
        log(LogLevel.ERROR, "", message.toString())
    }
}

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}