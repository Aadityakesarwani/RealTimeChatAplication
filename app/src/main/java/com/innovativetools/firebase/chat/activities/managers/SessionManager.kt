package com.innovativetools.firebase.chat.activities.managers

import android.content.Context
import android.content.SharedPreferences
import com.innovativetools.firebase.chat.activities.constants.IConstants


class SessionManager(context: Context) {
    private val pref: SharedPreferences

    //============== END
    init {
        pref = context.getSharedPreferences(context.packageName + PREF_NAME, 0)
    }

    fun setOnOffNotification(value: Boolean) {
        val editor = pref.edit()
        editor.putBoolean(KEY_ON_OFF_NOTIFICATION, value)
        editor.apply()
    }

    val isNotificationOn: Boolean
        get() = pref.getBoolean(KEY_ON_OFF_NOTIFICATION, IConstants.TRUE)

    fun setOnOffRTL(value: Boolean) {
        val editor = pref.edit()
        editor.putBoolean(KEY_ON_OFF_RTL, value)
        editor.apply()
    }

    val isRTLOn: Boolean
        get() = pref.getBoolean(KEY_ON_OFF_RTL, IConstants.FALSE)
    var isOnBoardingDone: Boolean
        get() = pref.getBoolean(KEY_ONBOARDING, IConstants.FALSE)
        set(value) {
            val editor = pref.edit()
            editor.putBoolean(KEY_ONBOARDING, value)
            editor.apply()
        }

    fun clearAll() {
        val editor = pref.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        // Shared preferences file name
        private const val PREF_NAME = "BytesBeeChatV1"
        private const val KEY_ON_OFF_NOTIFICATION = "onOffNotification"
        private const val KEY_ON_OFF_RTL = "onOffRTL"
        private const val KEY_ONBOARDING = "isOnBoardingDone"

        //============== START
        private var mInstance: SessionManager? = null
        fun get(): SessionManager? {
            return mInstance
        }

        fun init(ctx: Context) {
            if (mInstance == null) mInstance = SessionManager(ctx)
        }
    }
}