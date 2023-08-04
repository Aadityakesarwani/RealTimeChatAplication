package com.innovativetools.firebase.chat.activities.views.profileview

import android.widget.LinearLayout
import android.widget.TextView
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import com.innovativetools.firebase.chat.activities.R
import android.util.TypedValue

class HeaderView : LinearLayout {
    private var name: TextView? = null
    private var lastSeen: TextView? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        name = findViewById(R.id.txtGroupName)
        lastSeen = findViewById(R.id.txtSubtitle)
    }

    fun bindTo(name: String?, lastSeen: String?) {
        this.name!!.text = name
        this.lastSeen!!.text = lastSeen
    }

    fun setName(name: String?) {
        this.name!!.text = name
    }

    fun setLastSeen(lastSeen: String?) {
        this.lastSeen!!.text = lastSeen
    }

    fun setTextSize(size: Float) {
        name!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }
}