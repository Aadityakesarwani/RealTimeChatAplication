package com.innovativetools.firebase.chat.activities.managers

import com.innovativetools.firebase.chat.activities.managers.Utils.sout
import com.innovativetools.firebase.chat.activities.managers.Utils.getOpenFileIntent
import com.innovativetools.firebase.chat.activities.managers.Utils.getErrors
import com.innovativetools.firebase.chat.activities.models.AttachmentTypes.getDirectoryByType
import com.innovativetools.firebase.chat.activities.models.DownloadFileEvent
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.innovativetools.firebase.chat.activities.R
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import java.io.File
import java.lang.Exception

class DownloadUtil {
    fun loading(context: Context, downloadFileEvent: DownloadFileEvent) {
        try {
            val attach = downloadFileEvent.attachment
            val file = File(
                IConstants.SDPATH + getDirectoryPath(attach.attachmentType),
                IConstants.SLASH + context.getString(R.string.app_name) + IConstants.SLASH + attach.attachmentType + IConstants.SLASH + attach.attachmentFileName
            )
            sout("Downloading + Loading::: $file")
            if (file.exists()) {
                getOpenFileIntent(context, file.toString())
            } else {
                val screens = Screens(context)
                screens.showToast(R.string.msgDownloadingStarted)
                downloadFile(
                    context,
                    attach.attachmentPath,
                    attach.attachmentType,
                    attach.attachmentFileName
                )
            }
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    private fun downloadFile(context: Context, url: String?, type: String?, fileName: String?) {
        val mgr = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(fileName)
            .setDescription(context.getString(R.string.msgDownloadFile, fileName))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
            .setVisibleInDownloadsUi(false)
            .setDestinationInExternalPublicDir(
                getDirectoryPath(type),
                IConstants.SLASH + context.getString(R.string.app_name) + IConstants.SLASH + type + IConstants.SLASH + fileName
            )
        mgr.enqueue(request)
    }

    private fun getDirectoryPath(type: String?): String {
        return getDirectoryByType(type)
    }
}