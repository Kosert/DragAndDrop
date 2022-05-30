package com.example.draganddrop.classic

sealed class Item(val type: ItemType) {

    abstract val value: String?

    data class FixedItem(override var value: String? = null) : Item(ItemType.FIXED_ITEM)
    data class SortableItem(override val value: String) : Item(ItemType.SORTABLE_ITEM)
}
