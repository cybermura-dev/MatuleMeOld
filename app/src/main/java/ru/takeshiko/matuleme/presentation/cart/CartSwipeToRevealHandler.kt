package ru.takeshiko.matuleme.presentation.cart

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import androidx.core.math.MathUtils
import androidx.core.view.isVisible
import ru.takeshiko.matuleme.R
import java.lang.ref.WeakReference
import kotlin.math.abs

class CartSwipeToRevealHandler(
    context: Context,
    private val quantityPanel: View,
    private val deletePanel: View,
    private val productCard: View
) {
    private var initialX = 0f
    private var initialY = 0f
    private var isSwiping = false
    private var currentPosition = 0f

    private val swipeThreshold: Float
    private val maxSwipeDistance: Float
    private val touchSlop: Int

    private val ANIMATION_DURATION = 150L

    enum class SwipeState {
        CLOSED,
        DELETE,
        QUANTITY
    }
    var currentState = SwipeState.CLOSED
        private set

    companion object {
        var isAnySwiped = false
        var lastSwipedHandler: WeakReference<CartSwipeToRevealHandler>? = null
    }

    init {
        val swipeOffsetDp = context.resources.getDimension(R.dimen.swipe_offset)
        swipeThreshold = swipeOffsetDp
        maxSwipeDistance = swipeOffsetDp + 30f
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupSwipeHandler(onTouchAction: (() -> Unit)? = null) {
        productCard.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isAnySwiped && lastSwipedHandler?.get() != this) {
                        lastSwipedHandler?.get()?.resetSwipe()
                    }
                    onTouchAction?.invoke()
                    handleActionDown(event)
                }
                MotionEvent.ACTION_MOVE -> handleActionMove(v, event)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> handleActionUp(v)
                else -> false
            }
        }
    }

    private fun handleActionDown(event: MotionEvent): Boolean {
        initialX = event.rawX
        initialY = event.rawY
        currentPosition = productCard.translationX
        isSwiping = false
        return true
    }

    private fun handleActionMove(v: View, event: MotionEvent): Boolean {
        if (!isSwiping) {
            val dx = abs(event.rawX - initialX)
            val dy = abs(event.rawY - initialY)
            if (dx > touchSlop && dx > dy) {
                isSwiping = true
            }
        }
        if (isSwiping) {
            val deltaX = event.rawX - initialX
            updateCardPosition(currentPosition + deltaX)
            updatePanelsVisibility()
            v.parent.requestDisallowInterceptTouchEvent(true)
            return true
        }
        return false
    }

    private fun handleActionUp(v: View): Boolean {
        return if (isSwiping) {
            handleSwipeEnd()
            isSwiping = false
            true
        } else {
            v.performClick()
        }
    }

    private fun updateCardPosition(deltaX: Float) {
        val newPosition = MathUtils.clamp(deltaX, -maxSwipeDistance, maxSwipeDistance)
        productCard.translationX = newPosition
    }

    private fun updatePanelsVisibility() {
        when {
            productCard.translationX > swipeThreshold -> {
                if (!quantityPanel.isVisible) {
                    quantityPanel.alpha = 0f
                    quantityPanel.isVisible = true
                    quantityPanel.animate()
                        .alpha(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                if (deletePanel.isVisible) {
                    deletePanel.animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction { deletePanel.isVisible = false }
                        .start()
                }
            }
            productCard.translationX < -swipeThreshold -> {
                if (!deletePanel.isVisible) {
                    deletePanel.alpha = 0f
                    deletePanel.isVisible = true
                    deletePanel.animate()
                        .alpha(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                if (quantityPanel.isVisible) {
                    quantityPanel.animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction { quantityPanel.isVisible = false }
                        .start()
                }
            }
            else -> {
                if (quantityPanel.isVisible) {
                    quantityPanel.animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction { quantityPanel.isVisible = false }
                        .start()
                }
                if (deletePanel.isVisible) {
                    deletePanel.animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction { deletePanel.isVisible = false }
                        .start()
                }
            }
        }
    }

    private fun handleSwipeEnd() {
        val translationX = productCard.translationX
        val targetPosition = when {
            abs(translationX) < swipeThreshold -> 0f
            translationX > 0 -> maxSwipeDistance
            else -> -maxSwipeDistance
        }
        currentState = when {
            targetPosition == 0f -> SwipeState.CLOSED
            targetPosition > 0 -> SwipeState.QUANTITY
            else -> SwipeState.DELETE
        }
        isAnySwiped = currentState != SwipeState.CLOSED
        if (isAnySwiped) {
            lastSwipedHandler = WeakReference(this)
        }
        animateSwipe(targetPosition)
    }

    private fun animateSwipe(target: Float) {
        ValueAnimator.ofFloat(productCard.translationX, target).apply {
            duration = ANIMATION_DURATION
            addUpdateListener { animator ->
                productCard.translationX = animator.animatedValue as Float
                updatePanelsVisibility()
            }
            start()
        }
    }

    fun resetSwipe() {
        animateSwipe(0f)
        currentState = SwipeState.CLOSED
        isAnySwiped = false
        lastSwipedHandler = null
        if (quantityPanel.isVisible) {
            quantityPanel.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { quantityPanel.isVisible = false }
                .start()
        }
        if (deletePanel.isVisible) {
            deletePanel.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { deletePanel.isVisible = false }
                .start()
        }
    }
}