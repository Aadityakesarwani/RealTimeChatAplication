package com.innovativetools.firebase.chat.activities.managers

import com.innovativetools.firebase.chat.activities.managers.Utils.isEmpty
import com.innovativetools.firebase.chat.activities.managers.Utils.getRegularFont
import com.innovativetools.firebase.chat.activities.managers.Utils.getErrors
import android.content.Intent
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.TextView
import com.innovativetools.firebase.chat.activities.OnBoardingActivity
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.innovativetools.firebase.chat.activities.ProfileDialogActivity
import com.innovativetools.firebase.chat.activities.MessageActivity
import com.innovativetools.firebase.chat.activities.ViewUserProfileActivity
import com.innovativetools.firebase.chat.activities.GroupsMessagesActivity
import com.innovativetools.firebase.chat.activities.GroupsParticipantsActivity
import android.app.Activity
import android.content.Context
import android.os.Handler
import com.innovativetools.firebase.chat.activities.ImageViewerActivity
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.SettingsActivity
import com.innovativetools.firebase.chat.activities.WebViewBrowserActivity
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import com.innovativetools.firebase.chat.activities.models.Groups
import java.io.Serializable
import java.lang.Exception


class Screens(private val context: Context) {
    fun showClearTopScreen(cls: Class<*>?) {
        val intent = Intent(context, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    fun showCustomScreen(cls: Class<*>?) {
        val intent = Intent(context, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun startHomeScreen() {
        val startHome = Intent(Intent.ACTION_MAIN)
        startHome.addCategory(Intent.CATEGORY_HOME)
        startHome.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startHome.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(startHome)
    }

    private var toastMessage: Toast? = null
    fun showToast(strMsg: String?) {
        try {
            if (toastMessage != null) {
                toastMessage!!.cancel()
            }
            if (!isEmpty(strMsg)) {
                toastMessage = Toast.makeText(context, strMsg, Toast.LENGTH_LONG)
                try {
                    val toastLayout = toastMessage?.getView() as LinearLayout?
                    val txtToast = toastLayout!!.getChildAt(0) as TextView
                    txtToast.setTypeface(getRegularFont(context))
                } catch (e: Exception) {
                    getErrors(e)
                }
                toastMessage?.show()
            }
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun showToast(strMsg: Int) {
        showToast(context.getString(strMsg))
    }

    fun openOnBoardingScreen(isTakeTour: Boolean) {
        val intent = Intent(context, OnBoardingActivity::class.java)
        intent.putExtra(IConstants.EXTRA_STATUS, isTakeTour)
        context.startActivity(intent)
    }

    fun openProfilePictureActivity(`object`: Any?) {
        val intent = Intent(context, ProfileDialogActivity::class.java)
        intent.putExtra(IConstants.EXTRA_OBJ_GROUP, `object` as Serializable?)
        context.startActivity(intent)
    }

    fun openUserMessageActivity(userId: String?) {
        val intent = Intent(context, MessageActivity::class.java)
        intent.putExtra(IConstants.EXTRA_USER_ID, userId)
        context.startActivity(intent)
    }

    fun openViewProfileActivity(userId: String?) {
        val intent = Intent(context, ViewUserProfileActivity::class.java)
        intent.putExtra(IConstants.EXTRA_USER_ID, userId)
        context.startActivity(intent)
    }

    fun openGroupMessageActivity(`object`: Groups?) {
        val intent = Intent(Intent(context, GroupsMessagesActivity::class.java))
        intent.putExtra(IConstants.EXTRA_OBJ_GROUP, `object`)
        context.startActivity(intent)
    }

    fun openGroupParticipantActivity(groups: Groups?) {
        val intent = Intent(context, GroupsParticipantsActivity::class.java)
        intent.putExtra(IConstants.EXTRA_OBJ_GROUP, groups)
        (context as Activity).startActivityForResult(intent, IConstants.REQUEST_PARTICIPATE)
    }

    fun openFullImageViewActivity(view: View?, imgPath: String?, username: String?) {
        openFullImageViewActivity(view, imgPath, "", username)
    }

    fun openFullImageViewActivity(
        view: View?,
        imgPath: String?,
        groupName: String?,
        username: String?
    ) {
        val intent = Intent(context, ImageViewerActivity::class.java)
        intent.putExtra(IConstants.EXTRA_IMGPATH, imgPath)
        intent.putExtra(IConstants.EXTRA_GROUP_NAME, groupName)
        intent.putExtra(IConstants.EXTRA_USERNAME, username)
        try {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                (context as Activity), view!!, context.getString(R.string.app_name)
            )
            context.startActivity(intent, options.toBundle())
        } catch (e: Exception) {
            context.startActivity(intent)
        }
    }

    fun openSettingsActivity() {
        val intent = Intent(context, SettingsActivity::class.java)
        context.startActivity(intent)
    }

    fun openWebViewActivity(title: String?, path: String?) {
        val intent = Intent(context, WebViewBrowserActivity::class.java)
        intent.putExtra(IConstants.EXTRA_USERNAME, title)
        intent.putExtra(IConstants.EXTRA_LINK, path)
        context.startActivity(intent)
    }

    fun openGPSSettingScreen() {
        showToast(context.getString(R.string.msgGPSTurnOn))
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }, IConstants.CLICK_DELAY_TIME)
    }
}