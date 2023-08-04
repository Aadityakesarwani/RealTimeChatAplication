package com.innovativetools.firebase.chat.activities

import com.github.appintro.AppIntro2
import com.innovativetools.firebase.chat.activities.managers.Screens
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.github.appintro.AppIntroCustomLayoutFragment
import com.github.appintro.AppIntroPageTransformerType
import com.innovativetools.firebase.chat.activities.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.innovativetools.firebase.chat.activities.MainActivity
import com.innovativetools.firebase.chat.activities.LoginActivity
import com.innovativetools.firebase.chat.activities.managers.SessionManager

class OnBoardingActivity : AppIntro2() {
    private var screens: Screens? = null
    private var isTakeTour = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screens = Screens(applicationContext)
        isTakeTour = intent.getBooleanExtra(IConstants.EXTRA_STATUS, false)
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_slider_1))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_slider_2))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_slider_3))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_slider_4))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_slider_5))
        isVibrate = true
        setTransformer(AppIntroPageTransformerType.Parallax(1.0, -1.0, 2.0))
        setNavBarColor(ContextCompat.getColor(this, R.color.navGrayColor))
        isImmersive = true
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        nextScreen()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        nextScreen()
    }

    private fun nextScreen() {
        if (isTakeTour) {
            finish()
        } else {
            SessionManager.get()?.isOnBoardingDone = true
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                screens!!.showClearTopScreen(MainActivity::class.java)
            } else {
                screens!!.showClearTopScreen(LoginActivity::class.java)
            }
        }
    }
}