package com.innovativetools.firebase.chat.activities

import com.google.firebase.auth.FirebaseUser
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import com.innovativetools.firebase.chat.activities.R
import android.widget.TextView
import com.innovativetools.firebase.chat.activities.managers.Screens
import android.os.Looper
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.OnCompleteListener
import com.innovativetools.firebase.chat.activities.MainActivity
import com.innovativetools.firebase.chat.activities.LoginActivity
import com.innovativetools.firebase.chat.activities.OnBoardingActivity
import com.innovativetools.firebase.chat.activities.constants.IConstants
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.innovativetools.firebase.chat.activities.managers.SessionManager
import com.innovativetools.firebase.chat.activities.managers.Utils
import java.lang.Exception
import java.util.*

class SplashActivity : AppCompatActivity() {
    private var firebaseUser //Current User
            : FirebaseUser? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val window = window
        window.setFormat(PixelFormat.RGBA_8888)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setWindow(window)
        setContentView(R.layout.activity_splash)
        (findViewById<View>(R.id.txtName) as TextView).text =
            String.format(getString(R.string.app_company_name), getString(R.string.app_company))
        StartAnimations()
        load()
    }

    private var screens: Screens? = null
    private fun load() {
        screens = Screens(applicationContext)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            try { if (SessionManager.get()!!.isOnBoardingDone) {
                    firebaseUser = FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        firebaseUser!!.reload().addOnCompleteListener { task: Task<Void?> ->
                            if (task.isSuccessful) {
                                screens!!.showClearTopScreen(MainActivity::class.java)
                            } else {
                                screens!!.showToast(Objects.requireNonNull(task.exception)?.message)
                                screens!!.showClearTopScreen(LoginActivity::class.java)
                            }
                        }
                    } else {
                        screens!!.showClearTopScreen(LoginActivity::class.java)
                    }
                } else {
                    screens!!.showClearTopScreen(OnBoardingActivity::class.java)
                }
            } catch (e: Exception) {
                Utils.getErrors(e)
            }
        }, IConstants.SPLASH_DELAY.toLong())
    }

    private fun StartAnimations() {
        var anim = AnimationUtils.loadAnimation(this, R.anim.alpha)
        anim.reset()
        val l = findViewById<RelativeLayout>(R.id.lin_lay)
        l.clearAnimation()
        l.startAnimation(anim)
        anim = AnimationUtils.loadAnimation(this, R.anim.translate)
        anim.reset()
        val iv = findViewById<LinearLayout>(R.id.layout)
        iv.clearAnimation()
        iv.startAnimation(anim)
    }
}