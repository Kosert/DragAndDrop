package com.example.draganddrop.classic2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.rangeTo
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.draganddrop.classic.splitAt
import com.example.draganddrop.classic.swap
import com.example.draganddrop.classic.viewBinding
import com.example.draganddrop.databinding.ActivityClassic2Binding
import kotlin.math.abs

class Classic2Activity : AppCompatActivity() {

    private val binding by viewBinding(ActivityClassic2Binding::inflate)
    private val adapter: MainAdapter by lazy { MainAdapter(itemTouchHelper) }
    private val layoutManager by lazy { LinearLayoutManager(this) }

    private val droppableSlots = 4
    private val items = mutableListOf<Item>(
        *Array(droppableSlots) { Item.PlaceholderItem() },
        Item.Header(),
        Item.SortableItem("Jeden"),
        Item.SortableItem("Dwa"),
        Item.SortableItem("Trzy"),
        Item.SortableItem("Cztery"),
        Item.SortableItem("Pięć"),
        Item.SortableItem("Sześć"),
    )

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                private var draggingHolder: MainAdapter.ItemViewHolder? = null
                private var hoveredHolder: RecyclerView.ViewHolder? = null

                override fun isLongPressDragEnabled(): Boolean = false
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

                // invoked when one ViewHolder moves over another
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    //only item type can be grabbed
                    draggingHolder = viewHolder as MainAdapter.ItemViewHolder
                    hoveredHolder = target

                    val from = viewHolder.bindingAdapterPosition
                    val to = target.bindingAdapterPosition

                    Log.d("XD", "Swap: $from - ${items[from]} -> $to ${items[to]}")
                    // swap all items in between
                    // i.e.: 3 -> 6 should swap: 3 <-> 4, 4 <-> 5, 5 <-> 6
                    // it solves this case:
                    // Item, Item, Item, Item, Placeholder, Placeholder, Placeholder, Placeholder, Header
                    // Swap Item with Header and all placeholders are incorrectly moved after header
                    if (from > to) {
                        (from downTo to + 1).forEach {
                            Log.d("XD", "$it -> ${it - 1}")
                            items.swap(it, it - 1)
                            adapter.notifyItemMoved(it, it - 1)
                        }
                    } else {
                        (from until to).forEach {
                            Log.d("XD", "$it -> ${it + 1}")
                            items.swap(it, it + 1)
                            adapter.notifyItemMoved(it, it + 1)
                        }
                    }

                    when (target) {
                        is MainAdapter.HeaderItemHolder -> {
                            checkPlaceholders()
                        }
                    }

                    return true
                }

                // check that there are exactly [droppableSlots] items before header in the list
                private fun checkPlaceholders() {
                    val headerIndex = {
                        items.filter { it !is Item.PlaceholderItem || it.visible }
                            .indexOfFirst { it is Item.Header }
                    }

                    Log.d("XD", "Items: $items")
                    while (headerIndex() > droppableSlots) {
                        val firstIndex = items.indexOfFirst { it is Item.PlaceholderItem && it.visible }
//                        if (firstIndex == -1) {
//                            Log.d("XD", "REMOVING PLACEHOLDER: INDEX OF FIRST $firstIndex")
//                            break
//                        }
                        (items[firstIndex] as Item.PlaceholderItem).visible = false
                        Log.d("XD", "removing placeholder")
                        adapter.notifyItemChanged(firstIndex)
                    }

                    while (headerIndex() < droppableSlots) {
                        val firstIndex = items.indexOfFirst { it is Item.PlaceholderItem && !it.visible }
//                        if (firstIndex == -1) {
//                            Log.d("XD", "ADDING PLACEHOLDER: INDEX OF FIRST $firstIndex")
//                            break
//                        }
                        (items[firstIndex] as Item.PlaceholderItem).visible = true
                        Log.d("XD", "adding placeholder $firstIndex")
                        //fixme for some reason one item notify doesn't work properly
//                        adapter.notifyItemChanged(firstIndex)
                        adapter.notifyItemRangeChanged(0, droppableSlots)
                    }
                }

                // here we handle drag start
                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)
                    if (actionState != ItemTouchHelper.ACTION_STATE_DRAG)
                        return

                    viewHolder?.itemView?.alpha = 0.5f
                }

                // here we handle drag end
                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    viewHolder.itemView.alpha = 1f

                    Log.d("XD", "Before $items")
//                    val headerIndex = items.indexOfFirst { it is Item.Header }
//                    val (beforeHeader, rest) = items.splitAt(headerIndex)
//                    val sorted = beforeHeader.sortedBy { it.type.viewType }
//                    val newList = sorted + rest
//                    Log.d("XD", "After: $newList")

                    val placeholders = items.filter { it is Item.PlaceholderItem }
                    val newList = items
                        .filterNot { it is Item.PlaceholderItem }
                        .toMutableList()
                    val headerIndex = newList.indexOfFirst { it is Item.Header }
                    newList.addAll(headerIndex, placeholders)

                    Log.d("XD", "After: $newList")
                    items.clear()
                    items.addAll(newList)
                    //todo with move animation
                    recyclerView.adapter?.notifyDataSetChanged()//0, headerIndex)

                    draggingHolder = null
                    hoveredHolder = null
                }
            }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.recyclerMain.layoutManager = layoutManager
        binding.recyclerMain.setHasFixedSize(true)
        binding.recyclerMain.adapter = adapter.also {
            it.submitList(items)
        }

        itemTouchHelper.attachToRecyclerView(binding.recyclerMain)
    }

}

