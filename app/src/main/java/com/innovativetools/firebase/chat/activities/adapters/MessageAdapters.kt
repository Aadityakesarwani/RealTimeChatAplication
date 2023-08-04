package com.innovativetools.firebase.chat.activities.adapters

import com.innovativetools.firebase.chat.activities.models.Chat
import androidx.recyclerview.widget.RecyclerView
import com.innovativetools.firebase.chat.activities.views.audiowave.AudioPlayerView
import com.innovativetools.firebase.chat.activities.views.voiceplayer.RecordingPlayerView
import com.innovativetools.firebase.chat.activities.managers.Screens
import android.view.ViewGroup
import android.view.LayoutInflater
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.constants.IConstants
import android.widget.SeekBar.OnSeekBarChangeListener
import android.app.Activity
import android.content.Context
import com.innovativetools.firebase.chat.activities.models.LocationAddress
import com.google.gson.Gson
import android.content.Intent
import android.view.View
import android.widget.*
import com.innovativetools.firebase.chat.activities.models.DownloadFileEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.io.File
import java.lang.Exception
import java.util.*

class MessageAdapters(
    private val mContext: Context,
    private val mChats: ArrayList<Chat>,
    private val userName: String,
    private val strCurrentImage: String,
    private val imageUrl: String
) : RecyclerView.Adapter<MessageAdapters.ViewHolder>() {
    private val MSG_TYPE_RIGHT = 0
    private val MSG_TYPE_LEFT = 1
    private val myViewList: ArrayList<AudioPlayerView?>
    private val myRecList: ArrayList<RecordingPlayerView?>
    private var isAudioPlaying = false
    private var isRecordingPlaying = false
    private val screens: Screens

    init {
        myViewList = ArrayList()
        myRecList = ArrayList()
        screens = Screens(mContext)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == MSG_TYPE_RIGHT) {
            val view =
                LayoutInflater.from(mContext).inflate(R.layout.list_chat_right, viewGroup, false)
            ViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(mContext).inflate(R.layout.list_chat_left, viewGroup, false)
            ViewHolder(view)
        }
    }

    private fun showTextLayout(viewHolder: ViewHolder, chat: Chat) {
        try {
            viewHolder.txtShowMessage.visibility = View.VISIBLE
            viewHolder.txtShowMessage.text = chat.message
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    private fun setImageLayout(viewHolder: ViewHolder, chat: Chat) {
        try {
            viewHolder.imgPath.visibility = View.VISIBLE
            chat.imgPath?.let { Utils.setChatImage(mContext, it, viewHolder.imgPath) }
            viewHolder.imgPath.setOnClickListener(object : SingleClickListener() {
                override fun onClickView(v: View?) {
                    screens.openFullImageViewActivity(v, chat.imgPath, "")
                }
            })
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val chat = mChats[position]
        val attachType = chat.attachmentType
        try {
            viewHolder.txtShowMessage.visibility = View.GONE
            viewHolder.imgPath.visibility = View.GONE
            viewHolder.recordingLayout!!.visibility = View.GONE
            viewHolder.audioLayout!!.visibility = View.GONE
            viewHolder.documentLayout!!.visibility = View.GONE
            viewHolder.videoLayout!!.visibility = View.GONE
            viewHolder.contactLayout!!.visibility = View.GONE
            viewHolder.locationLayout!!.visibility = View.GONE
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
        if (Utils.isEmpty(attachType)) {
            if (Utils.isEmpty(chat.type)) { //This is for those who are already chat with older App and need to display proper.
                showTextLayout(viewHolder, chat)
            } else {
                if (chat.type.equals(IConstants.TYPE_IMAGE, ignoreCase = true)) {
                    setImageLayout(viewHolder, chat)
                } else {
                    showTextLayout(viewHolder, chat)
                }
            }
        } else {
            if (attachType.equals(IConstants.TYPE_TEXT, ignoreCase = true)) {
                showTextLayout(viewHolder, chat)
            } else if (attachType.equals(IConstants.TYPE_IMAGE, ignoreCase = true)) {
                setImageLayout(viewHolder, chat)
            } else if (attachType.equals(IConstants.TYPE_RECORDING, ignoreCase = true)) {
                try {
                    viewHolder.recordingLayout!!.visibility = View.VISIBLE
                    when (viewHolder.itemViewType) {
                        MSG_TYPE_LEFT -> {
                            val receiverPath = Utils.getReceiveDirectory(
                                mContext, attachType
                            ).toString() + IConstants.SLASH + chat.attachmentFileName
                            if (File(receiverPath).exists()) {
                                viewHolder.recordingPlayerView!!.setAudio(receiverPath)
                                if (chat.downloadProgress == IConstants.COMPLETED) {
                                    viewHolder.recordingPlayerView!!.hidePlayProgressAndPlay()
                                } else {
                                    viewHolder.recordingPlayerView!!.hidePlayProgressbar()
                                }
                            } else {
                                viewHolder.recordingPlayerView!!.showDownloadButton()
                                viewHolder.recordingPlayerView!!.imgDownload.setOnClickListener(
                                    object : SingleClickListener() {
                                        override fun onClickView(v: View?) {
                                            viewHolder.recordingPlayerView!!.showPlayProgressbar()
                                            viewHolder.broadcastDownloadEvent(chat)
                                        }
                                    })
                                viewHolder.recordingPlayerView!!.setAudio(null) //Default null value pass for file not found message
                                try {
                                    viewHolder.recordingPlayerView!!.txtProcess.text =
                                        Utils.getFileSize(chat.attachmentSize)
                                } catch (ignored: Exception) {
                                }
                            }
                        }
                        MSG_TYPE_RIGHT -> {
                            val path = Utils.getSentDirectory(
                                mContext, attachType
                            ).toString() + IConstants.SLASH + chat.attachmentFileName
                            if (File(path).exists()) {
                                viewHolder.recordingPlayerView!!.setAudio(path)
                            } else {
                                viewHolder.recordingPlayerView!!.setAudio(null) //Default null value pass for file not found message
                            }
                        }
                    }
                    viewHolder.recordingPlayerView!!.imgPlay.setOnClickListener(object :
                        SingleClickListener() {
                        override fun onClickView(v: View?) {
                            isRecordingPlaying = true
                            isAudioPlaying = false
                            playingTrack(viewHolder)
                        }
                    })
                    viewHolder.recordingPlayerView!!.imgPause.setOnClickListener(object :
                        SingleClickListener() {
                        override fun onClickView(v: View?) {
                            isRecordingPlaying = false
                            viewHolder.recordingPlayerView!!.imgPauseClickListener.onClick(v)
                        }
                    })
                    viewHolder.recordingPlayerView!!.seekBar.setOnSeekBarChangeListener(object :
                        OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (!Utils.isEmpty(
                                    viewHolder.recordingPlayerView!!.path
                                )
                            ) {
                                viewHolder.recordingPlayerView!!.seekBarListener.onProgressChanged(
                                    seekBar,
                                    progress,
                                    fromUser
                                )
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {
                            if (!Utils.isEmpty(
                                    viewHolder.recordingPlayerView!!.path
                                )
                            ) {
                                viewHolder.recordingPlayerView!!.seekBarListener.onStartTrackingTouch(
                                    seekBar
                                )
                            }
                            isRecordingPlaying = true
                            isAudioPlaying = false
                            playingTrack(viewHolder)
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar) {
                            if (!Utils.isEmpty(
                                    viewHolder.recordingPlayerView!!.path
                                )
                            ) {
                                viewHolder.recordingPlayerView!!.seekBarListener.onStopTrackingTouch(
                                    seekBar
                                )
                            } else {
                                viewHolder.recordingPlayerView!!.seekBar.progress = 0
                            }
                        }
                    })
                    when (viewHolder.itemViewType) {
                        MSG_TYPE_LEFT -> Utils.setProfileImage(
                            mContext, imageUrl, viewHolder.recordingPlayerView!!.voiceUserImage
                        )
                        MSG_TYPE_RIGHT -> Utils.setProfileImage(
                            mContext,
                            strCurrentImage,
                            viewHolder.recordingPlayerView!!.voiceUserImage
                        )
                    }
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
            } else if (attachType.equals(IConstants.TYPE_AUDIO, ignoreCase = true)) {
                try {
                    viewHolder.audioLayout!!.visibility = View.VISIBLE
                    when (viewHolder.itemViewType) {
                        MSG_TYPE_LEFT -> {
                            val receivePath = Utils.getReceiveDirectory(
                                mContext, attachType
                            ).toString() + IConstants.SLASH + chat.attachmentFileName
                            if (File(receivePath).exists()) {
                                viewHolder.audioPlayerView!!.setAudio(receivePath)
                                if (chat.downloadProgress == IConstants.COMPLETED) {
                                    viewHolder.audioPlayerView!!.hidePlayProgressAndPlay()
                                } else {
                                    viewHolder.audioPlayerView!!.hidePlayProgressbar()
                                }
                            } else {
                                viewHolder.audioPlayerView!!.showDownloadButton()
                                viewHolder.audioPlayerView!!.imgDownload?.setOnClickListener(object :
                                    SingleClickListener() {
                                    override fun onClickView(v: View?) {
                                        viewHolder.audioPlayerView!!.showPlayProgressbar()
                                        viewHolder.broadcastDownloadEvent(chat)
                                    }
                                })
                                viewHolder.audioPlayerView!!.setAudio(null) //Default null value pass for file not found message
                                try {
                                    viewHolder.audioPlayerView!!.txtProcess?.text =
                                        Utils.getFileSize(chat.attachmentSize)
                                } catch (ignored: Exception) {
                                }
                            }
                        }
                        MSG_TYPE_RIGHT -> {
                            val path = Utils.getSentDirectory(
                                mContext, attachType
                            ).toString() + IConstants.SLASH + chat.attachmentFileName
                            //                            Utils.sout("Audio Path right:: " + path + " >>>> " + new File(path).exists());
                            if (File(path).exists()) {
                                viewHolder.audioPlayerView!!.setAudio(path)
                            } else {
                                viewHolder.audioPlayerView!!.setAudio(null) //Default null value pass for file not found message
                            }
                        }
                    }
                    viewHolder.audioPlayerView!!.imgPlay?.setOnClickListener(object :
                        SingleClickListener() {
                        override fun onClickView(v: View?) {
                            isAudioPlaying = true
                            isRecordingPlaying = false
                            playingTrack(viewHolder)
                        }
                    })
                    viewHolder.audioPlayerView!!.imgPause?.setOnClickListener(object :
                        SingleClickListener() {
                        override fun onClickView(v: View?) {
                            isAudioPlaying = false
                            viewHolder.audioPlayerView!!.imgPauseClickListener.onClick(v)
                        }
                    })
                    viewHolder.audioPlayerView!!.seekBar?.setOnSeekBarChangeListener(object :
                        OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (!Utils.isEmpty(
                                    viewHolder.audioPlayerView!!.path
                                )
                            ) {
                                viewHolder.audioPlayerView!!.seekBarListener.onProgressChanged(
                                    seekBar,
                                    progress,
                                    fromUser
                                )
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {
                            if (!Utils.isEmpty(
                                    viewHolder.audioPlayerView!!.path
                                )
                            ) {
                                viewHolder.audioPlayerView!!.seekBarListener.onStartTrackingTouch(
                                    seekBar
                                )
                            }
                            isAudioPlaying = true
                            isRecordingPlaying = false
                            playingTrack(viewHolder)
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar) {
                            if (!Utils.isEmpty(
                                    viewHolder.audioPlayerView!!.path
                                )
                            ) {
                                viewHolder.audioPlayerView!!.seekBarListener.onStopTrackingTouch(
                                    seekBar
                                )
                            } else {
                                viewHolder.audioPlayerView!!.seekBar?.progress = 0
                            }
                        }
                    })
                    viewHolder.audioPlayerView!!.setFileName(chat.attachmentName)
                } catch (ignored: Exception) {
                }
            } else if (attachType.equals(IConstants.TYPE_DOCUMENT, ignoreCase = true)) {
                try {
                    viewHolder.documentLayout!!.visibility = View.VISIBLE
                    viewHolder.imgFileDownload!!.visibility = View.GONE
                    viewHolder.fileProgressBar!!.visibility = View.GONE
                    viewHolder.imgFileIcon!!.visibility = View.GONE
                    when (viewHolder.itemViewType) {
                        MSG_TYPE_LEFT -> {
                            val receivePath = Utils.getReceiveDirectory(
                                mContext, attachType
                            ).toString() + IConstants.SLASH + chat.attachmentFileName
                            if (File(receivePath).exists()) {
                                viewHolder.imgFileIcon!!.visibility = View.VISIBLE
                                viewHolder.imgFileDownload!!.visibility = View.GONE
                                viewHolder.fileProgressBar!!.visibility = View.GONE
                            } else {
                                viewHolder.imgFileIcon!!.visibility = View.GONE
                                viewHolder.imgFileDownload!!.visibility = View.VISIBLE
                                viewHolder.fileProgressBar!!.visibility = View.GONE
                                viewHolder.imgFileDownload!!.setOnClickListener(object :
                                    SingleClickListener() {
                                    override fun onClickView(v: View?) {
                                        viewHolder.imgFileDownload!!.visibility = View.GONE
                                        viewHolder.fileProgressBar!!.visibility = View.VISIBLE
                                        viewHolder.broadcastDownloadEvent(chat)
                                    }
                                })
                            }
                        }
                        MSG_TYPE_RIGHT -> viewHolder.imgFileIcon!!.visibility = View.VISIBLE
                    }
                    viewHolder.documentLayout!!.setOnClickListener(object : SingleClickListener() {
                        override fun onClickView(v: View?) {
                            when (viewHolder.itemViewType) {
                                MSG_TYPE_LEFT -> try {
                                    val receivePath = Utils.getReceiveDirectory(
                                        mContext, attachType
                                    ).toString() + IConstants.SLASH + chat.attachmentFileName
                                    if (File(receivePath).exists()) {
                                        mContext.startActivity(
                                            Utils.getOpenFileIntent(
                                                mContext, receivePath
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    Utils.getErrors(e)
                                }
                                MSG_TYPE_RIGHT -> try {
                                    val path = Utils.getSentDirectory(
                                        mContext, attachType
                                    ).toString() + IConstants.SLASH + chat.attachmentFileName
                                    mContext.startActivity(
                                        Utils.getOpenFileIntent(
                                            mContext, path
                                        )
                                    )
                                } catch (e: Exception) {
                                    Utils.getErrors(e)
                                    screens.showToast(R.string.msgFileNotFound)
                                }
                            }
                        }
                    })
                    viewHolder.txtFileName!!.text = chat.attachmentName
                    viewHolder.txtFileExt!!.text =
                        chat.attachmentFileName?.let {
                            Utils.getFileExtensionFromPath(it).uppercase(
                                Locale.getDefault()
                            )
                        }
                    viewHolder.txtFileSize!!.text = Utils.getFileSize(chat.attachmentSize)
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
            } else if (attachType.equals(IConstants.TYPE_VIDEO, ignoreCase = true)) {
                try {
                    viewHolder.videoLayout!!.visibility = View.VISIBLE
                    viewHolder.imgVideoPlay!!.visibility = View.GONE
                    viewHolder.videoProgressBar!!.visibility = View.GONE
                    when (viewHolder.itemViewType) {
                        MSG_TYPE_LEFT -> try {
                            val receivePath = File(
                                Utils.getReceiveDirectory(
                                    mContext, attachType
                                ).toString() + IConstants.SLASH + chat.attachmentFileName
                            )
                            if (receivePath.exists()) {
                                viewHolder.imgVideoPlay!!.visibility = View.VISIBLE
                                viewHolder.imgVideoDownload!!.visibility = View.GONE
                                viewHolder.videoProgressBar!!.visibility = View.GONE
                            } else {
                                viewHolder.imgVideoPlay!!.visibility = View.GONE
                                viewHolder.imgVideoDownload!!.visibility = View.VISIBLE
                                viewHolder.videoProgressBar!!.visibility = View.GONE
                                viewHolder.imgVideoDownload!!.setOnClickListener(object :
                                    SingleClickListener() {
                                    override fun onClickView(v: View?) {
                                        viewHolder.imgVideoDownload!!.visibility = View.GONE
                                        viewHolder.videoProgressBar!!.visibility = View.VISIBLE
                                        viewHolder.broadcastDownloadEvent(chat)
                                    }
                                })
                            }
                        } catch (ignored: Exception) {
                        }
                        MSG_TYPE_RIGHT -> try {
                            viewHolder.imgVideoPlay!!.visibility = View.VISIBLE
                            viewHolder.imgVideoDownload!!.visibility = View.GONE
                            viewHolder.videoProgressBar!!.visibility = View.GONE
                        } catch (ignored: Exception) {
                        }
                    }
                    viewHolder.imgVideoPlay!!.setOnClickListener(object : SingleClickListener() {
                        override fun onClickView(v: View?) {
                            when (viewHolder.itemViewType) {
                                MSG_TYPE_LEFT -> try {
                                    val receivePath = Utils.getReceiveDirectory(
                                        mContext, attachType
                                    ).toString() + IConstants.SLASH + chat.attachmentFileName
                                    if (File(receivePath).exists()) {
                                        Utils.openPlayingVideo(
                                            mContext, File(receivePath)
                                        )
                                    }
                                } catch (e: Exception) {
                                    Utils.getErrors(e)
                                }
                                MSG_TYPE_RIGHT -> try {
                                    val path = File(
                                        Utils.getSentDirectory(
                                            mContext, attachType
                                        ).toString() + IConstants.SLASH + chat.attachmentFileName
                                    )
                                    Utils.openPlayingVideo(
                                        mContext, path
                                    )
                                } catch (e: Exception) {
                                    Utils.getErrors(e)
                                }
                            }
                        }
                    })
                    viewHolder.txtVideoDuration!!.text = chat.attachmentDuration
                    viewHolder.txtVideoSize!!.text = Utils.getFileSize(chat.attachmentSize)
                    chat.attachmentData?.let {
                        Utils.setChatImage(
                            mContext, it, viewHolder.videoThumbnail
                        )
                    }
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
            } else if (attachType.equals(IConstants.TYPE_CONTACT, ignoreCase = true)) {
                try {
                    viewHolder.contactLayout!!.visibility = View.VISIBLE
                    viewHolder.txtContactName!!.text = chat.attachmentFileName
                    viewHolder.btnMessageContact!!.setOnClickListener(object :
                        SingleClickListener() {
                        override fun onClickView(v: View?) {
                            Utils.shareApp(
                                mContext as Activity
                            )
                        }
                    })
                    viewHolder.contactLayout!!.setOnClickListener(object : SingleClickListener() {
                        override fun onClickView(v: View?) {
                            chat.attachmentDuration?.let {
                                Utils.openCallIntent(
                                    mContext, it
                                )
                            }
                        }
                    })
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
            } else if (attachType.equals(IConstants.TYPE_LOCATION, ignoreCase = true)) {
                try {
                    viewHolder.locationLayout!!.visibility = View.VISIBLE
                    val locationAddress =
                        Gson().fromJson(chat.attachmentData, LocationAddress::class.java)
                    var topLeft = 0
                    var topRight = 0
                    when (viewHolder.itemViewType) {
                        MSG_TYPE_LEFT -> topRight = 16
                        MSG_TYPE_RIGHT -> topLeft = 16
                    }
                    Utils.showStaticMap(
                        mContext, locationAddress, topLeft, topRight, viewHolder.imgLocation
                    )
                    if (Utils.isEmpty(locationAddress.name)) {
                        viewHolder.txtLocationName!!.visibility = View.GONE
                    } else {
                        viewHolder.txtLocationName!!.visibility = View.VISIBLE
                        viewHolder.txtLocationName!!.text = locationAddress.name
                    }
                    viewHolder.txtAddress!!.text = locationAddress.address
                    viewHolder.locationLayout!!.setOnClickListener(object : SingleClickListener() {
                        override fun onClickView(v: View?) {
                            Utils.openMapWithAddress(
                                mContext, locationAddress
                            )
                        }
                    })
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
            } else {
                showTextLayout(viewHolder, chat)
            }
        }
        viewHolder.txtOnlyDate.visibility = View.GONE
        try {
            val first = Utils.dateToMillis(
                mChats[position - 1].datetime
            )
            val second = Utils.dateToMillis(chat.datetime)
            if (!Utils.hasSameDate(first, second)) {
                viewHolder.txtOnlyDate.visibility = View.VISIBLE
                viewHolder.txtOnlyDate.text = Utils.formatFullDate(chat.datetime)
            }
        } catch (e: Exception) {
            if (position == 0) {
                viewHolder.txtOnlyDate.visibility = View.VISIBLE
                viewHolder.txtOnlyDate.text = Utils.formatFullDate(chat.datetime)
            }
        }
        when (viewHolder.itemViewType) {
            MSG_TYPE_LEFT -> {
                viewHolder.txtName.text = userName
                viewHolder.imgMsgSeen.visibility = View.GONE
            }
            MSG_TYPE_RIGHT -> if (position == mChats.size - 1) {
                viewHolder.imgMsgSeen.visibility = View.VISIBLE
                if (chat.isMsgseen) {
                    viewHolder.imgMsgSeen.setImageResource(R.drawable.ic_check_read)
                } else {
                    viewHolder.imgMsgSeen.setImageResource(R.drawable.ic_check_delivery)
                }
            } else {
                viewHolder.imgMsgSeen.visibility = View.GONE
            }
        }
        var timeMilliSeconds: Long = 0
        try {
            timeMilliSeconds = Utils.dateToMillis(chat.datetime)
        } catch (ignored: Exception) {
        }
        if (timeMilliSeconds > 0) {
            viewHolder.txtMsgTime.text = Utils.formatLocalTime(timeMilliSeconds)
        } else {
            viewHolder.txtMsgTime.visibility = View.GONE
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView
        val txtShowMessage: TextView
        val txtMsgTime: TextView
        val imgMsgSeen: ImageView
        val txtOnlyDate: TextView
        val imgPath: ImageView

        //New Component
        var recordingLayout: RelativeLayout? = null
        var audioLayout: RelativeLayout? = null
        var documentLayout: RelativeLayout? = null
        var videoLayout: RelativeLayout? = null
        var contactLayout: RelativeLayout? = null
        var locationLayout: RelativeLayout? = null
        var recordingPlayerView: RecordingPlayerView? = null
        var audioPlayerView: AudioPlayerView? = null
        var txtFileName: TextView? = null
        var txtFileSize: TextView? = null
        var txtFileExt: TextView? = null
        var txtVideoDuration: TextView? = null
        var txtVideoSize: TextView? = null
        var txtContactName: TextView? = null
        var txtLocationName: TextView? = null
        var txtAddress: TextView? = null
        var imgFileIcon: ImageView? = null
        var imgFileDownload: ImageView? = null
        var videoThumbnail: ImageView? = null
        var imgVideoPlay: ImageView? = null
        var imgVideoDownload: ImageView? = null
        var imgUserContact: ImageView? = null
        var imgLocation: ImageView? = null
        var fileProgressBar: ProgressBar? = null
        var videoProgressBar: ProgressBar? = null
        var btnMessageContact: Button? = null

        init {
            txtOnlyDate = itemView.findViewById(R.id.txtOnlyDate)
            txtShowMessage = itemView.findViewById(R.id.txtShowMessage)
            txtName = itemView.findViewById(R.id.txtName)
            imgMsgSeen = itemView.findViewById(R.id.imgMsgSeen)
            txtMsgTime = itemView.findViewById(R.id.txtMsgTime)
            imgPath = itemView.findViewById(R.id.imgPath)
            try {
                recordingLayout = itemView.findViewById(R.id.recordingLayout)
                recordingPlayerView = itemView.findViewById(R.id.voicePlayerView)
                audioLayout = itemView.findViewById(R.id.audioLayout)
                audioPlayerView = itemView.findViewById(R.id.wave_audioPlayerView)
                documentLayout = itemView.findViewById(R.id.documentLayout)
                txtFileName = itemView.findViewById(R.id.txtFileName)
                txtFileSize = itemView.findViewById(R.id.txtFileSize)
                txtFileExt = itemView.findViewById(R.id.txtFileExt)
                imgFileIcon = itemView.findViewById(R.id.imgFileIcon)
                imgFileDownload = itemView.findViewById(R.id.imgFileDownload)
                fileProgressBar = itemView.findViewById(R.id.fileProgressBar)
                videoLayout = itemView.findViewById(R.id.videoLayout)
                videoThumbnail = itemView.findViewById(R.id.videoThumbnail)
                imgVideoPlay = itemView.findViewById(R.id.imgVideoPlay)
                imgVideoDownload = itemView.findViewById(R.id.imgVideoDownload)
                videoProgressBar = itemView.findViewById(R.id.videoProgressBar)
                txtVideoDuration = itemView.findViewById(R.id.txtVideoDuration)
                txtVideoSize = itemView.findViewById(R.id.txtVideoSize)
                contactLayout = itemView.findViewById(R.id.contactLayout)
                imgUserContact = itemView.findViewById(R.id.imgUserContact)
                txtContactName = itemView.findViewById(R.id.txtContactName)
                btnMessageContact = itemView.findViewById(R.id.btnMessageContact)
                locationLayout = itemView.findViewById(R.id.locationLayout)
                imgLocation = itemView.findViewById(R.id.imgLocation)
                txtLocationName = itemView.findViewById(R.id.txtLocationName)
                txtAddress = itemView.findViewById(R.id.txtAddress)
            } catch (e: Exception) {
                Utils.getErrors(e)
            }
        }

        fun broadcastDownloadEvent(chat: Chat) {
            val intent = Intent(IConstants.BROADCAST_DOWNLOAD_EVENT)
            intent.putExtra(
                IConstants.DOWNLOAD_DATA,
                DownloadFileEvent(chat, absoluteAdapterPosition)
            )
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
        }
    }

    fun playingTrack(viewHolder: ViewHolder) {
        try {
            if (isAudioPlaying) {
                if (Utils.isEmpty(viewHolder.audioPlayerView!!.path)) {
                    viewHolder.audioPlayerView!!.imgPlayNoFileClickListener.onClick(viewHolder.audioPlayerView)
                } else {
                    if (myViewList.isEmpty()) {
                        myViewList.add(viewHolder.audioPlayerView)
                        viewHolder.audioPlayerView!!.imgPlayClickListener.onClick(viewHolder.audioPlayerView)
                    } else { //call when one of audio already playing, so first pause that and playing new audio track
                        val oldPlayerView = myViewList[IConstants.ZERO]
                        oldPlayerView!!.imgPauseClickListener.onClick(viewHolder.audioPlayerView)
                        myViewList.removeAt(IConstants.ZERO)
                        viewHolder.audioPlayerView!!.imgPlay?.callOnClick()
                    }
                }
            } else { //Call when Audio already playing and trying to play Recording track, so pause audio track first
                if (!myViewList.isEmpty()) {
                    val oldPlayerView = myViewList[IConstants.ZERO]
                    oldPlayerView!!.imgPauseClickListener.onClick(viewHolder.audioPlayerView)
                    myViewList.removeAt(IConstants.ZERO)
                }
            }
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
        try {
            if (isRecordingPlaying) {
                if (Utils.isEmpty(viewHolder.recordingPlayerView!!.path)) {
                    viewHolder.recordingPlayerView!!.imgPlayNoFileClickListener.onClick(viewHolder.recordingPlayerView)
                } else {
                    if (myRecList.isEmpty()) {
                        myRecList.add(viewHolder.recordingPlayerView)
                        viewHolder.recordingPlayerView!!.imgPlayClickListener.onClick(viewHolder.recordingPlayerView)
                    } else { //call when one of recording already playing, so first pause and playing new recording track
                        val oldPlayerView = myRecList[IConstants.ZERO]
                        oldPlayerView!!.imgPauseClickListener.onClick(viewHolder.recordingPlayerView)
                        myRecList.removeAt(IConstants.ZERO)
                        viewHolder.recordingPlayerView!!.imgPlay.callOnClick()
                    }
                }
            } else { //Call when Recording already playing and trying to play Audio track, so pause recording track first
                if (!myRecList.isEmpty()) {
                    val oldPlayerView = myRecList[IConstants.ZERO]
                    oldPlayerView!!.imgPauseClickListener.onClick(viewHolder.recordingPlayerView)
                    myRecList.removeAt(IConstants.ZERO)
                }
            }
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    fun stopAudioFile() {
        if (!Utils.isEmpty(myViewList)) {
            try {
                for (i in myViewList.indices) {
                    val audioPlayerView = myViewList[IConstants.ZERO]
                    audioPlayerView!!.imgPause?.callOnClick()
                }
                myViewList.clear()
            } catch (e: Exception) {
                Utils.getErrors(e)
            }
        }
        if (!Utils.isEmpty(myRecList)) {
            try {
                for (i in myRecList.indices) {
                    val recordingPlayerView = myRecList[IConstants.ZERO]
                    recordingPlayerView!!.imgPause.callOnClick()
                }
                myRecList.clear()
            } catch (e: Exception) {
                Utils.getErrors(e)
            }
        }
    }

    override fun getItemCount(): Int {
        return mChats.size
    }

    override fun getItemViewType(position: Int): Int {
        val chat = mChats[position]
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!
        return if (chat.sender.equals(firebaseUser.uid, ignoreCase = true)) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }
}