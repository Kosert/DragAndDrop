package com.example.draganddrop.classic

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ItemTouchHelper.*
import com.example.draganddrop.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val items = mutableListOf<Item>(
        Item.FixedItem(),
        Item.FixedItem(),
        Item.SortableItem("Jeden"),
        Item.SortableItem("Dwa"),
        Item.SortableItem("Trzy"),
        Item.SortableItem("Cztery"),
    )

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {

            private var draggingHolder: RecyclerView.ViewHolder? = null
            private var hoveredHolder: RecyclerView.ViewHolder? = null

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (viewHolder is MainAdapter.FixedItemHolder) {
                    ItemTouchUIUtilHack.onDraw(
                        c,
                        recyclerView,
                        viewHolder.binding.draggableText,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                } else {
                    ItemTouchUIUtilHack.onDraw(
                        c,
                        recyclerView,
                        viewHolder.itemView,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

            override fun isLongPressDragEnabled(): Boolean = false

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                Log.d("XD", "onMove: $viewHolder, $target")
                draggingHolder = viewHolder //fixme what if dragging from Fixed
                hoveredHolder = target

                val adapter = recyclerView.adapter as MainAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                if (viewHolder is MainAdapter.FixedItemHolder && target is MainAdapter.ItemViewHolder) {
                    Log.d("XD", "Dragged: $from -> $to | ${items[from]} | ${items[to]}")
                    val draggedValue = items[from].value
                    if (items.none { it.type == ItemType.SORTABLE_ITEM && it.value == draggedValue }) {
                        val firstSortableIndex = items.indexOfFirst { it.type == ItemType.SORTABLE_ITEM }
                        items.add(firstSortableIndex, Item.SortableItem(draggedValue!!))
                        adapter.notifyItemInserted(firstSortableIndex)
                    }

                }

                if (viewHolder is MainAdapter.ItemViewHolder && target is MainAdapter.ItemViewHolder) {
                    items.swap(from, to)
                    adapter.notifyItemMoved(from, to)
                } else {
                    // todo                   adapter.notifyItemRemoved(from)
                    //target //todo highlight hovered fixed item
                }

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

            override fun onSelectedChanged(
                viewHolder: RecyclerView.ViewHolder?,
                actionState: Int
            ) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState != ACTION_STATE_DRAG)
                    return

                when (viewHolder) {
                    is MainAdapter.ItemViewHolder -> {
                        viewHolder.itemView.alpha = 0.5f
                    }
                    is MainAdapter.FixedItemHolder -> {
                        viewHolder.binding.draggableText.alpha = 0.5f
                    }
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                Log.d("XD", "Clear view")
                val adapter = recyclerView.adapter as MainAdapter

                (hoveredHolder as? MainAdapter.FixedItemHolder)?.let {
                    val item = items[it.bindingAdapterPosition] as Item.FixedItem

                    val draggedIndex = draggingHolder!!.bindingAdapterPosition
                    val draggedItem = items[draggedIndex]
                    val draggedValue = when (draggedItem) {
                        is Item.FixedItem -> draggedItem.value!!.also {
                            draggedItem.value = null
                            adapter.notifyItemChanged(draggedIndex)
                        }
                        is Item.SortableItem -> draggedItem.value.also {
                            items.removeAt(draggedIndex)
                            adapter.notifyItemRemoved(draggedIndex)
                        }
                    }

                    item.value = draggedValue
                    adapter.notifyItemChanged(it.bindingAdapterPosition)
                }

                when (viewHolder) {
                    is MainAdapter.ItemViewHolder -> {
                        viewHolder.itemView.alpha = 1f
                    }
                    is MainAdapter.FixedItemHolder -> {
                        viewHolder.binding.draggableText.alpha = 1f
                    }
                }

                draggingHolder = null
                hoveredHolder = null
            }
        }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = MainAdapter(itemTouchHelper).also {
            it.submitList(items)
        }

        itemTouchHelper.attachToRecyclerView(binding.recycler)
    }

}