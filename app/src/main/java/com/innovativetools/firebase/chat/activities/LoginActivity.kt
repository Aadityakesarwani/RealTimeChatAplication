package com.innovativetools.firebase.chat.activities


import android.widget.EditText
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import android.os.Bundle
import com.innovativetools.firebase.chat.activities.R
import com.shobhitpuri.custombuttons.GoogleSignInButton
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.innovativetools.firebase.chat.activities.ForgetPasswordActivity
import android.text.TextUtils
import com.innovativetools.firebase.chat.activities.RegisterActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.innovativetools.firebase.chat.activities.MainActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnCanceledListener
import android.content.Intent
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.app.Activity
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.lang.Exception
import java.net.URLEncoder
import java.util.*

class LoginActivity : BaseActivity() {
    private var mTxtEmail: EditText? = null
    private var mTxtPassword: EditText? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mTxtEmail = findViewById(R.id.txtEmail)
        mTxtPassword = findViewById(R.id.txtPassword)
        val mBtnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnGoogleSignIn = findViewById<GoogleSignInButton>(R.id.btnGoogleSignIn)
        val mTxtNewUser = findViewById<TextView>(R.id.txtNewUser)
        val txtForgetPassword = findViewById<TextView>(R.id.txtForgetPassword)
        try {
            // Configure Google Sign In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
        auth = FirebaseAuth.getInstance()
        Utils.setHTMLMessage(mTxtNewUser, getString(R.string.strNewSignUp))
        txtForgetPassword.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                screens!!.showCustomScreen(ForgetPasswordActivity::class.java)
            }
        })
        mBtnSignUp.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                val strEmail = mTxtEmail?.getText().toString().trim { it <= ' ' }
                val strPassword = mTxtPassword?.getText().toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)) {
                    screens!!.showToast(R.string.strAllFieldsRequired)
                } else {
                    login(strEmail, strPassword)
                }
            }
        })
        mTxtNewUser.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                screens!!.showCustomScreen(RegisterActivity::class.java)
            }
        })
        btnGoogleSignIn.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                signIn()
            }
        })
    }

    private fun login(email: String, password: String) {
        showProgress()
        auth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                hideProgress()
                if (task.isSuccessful) {
                    screens!!.showClearTopScreen(MainActivity::class.java)
                } else {
                    screens!!.showToast(R.string.strInvalidEmailPassword)
                }
            }.addOnFailureListener { e: Exception? -> hideProgress() }
            .addOnCanceledListener { hideProgress() }
    }

    //==========================================================================
    // ===== Google Sign in ====
    //==========================================================================
    private fun signIn() {
        try {
            showProgress()
            val signInIntent = mGoogleSignInClient!!.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth!!.signInWithCredential(credential)
                .addOnCompleteListener { task: Task<AuthResult?> ->
                    if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                        val user = auth!!.currentUser
                        Utils.sout("User::: " + user!!.photoUrl)
                        val userId = Objects.requireNonNull(user).uid
                        try {
                            val username = if (Utils.isEmpty(
                                    user.displayName
                                )
                            ) user.email else user.displayName
                            reference =
                                FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS)
                                    .child(userId)
                            reference!!.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        Utils.sout("UserID Available::: " + userId + " >> " + dataSnapshot.hasChildren())
                                        if (dataSnapshot.hasChildren()) {
                                            hideProgress()
                                            screens!!.showClearTopScreen(MainActivity::class.java)
                                        } else {
                                            val hashMap = HashMap<String, Any?>()
                                            hashMap[IConstants.EXTRA_ID] = userId
                                            hashMap[IConstants.EXTRA_EMAIL] = user.email
                                            hashMap[IConstants.EXTRA_USERNAME] =
                                                Utils.getCapsWord(username)
                                            hashMap[IConstants.EXTRA_PASSWORD] = user.email
                                            hashMap[IConstants.EXTRA_IMAGEURL] = if (Utils.isEmpty(
                                                    user.photoUrl
                                                )
                                            ) IConstants.IMG_DEFAULTS else URLEncoder.encode(
                                                user.photoUrl.toString(), "UTF-8"
                                            )
                                            hashMap[IConstants.EXTRA_ACTIVE] = true
                                            hashMap[IConstants.EXTRA_IS_ONLINE] =
                                                IConstants.STATUS_ONLINE
                                            hashMap[IConstants.EXTRA_SEARCH] =
                                                Objects.requireNonNull(username)?.lowercase(
                                                    Locale.getDefault()
                                                )?.trim { it <= ' ' }
                                            hashMap[IConstants.EXTRA_CREATED_AT] =
                                                Utils.dateTime
                                            hashMap[IConstants.EXTRA_VERSION] =
                                                BuildConfig.VERSION_NAME
                                            hashMap[IConstants.EXTRA_SIGNUP_TYPE] =
                                                IConstants.TYPE_GOOGLE
                                            hashMap[IConstants.EXTRA_SOCIAL_TOKEN] = idToken
                                            reference!!.setValue(hashMap)
                                                .addOnCompleteListener { task1: Task<Void?> ->
                                                    try {
                                                        if (task1.isSuccessful) {
                                                            hideProgress()
                                                            screens!!.showClearTopScreen(
                                                                MainActivity::class.java
                                                            )
                                                        }
                                                    } catch (e: Exception) {
                                                        Utils.getErrors(e)
                                                    }
                                                }
                                        }
                                    } catch (ignored: Exception) {
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    hideProgress()
                                }
                            })
                        } catch (e: Exception) {
                            hideProgress()
                            Utils.getErrors(e)
                        }
                    } else { // If sign in fails, display a message to the user.
                        hideProgress()
                        Utils.getErrors(task.exception)
                        screens!!.showToast(Objects.requireNonNull(task.exception)?.localizedMessage)
                    }
                }.addOnFailureListener { e: Exception ->
                hideProgress()
                screens!!.showToast(e.message)
            }.addOnCanceledListener { hideProgress() }
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    val googleSignInLauncher =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val data = result.data!!
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try { // Google Sign In was successful, authenticate with Firebase
                        val account = task.getResult(ApiException::class.java)
                        firebaseAuthWithGoogle(account.idToken)
                    } catch (e: ApiException) { // Google Sign In failed, update UI appropriately
                        Utils.getErrors(e)
                    }
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
            }
        }
}