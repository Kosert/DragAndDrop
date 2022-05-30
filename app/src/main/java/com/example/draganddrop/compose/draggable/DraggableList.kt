package com.example.draganddrop.compose.draggable

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

internal class DragTargetInfo<T>(default: T) {
    var index: Int = 0
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(0f)
    var dragOffset by mutableStateOf(0f)
    var dataToDrop by mutableStateOf<T>(default)
}

@Composable
fun DraggableList(
    list: List<String>,
    onMove: (index1: Int, index2: Int) -> Unit,
    removeItem: (String) -> Unit,
    addItem: (String) -> Unit
) {
    val draggingObject = remember { mutableStateOf(DragTargetInfo("")) }
    val fixedItems = remember { mutableStateListOf<String?>(null, null) }
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        listState
    ) {

        itemsIndexed(fixedItems, key = { index, item -> item?.let { "fixed_$it" } ?: index }) { index, item ->
            DropTarget<String>(draggingObject = draggingObject) { hovered, dropped ->
                if (list.contains(fixedItems[index])) {
                    fixedItems[index] = null
                }

                if (fixedItems[index] == null && dropped != null) {
                    val existing = fixedItems.indexOf(dropped)
                    if (existing >= 0) {
                        fixedItems[existing] = null
                    }
                    fixedItems[index] = dropped
                    removeItem(dropped)
                }

                Text(
                    text = hovered?.let { "Item dragged: $hovered" } ?: "Drag Item here",
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .background(
                            if (hovered != null) Color.Red else Color.LightGray,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(8.dp)
                )
                fixedItems[index]?.let { value ->
                    DraggableItem(
                        index = -1,
                        name = value,
                        draggingObject = draggingObject,
                        lazyListState = listState,
                        onMove = { index1, index2 -> onMove(index1 - fixedItems.size, index2 - fixedItems.size) },
                        addItem = { addItem(it) } //todo maybe add to list as first position on start drag
                    )
                }
            }
        }

        itemsIndexed(list, key = { _, item -> item }) { index, item ->
            DraggableItem(
                index = index + fixedItems.size,
                name = item,
                draggingObject = draggingObject,
                lazyListState = listState,
                onMove = { index1, index2 -> onMove(index1 - fixedItems.size, index2 - fixedItems.size) },
                addItem = { addItem(it) }
            )
        }
    }
}

//@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.DraggableItem(
    index: Int,
    name: String,
    draggingObject: MutableState<DragTargetInfo<String>>,
    lazyListState: LazyListState,
    onMove: (index1: Int, index2: Int) -> Unit,
    addItem: (item: String) -> Unit,
) {
//    val default = draggingObject.value.takeIf { it.dataToDrop == name }
    val fixedItemCount = 2 //fixme pass this as parameter

    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(Offset.Zero) }

    val itemHeight = with(LocalDensity.current) { 40.dp.toPx() }
    val margin = itemHeight / 2
    Text(
        text = name,
        modifier = Modifier
//            .animateItemPlacement()
            .onGloballyPositioned {
                currentPosition = it.localToWindow(Offset.Zero)
            }
            .zIndex(if (isDragging) 1f else 0f)
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .alpha(if (isDragging) 0.7f else 1f)
            .padding(6.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(8.dp)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    offsetY += delta
                    draggingObject.value.dragPosition = currentPosition.y
                    draggingObject.value.dragOffset = offsetY

                    val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                    val currentItem = visibleItems.firstOrNull { it.key == "fixed_$name" }
                        ?: visibleItems.firstOrNull { it.key == name }
                        ?: TODO()

                    val currentItemPosition = currentItem.offset + offsetY + margin

                    val hovered = lazyListState.layoutInfo.visibleItemsInfo
                        .filterNot { it.index < fixedItemCount }
                        .filterNot { it.key == name }
                        .firstOrNull { currentItemPosition > it.offset && currentItemPosition < it.offsetEnd }

                    hovered?.let {
                        Log.d("XD", "Hovered: ${hovered.index}")
                        if (index < it.index) {
                            offsetY -= itemHeight * 1.2f
                        } else {
                            offsetY += itemHeight * 1.2f
                        }

                        if (index == -1) {
                            addItem(name)
                        } else {
                            onMove(index, it.index)
                        }
                    }
                },
                onDragStarted = {
//                    if (index < fixedItemCount) {
//                        addItem(name)
//                    }

                    isDragging = true
                    draggingObject.value.index = index
                    draggingObject.value.dataToDrop = name
                    draggingObject.value.dragPosition = currentPosition.y
                    draggingObject.value.isDragging = true
                },
                onDragStopped = {
//                    val currentItem = lazyListState.layoutInfo.visibleItemsInfo.first { it.key == name }
//                    val currentItemPosition = currentItem.offset + offsetY + margin
//                    val hovered = lazyListState.layoutInfo.visibleItemsInfo
//                        .filterNot { it.index == 0 }
//                        .filterNot { it.index == index }
//                        .firstOrNull { currentItemPosition > it.offset && currentItemPosition < it.offsetEnd }
//                    hovered?.let { onMove(index, it.index) }

                    isDragging = false
                    draggingObject.value.isDragging = false
                    offsetY = 0f
                },
            ),
    )
}