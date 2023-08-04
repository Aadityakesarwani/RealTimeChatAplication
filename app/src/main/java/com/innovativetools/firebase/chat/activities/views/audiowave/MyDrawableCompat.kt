package com.innovativetools.firebase.chat.activities.views.audiowave

import com.innovativetools.firebase.chat.activities.managers.Utils.isAboveQ
import android.graphics.drawable.Drawable
import android.graphics.BlendModeColorFilter
import android.graphics.BlendMode
import android.graphics.PorterDuff
import androidx.annotation.ColorInt

object MyDrawableCompat {
    @JvmStatic
    fun setColorFilter(drawable: Drawable, @ColorInt color: Int) {
        if (isAboveQ) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }
}