package com.innovativetools.firebase.chat.activities.views.audiowave

import android.content.Context
import com.innovativetools.firebase.chat.activities.views.audiowave.AudioWave
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import com.innovativetools.firebase.chat.activities.R
import android.graphics.Shader
import android.util.AttributeSet

class Config(context: Context, attrs: AttributeSet?, private val audioWave: AudioWave) {
    var color = 0
    var startColor = 0
        private set
    var endColor = 0
        private set
    var thickness = 0f
        private set
    var colorGradient = false
        private set
    var paintWave = Paint()
        private set

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.AudioWave, 0, 0)
        if (attrs != null) {
            thickness = a.getFloat(R.styleable.AudioWave_waveThickness, 1f)
            color = a.getColor(R.styleable.AudioWave_waveColor, Color.parseColor("#691A40"))
            colorGradient = a.getBoolean(R.styleable.AudioWave_colorGradient, false)
            startColor = a.getColor(R.styleable.AudioWave_startColor, Color.parseColor("#93278F"))
            endColor = a.getColor(R.styleable.AudioWave_endColor, Color.parseColor("#00A99D"))
            a.recycle()
            paintWave.strokeWidth = thickness
            paintWave.isAntiAlias = true
            paintWave.style = Paint.Style.FILL
            paintWave.color = color
            paintWave.alpha = 255
        }
    }

    fun setColor(color: Int): Config {
        this.color = color
        paintWave.color = this.color
        audioWave.invalidate()
        return this
    }

    fun setStartColor(startColor: Int): Config {
        this.startColor = startColor
        audioWave.invalidate()
        return this
    }

    fun setEndColor(endColor: Int): Config {
        this.endColor = endColor
        audioWave.invalidate()
        return this
    }

    fun setThickness(thickness: Float): Config {
        this.thickness = thickness
        paintWave.strokeWidth = this.thickness
        audioWave.invalidate()
        return this
    }

    fun setColorGradient(colorGradient: Boolean): Config {
        this.colorGradient = colorGradient
        audioWave.invalidate()
        return this
    }

    fun setPaintWave(paintWave: Paint): Config {
        this.paintWave = paintWave
        audioWave.invalidate()
        return this
    }

    fun setGradients(audioWave: AudioWave): Paint {

        val paintWave = Paint() // create a new Paint object

        paintWave.shader = LinearGradient(
            0F, 0F,                            // Start coordinates (left-top corner)
            audioWave.width.toFloat(), 0F,     // End coordinates (right-top corner)
            startColor, endColor,              // Start and end colors of the gradient
            Shader.TileMode.MIRROR             // Tile mode for handling areas outside gradient bounds
        )

        audioWave.invalidate()

        return paintWave
    }


    fun reSetupPaint(): Paint {
        paintWave = Paint()
        paintWave.strokeWidth = thickness
        paintWave.isAntiAlias = true
        paintWave.style = Paint.Style.FILL
        paintWave.color = color
        paintWave.alpha = 255
        return paintWave
    }
}