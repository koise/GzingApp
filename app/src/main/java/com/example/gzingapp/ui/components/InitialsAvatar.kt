package com.example.gzingapp.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.gzingapp.utils.InitialsGenerator

class InitialsAvatar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private var initials: String = ""
    private var backgroundColor: Int = Color.BLUE
    private var textColor: Int = Color.WHITE
    private var textSize: Float = 24f
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        paint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }
    
    fun setInitials(initials: String) {
        this.initials = initials.uppercase()
        invalidate()
    }
    
    fun setInitialsFromName(name: String) {
        setInitials(InitialsGenerator.generateInitials(name))
        setBackgroundColorFromName(name)
    }
    
    fun setInitialsFromNames(firstName: String, lastName: String) {
        setInitials(InitialsGenerator.generateInitials(firstName, lastName))
        setBackgroundColorFromName("$firstName $lastName")
    }
    
    fun setAvatarBackgroundColor(color: Int) {
        this.backgroundColor = color
        invalidate()
    }
    
    fun setBackgroundColorFromName(name: String) {
        val colorHex = InitialsGenerator.generateColor(name)
        this.backgroundColor = Color.parseColor(colorHex)
        invalidate()
    }
    
    fun setTextColor(color: Int) {
        this.textColor = color
        invalidate()
    }
    
    fun setTextSize(size: Float) {
        this.textSize = size
        textPaint.textSize = size
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) / 2f
        
        // Draw background circle
        paint.color = backgroundColor
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        // Draw initials text
        if (initials.isNotEmpty()) {
            textPaint.color = textColor
            textPaint.textSize = textSize
            
            // Calculate text position
            val textY = centerY + (textPaint.descent() - textPaint.ascent()) / 2 - textPaint.descent()
            canvas.drawText(initials, centerX, textY, textPaint)
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = minOf(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
    }
}
