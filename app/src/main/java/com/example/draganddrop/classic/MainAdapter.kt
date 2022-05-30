package com.example.draganddrop.classic

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.draganddrop.databinding.ItemFixedLayoutBinding
import com.example.draganddrop.databinding.ItemLayoutBinding

class MainAdapter(
    private val itemTouchHelper: ItemTouchHelper
) : ListAdapter<Item, RecyclerView.ViewHolder>(diffCallback) {

    inner class ItemViewHolder(
        val binding: ItemLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        var item: Item.SortableItem? = null
            private set

        @SuppressLint("ClickableViewAccessibility")
        fun bindView(item: Item.SortableItem) {
            this.item = item
            binding.root.text = item.value
            binding.root.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(this)
                }
                return@setOnTouchListener false
            }
        }
    }

    inner class FixedItemHolder(
        val binding: ItemFixedLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        var item: Item.FixedItem? = null
            private set

        @SuppressLint("ClickableViewAccessibility")
        fun bindView(item: Item.FixedItem) {
            this.item = item
            binding.draggableText.isVisible = item.value != null
            binding.draggableText.text = item.value

            binding.draggableText.setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(this)
                }
                return@setOnTouchListener false
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Item.FixedItem -> ItemType.FIXED_ITEM
            is Item.SortableItem -> ItemType.SORTABLE_ITEM
        }.viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (ItemType(viewType)) {
            ItemType.FIXED_ITEM -> {
                FixedItemHolder(ItemFixedLayoutBinding.inflate(inflater, parent, false))
            }
            ItemType.SORTABLE_ITEM -> {
                ItemViewHolder(ItemLayoutBinding.inflate(inflater, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> holder.bindView(getItem(position) as Item.SortableItem)
            is FixedItemHolder -> holder.bindView(getItem(position) as Item.FixedItem)
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Item>() {

            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.type == newItem.type && oldItem.value == newItem.value
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.type == newItem.type && oldItem.value == newItem.value
            }
        }
    }
}