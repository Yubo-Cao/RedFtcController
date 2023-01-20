package org.firstinspires.ftc.teamcode

data class Detection(val bbox: BBox, val label: String, val confidence: Float) {
    companion object {
        val LABELS = arrayOf("Dragon", "Robot", "Console")
        fun fromArray(array: FloatArray, width: Float, height: Float): Detection {
            val bbox = BBox(array[0], array[1], array[2], array[3]).scale(
                width.toDouble(),
                height.toDouble()
            )
            val confidence = array[4]
            val labelIndex = array.sliceArray(5..array.size).withIndex().maxBy { it.value }.index
            val label = LABELS[labelIndex]
            return Detection(bbox, label, confidence)
        }
    }
}