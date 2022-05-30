package com.example.draganddrop.classic2

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.draganddrop.R
import com.example.draganddrop.databinding.HeaderLayoutBinding
import com.example.draganddrop.databinding.ItemLayoutBinding
import com.example.draganddrop.databinding.PlaceholderItemBinding

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

    inner class HeaderItemHolder(
        val binding: HeaderLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.isGone = true
        }
    }

    inner class PlaceholderItemHolder(
        val binding: PlaceholderItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(item: Item.PlaceholderItem) {
            //binding.root.visibility = if (item.visible) View.INVISIBLE else View.GONE
//            binding.root.isVisible = item.visible
//            if (item.visible) {
//                this.itemView.setBackgroundColor(this.itemView.context.getColor(R.color.purple_200))
//            } else {
//                this.itemView.setBackgroundColor(Color.TRANSPARENT)
//            }

            this.itemView.isVisible = item.visible
            this.itemView.layoutParams = if (item.visible) {
                val height = this.itemView.context.resources.getDimensionPixelSize(R.dimen.itemHeight)
                RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            } else {
                RecyclerView.LayoutParams(0, 0)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Item.SortableItem -> ItemType.SORTABLE_ITEM
            is Item.Header -> ItemType.HEADER
            is Item.PlaceholderItem -> ItemType.PLACEHOLDER
        }.viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (ItemType(viewType)) {
            ItemType.SORTABLE_ITEM -> ItemViewHolder(
                ItemLayoutBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
            ItemType.HEADER -> HeaderItemHolder(
                HeaderLayoutBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
            ItemType.PLACEHOLDER -> PlaceholderItemHolder(
                PlaceholderItemBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ItemViewHolder -> holder.bindView(item as Item.SortableItem)
            is PlaceholderItemHolder -> holder.bindView(item as Item.PlaceholderItem)
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Item>() {

            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.type == newItem.type && oldItem.value == newItem.value
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                if (oldItem is Item.PlaceholderItem && newItem is Item.PlaceholderItem) {
                    return oldItem.visible == newItem.visible
                }

                return oldItem.type == newItem.type && oldItem.value == newItem.value
            }
        }
    }
}