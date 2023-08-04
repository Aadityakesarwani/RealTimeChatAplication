package com.innovativetools.firebase.chat.activities.fragments

import androidx.recyclerview.widget.RecyclerView
import com.innovativetools.firebase.chat.activities.adapters.UserAdapters
import com.innovativetools.firebase.chat.activities.managers.Screens
import android.app.Activity
import android.os.Bundle
import android.app.ProgressDialog
import android.content.Context
import androidx.fragment.app.Fragment
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.User
import java.lang.Exception
import java.util.ArrayList

open class BaseFragment : Fragment() {
    @JvmField
    var mRecyclerView: RecyclerView? = null
    @JvmField
    var mUsers: ArrayList<User>? = null
    @JvmField
    var userAdapters: UserAdapters? = null
    @JvmField
    var screens: Screens? = null
    @JvmField
    var mActivity: Activity? = null
    @JvmField
    var mContext: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity
        screens = Screens(requireActivity())
        mContext = context
    }

    private var pd: ProgressDialog? = null
    fun showProgress() {
        try {
            if (pd == null) {
                pd = ProgressDialog(mActivity)
            }
            pd!!.setMessage(getString(R.string.msg_please_wait))
            pd!!.show()
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    fun hideProgress() {
        try {
            if (pd != null) {
                pd!!.dismiss()
                pd = null
            }
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }
}