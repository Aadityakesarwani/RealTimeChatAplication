package com.innovativetools.firebase.chat.activities.models

import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.innovativetools.firebase.chat.activities.managers.Utils
import java.io.Serializable
import java.lang.Exception
import java.net.URLDecoder

class User : Serializable {
    var id: String? = null
    var username: String? = null
    var email: String? = null
    var myImg: String? = null
        private set
    var status: String? = null
    var isOnline = IConstants.STATUS_OFFLINE
    var search: String? = null
    var password: String? = null
    var isActive = false
    var isTyping = false
    var typingwith: String? = null
    var about: String? = null
    var gender: String? = null
    var genders = IConstants.GEN_UNSPECIFIED
    var lastSeen: String? = null
    var isChecked = false
    var isAdmin = false
    var isHideEmail = IConstants.FALSE
    var isHideProfilePhoto = IConstants.FALSE
    var signup_type = IConstants.TYPE_EMAIL
    var social_token = ""
    fun getImageURL(): String? {
        return if (isHideProfilePhoto) {
            IConstants.IMG_PREVIEW
        } else myImg
    }

    fun setImageURL(imageURL: String) {
        var imageURL = imageURL
        if (imageURL.startsWith("https%3A%2F%2") || imageURL.startsWith("http%3A%2F%2")) {
            try {
                imageURL = URLDecoder.decode(imageURL, "UTF-8")
            } catch (e: Exception) {
                Utils.getErrors(e)
            }
        }
        myImg = imageURL
    }

    override fun toString(): String {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", imageURL='" + myImg + '\'' +
                ", status='" + status + '\'' +
                ", isOnline=" + isOnline +
                ", search='" + search + '\'' +
                ", password='" + password + '\'' +
                ", active=" + isActive +
                ", typing=" + isTyping +
                ", typingwith='" + typingwith + '\'' +
                ", about='" + about + '\'' +
                ", gender='" + gender + '\'' +
                ", genders=" + genders +
                ", lastSeen='" + lastSeen + '\'' +
                ", isChecked=" + isChecked +
                ", isAdmin=" + isAdmin +
                ", hideEmail=" + isHideEmail +
                ", hideProfilePhoto=" + isHideProfilePhoto +
                ", signup_type='" + signup_type + '\'' +
                ", social_token='" + social_token + '\'' +
                '}'
    }
}