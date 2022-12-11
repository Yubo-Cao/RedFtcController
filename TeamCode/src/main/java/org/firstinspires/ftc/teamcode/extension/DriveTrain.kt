package org.firstinspires.ftc.teamcode.extension

import org.firstinspires.ftc.teamcode.utils.Vector
import org.firstinspires.ftc.teamcode.utils.Velocity
import kotlin.math.abs
import kotlin.math.sqrt

interface DriveTrain {
    /**
     * Run the drive train at the given velocity. No dead zone handling.
     *
     * @param velocity The velocity to run the drive train at.
     */
    fun run(velocity: Velocity)

    /**
     * Run with a game pad stick. Dead zone handling is done.
     *
     * @param x: desired x-direction speed
     * @param y: desired y-direction speed
     * @param rotate: desired self-rotation (in yaw), default to 0.0
     * @param speed: desired speed, default to 0.75
     */
    fun run(
        x: Double,
        y: Double,
        rotate: Double = 0.0,
        speed: Double = 0.75
    ) {
        assert(x in -1.0..1.0)
        assert(y in -1.0..1.0)

        run(
            Velocity(
                Vector(
                    coerce(x, DEAD_ZONE_X),
                    coerce(y, DEAD_ZONE_Y),
                ) / sqrt(2.0) * speed,
                rotate
            ),
        )
    }


    private fun coerce(value: Double, deadZone: Double): Double {
        return if (abs(value) < deadZone) 0.0 else value
    }

    /**
     * Run forward
     *
     * @param speed: desired speed, default to 0.75
     */
    fun forward(speed: Double = 0.75) {
        run(0.0, 1.0, 0.0, speed)
    }

    /**
     * Run backward
     *
     * @param speed: desired speed, default to 0.75
     */
    fun backward(speed: Double = 0.75) {
        run(0.0, -1.0, 0.0, speed)
    }

    /**
     * Run left
     *
     * @param speed: desired speed, default to 0.75
     */
    fun left(speed: Double = 0.75) {
        run(-1.0, 0.0, 0.0, speed)
    }

    /**
     * Run right
     *
     * @param speed: desired speed, default to 0.75
     */
    fun right(speed: Double = 0.75) {
        run(1.0, 0.0, 0.0, speed)
    }

    /**
     * Stop
     */
    fun stop() {
        run(0.0, 0.0, 0.0, 0.0)
    }

    /**
     * Run command, written with DSL.
     *
     * @param command: the command to run
     * - ^, v, <, >: forward, backward, left, right
     * - s: stop
     * - append <decimal> in front of ^, v, <, > to specify speed; default to whatever passed in speed param.
     * - append :long after ^, v, <, > to specify sleep time; default to whatever passed in sleep param.
     */
    fun run(command: String, sleep: Double = 500.0, speed: Double = 0.75) {
        var idx = 0
        fun expect(c: Char) {
            if (idx >= command.length || command[idx] != c) {
                throw IllegalArgumentException("Expected $c at $idx")
            }
            idx++
        }

        fun decimal(): Double {
            var decimal = 0.0
            while (idx < command.length && command[idx] in '0'..'9') {
                decimal = decimal * 10 + (command[idx] - '0')
                idx++
            }
            if (command[idx] == '.') {
                idx++
                var decimalPlace = 0.1
                while (idx < command.length && command[idx] in '0'..'9') {
                    decimal += (command[idx] - '0') * decimalPlace
                    decimalPlace /= 10
                    idx++
                }
            }
            return decimal
        }

        fun speed(): Double {
            if (command[idx] == '<') {
                idx++
                val result = decimal()
                expect('>')
                return result
            }
            return speed
        }

        fun sleep(): Double {
            if (command[idx] == ':') {
                idx++
                return decimal()
            }
            return sleep
        }

        fun cmd(): (Double) -> Unit {
            return when (command[idx++]) {
                '^' -> { speed -> forward(speed) }
                'v' -> { speed -> backward(speed) }
                '<' -> { speed -> left(speed) }
                '>' -> { speed -> right(speed) }
                's' -> { _ -> stop() }
                else -> throw IllegalArgumentException("Unknown command at $idx")
            }
        }

        while (idx < command.length) {
            val a = speed()
            val b = sleep()
            val c = cmd()

            try {
                c(a)
            } catch (e: Exception) {
                stop()
                throw IllegalArgumentException("Invalid command at $idx")
            }
            Thread.sleep(b.toLong())
        }
    }

    companion object {
        const val DEAD_ZONE_X = 0.05
        const val DEAD_ZONE_Y = 0.05
    }
}