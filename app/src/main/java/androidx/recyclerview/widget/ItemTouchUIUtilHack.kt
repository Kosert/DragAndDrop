package androidx.recyclerview.widget

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchUIUtilHack

object ItemTouchUIUtilHack {

    fun onDraw(
        c: Canvas?,
        recyclerView: RecyclerView?,
        view: View?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        ItemTouchUIUtilImpl.INSTANCE.onDraw(
            c,
            recyclerView,
            view,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }


}