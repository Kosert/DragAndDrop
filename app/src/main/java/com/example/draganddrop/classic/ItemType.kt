package com.example.draganddrop.classic

enum class ItemType(val viewType: Int) {

    FIXED_ITEM(0),
    SORTABLE_ITEM(1),
    ;

    companion object {
        operator fun invoke(value: Int) = values().first { it.viewType == value }
    }
}