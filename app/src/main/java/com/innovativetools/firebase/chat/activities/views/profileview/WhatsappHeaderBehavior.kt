package com.innovativetools.firebase.chat.activities.views.profileview

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.innovativetools.firebase.chat.activities.views.profileview.HeaderView
import com.google.android.material.appbar.AppBarLayout
import com.innovativetools.firebase.chat.activities.views.profileview.WhatsappHeaderBehavior
import com.innovativetools.firebase.chat.activities.R
import android.util.TypedValue
import android.view.View

class WhatsappHeaderBehavior : CoordinatorLayout.Behavior<HeaderView> {
    private val mContext: Context
    private var mStartMarginLeft = 0
    private var mEndMarginLeft = 0
    private var mMarginRight = 0
    private var mStartMarginBottom = 0
    private var mTitleStartSize = 0f
    private var mTitleEndSize = 0f
    private var isHide = false

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
    }

    constructor(context: Context?, attrs: AttributeSet?, mContext: Context) : super(
        context,
        attrs
    ) {
        this.mContext = mContext
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: HeaderView,
        dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: HeaderView,
        dependency: View
    ): Boolean {
        shouldInitProperties()
        val maxScroll = (dependency as AppBarLayout).totalScrollRange
        val percentage = Math.abs(dependency.getY()) / maxScroll.toFloat()
        var childPosition = ((dependency.getHeight()
                + dependency.getY()) - child.height
                - (getToolbarHeight(mContext) - child.height) * percentage / 2)
        childPosition = childPosition - mStartMarginBottom * (1f - percentage)
        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        if (Math.abs(dependency.getY()) >= maxScroll / 2) {
            val layoutPercentage =
                (Math.abs(dependency.getY()) - maxScroll / 2) / Math.abs(maxScroll / 2)
            lp.leftMargin = (layoutPercentage * mEndMarginLeft).toInt() + mStartMarginLeft
            child.setTextSize(
                getTranslationOffset(
                    mTitleStartSize,
                    mTitleEndSize,
                    layoutPercentage
                )
            )
        } else {
            lp.leftMargin = mStartMarginLeft
        }
        lp.rightMargin = mMarginRight
        child.layoutParams = lp
        child.y = childPosition
        if (isHide && percentage < 1) {
            child.visibility = View.VISIBLE
            isHide = false
        } else if (!isHide && percentage == 1f) {
            child.visibility = View.GONE
            isHide = true
        }
        return true
    }

    protected fun getTranslationOffset(
        expandedOffset: Float,
        collapsedOffset: Float,
        ratio: Float
    ): Float {
        return expandedOffset + ratio * (collapsedOffset - expandedOffset)
    }

    private fun shouldInitProperties() {
        if (mStartMarginLeft == 0) {
            mStartMarginLeft =
                mContext.resources.getDimensionPixelOffset(R.dimen.header_view_start_margin_left)
        }
        if (mEndMarginLeft == 0) {
            mEndMarginLeft =
                mContext.resources.getDimensionPixelOffset(R.dimen.header_view_end_margin_left)
        }
        if (mStartMarginBottom == 0) {
            mStartMarginBottom =
                mContext.resources.getDimensionPixelOffset(R.dimen.header_view_start_margin_bottom)
        }
        if (mMarginRight == 0) {
            mMarginRight =
                mContext.resources.getDimensionPixelOffset(R.dimen.header_view_end_margin_right)
        }
        if (mTitleStartSize == 0f) {
            mTitleEndSize =
                mContext.resources.getDimensionPixelSize(R.dimen.header_view_end_text_size)
                    .toFloat()
        }
        if (mTitleStartSize == 0f) {
            mTitleStartSize =
                mContext.resources.getDimensionPixelSize(R.dimen.header_view_start_text_size)
                    .toFloat()
        }
    }

    companion object {
        fun getToolbarHeight(context: Context): Int {
            var result = 0
            val tv = TypedValue()
            if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                result = TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    context.resources.displayMetrics
                )
            }
            return result
        }
    }
}