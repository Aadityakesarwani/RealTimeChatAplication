package com.innovativetools.firebase.chat.activities.adapters

import androidx.recyclerview.widget.RecyclerView
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import com.google.firebase.auth.FirebaseUser
import com.innovativetools.firebase.chat.activities.managers.Screens
import com.google.firebase.auth.FirebaseAuth
import android.view.ViewGroup
import android.view.LayoutInflater
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.constants.IConstants
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.*
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.Chat
import com.innovativetools.firebase.chat.activities.models.User
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.lang.Exception
import java.util.ArrayList

class UserAdapters(private val mContext: Context, usersList: ArrayList<User>?, isChat: Boolean) :
    RecyclerView.Adapter<UserAdapters.ViewHolder>(), SectionedAdapter {
    private val mUsers: ArrayList<User>
    private val isChat: Boolean
    private val firebaseUser: FirebaseUser?
    private var theLastMsg: String? = null
    private var txtLastDate: String? = null
    private var isMsgSeen = false
    private var unReadCount = 0
    private val screens: Screens

    init {
        mUsers = Utils.removeDuplicates<String>(usersList)!!
        this.isChat = isChat
        firebaseUser = FirebaseAuth.getInstance().currentUser
        screens = Screens(mContext)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.list_users, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val user = mUsers[i]
        val strAbout = user.about
        viewHolder.txtUsername.text = user.username
        user.getImageURL()?.let { Utils.setProfileImage(mContext, it, viewHolder.imageView) }
        viewHolder.txtLastMsg.visibility = View.VISIBLE
        viewHolder.imgPhoto.visibility = View.GONE
        if (isChat) {
            viewHolder.txtUnreadCounter.visibility = View.INVISIBLE
            user.id?.let { lastMessage(it, viewHolder.txtLastMsg, viewHolder.txtLastDate, viewHolder.imgPhoto) }
            user.id?.let { lastMessageCount(it, viewHolder.txtUnreadCounter) }
            viewHolder.txtLastDate.visibility = View.VISIBLE
        } else {
            viewHolder.txtUnreadCounter.visibility = View.GONE
            viewHolder.txtLastDate.visibility = View.GONE
            if (Utils.isEmpty(strAbout)) {
                viewHolder.txtLastMsg.text = mContext.getString(R.string.strAboutStatus)
            } else {
                viewHolder.txtLastMsg.text = strAbout
            }
        }
        if (user.isOnline == IConstants.STATUS_ONLINE) {
            viewHolder.imgOn.visibility = View.VISIBLE
            viewHolder.imgOff.visibility = View.GONE
        } else {
            viewHolder.imgOn.visibility = View.GONE
            viewHolder.imgOff.visibility = View.VISIBLE
        }
        viewHolder.imageView.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                screens.openProfilePictureActivity(user)
            }
        })
        viewHolder.itemView.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                screens.openUserMessageActivity(user.id)
            }
        })
        viewHolder.itemView.setOnLongClickListener { v: View? ->
            if (isChat) {
                Utils.setVibrate(mContext)
                val receiverId = user.id
                val currentUser = firebaseUser!!.uid
                Utils.showYesNoDialog(
                    mContext as Activity,
                    R.string.strDelete,
                    R.string.strDeleteConversion
                ) {
                    val queryCurrent: Query =
                        FirebaseDatabase.getInstance().reference.child(IConstants.REF_CHATS)
                            .child(currentUser + IConstants.SLASH + receiverId)
                    queryCurrent.ref.removeValue()
                    val queryReceiver: Query =
                        FirebaseDatabase.getInstance().reference.child(IConstants.REF_CHATS)
                            .child(receiverId + IConstants.SLASH + currentUser)
                    queryReceiver.ref.removeValue()
                }
            }
            true
        }
    }

    private fun lastMessage(
        userId: String,
        lastMsg: TextView,
        lastDate: TextView,
        imgPath: ImageView
    ) {
        theLastMsg = "default"
        txtLastDate = "Now"
        try {
            val reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_CHATS).child(
                firebaseUser!!.uid + IConstants.SLASH + userId
            ).limitToLast(1)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (snapshot in dataSnapshot.children) {
                            val chat = snapshot.getValue(Chat::class.java)!!
                            try {
                                if (Utils.isEmpty(
                                        chat.attachmentType
                                    )
                                ) {
                                    if (!Utils.isEmpty(
                                            chat.message
                                        )
                                    ) {
                                        theLastMsg = chat.message
                                        txtLastDate = chat.datetime
                                    }
                                } else {
                                    imgPath.visibility = View.VISIBLE
                                    if (chat.attachmentType.equals(
                                            IConstants.TYPE_IMAGE,
                                            ignoreCase = true
                                        )
                                    ) {
                                        theLastMsg = mContext.getString(R.string.lblImage)
                                        txtLastDate = chat.datetime
                                        imgPath.setImageResource(R.drawable.ic_small_photo)
                                    } else if (chat.attachmentType.equals(
                                            IConstants.TYPE_AUDIO,
                                            ignoreCase = true
                                        )
                                    ) {
                                        theLastMsg = mContext.getString(R.string.lblAudio)
                                        txtLastDate = chat.datetime
                                        imgPath.setImageResource(R.drawable.ic_small_audio)
                                    } else if (chat.attachmentType.equals(
                                            IConstants.TYPE_VIDEO,
                                            ignoreCase = true
                                        )
                                    ) {
                                        theLastMsg = mContext.getString(R.string.lblVideo)
                                        txtLastDate = chat.datetime
                                        imgPath.setImageResource(R.drawable.ic_small_video)
                                    } else if (chat.attachmentType.equals(
                                            IConstants.TYPE_DOCUMENT,
                                            ignoreCase = true
                                        )
                                    ) {
                                        theLastMsg = mContext.getString(R.string.lblDocument)
                                        txtLastDate = chat.datetime
                                        imgPath.setImageResource(R.drawable.ic_small_document)
                                    } else if (chat.attachmentType.equals(
                                            IConstants.TYPE_CONTACT,
                                            ignoreCase = true
                                        )
                                    ) {
                                        theLastMsg = mContext.getString(R.string.lblContact)
                                        txtLastDate = chat.datetime
                                        imgPath.setImageResource(R.drawable.ic_small_contact)
                                    } else if (chat.attachmentType.equals(
                                            IConstants.TYPE_LOCATION,
                                            ignoreCase = true
                                        )
                                    ) {
                                        theLastMsg = mContext.getString(R.string.lblLocation)
                                        txtLastDate = chat.datetime
                                        imgPath.setImageResource(R.drawable.ic_small_location)
                                    } else if (chat.attachmentType.equals(
                                            IConstants.TYPE_RECORDING,
                                            ignoreCase = true
                                        )
                                    ) {
                                        theLastMsg = mContext.getString(R.string.lblVoiceRecording)
                                        txtLastDate = chat.datetime
                                        imgPath.setImageResource(R.drawable.ic_small_recording)
                                    } else {
                                        imgPath.visibility = View.GONE
                                        theLastMsg = chat.message
                                        txtLastDate = chat.datetime
                                    }
                                }
                            } catch (ignored: Exception) {
                            }
                        }
                        if ("default" == theLastMsg) {
                            lastMsg.setText(R.string.msgNoMessage)
                            lastDate.text = IConstants.EMPTY
                        } else {
                            lastMsg.text = theLastMsg
                            try {
                                lastDate.text = Utils.formatDateTime(
                                    mContext, txtLastDate
                                )
                            } catch (ignored: Exception) {
                            }
                        }
                        theLastMsg = "default"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } catch (ignored: Exception) {
        }
    }

    private fun lastMessageCount(userId: String, txtUnreadCounter: TextView) {
        isMsgSeen = false
        unReadCount = 0
        try {
            val reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_CHATS).child(
                firebaseUser!!.uid + IConstants.SLASH + userId
            ).orderByChild(IConstants.EXTRA_SEEN).equalTo(false)
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (snapshot in dataSnapshot.children) {
                            val chat = snapshot.getValue(Chat::class.java)!!
                            try {
                                if (!Utils.isEmpty(
                                        chat.message
                                    )
                                ) {
                                    if (chat.sender.equals(firebaseUser.uid, ignoreCase = true)) {
                                        isMsgSeen = true
                                    } else {
                                        isMsgSeen = chat.isMsgseen
                                        if (!isMsgSeen) {
                                            unReadCount++
                                        }
                                    }
                                }
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                    if (isMsgSeen || unReadCount == IConstants.ZERO) {
                        txtUnreadCounter.visibility = View.INVISIBLE
                    } else {
                        val readCount = if (unReadCount > 99) "99+" else unReadCount.toString()
                        txtUnreadCounter.visibility = View.VISIBLE
                        txtUnreadCounter.text = readCount
                    }
                    unReadCount = 0
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } catch (ignored: Exception) {
        }
    }

    override fun getSectionName(position: Int): String {
        return if (!Utils.isEmpty(mUsers)) {
            mUsers[position].username!!.substring(IConstants.ZERO, IConstants.ONE)
        } else {
            " "
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView
        val txtUsername: TextView
        val imgOn: ImageView
        val imgOff: ImageView
        val txtLastMsg: TextView
        val txtLastDate: TextView
        val txtUnreadCounter: TextView
        val imgPhoto: ImageView

        init {
            imageView = itemView.findViewById(R.id.imageView)
            txtUsername = itemView.findViewById(R.id.txtUsername)
            imgOn = itemView.findViewById(R.id.imgOn)
            imgOff = itemView.findViewById(R.id.imgOff)
            txtLastMsg = itemView.findViewById(R.id.txtLastMsg)
            txtLastDate = itemView.findViewById(R.id.txtLastDate)
            txtUnreadCounter = itemView.findViewById(R.id.txtUnreadCounter)
            imgPhoto = itemView.findViewById(R.id.imgPhoto)
        }
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }
}