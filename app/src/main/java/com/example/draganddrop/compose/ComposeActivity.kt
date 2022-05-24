package com.example.draganddrop.compose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.draganddrop.compose.draggable.DraggableList
import com.example.draganddrop.compose.draggable.move
import com.example.draganddrop.compose.theme.DragAndDropTestTheme

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DragAndDropTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting()
                }
            }
        }
    }
}

@Composable
fun Greeting() = Column {

    val list = listOf(
        "Jeden",
        "Dwa",
        "Trzy",
        "Cztery",
    ).toMutableStateList()

    DraggableList(
        list = list,
        onMove = { draggedIndex, draggedTo ->
            Log.d("XD", "Move: ${list[draggedIndex]} -> ${list[draggedTo]}")
            list.move(draggedIndex, draggedTo)
        },
        removeItem = { list.remove(it) },
        addItem = {
            if (!list.contains(it)) {
                list.add(0, it)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DragAndDropTestTheme {
        Greeting()
    }
}