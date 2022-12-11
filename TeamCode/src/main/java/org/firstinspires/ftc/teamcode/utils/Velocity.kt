package org.firstinspires.ftc.teamcode.utils

/**
 * A class representing the velocity of a robot.
 *
 * @param v: The velocity vector, (x, y)
 * @param ω: The angular velocity, in radians per second
 */
data class Velocity(val v: Vector, val ω: Double = 0.0) {
    constructor(x: Double, y: Double, w: Double = 0.0) : this(Vector(x, y), w)

    val x = v.x
    val y = v.y
    val magnitude = v.magnitude
    val angle = v.angle
    val rotate = ω
    val unit = v.unit
}