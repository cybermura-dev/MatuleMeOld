package ru.takeshiko.matuleme.data.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.DrawableRes
import ru.takeshiko.matuleme.databinding.BlurOverlayBinding
import ru.takeshiko.matuleme.databinding.MaterialToastBinding

class MaterialToast(private val context: Context) {

    companion object {
        const val LENGTH_SHORT = 2000L
        const val LENGTH_LONG = 3500L

        private const val ENTRY_DURATION = 350L
        private const val EXIT_DURATION = 200L
        private const val BLUR_ALPHA_DURATION = 300L
        private const val BLUR_RADIUS = 25f
    }

    private val handler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null
    private var overlayBinding: BlurOverlayBinding? = null
    private var isShowing = false

    init {
        val activity = context as? Activity
        if (activity != null && !activity.isDestroyed && !activity.isFinishing) {
            overlayBinding = BlurOverlayBinding.inflate(LayoutInflater.from(activity)).also { binding ->
                activity.findViewById<ViewGroup>(android.R.id.content).apply {
                    if (binding.root.parent == null) addView(binding.root)
                }
                binding.root.setOnClickListener { hide() }
                binding.blurBackground.setBackgroundColor(Color.parseColor("#99000000"))
            }
        }
    }


    fun show(
        title: String,
        message: String,
        @DrawableRes iconRes: Int,
        duration: Long = LENGTH_SHORT,
        onDismiss: (() -> Unit)? = null
    ) {
        if (isShowing) hide()

        val activity = context as? Activity ?: return
        val toastBinding = MaterialToastBinding.inflate(LayoutInflater.from(activity))

        toastBinding.ivToastIcon.setImageResource(iconRes)
        toastBinding.tvToastTitle.text = title
        toastBinding.tvToastMessage.text = message

        overlayBinding?.toastContainer?.apply {
            removeAllViews()
            addView(toastBinding.root)
        }
        overlayBinding?.root?.visibility = View.VISIBLE

        applyBlurEffectIfPossible()
        animateBlurIn()
        animateToastEntry(toastBinding.root)

        isShowing = true

        hideRunnable = Runnable {
            animateBlurOut()
            overlayBinding?.toastContainer?.getChildAt(0)?.let { toastView ->
                animateToastExit(toastView) {
                    overlayBinding?.root?.visibility = View.GONE
                    overlayBinding?.toastContainer?.removeAllViews()
                    isShowing = false
                    onDismiss?.invoke()
                }
            }
        }
        handler.postDelayed(hideRunnable!!, duration)
        overlayBinding?.root?.setOnClickListener {
            hideRunnable?.let(handler::removeCallbacks)
            hide(onDismiss)
        }
    }


    private fun hide(onDismiss: (() -> Unit)? = null) {
        if (!isShowing) return
        hideRunnable?.let(handler::removeCallbacks)
        hideRunnable = null

        animateBlurOut()

        overlayBinding?.toastContainer?.getChildAt(0)?.let { toastView ->
            animateToastExit(toastView) {
                overlayBinding?.root?.visibility = View.GONE
                overlayBinding?.toastContainer?.removeAllViews()
                isShowing = false
                onDismiss?.invoke()
            }
        }
    }

    fun release() {
        hideRunnable?.let(handler::removeCallbacks)
        hideRunnable = null
        val rootView = (context as? Activity)?.findViewById<ViewGroup>(android.R.id.content)
        overlayBinding?.root?.let { rootView?.removeView(it) }
        overlayBinding = null
    }

    private fun applyBlurEffectIfPossible() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            overlayBinding?.blurBackground?.run {
                try {
                    setRenderEffect(
                        RenderEffect.createBlurEffect(BLUR_RADIUS, BLUR_RADIUS, Shader.TileMode.DECAL)
                    )
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun animateBlurIn() {
        overlayBinding?.blurBackground?.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(BLUR_ALPHA_DURATION)
                .start()
        }
    }

    private fun animateBlurOut() {
        overlayBinding?.blurBackground?.animate()
            ?.alpha(0f)
            ?.setDuration(EXIT_DURATION)
            ?.start()
    }

    private fun animateToastEntry(view: View) {
        view.apply {
            alpha = 0f
            scaleX = 0.85f
            scaleY = 0.85f
        }

        val fadeIn = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.85f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.85f, 1f)

        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            duration = ENTRY_DURATION
            interpolator = OvershootInterpolator(0.7f)
            start()
        }
    }

    private fun animateToastExit(view: View, onEnd: () -> Unit) {
        val fadeOut = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.85f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.85f)

        AnimatorSet().apply {
            playTogether(fadeOut, scaleX, scaleY)
            duration = EXIT_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd()
                }
            })
            start()
        }
    }
}
