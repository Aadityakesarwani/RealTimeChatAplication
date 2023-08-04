package com.innovativetools.firebase.chat.activities

import android.widget.EditText
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.innovativetools.firebase.chat.activities.R
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.innovativetools.firebase.chat.activities.constants.IDialogListener
import com.innovativetools.firebase.chat.activities.LoginActivity
import com.innovativetools.firebase.chat.activities.managers.Utils

class ForgetPasswordActivity : BaseActivity(), View.OnClickListener {
    private var txtEmail: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)
        backButton()
        txtEmail = findViewById(R.id.txtEmail)
        val btnSend = findViewById<Button>(R.id.btnSend)
        auth = FirebaseAuth.getInstance()
        btnSend.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btnSend) {
            val strEmail = txtEmail!!.text.toString().trim { it <= ' ' }
            if (Utils.isEmpty(strEmail)) {
                screens!!.showToast(R.string.strAllFieldsRequired)
            } else if (!Utils.isValidEmail(strEmail)) {
                screens!!.showToast(R.string.strInvalidEmail)
            } else {
                showProgress()
                auth!!.sendPasswordResetEmail(strEmail).addOnCompleteListener { task: Task<Void?> ->
                    hideProgress()
                    if (task.isSuccessful) {
                        Utils.showOKDialog(
                            mActivity!!, IConstants.ZERO, R.string.lblSendYouForgetEmail
                        ) { screens!!.showClearTopScreen(LoginActivity::class.java) }
                    }
                }
            }
        }
    }
}