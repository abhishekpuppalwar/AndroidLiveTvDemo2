package com.samples.tvapp

import java.util.*


/** Static utilities for collections  */
object CollectionUtils {
    /**
     * Returns an array with the arrays concatenated together.
     *
     * @see [Stackoverflow answer](http://stackoverflow.com/a/784842/1122089) by [Joachim Sauer](http://stackoverflow.com/users/40342/joachim-sauer)
     */
    fun <T> concatAll(first: Array<T>, vararg rest: Array<T>): Array<T> {
        var totalLength = first.size
        for (array in rest) {
            totalLength += array.size
        }
        val result = Arrays.copyOf(first, totalLength)
        var offset = first.size
        for (array in rest) {
            System.arraycopy(array, 0, result, offset, array.size)
            offset += array.size
        }
        return result
    }
}