package io.improbable.keanu.research

interface IArray2D<T> {

    operator fun get(i: Int, j: Int): T
    operator fun set(i: Int, j: Int, v: T)
    fun iSize(): Int
    fun jSize(): Int
}