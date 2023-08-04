package com.innovativetools.firebase.chat.activities

import com.innovativetools.firebase.chat.activities.managers.Screens
import android.os.Bundle
import com.innovativetools.firebase.chat.activities.R
import android.content.Intent
import android.view.View
import android.widget.ImageView
import com.innovativetools.firebase.chat.activities.constants.IConstants
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.Groups
import com.innovativetools.firebase.chat.activities.models.User
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.lang.Exception

class ProfileDialogActivity : AppCompatActivity() {
    private var username: String? = null
    private var user: User? = null
    private var groups: Groups? = null
    private var isGroupObj = false
    private var screens: Screens? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.profile_dialog)
        screens = Screens(this)
        val intent = intent
        var imgPath: String?
        try {
            user = intent.getSerializableExtra(IConstants.EXTRA_OBJ_GROUP) as User?
            username = user!!.username
            imgPath = user!!.getImageURL()
        } catch (e: Exception) {
            isGroupObj = true
            groups = intent.getSerializableExtra(IConstants.EXTRA_OBJ_GROUP) as Groups?
            username = groups!!.groupName
            imgPath = groups!!.groupImg
        }
        try {
            val txtUsername = findViewById<TextView>(R.id.txtUsername)
            val profileImage = findViewById<ImageView>(R.id.profile_image)
            Glide.with(this)
                .load(imgPath)
                .apply(
                    if (isGroupObj) RequestOptions().placeholder(R.drawable.img_group_default_orange) else RequestOptions().placeholder(
                        R.drawable.profile_avatar
                    )
                )
                .into(profileImage)
            txtUsername.text = username
            val imgChatView = findViewById<ImageView>(R.id.imgChatView)
            val imgInfoView = findViewById<ImageView>(R.id.imgInfoView)
            imgChatView.setOnClickListener(object : SingleClickListener() {
                override fun onClickView(v: View?) {
                    if (isGroupObj) {
                        screens!!.openGroupMessageActivity(groups)
                    } else {
                        screens!!.openUserMessageActivity(user!!.id)
                    }
                    finish()
                }
            })
            imgInfoView.setOnClickListener(object : SingleClickListener() {
                override fun onClickView(v: View?) {
                    if (isGroupObj) {
                        screens!!.openGroupParticipantActivity(groups)
                    } else {
                        screens!!.openViewProfileActivity(user!!.id)
                    }
                    finish()
                }
            })
            profileImage.setOnClickListener(object : SingleClickListener() {
                override fun onClickView(v: View?) {
                    try {
                        val strAvatarImg: String
                        if (isGroupObj) {
                            strAvatarImg = groups!!.groupImg.toString()
                            screens!!.openFullImageViewActivity(
                                v,
                                strAvatarImg,
                                groups!!.groupName,
                                ""
                            )
                        } else {
                            strAvatarImg = user!!.getImageURL().toString()
                            screens!!.openFullImageViewActivity(v, strAvatarImg, username)
                        }
                    } catch (e: Exception) {
                        Utils.getErrors(e)
                    }
                }
            })
        } catch (ignored: Exception) {
        }
    }
}