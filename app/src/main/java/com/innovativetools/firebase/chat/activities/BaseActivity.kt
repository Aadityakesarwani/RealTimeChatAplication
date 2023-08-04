package com.innovativetools.firebase.chat.activities

import android.Manifest
import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.auth.FirebaseUser
import com.innovativetools.firebase.chat.activities.managers.Screens
import android.os.Bundle
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.lang.Exception

open class BaseActivity : AppCompatActivity() {
    @JvmField
    protected val permissionsRecord = arrayOf(
        Manifest.permission.VIBRATE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    @JvmField
    protected val permissionsContact =
        arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @JvmField
    protected val permissionsStorage = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @JvmField
    var mActivity: Activity? = null

    @JvmField
    var auth //Auth init
            : FirebaseAuth? = null
    var authStateListener: AuthStateListener? = null
    @JvmField
    var reference //Database related
            : DatabaseReference? = null
    @JvmField
    var firebaseUser //Current User
            : FirebaseUser? = null
    @JvmField
    var screens: Screens? = null
    var imgBack: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this
        screens = Screens(mActivity!!)
        try {
            authStateListener = AuthStateListener { firebaseAuth: FirebaseAuth? ->
                try {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        if (javaClass.simpleName.equals(
                                "LoginActivity",
                                ignoreCase = true
                            ) || javaClass.simpleName.equals("RegisterActivity", ignoreCase = true)
                        ) {
                        } else {
                            screens!!.showClearTopScreen(LoginActivity::class.java)
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener!!)
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    fun backButton() {
        try {
            imgBack = findViewById(R.id.imgBack)
            imgBack!!.setOnClickListener(object : SingleClickListener() {
                override fun onClickView(v: View?) {
                    finish()
                }
            })
        } catch (ignored: Exception) {
        }
    }

    private var pd: ProgressDialog? = null
    fun showProgress() {
        try {
            if (pd == null) {
                pd = ProgressDialog(mActivity)
            }
            pd!!.setMessage(getString(R.string.msg_please_wait))
            pd!!.show()
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    fun hideProgress() {
        try {
            if (pd != null) {
                pd!!.dismiss()
                pd = null
            }
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    protected fun permissionsAvailable(permissions: Array<String>): Boolean {
        var granted = true
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                granted = false
                break
            }
        }
        return granted
    }
}