package com.innovativetools.firebase.chat.activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.github.chrisbanes.photoview.PhotoView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.lang.Exception

class ImageViewerActivity : AppCompatActivity() {
    var placeholder = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        setContentView(R.layout.activity_image_fullscreen)
        val imgPath = extras!!.getString(IConstants.EXTRA_IMGPATH)
        val imageUri = Uri.parse(imgPath)
        val groupName = extras.getString(IConstants.EXTRA_GROUP_NAME, "")
        val username = extras.getString(IConstants.EXTRA_USERNAME, "")
        findViewById<View>(R.id.imgBack).setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                onBackPressed()
            }
        })
        val imageViewZoom = findViewById<PhotoView>(R.id.imgPath)
        val txtMyName = findViewById<TextView>(R.id.txtMyName)
        if (Utils.isEmpty(groupName)) {
            if (Utils.isEmpty(username)) {
                txtMyName.visibility = View.GONE
            } else {
                txtMyName.visibility = View.VISIBLE
                txtMyName.text = username
            }
        } else {
            txtMyName.text = groupName
        }
        placeholder = if (!Utils.isEmpty(groupName) && imgPath.equals(
                IConstants.IMG_DEFAULTS,
                ignoreCase = true
            )
        ) {
            R.drawable.img_group_default_orange
        } else {
            R.drawable.profile_avatar
        }
        try {
            if (imgPath == IConstants.IMG_DEFAULTS) {
                Glide.with(this).load(placeholder).into(imageViewZoom)
            } else {
                Glide.with(this).load(imageUri).into(imageViewZoom)
            }
        } catch (ignored: Exception) {
        }
    }
}