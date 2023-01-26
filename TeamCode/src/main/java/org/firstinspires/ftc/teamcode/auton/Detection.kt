package org.firstinspires.ftc.teamcode.auton

import android.util.Log

data class Detection(val bbox: BBox, val label: String, val confidence: Double) {
    companion object {
        /**
         * Labels for the model
         */
        val LABELS = arrayOf("Dragon", "Robot", "Console")

        /**
         * Load a detection from an array.
         * The array should be in the format:
         * - 0: x1
         * - 1: y1
         * - 2: x2
         * - 3: y2
         * - 4: confidence
         * - 5+: confidence for each label.
         *
         * For example, if the model has 3 labels, the array should be:
         * [0, 0.1, 0, 0.2, 0.9, 0.1, 0.2, 0.7]
         * Represents a detection with a confidence of 0.9, and a label of "Console" with a confidence of 0.7
         * and a rectangle from (0, 0.1) to (0, 0.2)
         */
        fun from(arr: FloatArray, width: Double = 1.0, height: Double = 1.0): Detection {
            val array = arr.map { it.toDouble() }.toDoubleArray()
            if (array.size != 5 + LABELS.size) throw IllegalArgumentException(
                "Array must be of size ${5 + LABELS.size}"
            )
            try {
                return Detection(
                    BBox(array[0], array[1], array[2], array[3]).scale(width, height),
                    LABELS[array.sliceArray(5..array.size).withIndex().maxBy { it.value }.index],
                    array[4]
                )
            } catch (e: Exception) {
                Log.e("Detection", e.stackTraceToString())
                throw e
            }
        }
    }
}