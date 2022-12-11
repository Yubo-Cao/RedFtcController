package org.firstinspires.ftc.teamcode.utils

class Matrix(val rows: Int, val columns: Int) : Iterable<DoubleArray> {
    private val data = Array(rows) { DoubleArray(columns) }

    constructor(data: Array<DoubleArray>) : this(data.size, data[0].size) {
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                this.data[i][j] = data[i][j]
            }
        }
    }

    constructor(data: DoubleArray) : this(data.size, 1) {
        for (i in 0 until rows) {
            this.data[i][0] = data[i]
        }
    }

    operator fun get(row: Int) = Matrix(data[row])
    operator fun get(row: Int, column: Int) = data[row][column]
    operator fun get(row: Int, column: IntRange) = Matrix(data[row].slice(column).toDoubleArray())
    operator fun get(row: IntRange, column: Int) =
        Matrix(data.slice(row).map { it[column] }.toDoubleArray())

    operator fun get(row: IntRange, column: IntRange) =
        Matrix(data.slice(row).map { it.slice(column).toDoubleArray() }.toTypedArray())


    operator fun set(row: Int, column: Int, value: Double) {
        data[row][column] = value
    }

    operator fun set(row: Int, value: Matrix) {
        for (i in 0 until columns) {
            data[row][i] = value[0, i]
        }
    }

    operator fun set(row: Int, value: Iterable<Double>) {
        for (i in 0 until columns) {
            data[row][i] = value.elementAt(i)
        }
    }

    operator fun set(row: Int, value: DoubleArray) {
        for (i in 0 until columns) {
            data[row][i] = value[i]
        }
    }

    operator fun set(row: IntRange, column: Int, value: Matrix) {
        for (i in 0 until row.count()) {
            data[row.first + i][column] = value[i, 0]
        }
    }

    operator fun set(row: IntRange, column: Int, value: Iterable<Double>) {
        for (i in 0 until row.count()) {
            data[row.first + i][column] = value.elementAt(i)
        }
    }

    operator fun set(row: IntRange, column: Int, value: DoubleArray) {
        for (i in 0 until row.count()) {
            data[row.first + i][column] = value[i]
        }
    }

    operator fun set(row: Int, column: IntRange, value: Matrix) {
        for (i in 0 until column.count()) {
            data[row][column.first + i] = value[0, i]
        }
    }

    operator fun set(row: Int, column: IntRange, value: Iterable<Double>) {
        for (i in 0 until column.count()) {
            data[row][column.first + i] = value.elementAt(i)
        }
    }

    operator fun set(row: Int, column: IntRange, value: DoubleArray) {
        for (i in 0 until column.count()) {
            data[row][column.first + i] = value[i]
        }
    }

    operator fun set(row: IntRange, column: IntRange, value: Matrix) {
        for (i in 0 until row.count()) {
            for (j in 0 until column.count()) {
                data[row.first + i][column.first + j] = value[i, j]
            }
        }
    }

    operator fun set(row: IntRange, column: IntRange, value: Iterable<Double>) {
        for (i in 0 until row.count()) {
            for (j in 0 until column.count()) {
                data[row.first + i][column.first + j] = value.elementAt(i * column.count() + j)
            }
        }
    }

    operator fun set(row: IntRange, column: IntRange, value: DoubleArray) {
        for (i in 0 until row.count()) {
            for (j in 0 until column.count()) {
                data[row.first + i][column.first + j] = value[i * column.count() + j]
            }
        }
    }

    operator fun plus(other: Matrix): Matrix {
        if (rows != other.rows || columns != other.columns) {
            throw IllegalArgumentException("Matrix dimensions must agree")
        }

        val result = Matrix(rows, columns)
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                result[i, j] = data[i][j] + other[i, j]
            }
        }
        return result
    }

    operator fun plusAssign(other: Matrix) {
        if (rows != other.rows || columns != other.columns) {
            throw IllegalArgumentException("Matrix dimensions must agree")
        }

        for (i in 0 until rows) {
            for (j in 0 until columns) {
                data[i][j] += other[i, j]
            }
        }
    }

    operator fun plus(other: Double): Matrix {
        val result = Matrix(rows, columns)
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                result[i, j] = data[i][j] + other
            }
        }
        return result
    }

    operator fun plusAssign(other: Double) {
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                data[i][j] += other
            }
        }
    }

    operator fun unaryMinus(): Matrix {
        val result = Matrix(rows, columns)
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                result[i, j] = -data[i][j]
            }
        }
        return result
    }

    operator fun unaryPlus(): Matrix {
        return this // no-op
    }

    operator fun times(other: Matrix): Matrix {
        if (columns != other.rows) {
            throw IllegalArgumentException("Matrix dimensions must agree")
        }

        val result = Matrix(rows, other.columns)
        for (i in 0 until rows) {
            for (j in 0 until other.columns) {
                for (k in 0 until columns) {
                    result[i, j] += data[i][k] * other[k, j]
                }
            }
        }
        return result
    }

    operator fun timesAssign(other: Matrix) {
        if (columns != other.rows) {
            throw IllegalArgumentException("Matrix dimensions must agree")
        }

        for (i in 0 until rows) {
            for (j in 0 until other.columns) {
                for (k in 0 until columns) {
                    data[i][j] += data[i][k] * other[k, j]
                }
            }
        }
    }

    operator fun times(other: Double): Matrix {
        val result = Matrix(rows, columns)
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                result[i, j] = data[i][j] * other
            }
        }
        return result
    }


    override fun iterator(): Iterator<DoubleArray> {
        return data.iterator()
    }

    fun copy(): Matrix {
        val result = Matrix(rows, columns)
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                result[i, j] = data[i][j]
            }
        }
        return result
    }
}