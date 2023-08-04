package com.innovativetools.firebase.chat.activities.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.innovativetools.firebase.chat.activities.managers.Screens
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.innovativetools.firebase.chat.activities.R
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.User
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.util.ArrayList

class GroupsParticipantsAdapters(private val mContext: Context, usersList: ArrayList<User>?) :
    RecyclerView.Adapter<GroupsParticipantsAdapters.ViewHolder>() {
    private val mUsers: ArrayList<User>
    private val screens: Screens

    init {
        mUsers = Utils.removeDuplicates<String>(usersList)!!
        screens = Screens(mContext)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.list_group_participants, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val user = mUsers[i]
        val strAbout = user.about
        viewHolder.txtUsername.text = user.username
        if (user.username.equals(mContext.getString(R.string.strYou), ignoreCase = true)) {
            user.myImg?.let { Utils.setProfileImage(mContext, it, viewHolder.imageView) }
        } else {
            user.getImageURL()?.let { Utils.setProfileImage(mContext, it, viewHolder.imageView) }
        }
        viewHolder.txtLastMsg.visibility = View.VISIBLE
        if (Utils.isEmpty(strAbout)) {
            viewHolder.txtLastMsg.text = mContext.getString(R.string.strAboutStatus)
        } else {
            viewHolder.txtLastMsg.text = strAbout
        }
        viewHolder.txtAdmin.visibility = View.GONE
        if (user.isAdmin) {
            viewHolder.txtAdmin.visibility = View.VISIBLE
        }
        viewHolder.imageView.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                screens.openViewProfileActivity(user.id)
            }
        })
        viewHolder.itemView.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                if (!FirebaseAuth.getInstance().currentUser!!
                        .uid.equals(user.id, ignoreCase = true)
                ) {
                    screens.openUserMessageActivity(user.id)
                }
            }
        })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView
        val txtUsername: TextView
        val txtLastMsg: TextView
        val txtAdmin: TextView

        init {
            imageView = itemView.findViewById(R.id.imageView)
            txtUsername = itemView.findViewById(R.id.txtUsername)
            txtLastMsg = itemView.findViewById(R.id.txtLastMsg)
            txtAdmin = itemView.findViewById(R.id.txtAdmin)
        }
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }
}