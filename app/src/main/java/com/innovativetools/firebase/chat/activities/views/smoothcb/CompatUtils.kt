package com.innovativetools.firebase.chat.activities.views.smoothcb

import android.content.Context

object CompatUtils {
    @JvmStatic
    fun dp2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}