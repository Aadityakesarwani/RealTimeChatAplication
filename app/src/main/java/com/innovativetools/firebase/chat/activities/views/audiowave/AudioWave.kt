package com.innovativetools.firebase.chat.activities.views.audiowave

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.innovativetools.firebase.chat.activities.views.audiowave.AudioWave

class AudioWave : View {
    val size = 4
    private var mBytes: ByteArray? = null
    private var mPoints: FloatArray? = null
    private val mRect = Rect()
    var config: Config? = null
        private set

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mBytes = null
        config = Config(context, attrs, this)
    }

    fun updateVisualizer(bytes: ByteArray?) {
        mBytes = bytes
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mBytes == null) {
            return
        }
        if (mPoints == null || mPoints!!.size < mBytes!!.size * size) {
            mPoints = FloatArray(mBytes!!.size * size)
        }
        mRect[0, 0, width] = height
        for (i in 0 until mBytes!!.size - 1) {
            mPoints!![i * size] = (mRect.width() * i / (mBytes!!.size - 1)).toFloat()
            mPoints!![i * size + 1] =
                (mRect.height() / 2 + (mBytes!![i] + 128).toByte() * (mRect.height() / 2) / 128).toFloat()
            mPoints!![i * size + 2] = (mRect.width() * (i + 1) / (mBytes!!.size - 1)).toFloat()
            mPoints!![i * size + 3] =
                (mRect.height() / 2 + (mBytes!![i + 1] + 128).toByte() * (mRect.height() / 2) / 128).toFloat()
        }
        if (config!!.colorGradient) {
            config!!.reSetupPaint()
            config!!.setGradients(this)
        } else {
            config!!.reSetupPaint()
        }
        canvas.drawLines(mPoints!!, config!!.paintWave)
    }

    fun setConfig(config: Config?): AudioWave {
        this.config = config
        return this
    }
}