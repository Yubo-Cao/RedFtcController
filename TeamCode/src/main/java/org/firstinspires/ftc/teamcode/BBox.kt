package org.firstinspires.ftc.teamcode

import org.opencv.core.Rect
import org.opencv.core.Rect2d

data class BBox(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double
) {
    val area: Double
        get() = width * height
    val centerX: Double
        get() = x + width / 2
    val centerY: Double
        get() = y + height / 2

    constructor(x: Int, y: Int, width: Int, height: Int) : this(
        x.toDouble(),
        y.toDouble(),
        width.toDouble(),
        height.toDouble()
    )

    companion object {
        fun fromRect(rect: Rect) = BBox(rect.x, rect.y, rect.width, rect.height)
        fun fromRect(rect: Rect2d) = BBox(rect.x, rect.y, rect.width, rect.height)

        fun fromXYXY(x1: Double, y1: Double, x2: Double, y2: Double) =
            BBox(x1, y1, x2 - x1, y2 - y1)

        fun fromCXCYWH(cX: Double, cY: Double, width: Double, height: Double) =
            BBox(cX - width / 2, cY - height / 2, width, height)

        fun iou(box1: BBox, box2: BBox): Double {
            val x1 = box1.x.coerceAtLeast(box2.x)
            val y1 = box1.y.coerceAtLeast(box2.y)
            val x2 = (box1.x + box1.width).coerceAtMost(box2.x + box2.width)
            val y2 = (box1.y + box1.height).coerceAtMost(box2.y + box2.height)
            val intersection = 0.0.coerceAtLeast(x2 - x1) * 0.0.coerceAtLeast(y2 - y1)
            val union = box1.width * box1.height + box2.width * box2.height - intersection
            return intersection / union
        }
    }

    fun toRect() = Rect(x.toInt(), y.toInt(), width.toInt(), height.toInt())
    fun toRect2d() = Rect2d(x, y, width, height)
    fun toXYXY() = listOf(x, y, x + width, y + height)
    fun toCXCYWH() = listOf(x + width / 2, y + height / 2, width, height)

    infix fun intersect(other: BBox): BBox {
        val x1 = x.coerceAtLeast(other.x)
        val y1 = y.coerceAtLeast(other.y)
        val x2 = (x + width).coerceAtMost(other.x + other.width)
        val y2 = (y + height).coerceAtMost(other.y + other.height)
        return BBox.fromXYXY(x1, y1, x2, y2)
    }

    infix fun union(other: BBox): BBox {
        val x1 = x.coerceAtMost(other.x)
        val y1 = y.coerceAtMost(other.y)
        val x2 = (x + width).coerceAtLeast(other.x + other.width)
        val y2 = (y + height).coerceAtLeast(other.y + other.height)
        return BBox.fromXYXY(x1, y1, x2, y2)
    }

    fun scale(scaleX: Double, scaleY: Double) =
        BBox(x * scaleX, y * scaleY, width * scaleX, height * scaleY)

    fun scale(scale: Double) = scale(scale, scale)

    fun iou(other: BBox): Double = BBox.iou(this, other)
}
