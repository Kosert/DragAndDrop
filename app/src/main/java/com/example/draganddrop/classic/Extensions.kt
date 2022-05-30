package com.example.draganddrop.classic

fun <T> MutableList<T>.swap(index1: Int, index2: Int){
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

fun <T> List<T>.splitAt(index: Int): Pair<List<T>, List<T>> = Pair(
    this.subList(0, index),
    this.subList(index, this.size)
)