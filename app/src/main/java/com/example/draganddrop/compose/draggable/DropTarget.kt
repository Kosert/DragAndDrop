package com.example.draganddrop.compose.draggable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
internal fun <T> DropTarget(
    modifier: Modifier = Modifier,
    draggingObject: MutableState<DragTargetInfo<T>>,
    content: @Composable (BoxScope.(hovered: T?, dropped: T?) -> Unit)
) {

    var isCurrentDropTarget by remember { mutableStateOf(false) }
    val dragInfo by draggingObject
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset

    val margin = with(LocalDensity.current) { 20.dp.toPx() }

    Box(
        modifier = modifier
            .onGloballyPositioned {
                it.boundsInWindow().let { rect ->
                    isCurrentDropTarget =
                        rect.contains(Offset(0f, dragPosition + dragOffset + margin))
                }
            }
    ) {
        content(
            dragInfo.dataToDrop.takeIf { isCurrentDropTarget && dragInfo.isDragging },
            dragInfo.dataToDrop.takeIf { isCurrentDropTarget && !dragInfo.isDragging }
        )
    }
}