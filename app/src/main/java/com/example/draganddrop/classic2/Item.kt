package com.example.draganddrop.classic2

sealed class Item(val type: ItemType) {

    abstract val value: String?

    data class SortableItem(override val value: String) : Item(ItemType.SORTABLE_ITEM)

    data class PlaceholderItem(var visible: Boolean = true) : Item(ItemType.PLACEHOLDER) {
        override val value: String?
            get() = null
    }

    class Header : Item(ItemType.HEADER) {
        override val value: String?
            get() = null

        override fun toString(): String = "HeaderItem"
    }
}
