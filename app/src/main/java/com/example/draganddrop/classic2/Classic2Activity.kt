package com.example.draganddrop.classic2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.draganddrop.classic.viewBinding
import com.example.draganddrop.databinding.ActivityClassic2Binding

class Classic2Activity : AppCompatActivity() {

    private val binding by viewBinding(ActivityClassic2Binding::inflate)
    private val adapter: MainAdapter by lazy { MainAdapter(itemTouchHelper) }
    private val layoutManager by lazy { LinearLayoutManager(this) }

    private val droppableSlots = 4
    private val items = mutableListOf<Item>(
        *Array(droppableSlots) { Item.PlaceholderItem() },
        Item.Header(),
        Item.SortableItem("First"),
        Item.SortableItem("Second"),
        Item.SortableItem("Third"),
        Item.SortableItem("Fourth"),
        Item.SortableItem("Fifth"),
        Item.SortableItem("Sixth"),
        Item.SortableItem("Seventh"),
    )

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                override fun isLongPressDragEnabled(): Boolean = false
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

                // invoked when one ViewHolder moves over another
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val from = viewHolder.bindingAdapterPosition
                    val to = target.bindingAdapterPosition
                    Log.d("XD", "Swap: $from - ${items[from]} -> $to ${items[to]}")

                    // check if after moving there will be max [droppableSlots] SortableItems before HeaderItem
                    val isIllegal = with(items.toMutableList()) {
                        this.move(from, to)
                        val headerIndex = this.indexOfFirst { it is Item.Header }
                        this.subList(0, headerIndex)
                            .count { it is Item.SortableItem } > droppableSlots
                    }
                    if (isIllegal) {
                        Log.d("XD", "This swap tries to exceed max droppable items")
                        return false
                    }

                    items.move(from, to) { index1, index2 ->
                        adapter.notifyItemMoved(index1, index2)
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
                        val firstIndex =
                            items.indexOfFirst { it is Item.PlaceholderItem && it.visible }
//                        if (firstIndex == -1) {
//                            Log.d("XD", "REMOVING PLACEHOLDER: INDEX OF FIRST $firstIndex")
//                            break
//                        }
                        // hide and show instead of removing from list as remove/add
                        // resulted in notifying about collection change and drag was
                        // interrupted by adapter update
                        (items[firstIndex] as Item.PlaceholderItem).visible = false
                        Log.d("XD", "removing placeholder")
                        adapter.notifyItemChanged(firstIndex)
                    }

                    while (headerIndex() < droppableSlots) {
                        val firstIndex =
                            items.indexOfFirst { it is Item.PlaceholderItem && !it.visible }
//                        if (firstIndex == -1) {
//                            Log.d("XD", "ADDING PLACEHOLDER: INDEX OF FIRST $firstIndex")
//                            break
//                        }
                        (items[firstIndex] as Item.PlaceholderItem).visible = true
                        Log.d("XD", "adding placeholder $firstIndex")
                        //fixme for some reason one item notify doesn't work properly (maybe investigate in future?)
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
                    adapter.notifyDataSetChanged()
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

