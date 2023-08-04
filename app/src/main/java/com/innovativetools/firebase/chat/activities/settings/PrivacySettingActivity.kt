package com.innovativetools.firebase.chat.activities.settings

import com.innovativetools.firebase.chat.activities.managers.Utils.updateGenericUserField
import com.innovativetools.firebase.chat.activities.BaseActivity
import android.os.Bundle
import android.view.View
import com.innovativetools.firebase.chat.activities.R
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.ads.AdView
import android.widget.LinearLayout
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.ads.AdRequest
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.innovativetools.firebase.chat.activities.BuildConfig
import com.innovativetools.firebase.chat.activities.models.User
import com.innovativetools.firebase.chat.activities.views.SingleClickListener

class PrivacySettingActivity : BaseActivity(), View.OnClickListener {
    private var emailOnOff: SwitchCompat? = null
    private var profilePhotoOnOff: SwitchCompat? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_settings)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        assert(firebaseUser != null)
        val currentId = firebaseUser!!.uid
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
        supportActionBar!!.setTitle(R.string.strPrivacySetting)
        mToolbar.setNavigationOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                onBackPressed()
            }
        })
        val layoutEmail = findViewById<LinearLayout>(R.id.layoutEmail)
        val layoutProfilePhoto = findViewById<LinearLayout>(R.id.layoutProfilePhoto)
        profilePhotoOnOff = findViewById(R.id.profilePhotoOnOff)
        profilePhotoOnOff?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            updateGenericUserField(
                currentId,
                IConstants.EXTRA_HIDE_PROFILE_PHOTO,
                b
            )
        })
        emailOnOff = findViewById(R.id.emailOnOff)
        emailOnOff?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            updateGenericUserField(
                currentId,
                IConstants.EXTRA_HIDE_EMAIL,
                b
            )
        })
        reference =
            FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(currentId)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    val user = dataSnapshot.getValue(
                        User::class.java
                    )!!
                    emailOnOff?.setChecked(user.isHideEmail)
                    profilePhotoOnOff?.setChecked(user.isHideProfilePhoto)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        layoutEmail.setOnClickListener(this)
        layoutProfilePhoto.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.layoutEmail) {
            if (emailOnOff!!.isChecked) {
                emailOnOff!!.isChecked = IConstants.FALSE
            } else {
                emailOnOff!!.isChecked = IConstants.TRUE
            }
        } else if (id == R.id.layoutProfilePhoto) {
            if (profilePhotoOnOff!!.isChecked) {
                profilePhotoOnOff!!.isChecked = IConstants.FALSE
            } else {
                profilePhotoOnOff!!.isChecked = IConstants.TRUE
            }
        }
    }
}