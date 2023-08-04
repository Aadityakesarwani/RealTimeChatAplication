package com.innovativetools.firebase.chat.activities

import android.app.Application
import android.os.Handler
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.firebase.database.FirebaseDatabase
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import com.innovativetools.firebase.chat.activities.fcm.ApplicationLifecycleManager
import androidx.lifecycle.ProcessLifecycleOwner
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.innovativetools.firebase.chat.activities.managers.SessionManager
import com.innovativetools.firebase.chat.activities.managers.Utils
import java.lang.Exception

class UIApplication : Application(), DefaultLifecycleObserver {
    override fun onCreate() {
        super<Application>.onCreate()
        SessionManager.init(this)
        MobileAds.initialize(this) { initializationStatus: InitializationStatus? -> }
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        EmojiManager.install(IosEmojiProvider())
        registerActivityLifecycleCallbacks(ApplicationLifecycleManager())
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
//        App visible/foreground
        if (owner.lifecycle.currentState == Lifecycle.State.RESUMED) {
            try {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(
                    { Utils.readStatus(IConstants.STATUS_ONLINE) },
                    IConstants.ONE.toLong()
                )
            } catch (ignored: Exception) {
            }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
//        App in background
        if (owner.lifecycle.currentState == Lifecycle.State.STARTED) {
            try {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(
                    { Utils.readStatus(IConstants.STATUS_OFFLINE) },
                    IConstants.ONE.toLong()
                )
            } catch (ignored: Exception) {
            }
        }
    }
}