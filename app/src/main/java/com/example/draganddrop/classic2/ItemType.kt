package com.example.draganddrop.classic2

enum class ItemType(val viewType: Int) {

    SORTABLE_ITEM(0),
    PLACEHOLDER(1),
    HEADER(2),
    ;

    companion object {
        operator fun invoke(value: Int) = values().first { it.viewType == value }
    }
}