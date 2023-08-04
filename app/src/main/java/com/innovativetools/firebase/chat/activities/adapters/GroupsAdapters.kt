package com.innovativetools.firebase.chat.activities.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.innovativetools.firebase.chat.activities.managers.Screens
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.constants.IConstants
import android.widget.TextView
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.Groups
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.lang.Exception
import java.util.ArrayList

class GroupsAdapters(private val mContext: Context, private val mGroups: ArrayList<Groups>) :
    RecyclerView.Adapter<GroupsAdapters.ViewHolder>() {
    private val screens: Screens

    init {
        screens = Screens(mContext)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.list_groups, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val groups = mGroups[i]
        viewHolder.txtGroupName.text = groups.groupName
        try {
            groups.groupImg?.let { Utils.setGroupImage(mContext, it, viewHolder.imageView) }
        } catch (ignored: Exception) {
        }
        try {
            viewHolder.txtLastMsg.visibility = View.VISIBLE
            viewHolder.txtLastDate.visibility = View.VISIBLE
            viewHolder.imgPhoto.visibility = View.GONE
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
        try {
            if (Utils.isEmpty(groups.type)) {
                if (Utils.isEmpty(groups.lastMsg)) {
                    viewHolder.txtLastMsg.text = mContext.getString(R.string.msgTapToStartChat)
                } else {
                    viewHolder.txtLastMsg.text = groups.lastMsg
                }
            } else {
                viewHolder.imgPhoto.visibility = View.VISIBLE
                if (groups.type.equals(IConstants.TYPE_IMAGE, ignoreCase = true)) {
                    setImageAndText(viewHolder, R.string.lblPhoto, R.drawable.ic_small_photo)
                } else if (groups.type.equals(IConstants.TYPE_RECORDING, ignoreCase = true)) {
                    setImageAndText(
                        viewHolder,
                        R.string.lblVoiceRecording,
                        R.drawable.ic_small_recording
                    )
                } else if (groups.type.equals(IConstants.TYPE_AUDIO, ignoreCase = true)) {
                    setImageAndText(viewHolder, R.string.lblAudio, R.drawable.ic_small_audio)
                } else if (groups.type.equals(IConstants.TYPE_VIDEO, ignoreCase = true)) {
                    setImageAndText(viewHolder, R.string.lblVideo, R.drawable.ic_small_video)
                } else if (groups.type.equals(IConstants.TYPE_DOCUMENT, ignoreCase = true)) {
                    setImageAndText(viewHolder, R.string.lblDocument, R.drawable.ic_small_document)
                } else if (groups.type.equals(IConstants.TYPE_CONTACT, ignoreCase = true)) {
                    setImageAndText(viewHolder, R.string.lblContact, R.drawable.ic_small_contact)
                } else if (groups.type.equals(IConstants.TYPE_LOCATION, ignoreCase = true)) {
                    setImageAndText(viewHolder, R.string.lblLocation, R.drawable.ic_small_location)
                } else {
                    viewHolder.imgPhoto.visibility = View.GONE
                    if (Utils.isEmpty(groups.lastMsg)) {
                        viewHolder.txtLastMsg.text = mContext.getString(R.string.msgTapToStartChat)
                    } else {
                        viewHolder.txtLastMsg.text = groups.lastMsg
                    }
                }
            }
            if (Utils.isEmpty(groups.lastMsgTime)) {
                viewHolder.txtLastDate.text = ""
            } else {
                viewHolder.txtLastDate.text = Utils.formatDateTime(
                    mContext, groups.lastMsgTime
                )
            }
        } catch (ignored: Exception) {
        }
        viewHolder.imageView.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                //screens.openGroupParticipantActivity(groups);
                screens.openProfilePictureActivity(groups)
            }
        })
        viewHolder.itemView.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                screens.openGroupMessageActivity(groups)
            }
        })
    }

    private fun setImageAndText(viewHolder: ViewHolder, msg: Int, photo: Int) {
        viewHolder.txtLastMsg.text = mContext.getString(msg)
        viewHolder.imgPhoto.setImageResource(photo)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView
        val txtGroupName: TextView
        val txtLastMsg: TextView
        val txtLastDate: TextView
        val imgPhoto: ImageView

        init {
            imageView = itemView.findViewById(R.id.imageView)
            txtGroupName = itemView.findViewById(R.id.txtGroupName)
            txtLastMsg = itemView.findViewById(R.id.txtLastMsg)
            txtLastDate = itemView.findViewById(R.id.txtLastDate)
            imgPhoto = itemView.findViewById(R.id.imgPhoto)
        }
    }

    override fun getItemCount(): Int {
        return mGroups.size
    }
}