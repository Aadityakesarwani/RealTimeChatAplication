package com.innovativetools.firebase.chat.activities.fcm

import com.innovativetools.firebase.chat.activities.fcm.ApplicationLifecycleManager.Companion.isAppVisible
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.innovativetools.firebase.chat.activities.fcm.ApplicationLifecycleManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import android.graphics.Bitmap
import com.innovativetools.firebase.chat.activities.R
import android.os.Bundle
import android.content.Intent
import com.innovativetools.firebase.chat.activities.MessageActivity
import com.innovativetools.firebase.chat.activities.GroupsMessagesActivity
import com.google.gson.Gson
import android.app.PendingIntent
import android.media.RingtoneManager
import android.app.NotificationManager
import android.os.Build
import android.app.NotificationChannel
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.innovativetools.firebase.chat.activities.managers.SessionManager
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.Groups
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Utils.uploadToken(s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (!SessionManager.get()!!.isNotificationOn) {
            return
        }
        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            val sent = remoteMessage.data[IConstants.FCM_SENT]
            if (!isAppVisible) {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    assert(sent != null)
                    if (sent.equals(firebaseUser.uid, ignoreCase = true)) {
                        sendNotification(remoteMessage)
                    }
                }
            }
        }
    }

    private var strGroups: String? = ""
    private var type: String? = ""
    private var username: String? = ""
    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data[IConstants.FCM_USER]
        val icon = remoteMessage.data[IConstants.FCM_ICON]
        val title = remoteMessage.data[IConstants.FCM_TITLE]
        val message = remoteMessage.data[IConstants.FCM_BODY]
        try {
            type = remoteMessage.data[IConstants.FCM_TYPE]
            username = remoteMessage.data[IConstants.FCM_USERNAME]
        } catch (ignored: Exception) {
        }
        try {
            strGroups = remoteMessage.data[IConstants.FCM_GROUPS]
        } catch (ignored: Exception) {
        }
        var bitmap: Bitmap? = null
        val body: String?
        if (!Utils.isEmpty(type)) {
            if (type.equals(IConstants.TYPE_IMAGE, ignoreCase = true)) {
                bitmap = getBitmapFromURL(message)
                body = String.format(getString(R.string.strPhotoSent), username)
            } else {
                body = "$username: $message"
            }
        } else {
            body = message
        }
        val bundle = Bundle()
        val intent: Intent
        if (Utils.isEmpty(strGroups)) {
            intent = Intent(this, MessageActivity::class.java)
            bundle.putString(IConstants.EXTRA_USER_ID, user)
        } else {
            intent = Intent(this, GroupsMessagesActivity::class.java)
            val groups = Gson().fromJson(strGroups, Groups::class.java)
            bundle.putSerializable(IConstants.EXTRA_OBJ_GROUP, groups)
        }
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent: PendingIntent
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this,
                0 /* Request code */,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this,
                0 /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )
        }
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        notificationBuilder
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setTicker(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        if (bitmap != null) {
            notificationBuilder.setLargeIcon(bitmap)
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(bitmap)
            )
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(Date().time.toInt(), notificationBuilder.build())
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    private fun getBitmapFromURL(strURL: String?): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Utils.getErrors(e)
            null
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}