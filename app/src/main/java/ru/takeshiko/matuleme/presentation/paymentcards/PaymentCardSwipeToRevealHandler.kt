package ru.takeshiko.matuleme.presentation.paymentcards

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

class PaymentCardSwipeToRevealHandler(
    context: Context,
    private val leftPanel: View,
    private val rightPanel: View,
    private val cardView: View
) {
    private var initialX = 0f
    private var initialY = 0f
    private var isSwiping = false
    private var currentPosition = 0f

    private val swipeThreshold: Float
    private val maxLeftSwipeDistance: Float
    private val maxRightSwipeDistance: Float
    private val touchSlop: Int

    private val ANIMATION_DURATION = 150L

    enum class SwipeState {
        CLOSED,
        PRIMARY,
        DELETE
    }
    var currentState = SwipeState.CLOSED
        private set

    companion object {
        var isAnySwiped = false
        var lastSwipedHandler: WeakReference<PaymentCardSwipeToRevealHandler>? = null
    }

    init {
        val swipeOffsetDp = context.resources.getDimension(R.dimen.swipe_offset)
        swipeThreshold = swipeOffsetDp
        maxLeftSwipeDistance = swipeOffsetDp + 30f
        maxRightSwipeDistance = swipeOffsetDp * 2.15f
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupSwipeHandler(onTouchAction: (() -> Unit)? = null) {
        cardView.setOnTouchListener { v, event ->
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
        currentPosition = cardView.translationX
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
        val newPosition = if (deltaX > 0) {
            MathUtils.clamp(deltaX, 0f, maxLeftSwipeDistance)
        } else {
            MathUtils.clamp(deltaX, -maxRightSwipeDistance, 0f)
        }
        cardView.translationX = newPosition
    }

    private fun updatePanelsVisibility() {
        when {
            cardView.translationX > swipeThreshold -> {
                if (!leftPanel.isVisible) {
                    leftPanel.alpha = 0f
                    leftPanel.isVisible = true
                    leftPanel.animate()
                        .alpha(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                if (rightPanel.isVisible) {
                    rightPanel.animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction { rightPanel.isVisible = false }
                        .start()
                }
            }
            cardView.translationX < -swipeThreshold -> {
                if (!rightPanel.isVisible) {
                    rightPanel.alpha = 0f
                    rightPanel.isVisible = true
                    rightPanel.animate()
                        .alpha(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                if (leftPanel.isVisible) {
                    leftPanel.animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction { leftPanel.isVisible = false }
                        .start()
                }
            }
            else -> {
                if (leftPanel.isVisible) {
                    leftPanel.animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction { leftPanel.isVisible = false }
                        .start()
                }
                if (rightPanel.isVisible) {
                    rightPanel.animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction { rightPanel.isVisible = false }
                        .start()
                }
            }
        }
    }

    private fun handleSwipeEnd() {
        val translationX = cardView.translationX
        val targetPosition = when {
            abs(translationX) < swipeThreshold -> 0f
            translationX > 0 -> maxLeftSwipeDistance
            else -> -maxRightSwipeDistance
        }
        currentState = when {
            targetPosition == 0f -> SwipeState.CLOSED
            targetPosition > 0 -> SwipeState.PRIMARY
            else -> SwipeState.DELETE
        }
        isAnySwiped = currentState != SwipeState.CLOSED
        if (isAnySwiped) {
            lastSwipedHandler = WeakReference(this)
        }
        animateSwipe(targetPosition)
    }

    private fun animateSwipe(target: Float) {
        ValueAnimator.ofFloat(cardView.translationX, target).apply {
            duration = ANIMATION_DURATION
            addUpdateListener { animator ->
                cardView.translationX = animator.animatedValue as Float
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
        if (leftPanel.isVisible) {
            leftPanel.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { leftPanel.isVisible = false }
                .start()
        }
        if (rightPanel.isVisible) {
            rightPanel.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { rightPanel.isVisible = false }
                .start()
        }
    }
}