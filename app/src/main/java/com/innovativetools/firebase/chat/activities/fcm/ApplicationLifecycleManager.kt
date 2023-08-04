package com.innovativetools.firebase.chat.activities.fcm

import android.app.Application.ActivityLifecycleCallbacks
import android.app.Activity
import android.os.Bundle
import com.innovativetools.firebase.chat.activities.fcm.ApplicationLifecycleManager
import com.innovativetools.firebase.chat.activities.managers.SessionManager
import com.innovativetools.firebase.chat.activities.managers.Utils

/**
 * Determines global app lifecycle states.
 *
 *
 * The following is the reference of activities states:
 *
 *
 * The **visible** lifetime of an activity happens between a call to onStart()
 * until a corresponding call to onStop(). During this time the user can see the
 * activity on-screen, though it may not be in the foreground and interacting with
 * the user. The onStart() and onStop() methods can be called multiple times, as
 * the activity becomes visible and hidden to the user.
 *
 *
 * The **foreground** lifetime of an activity happens between a call to onResume()
 * until a corresponding call to onPause(). During this time the activity is in front
 * of all other activities and interacting with the user. An activity can frequently
 * go between the resumed and paused states -- for example when the device goes to
 * sleep, when an activity result is delivered, when a new intent is delivered --
 * so the code in these methods should be fairly lightweight.
 */
class ApplicationLifecycleManager : ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (SessionManager.get()!!.isRTLOn) {
            Utils.RTLSupport(activity.window)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        foregroundActivityCount++
    }

    override fun onActivityPaused(activity: Activity) {
        foregroundActivityCount--
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityStarted(activity: Activity) {
        visibleActivityCount++
    }

    override fun onActivityStopped(activity: Activity) {
        visibleActivityCount--
    }

    companion object {
        /**
         * Manages the state of opened vs closed activities, should be 0 or 1.
         * It will be 2 if this value is checked between activity B onStart() and
         * activity A onStop().
         * It could be greater if the top activities are not fullscreen or have
         * transparent backgrounds.
         */
        private var visibleActivityCount = 0

        /**
         * Manages the state of opened vs closed activities, should be 0 or 1
         * because only one can be in foreground at a time. It will be 2 if this
         * value is checked between activity B onResume() and activity A onPause().
         */
        private var foregroundActivityCount = 0

        /**
         * Returns true if app has foreground
         */
        val isAppInForeground: Boolean
            get() = foregroundActivityCount > 0

        /**
         * Returns true if any activity of app is visible (or device is sleep when
         * an activity was visible)
         */
        val isAppVisible: Boolean
            get() = visibleActivityCount > 0
    }
}