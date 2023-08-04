package com.innovativetools.firebase.chat.activities.views

import android.os.SystemClock
import android.view.View

/**
 * Handling the problem that double-clicking on a control quickly 2 times (or more times) will cause onClick to be triggered 2 times (or more times)
 * Filter by judging the time interval of 2 click events
 *
 *
 * Subclasses respond to click events by implementing [.onClickView]
 */
abstract class SingleClickListener : View.OnClickListener {
    /**
     * Last click time
     */
    private var mLastClickTime: Long = 0

    /**
     * Click response function
     *
     * @param v The view that was clicked.
     */
    abstract fun onClickView(v: View?)
    override fun onClick(v: View) {
        val currentClickTime = SystemClock.uptimeMillis()
        val elapsedTime = currentClickTime - mLastClickTime
        //There may be 2 hits or 3 hits, to ensure that mLastClickTime always records the time of the last click
        mLastClickTime = currentClickTime
        if (elapsedTime <= MIN_CLICK_INTERVAL) return
        onClickView(v)
    }

    companion object {
        /**
         * The shortest time interval between click events
         */
        private const val MIN_CLICK_INTERVAL: Long = 1000
    }
}