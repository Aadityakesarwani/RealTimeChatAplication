package com.innovativetools.firebase.chat.activities.adapters

import android.content.Context
import com.innovativetools.firebase.chat.activities.constants.IGroupListener
import androidx.recyclerview.widget.RecyclerView
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.innovativetools.firebase.chat.activities.views.smoothcb.SmoothCheckBox
import com.innovativetools.firebase.chat.activities.managers.Screens
import android.widget.TextView
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.Groups
import com.innovativetools.firebase.chat.activities.models.User
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.util.ArrayList

class GroupsUserAdapters(
    private val mContext: Context,
    usersList: ArrayList<User>,
    mSelectedUsers: ArrayList<User>,
    mSelectedMembersId: MutableList<String>,
    mDeletedMembersId: MutableSet<String>,
    isEditGroup: Boolean,
    groups: Groups,
    groupListener: IGroupListener
) : RecyclerView.Adapter<GroupsUserAdapters.ViewHolder>(), SectionedAdapter {
    private val mUsers: ArrayList<User>
    private val mSelectedUsers: ArrayList<User>
    private val mSelectedMembersId: MutableList<String>
    private val groupListener: IGroupListener
    private val isEditGroup: Boolean
    private val groups: Groups
    private val mDeletedMembersId: MutableSet<String>

    init {
        mUsers = Utils.removeDuplicates<String>(usersList)!!
        this.mSelectedUsers = mSelectedUsers
        this.mSelectedMembersId = mSelectedMembersId
        this.mDeletedMembersId = mDeletedMembersId
        this.groupListener = groupListener
        this.isEditGroup = isEditGroup
        this.groups = groups
        if (isEditGroup) {
            for (i in mUsers.indices) {
                if (groups.members!!.contains(mUsers[i].id)) {
                    this.mSelectedUsers.add(mUsers[i])
                    mUsers[i].id?.let { this.mSelectedMembersId.add(it) }
                }
            }
            groupListener.setSubTitle()
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.list_users_group, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val user = mUsers[position]
        val strAbout = user.about
        viewHolder.txtUsername.text = user.username
        user.getImageURL()?.let { Utils.setProfileImage(mContext, it, viewHolder.imageView) }
        viewHolder.txtLastMsg.visibility = View.VISIBLE
        if (Utils.isEmpty(strAbout)) {
            viewHolder.txtLastMsg.text = mContext.getString(R.string.strAboutStatus)
        } else {
            viewHolder.txtLastMsg.text = strAbout
        }
        if (user.isOnline == IConstants.STATUS_ONLINE) {
            viewHolder.imgOn.visibility = View.VISIBLE
            viewHolder.imgOff.visibility = View.GONE
        } else {
            viewHolder.imgOn.visibility = View.GONE
            viewHolder.imgOff.visibility = View.VISIBLE
        }
        viewHolder.cb.setOnCheckedChangeListener {
          checkBox: SmoothCheckBox?, isChecked: Boolean ->
            user.isChecked = isChecked
       }

        if (isEditGroup) {
            viewHolder.cb.isChecked = user.id?.let { groups.members!!.contains(it) } == true
        } else {
            viewHolder.cb.isChecked = user.isChecked
        }
        viewHolder.imageView.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                val screens = Screens(mContext)
                screens.openViewProfileActivity(user.id)
            }
        })
        viewHolder.itemView.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                user.isChecked = !user.isChecked
                viewHolder.cb.setChecked(user.isChecked, true)
                if (user.isChecked) {
                    mSelectedUsers.add(user)
                    user.id?.let { mSelectedMembersId.add(it) }
                    mDeletedMembersId.remove(user.id)
                } else {
                    mSelectedUsers.remove(user)
                    mSelectedMembersId.remove(user.id)
                    user.id?.let { mDeletedMembersId.add(it) }
                }
                groupListener.setSubTitle()
            }
        })
        viewHolder.cb.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                Utils.sout("Click on cb")
                user.isChecked = !user.isChecked
                viewHolder.cb.setChecked(user.isChecked, true)
                if (user.isChecked) {
                    mSelectedUsers.add(user)
                    user.id?.let { mSelectedMembersId.add(it) }
                    mDeletedMembersId.remove(user.id)
                } else {
                    mSelectedUsers.remove(user)
                    mSelectedMembersId.remove(user.id)
                    user.id?.let { mDeletedMembersId.add(it) }
                }
                groupListener.setSubTitle()
            }
        })
    }

    override fun getSectionName(position: Int): String {
        return if (!Utils.isEmpty(mUsers)) {
            mUsers[position].username!!.substring(IConstants.ZERO, IConstants.ONE)
        } else {
            "-"
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView
        val txtUsername: TextView
        val imgOn: ImageView
        val imgOff: ImageView
        val txtLastMsg: TextView
        val cb: SmoothCheckBox

        init {
            imageView = itemView.findViewById(R.id.imageView)
            txtUsername = itemView.findViewById(R.id.txtUsername)
            imgOn = itemView.findViewById(R.id.imgOn)
            imgOff = itemView.findViewById(R.id.imgOff)
            txtLastMsg = itemView.findViewById(R.id.txtLastMsg)
            cb = itemView.findViewById(R.id.scb)
        }
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }
}