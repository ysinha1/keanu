package io.improbable.keanu.research

open class Array2D<T>(iSize: Int, jSize: Int, vals: (Int, Int) -> T) : IArray2D<T> {

    private val stride = jSize
    var data = ArrayList<T>(iSize * jSize)

    init {
        for (i in 0 until iSize) {
            for (j in 0 until jSize) {
                data.add(vals(i, j))
            }
        }
    }

    override operator fun get(i: Int, j: Int): T {
        return data[i * stride + j]
    }

    override operator fun set(i: Int, j: Int, v: T) {
        data[i * stride + j] = v
    }

    override fun iSize(): Int {
        return data.size / stride
    }

    override fun jSize(): Int {
        return stride
    }

    override fun toString(): String {
        var out = String()
        for (i in 0 until iSize()) {
            for (j in 0 until jSize()) {
                out += get(i, j).toString() + " "
            }
            out += "\n"
        }
        return out
    }

    fun horizontallyStack(rightArray: IArray2D<T>) : Array2D<T> {
        if (jSize() != rightArray.jSize()) {
            throw IllegalArgumentException("In order to merge horizontally, both array's vertical dimensions must be the same")
        }
        return Array2D(iSize()+rightArray.iSize(), jSize(),
            { i, j ->
                if (i < iSize()) {
                    return@Array2D this[i, j]
                } else {
                    return@Array2D rightArray[i-iSize(), j]
                }
        })
    }

    fun verticallyStack(bottomArray: IArray2D<T>) : Array2D<T> {
        if (iSize() != bottomArray.iSize()) {
            throw IllegalArgumentException("In order to merge vertically, both array's horizontal dimensions must be the same")
        }
        return Array2D(iSize(), jSize()+bottomArray.jSize(),
            { i, j ->
                if (j < jSize()) {
                    return@Array2D this[i, j]
                } else {
                    return@Array2D bottomArray[i,j-jSize()]
                }
            })
    }

    open class RotateClockwise90<T>(var data: IArray2D<T>) : IArray2D<T> {
        override fun get(i: Int, j: Int): T {
            return data.get(data.iSize() - j - 1, i)
        }

        override fun set(i: Int, j: Int, v: T) {
            data.set(data.iSize() - j - 1, i, v)
        }

        override fun iSize(): Int {
            return data.jSize()
        }

        override fun jSize(): Int {
            return data.iSize()
        }
    }


    open class RotateAntiClockwise90<T>(var data: IArray2D<T>) : IArray2D<T> {
        override fun get(i: Int, j: Int): T {
            return data.get(j, data.jSize() - i - 1)
        }

        override fun set(i: Int, j: Int, v: T) {
            data.set(j, data.jSize() - i - 1, v)
        }

        override fun iSize(): Int {
            return data.jSize()
        }

        override fun jSize(): Int {
            return data.iSize()
        }
    }


    open class Rotate180<T>(var data: IArray2D<T>) : IArray2D<T> {
        override fun get(i: Int, j: Int): T {
            return data.get(data.iSize() - i - 1, data.jSize() - j - 1)
        }

        override fun set(i: Int, j: Int, v: T) {
            data.set(data.iSize() - i - 1, data.jSize() - j - 1, v)
        }

        override fun iSize(): Int {
            return data.iSize()
        }

        override fun jSize(): Int {
            return data.jSize()
        }
    }
}