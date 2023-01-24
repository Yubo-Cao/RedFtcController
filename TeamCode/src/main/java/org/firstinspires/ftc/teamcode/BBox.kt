package org.firstinspires.ftc.teamcode

import org.opencv.core.Rect
import org.opencv.core.Rect2d

/**
 * A bounding box
 * @param x The X coordinate of the top left corner
 * @param y The Y coordinate of the top left corner
 * @param width The width of the box
 * @param height The height of the box
 * @constructor Creates a new BBox
 * @throws IllegalArgumentException If any of the parameters are negative
 */
data class BBox(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double
) {
    init {
        require(width >= 0) { "Width must be >= 0" }
        require(height >= 0) { "Height must be >= 0" }
        require(x >= 0) { "X must be >= 0" }
        require(y >= 0) { "Y must be >= 0" }
    }

    /**
     * The area of the BBox
     * @return The area of the BBox
     */
    val area: Double
        get() = width * height

    /**
     * The center X coordinate
     * @return The center X coordinate
     */
    val centerX: Double
        get() = x + width / 2

    /**
     * The center Y coordinate
     * @return The center Y coordinate
     */
    val centerY: Double
        get() = y + height / 2

    /**
     * Convenience constructor for BBox from integers
     */
    constructor(x: Int, y: Int, width: Int, height: Int) : this(
        x.toDouble(),
        y.toDouble(),
        width.toDouble(),
        height.toDouble()
    )

    companion object {
        /**
         * Create a BBox from an OpenCV [Rect]
         * @param rect The rect to convert
         * @return The BBox
         */
        fun fromRect(rect: Rect) = BBox(rect.x, rect.y, rect.width, rect.height)

        /**
         * Create a BBox from an OpenCV [Rect2d]
         * @param rect The rect to convert
         * @return The BBox
         */
        fun fromRect(rect: Rect2d) = BBox(rect.x, rect.y, rect.width, rect.height)

        /**
         * Create a BBox from an array of coordinates, `[xmin, ymin, xmax, ymax]`
         * @param arr The array to convert
         * @return The BBox
         */
        fun fromXYXY(x1: Double, y1: Double, x2: Double, y2: Double) =
            BBox(x1, y1, x2 - x1, y2 - y1)

        /**
         * Create a BBox from an array of coordinates, `[cx, cy, width, height]`
         * @param arr The array to convert
         * @return The BBox
         */
        fun fromCXCYWH(cX: Double, cY: Double, width: Double, height: Double) =
            BBox(cX - width / 2, cY - height / 2, width, height)
    }

    /**
     * Convert the BBox to an OpenCV Rect
     * @return The Rect
     */
    fun toRect() = Rect(x.toInt(), y.toInt(), width.toInt(), height.toInt())

    /**
     * Convert the BBox to an OpenCV Rect2d
     * @return The Rect2d
     */
    fun toRect2d() = Rect2d(x, y, width, height)

    /**
     * Convert the BBox to an array of coordinates, [xmin, ymin, xmax, ymax]
     * @return The array of coordinates
     */
    fun toXYXY() = listOf(x, y, x + width, y + height)

    /**
     * Convert the BBox to an array of coordinates, [cx, cy, width, height]
     * @return The array of coordinates
     */
    fun toCXCYWH() = listOf(x + width / 2, y + height / 2, width, height)

    /**
     * Calculate the intersection of this BBox and another
     * @param other The other BBox
     * @return The intersection of the two BBoxes
     */
    infix fun intersect(other: BBox): BBox {
        val x1 = x.coerceAtLeast(other.x)
        val y1 = y.coerceAtLeast(other.y)
        val x2 = (x + width).coerceAtMost(other.x + other.width)
        val y2 = (y + height).coerceAtMost(other.y + other.height)
        return fromXYXY(x1, y1, x2, y2)
    }

    /**
     * Calculate the union of this BBox and another
     * @param other The other BBox
     * @return The union of the two BBoxes
     */
    infix fun union(other: BBox): BBox {
        val x1 = x.coerceAtMost(other.x)
        val y1 = y.coerceAtMost(other.y)
        val x2 = (x + width).coerceAtLeast(other.x + other.width)
        val y2 = (y + height).coerceAtLeast(other.y + other.height)
        return fromXYXY(x1, y1, x2, y2)
    }

    /**
     * Scale the BBox by a different factor in each direction
     * @param scaleX The scale factor in the x direction
     * @param scaleY The scale factor in the y direction
     */
    fun scale(scaleX: Double, scaleY: Double) =
        BBox(x * scaleX, y * scaleY, width * scaleX, height * scaleY)

    /**
     * Scale the BBox by a single factor
     * @param scale The scale factor
     * @return The scaled BBox
     */
    fun scale(scale: Double) = scale(scale, scale)

    /**
     * Calculate the intersection over union of this BBox and another
     * @param other The other BBox
     * @return The intersection over union
     */
    infix fun iou(other: BBox): Double =
        (this intersect other).area / (this.area + other.area - (this intersect other).area)
}
