package es.lifevit.pillreminder.views.adapters.spacing

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View


class SpacesItemDecorationVerticalLinearLayout(private val spaceTopBottom: Int, private val spaceBetweenElements: Int, private val spaceLeftRight: Int) : RecyclerView.ItemDecoration() {


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        val position = parent.getChildAdapterPosition(view)
        val lastElement = parent.adapter!!.itemCount - 1

        outRect.bottom = if (position == lastElement) spaceTopBottom else 0

        // Add top margin only for the first item to avoid double space between items
        outRect.top = if (position == 0) spaceTopBottom else spaceBetweenElements

        outRect.left = spaceLeftRight
        outRect.right = spaceLeftRight
    }
}
