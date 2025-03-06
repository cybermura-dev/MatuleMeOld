package ru.takeshiko.matuleme.data.utils

import android.content.res.Configuration
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.floor
import kotlin.math.max

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean = true
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount

            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}

private fun calculateAdaptiveSpanCount(
    screenWidthDp: Float,
    cardWidthDp: Float,
    spacingDp: Float,
    minSpanCount: Int
): Int {
    return max(
        minSpanCount,
        floor((screenWidthDp + spacingDp) / (cardWidthDp + spacingDp)).toInt()
    )
}

fun RecyclerView.setupAdaptiveGridLayout(
    adapter: RecyclerView.Adapter<*>,
    cardWidthDp: Int = 160,
    spacingDp: Int = 16,
    minSpanCount: Int = 2
) {
    val context = context
    val displayMetrics = resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density

    val spanCount = calculateAdaptiveSpanCount(
        screenWidthDp,
        cardWidthDp.toFloat(),
        spacingDp.toFloat(),
        minSpanCount
    )

    val adjustedSpanCount = when {
        context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ->
            spanCount * 1.5f
        context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE ->
            spanCount * 1.2f
        else -> spanCount.toFloat()
    }.toInt()

    layoutManager = GridLayoutManager(context, adjustedSpanCount)
    addItemDecoration(
        GridSpacingItemDecoration(
            spanCount = adjustedSpanCount,
            spacing = (spacingDp * displayMetrics.density).toInt(),
            includeEdge = true
        )
    )
    this.adapter = adapter
}