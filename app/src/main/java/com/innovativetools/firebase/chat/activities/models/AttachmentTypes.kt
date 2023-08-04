package com.innovativetools.firebase.chat.activities.models

import android.net.Uri
import com.innovativetools.firebase.chat.activities.models.AttachmentTypes.AttachmentType
import com.innovativetools.firebase.chat.activities.models.AttachmentTypes
import com.innovativetools.firebase.chat.activities.constants.IConstants
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import com.innovativetools.firebase.chat.activities.managers.Utils
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

object AttachmentTypes {
    const val IMAGE = 0
    const val VIDEO = 1
    const val CONTACT = 2
    const val AUDIO = 3
    const val LOCATION = 4
    const val DOCUMENT = 5
    const val RECORDING = 6
    const val NONE_TEXT = 7
    const val NONE_TYPING = 8
    fun getTypeName(@AttachmentType attachmentType: Int): String {
        return when (attachmentType) {
            IMAGE -> IConstants.TYPE_IMAGE
            AUDIO -> IConstants.TYPE_AUDIO
            VIDEO -> IConstants.TYPE_VIDEO
            CONTACT -> IConstants.TYPE_CONTACT
            DOCUMENT -> IConstants.TYPE_DOCUMENT
            LOCATION -> IConstants.TYPE_LOCATION
            RECORDING -> IConstants.TYPE_RECORDING
            NONE_TEXT -> "none_text"
            NONE_TYPING -> "none_typing"
            else -> "none"
        }
    }

    @JvmStatic
    fun getTypeName(attachmentType: String?): String {
        return when (attachmentType) {
            IConstants.TYPE_IMAGE -> IConstants.TYPE_IMAGE
            IConstants.TYPE_AUDIO -> IConstants.TYPE_AUDIO
            IConstants.TYPE_VIDEO -> IConstants.TYPE_VIDEO
            IConstants.TYPE_CONTACT -> IConstants.TYPE_CONTACT
            IConstants.TYPE_DOCUMENT -> IConstants.TYPE_DOCUMENT
            IConstants.TYPE_LOCATION -> IConstants.TYPE_LOCATION
            IConstants.TYPE_RECORDING -> IConstants.TYPE_RECORDING
            else -> "none"
        }
    }

    @JvmStatic
    fun getDirectoryByType(type: String?): String {
        when (type) {
            IConstants.TYPE_AUDIO, IConstants.TYPE_RECORDING -> return Utils.musicFolder
            IConstants.TYPE_VIDEO -> return Utils.moviesFolder
            IConstants.TYPE_DOCUMENT, IConstants.TYPE_CONTACT -> return Utils.downloadFolder
        }
        return Utils.downloadFolder
    }

    @JvmStatic
    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun getTargetUri(type: String?): Uri {
        when (type) {
            IConstants.TYPE_AUDIO, IConstants.TYPE_RECORDING -> return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            IConstants.TYPE_VIDEO -> return MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            IConstants.TYPE_DOCUMENT, IConstants.TYPE_CONTACT -> return MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }
        return MediaStore.Downloads.EXTERNAL_CONTENT_URI
    }

    @JvmStatic
    fun getExtension(fileExtension: String, attachmentType: Int): String {
        when (attachmentType) {
            AUDIO, RECORDING -> if (!fileExtension.endsWith(IConstants.EXT_MP3)) return IConstants.EXT_MP3
        }
        return fileExtension
    }

    @IntDef(*[IMAGE, VIDEO, CONTACT, AUDIO, LOCATION, DOCUMENT, NONE_TEXT, NONE_TYPING, RECORDING])
    @Retention(
        RetentionPolicy.SOURCE
    )
    annotation class AttachmentType
}