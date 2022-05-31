package com.example.draganddrop.classic2

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

/**
Swap all items in between.
For example, given: 3 <-> 6, it will do 3 swaps: 3 <-> 4, 4 <-> 5, 5 <-> 6

it solves case where array is: {Item, Item, Placeholder, Placeholder, Header}
If only Item and Header are swapped then all placeholders are incorrectly moved after Header.

Optional [listener] can be passed to be invoked after each swap
 */
fun <T> MutableList<T>.move(from: Int, to: Int, listener: (Int, Int) -> Unit = { _, _ -> }) {
    if (from > to) {
        (from downTo to + 1).forEach {
            this.swap(it, it - 1)
            listener(it, it - 1)
        }
    } else {
        (from until to).forEach {
            this.swap(it, it + 1)
            listener(it, it + 1)
        }
    }
}