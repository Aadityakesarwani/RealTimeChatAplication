package com.innovativetools.firebase.chat.activities


import android.widget.EditText
import android.os.Bundle
import com.innovativetools.firebase.chat.activities.R
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import android.text.TextUtils
import android.view.View
import android.widget.Button
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.innovativetools.firebase.chat.activities.MainActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.Task
import com.google.firebase.BuildConfig
import com.innovativetools.firebase.chat.activities.managers.Utils
import java.lang.Exception
import java.util.*

class RegisterActivity : BaseActivity(), View.OnClickListener {
    private var mTxtEmail: EditText? = null
    private var mTxtUsername: EditText? = null
    private var mTxtPassword: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mTxtEmail = findViewById(R.id.txtEmail)
        mTxtUsername = findViewById(R.id.txtUsername)
        mTxtPassword = findViewById(R.id.txtPassword)
        val mBtnRegister = findViewById<Button>(R.id.btnRegister)
        val mTxtExistingUser = findViewById<TextView>(R.id.txtExistingUser)

//        mTxtExistingUser.setText(HtmlCompat.fromHtml(getString(R.string.strExistUser), HtmlCompat.FROM_HTML_MODE_LEGACY));
        Utils.setHTMLMessage(mTxtExistingUser, getString(R.string.strExistUser))
        mBtnRegister.setOnClickListener(this)
        mTxtExistingUser.setOnClickListener(this)
        auth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btnRegister) {
            val strEmail = mTxtEmail!!.text.toString().trim { it <= ' ' }
            val strUsername = mTxtUsername!!.text.toString().trim { it <= ' ' }
            val strPassword = mTxtPassword!!.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strUsername) || TextUtils.isEmpty(
                    strPassword
                )
            ) {
                screens!!.showToast(R.string.strAllFieldsRequired)
            } else if (!Utils.isValidEmail(strEmail)) {
                screens!!.showToast(R.string.strInvalidEmail)
            } else {
                register(strEmail, strUsername, strPassword)
            }
        } else if (id == R.id.txtExistingUser) {
            finish()
        }
    }

    private fun register(email: String, username: String, password: String) {
        showProgress()
        auth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val firebaseUser = auth!!.currentUser!!
                    val userId = firebaseUser.uid
                    reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS)
                        .child(userId)
                    val hashMap = HashMap<String, Any>()
                    hashMap[IConstants.EXTRA_ID] = userId
                    hashMap[IConstants.EXTRA_EMAIL] = email
                    hashMap[IConstants.EXTRA_USERNAME] = Utils.getCapsWord(username)
                    hashMap[IConstants.EXTRA_PASSWORD] = password
                    hashMap[IConstants.EXTRA_IMAGEURL] = IConstants.IMG_DEFAULTS
                    hashMap[IConstants.EXTRA_ACTIVE] = true
                    hashMap[IConstants.EXTRA_IS_ONLINE] = IConstants.STATUS_ONLINE
                    hashMap[IConstants.EXTRA_SEARCH] =
                        username.lowercase(Locale.getDefault()).trim { it <= ' ' }
                    hashMap[IConstants.EXTRA_CREATED_AT] = Utils.dateTime
                    hashMap[IConstants.EXTRA_VERSION] = BuildConfig.VERSION_NAME
                    hashMap[IConstants.EXTRA_SIGNUP_TYPE] = IConstants.TYPE_EMAIL
                    reference!!.setValue(hashMap).addOnCompleteListener { task1: Task<Void?>? ->
                        hideProgress()
                        screens!!.showClearTopScreen(MainActivity::class.java)
                    }
                }
            }.addOnFailureListener { e: Exception ->
            hideProgress()
            screens!!.showToast(e.message)
        }.addOnCanceledListener { hideProgress() }
    }
}