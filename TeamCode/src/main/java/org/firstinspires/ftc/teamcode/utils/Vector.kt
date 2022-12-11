package org.firstinspires.ftc.teamcode.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector(val x: Double, val y: Double) {
    val magnitude: Double
        get() = sqrt(x * x + y * y)
    val angle: Double
        get() = atan2(y, x)
    val unit: Vector
        get() = copy(x = x / magnitude, y = y / magnitude)

    companion object {
        val zero = Vector(0.0, 0.0)
    }

    operator fun plus(other: Vector) = Vector(x + other.x, y + other.y)

    operator fun minus(other: Vector) = Vector(x - other.x, y - other.y)

    operator fun times(scalar: Double) = Vector(x * scalar, y * scalar)

    operator fun times(other: Vector) = x * other.x + y * other.y

    operator fun div(scalar: Double) = Vector(x / scalar, y / scalar)

    operator fun unaryMinus() = Vector(-x, -y)

    fun with(
        x: Double = Double.NaN,
        y: Double = Double.NaN,
        magnitude: Double = Double.NaN,
        angle: Double = Double.NaN
    ) = Vector(if (x.isNaN()) this.x else x, if (y.isNaN()) this.y else y)
        .let {
            if (magnitude.isNaN()) it else it.unit * magnitude
        }
        .let {
            if (angle.isNaN()) it
            else Vector(it.magnitude * cos(angle), it.magnitude * sin(angle))
        }

    fun project(other: Vector) = other.unit * (this * other.unit)

    fun rotate(angle: Double) = Vector(
        x * cos(angle) - y * sin(angle),
        x * sin(angle) + y * cos(angle)
    )

    operator fun compareTo(other: Vector) = magnitude.compareTo(other.magnitude)
    operator fun compareTo(other: Double) = magnitude.compareTo(other)
}