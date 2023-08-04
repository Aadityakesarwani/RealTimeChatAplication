package com.innovativetools.firebase.chat.activities

import com.innovativetools.firebase.chat.activities.BaseActivity
import com.google.android.gms.ads.interstitial.InterstitialAd
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import android.webkit.WebView
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.ads.AdRequest
import com.innovativetools.firebase.chat.activities.managers.SessionManager
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets

class WebViewBrowserActivity : BaseActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        try {
            val mToolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(mToolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = intent.getStringExtra(IConstants.EXTRA_USERNAME)
            mToolbar.setNavigationOnClickListener(object : SingleClickListener() {
                override fun onClickView(v: View?) {
                    onBackPressed()
                }
            })
        } catch (e: Exception) {
            Utils.getErrors(e)
        }

//        mInterstitialAd = new InterstitialAd(this);
        val adView = findViewById<AdView>(R.id.adView)
        if (BuildConfig.ADS_SHOWN) {
            adView.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            InterstitialAd.load(
                mActivity!!,
                getString(R.string.interstitial_app_id),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        super.onAdLoaded(interstitialAd)
                        mInterstitialAd = interstitialAd
                        mInterstitialAd!!.show(mActivity!!)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Handle the error
                        mInterstitialAd = null
                    }
                })
        } else {
            adView.visibility = View.GONE
        }
        val linkPath = intent.getStringExtra(IConstants.EXTRA_LINK)
        val webView = findViewById<WebView>(R.id.webView)
        val sb = StringBuilder()
        try {
            val `is` = assets.open(linkPath!!)
            val br: BufferedReader
            br = BufferedReader(InputStreamReader(`is`, StandardCharsets.UTF_8))
            var str: String?
            while (br.readLine().also { str = it } != null) {
                sb.append(str)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val mimeType = "text/html;charset=UTF-8"
            val encoding = "utf-8"
            val htmlText = sb.toString()
            val rtl = if (SessionManager.get()!!.isRTLOn) "dir=\"rtl\"" else ""
            val text = ("<html><head>"
                    + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_res/font/roboto_regular.ttf\")}body{font-family: MyFont;color: #8D8D8D;}"
                    + "</style></head>"
                    + "<body " + rtl + ">"
                    + htmlText
                    + "</body></html>")
            webView.loadDataWithBaseURL(null, text, mimeType, encoding, null)
        } catch (e: Exception) {
            try {
                webView.loadUrl("file:///android_asset/$linkPath")
                webView.settings.javaScriptEnabled = true
            } catch (en: Exception) {
                Utils.getErrors(en)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}