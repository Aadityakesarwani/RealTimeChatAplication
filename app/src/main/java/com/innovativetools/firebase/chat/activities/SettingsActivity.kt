package com.innovativetools.firebase.chat.activities

import com.innovativetools.firebase.chat.activities.BaseActivity
import android.os.Bundle
import android.os.Handler
import com.innovativetools.firebase.chat.activities.R
import com.google.android.gms.ads.AdView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.CompoundButton
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.innovativetools.firebase.chat.activities.settings.PrivacySettingActivity
import android.os.Looper
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.ads.AdRequest
import com.innovativetools.firebase.chat.activities.constants.IDialogListener
import com.innovativetools.firebase.chat.activities.MainActivity
import com.innovativetools.firebase.chat.activities.managers.SessionManager
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.views.SingleClickListener

class SettingsActivity : BaseActivity(), View.OnClickListener {
    private var notificationOnOff: SwitchCompat? = null
    private var rtlOnOff: SwitchCompat? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val adView = findViewById<AdView>(R.id.adView)
        if (BuildConfig.ADS_SHOWN) {
            adView.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } else {
            adView.visibility = View.GONE
        }
        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.strSettings)
        mToolbar.setNavigationOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                onBackPressed()
            }
        })
        val layoutNotification = findViewById<LinearLayout>(R.id.layoutNotification)
        val layoutRTL = findViewById<LinearLayout>(R.id.layoutRTL)
        val layoutPrivacySettings = findViewById<LinearLayout>(R.id.layoutPrivacySettings)
        val layoutRateApp = findViewById<LinearLayout>(R.id.layoutRateApp)
        val layoutShare = findViewById<LinearLayout>(R.id.layoutShare)
        val layoutAbout = findViewById<LinearLayout>(R.id.layoutAbout)
        val layoutPrivacyPolicy = findViewById<LinearLayout>(R.id.layoutPrivacyPolicy)
        val layoutLogout = findViewById<LinearLayout>(R.id.layoutLogout)
        val layoutTakeTour = findViewById<LinearLayout>(R.id.layoutTakeTour)
        val mTxtVersionName = findViewById<TextView>(R.id.txtAppVersion)
        mTxtVersionName.text =
            String.format(getString(R.string.settingVersion), BuildConfig.VERSION_NAME)
        rtlOnOff = findViewById(R.id.rtlOnOff)
        rtlOnOff?.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                restartApp()
            }
        })
        rtlOnOff?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            SessionManager.get()?.setOnOffRTL(b)
        })
        notificationOnOff = findViewById(R.id.notificationOnOff)
        notificationOnOff?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            SessionManager.get()?.setOnOffNotification(b)
        })
        if (SessionManager.get()!!.isNotificationOn) {
            notificationOnOff?.setChecked(IConstants.TRUE)
        } else {
            notificationOnOff?.setChecked(IConstants.FALSE)
        }
        if (SessionManager.get()!!.isRTLOn) {
            rtlOnOff?.setChecked(IConstants.TRUE)
        } else {
            rtlOnOff?.setChecked(IConstants.FALSE)
        }
        layoutNotification.setOnClickListener(this)
        layoutRTL.setOnClickListener(this)
        layoutPrivacySettings.setOnClickListener(this)
        layoutRateApp.setOnClickListener(this)
        layoutShare.setOnClickListener(this)
        layoutAbout.setOnClickListener(this)
        layoutPrivacyPolicy.setOnClickListener(this)
        layoutLogout.setOnClickListener(this)
        layoutTakeTour.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.layoutNotification) {
            if (notificationOnOff!!.isChecked) {
                notificationOnOff!!.isChecked = IConstants.FALSE
            } else {
                notificationOnOff!!.isChecked = IConstants.TRUE
            }
        } else if (id == R.id.layoutRTL) {
            if (rtlOnOff!!.isChecked) {
                rtlOnOff!!.isChecked = IConstants.FALSE
            } else {
                rtlOnOff!!.isChecked = IConstants.TRUE
            }
            restartApp()
        } else if (id == R.id.layoutPrivacySettings) {
            screens!!.showCustomScreen(PrivacySettingActivity::class.java)
        } else if (id == R.id.layoutTakeTour) {
            screens!!.openOnBoardingScreen(IConstants.TRUE)
        } else if (id == R.id.layoutRateApp) {
            Utils.rateApp(mActivity!!)
        } else if (id == R.id.layoutShare) {
            Utils.shareApp(mActivity!!)
        } else if (id == R.id.layoutAbout) {
            Handler(Looper.getMainLooper()).postDelayed({
                screens!!.openWebViewActivity(
                    getString(R.string.lblAboutUs),
                    IConstants.PATH_ABOUT_US
                )
            }, IConstants.CLICK_DELAY_TIME)
        } else if (id == R.id.layoutPrivacyPolicy) {
            Handler(Looper.getMainLooper()).postDelayed({
                screens!!.openWebViewActivity(
                    getString(R.string.lblPrivacyPolicy),
                    IConstants.PATH_PRIVACY_POLICY
                )
            }, IConstants.CLICK_DELAY_TIME)
        } else if (id == R.id.layoutLogout) {
            Utils.logout(mActivity!!)
        }
    }

    private fun restartApp() {
        Utils.showOKDialog(
            mActivity!!, R.string.ref_title, R.string.ref_message
        ) { screens!!.showClearTopScreen(MainActivity::class.java) }
    }
}