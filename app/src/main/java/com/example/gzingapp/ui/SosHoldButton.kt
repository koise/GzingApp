package com.example.gzingapp.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.gzingapp.R
import kotlinx.coroutines.*

class SosHoldButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var onSosActivated: (() -> Unit)? = null
    private var isHolding = false
    private var holdProgress = 0f
    private var holdStartTime = 0L
    private val holdDuration = 3000L // 3 seconds
    
    // Drawing components
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FF0000")
        style = Paint.Style.FILL
    }
    
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        textSize = 48f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    
    private val rectF = RectF()
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    
    // Vibration
    private var vibrator: Vibrator? = null
    private var vibrationJob: Job? = null
    
    // Animation
    private var progressAnimator: ValueAnimator? = null
    
    init {
        // Initialize vibrator
        try {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } catch (e: Exception) {
            // Vibrator not available
        }
    }
    
    fun setOnSosActivatedListener(listener: (() -> Unit)?) {
        onSosActivated = listener
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = minOf(w, h) / 2f - 20f
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)
        
        // Draw progress arc if holding
        if (isHolding && holdProgress > 0) {
            val sweepAngle = 360f * holdProgress
            canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)
        }
        
        // Draw SOS text
        val textY = centerY + (textPaint.descent() - textPaint.ascent()) / 2 - textPaint.descent()
        canvas.drawText("SOS", centerX, textY, textPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isPointInCircle(event.x, event.y)) {
                    startHold()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isHolding) {
                    cancelHold()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun isPointInCircle(x: Float, y: Float): Boolean {
        val distance = kotlin.math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY))
        return distance <= radius
    }
    
    private fun startHold() {
        if (isHolding) return
        
        isHolding = true
        holdStartTime = System.currentTimeMillis()
        holdProgress = 0f
        
        // Start vibration pattern
        startVibration()
        
        // Start progress animation
        startProgressAnimation()
        
        // Visual feedback
        animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
    }
    
    private fun cancelHold() {
        if (!isHolding) return
        
        isHolding = false
        holdProgress = 0f
        
        // Stop vibration
        stopVibration()
        
        // Stop progress animation
        progressAnimator?.cancel()
        
        // Reset visual state
        animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        
        invalidate()
    }
    
    private fun completeHold() {
        if (!isHolding) return
        
        isHolding = false
        holdProgress = 1f
        
        // Stop vibration
        stopVibration()
        
        // Stop progress animation
        progressAnimator?.cancel()
        
        // Trigger SOS activation
        onSosActivated?.invoke()
        
        // Reset visual state
        animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        
        invalidate()
    }
    
    private fun startProgressAnimation() {
        progressAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = holdDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                holdProgress = animation.animatedValue as Float
                invalidate()
                
                // Check if hold is complete
                if (holdProgress >= 1f) {
                    completeHold()
                }
            }
            start()
        }
    }
    
    private fun startVibration() {
        vibrationJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                while (isHolding && isActive) {
                    vibrator?.let { v ->
                        if (v.hasVibrator()) {
                            // Create emergency vibration pattern
                            val pattern = longArrayOf(0, 200, 100, 200, 100, 500)
                            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                            
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                val vibrationEffect = VibrationEffect.createWaveform(pattern, amplitudes, 0)
                                v.vibrate(vibrationEffect)
                            } else {
                                @Suppress("DEPRECATION")
                                v.vibrate(pattern, 0)
                            }
                        }
                    }
                    delay(1000) // Repeat every second
                }
            } catch (e: Exception) {
                // Handle vibration error
            }
        }
    }
    
    private fun stopVibration() {
        vibrationJob?.cancel()
        vibrationJob = null
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Handle vibration cancel error
        }
    }
    
    fun stopVibrationAndReset() {
        stopVibration()
        if (isHolding) {
            cancelHold()
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopVibration()
        progressAnimator?.cancel()
    }
}
