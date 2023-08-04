package com.innovativetools.firebase.chat.activities

import com.innovativetools.firebase.chat.activities.fcm.RetroClient.getClient

import com.innovativetools.firebase.chat.activities.views.files.PickerManagerCallbacks
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.innovativetools.firebase.chat.activities.adapters.MessageAdapters
import com.innovativetools.firebase.chat.activities.fcm.APIService
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vanniktech.emoji.EmojiPopup
import androidx.cardview.widget.CardView
import com.vanniktech.emoji.EmojiEditText
import com.innovativetools.firebase.chat.activities.views.files.PickerManager
import android.media.MediaRecorder
import com.devlomi.record_view.RecordView
import com.devlomi.record_view.RecordButton
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.fcm.RetroClient
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.google.firebase.auth.FirebaseAuth
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener
import android.view.View.OnTouchListener
import com.devlomi.record_view.OnRecordClickListener
import com.devlomi.record_view.OnRecordListener
import android.text.Editable
import com.devlomi.record_view.RecordPermissionHandler
import com.devlomi.record_view.OnBasketAnimationEnd
import android.text.TextUtils
import android.provider.MediaStore
import com.wafflecopter.multicontactpicker.MultiContactPicker
import com.wafflecopter.multicontactpicker.LimitColumn
import com.google.android.gms.common.GoogleApiAvailability
import android.annotation.SuppressLint
import android.app.Activity
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.google.android.libraries.places.api.model.Place
import com.rtchagas.pingplacepicker.PingPlacePicker
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.innovativetools.firebase.chat.activities.constants.IDialogListener
import com.innovativetools.firebase.chat.activities.fcmmodels.Sender
import com.innovativetools.firebase.chat.activities.fcmmodels.MyResponse
import android.text.TextWatcher
import android.app.ProgressDialog
import com.innovativetools.firebase.chat.activities.async.BaseTask
import android.media.ThumbnailUtils
import android.graphics.Bitmap
import com.google.firebase.storage.UploadTask
import com.innovativetools.firebase.chat.activities.models.AttachmentTypes.AttachmentType
import com.innovativetools.firebase.chat.activities.managers.FirebaseUploader
import com.innovativetools.firebase.chat.activities.managers.FirebaseUploader.UploadListener
import com.wafflecopter.multicontactpicker.ContactResult
import android.provider.ContactsContract
import android.content.res.AssetFileDescriptor
import android.content.pm.PackageManager
import android.app.DownloadManager
import android.content.*
import com.innovativetools.firebase.chat.activities.managers.DownloadUtil
import com.innovativetools.firebase.chat.activities.views.files.MediaFile.MediaFileType
import com.innovativetools.firebase.chat.activities.views.files.MediaFile
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.*
import com.google.android.gms.tasks.Continuation
import com.google.firebase.database.*
import com.innovativetools.firebase.chat.activities.async.CustomCallable
import com.innovativetools.firebase.chat.activities.async.TaskRunner
import com.innovativetools.firebase.chat.activities.databinding.ActivityMessageBinding
import com.innovativetools.firebase.chat.activities.fcmmodels.Data
import com.innovativetools.firebase.chat.activities.fcmmodels.Token
import com.innovativetools.firebase.chat.activities.managers.SessionManager
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.*
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import com.innovativetools.firebase.chat.activities.views.files.FileUtils
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*

class MessageActivity : BaseActivity(), View.OnClickListener, PickerManagerCallbacks {
    private var mImageView: CircleImageView? = null
    private var mTxtUsername: TextView? = null
    private var txtTyping: TextView? = null
    private var layoutManager: LinearLayoutManager? = null

    private var mRecyclerView: RecyclerView? = null
    private var currentId: String? = null
    private var userId: String? = null
    private var userName = "Sender"
    private var strSender: String? = null
    private var strReceiver: String? = null

    private var mToolbar: Toolbar? = null
    private var chats: ArrayList<Chat>? = null
    private var messageAdapters: MessageAdapters? = null
    private var seenListenerSender: ValueEventListener? = null
    private var seenReferenceSender: Query? = null
    private var apiService: APIService? = null
    var notify = false
    private var onlineStatus: String? = null
    private var strUsername: String? = null
    private var strCurrentImage: String? = null

    private var imgAvatar: ImageView? = null;
    private var imageUri: Uri? = null
    private var uploadTask: StorageTask<*>? = null
    private var storage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var storageAttachment: StorageReference? = null

    //New Component
    private var btnGoToBottom: LinearLayout? = null
    private var emojiIcon: EmojiPopup? = null
    private var mainAttachmentLayout: CardView? = null
    private var attachmentBGView: View? = null
    private var newMessage: EmojiEditText? = null
    private var imgAddAttachment: ImageView? = null
    private var imgAttachmentEmoji: ImageView? = null
    private var imgCamera: ImageView? = null
    private var rootView: RelativeLayout? = null

    //Picker
    private var pickerManager: PickerManager? = null

    //Recording
    private var recordWaitHandler: Handler? = null
    private var recordTimerHandler: Handler? = null
    private var recordRunnable: Runnable? = null
    private var recordTimerRunnable: Runnable? = null
    private var mRecorder: MediaRecorder? = null
    private var recordFilePath: String? = null
    private var recordView: RecordView? = null
    private var recordButton: RecordButton? = null
    private var isStart = false
    private var firstVisible = -1
    private var rlChatView: RelativeLayout? = null
    private var vCardData: String? = null
    private var displayName: String? = null
    private var phoneNumber: String? = null
    private var fileUri: File? = null
    private var imgUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        try {
            mActivity = this
            apiService = getClient(IConstants.FCM_URL)!!.create(APIService::class.java)
            initUI()
            txtTyping?.text = IConstants.EMPTY
            try {
                setSupportActionBar(mToolbar)
                Objects.requireNonNull(supportActionBar)?.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.title = IConstants.EMPTY
            } catch (ignored: Exception) {
            }
            mToolbar?.setNavigationOnClickListener(object : SingleClickListener() {
                override fun onClickView(v: View?) {
                    onBackPressed()
                }
            })
            firebaseUser = FirebaseAuth.getInstance().currentUser
            assert(firebaseUser != null)
            currentId = firebaseUser!!.uid
            reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(
                currentId!!
            )
            reference!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        val user = dataSnapshot.getValue(
                            User::class.java
                        )!!
                        strUsername = user.username
                        strCurrentImage = user.getImageURL()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
            val intent = intent
            userId = intent.getStringExtra(IConstants.EXTRA_USER_ID)
            strSender = currentId + IConstants.SLASH + userId
            strReceiver = userId + IConstants.SLASH + currentId
            storage = FirebaseStorage.getInstance()
            storageReference =
                storage!!.getReference(IConstants.REF_CHAT_PHOTO_UPLOAD + IConstants.SLASH + strSender)
            storageAttachment =
                storage!!.getReference(IConstants.REF_CHAT_ATTACHMENT + IConstants.SLASH + strSender)
            mRecyclerView?.setHasFixedSize(true)
            layoutManager = LinearLayoutManager(mActivity)
            layoutManager!!.stackFromEnd = true
            mRecyclerView?.layoutManager = layoutManager
            btnGoToBottom?.visibility = View.GONE
            mRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    firstVisible = try {
                        if (firstVisible == -1) layoutManager!!.findFirstCompletelyVisibleItemPosition() else if (messageAdapters!!.itemCount >= IConstants.TWO) messageAdapters!!.itemCount - IConstants.TWO else IConstants.ZERO
                    } catch (e: Exception) {
                        IConstants.ZERO
                    }
                    if (layoutManager!!.findLastVisibleItemPosition() < firstVisible) {
                        btnGoToBottom?.visibility = View.VISIBLE
                    } else {
                        btnGoToBottom?.visibility = View.GONE
                    }
                }
            })
            btnGoToBottom?.setOnClickListener(object : SingleClickListener() {
                override fun onClickView(v: View?) {
                    try {
                        if (firstVisible != -1) {
                            mRecyclerView?.smoothScrollToPosition(messageAdapters!!.itemCount - IConstants.ONE)
                        }
                        btnGoToBottom?.visibility = View.GONE
                    } catch (ignored: Exception) {
                    }
                }
            })
            rlChatView!!.visibility = View.VISIBLE
            recordButton!!.visibility = View.VISIBLE
            reference =
                FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(userId!!)
            reference!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        val user = dataSnapshot.getValue(
                            User::class.java
                        )!!
                        mTxtUsername?.text = user.username
                        userName = user.username.toString()
                        onlineStatus = Utils.showOnlineOffline(mActivity!!, user.isOnline)
                        txtTyping?.text = onlineStatus
                        user.getImageURL()?.let {
                            Utils.setProfileImage(
                                applicationContext, it, mImageView!!
                            )
                        }
                        user.getImageURL()?.let { readMessages(it) }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
            val viewProfile = findViewById<LinearLayout>(R.id.viewProfile)
            viewProfile.setOnClickListener(object : SingleClickListener() {
                override fun onClickView(v: View?) {
                    screens!!.openViewProfileActivity(userId)
                }
            })
            emojiIcon = EmojiPopup.Builder.fromRootView(rootView!!).setOnEmojiPopupShownListener {
                hideAttachmentView()
                imgAttachmentEmoji?.setImageResource(R.drawable.ic_keyboard_24dp)
            }
                .setOnEmojiPopupDismissListener { imgAttachmentEmoji?.setImageResource(R.drawable.ic_insert_emoticon_gray) }
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style).build(
                    newMessage!!
                )
            newMessage!!.setOnTouchListener { v: View?, event: MotionEvent? ->
                hideAttachmentView()
                false
            }
            Utils.uploadTypingStatus()
            typingListening()
            readTyping()
            seenMessage()
            val handler = Handler(Looper.getMainLooper())
            //This permission required because when you playing the recorded your voice, at that time audio wave effect shown.
            handler.postDelayed({ permissionRecording() }, 800)

            Toast.makeText(mActivity, "MessageActivtu", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(mActivity, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun initUI() {
        mImageView = findViewById(R.id.imageView)
        txtTyping = findViewById(R.id.txtTyping)
        mTxtUsername = findViewById(R.id.txtUsername)
        mToolbar = findViewById(R.id.toolbar)
        mRecyclerView = findViewById(R.id.recyclerView)

        //New Component
        rootView = findViewById(R.id.rootView)
        rlChatView = findViewById(R.id.rlChatView)
        btnGoToBottom = findViewById(R.id.btnBottom)
        newMessage = findViewById(R.id.newMessage)
        imgAddAttachment = findViewById(R.id.imgAddAttachment)
        imgCamera = findViewById(R.id.imgCamera)
        mainAttachmentLayout = findViewById(R.id.mainAttachmentLayout)
        mainAttachmentLayout?.setVisibility(View.GONE)
        attachmentBGView = findViewById(R.id.attachmentBGView)
        attachmentBGView?.setVisibility(View.GONE)
        attachmentBGView?.setOnClickListener(this)
        imgAttachmentEmoji = findViewById(R.id.imgAttachmentEmoji)
        imgAddAttachment?.setOnClickListener(this)
        imgCamera?.setOnClickListener(this)
        imgAttachmentEmoji?.setOnClickListener(this)
        findViewById<View>(R.id.btnAttachmentVideo).setOnClickListener(this)
        findViewById<View>(R.id.btnAttachmentContact).setOnClickListener(this)
        findViewById<View>(R.id.btnAttachmentGallery).setOnClickListener(this)
        findViewById<View>(R.id.btnAttachmentAudio).setOnClickListener(this)
        findViewById<View>(R.id.btnAttachmentLocation).setOnClickListener(this)
        findViewById<View>(R.id.btnAttachmentDocument).setOnClickListener(this)
        recordView = findViewById(R.id.recordView)
        recordButton = findViewById(R.id.recordButton)
        recordButton?.setRecordView(recordView) //IMPORTANT
        initListener()
        pickerManager = PickerManager(this, this, this)
    }

    private fun initListener() {


        try{

            //ListenForRecord must be false ,otherwise onClick will not be called
            recordButton!!.setOnRecordClickListener { v: View? ->
                if (!blockUnblockCheckBeforeSend()) {
                    clickToSend()
                }
            }

            //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
            val isRTLOn = SessionManager.get()?.isRTLOn
            recordView!!.setRTLDirection(isRTLOn!!)
            recordView!!.setSlideMarginRight(recordView!!.slideMargin)
            recordView!!.cancelBounds = 8f
            recordView!!.setSlideFont(Utils.getRegularFont(mActivity))
            recordView!!.setCounterTimerFont(Utils.getBoldFont(mActivity))
            //prevent recording under one Second
            recordView!!.setLessThanSecondAllowed(false)
            recordView!!.setSoundEnabled(true)
            recordView!!.timeLimit = 60000 //1000 = 1 second
            recordView!!.setTrashIconColor(resources.getColor(R.color.red_500))
            recordView!!.setOnRecordListener(object : OnRecordListener {
                override fun onStart() {
                    if (!blockUnblockCheckBeforeSend()) {
                        hideAttachmentView()
                        if (Objects.requireNonNull(newMessage!!.text).toString().trim { it <= ' ' }
                                .isEmpty()) {
                            if (recordWaitHandler == null) recordWaitHandler =
                                Handler(Looper.getMainLooper())
                            recordRunnable = Runnable { recordingStart() }
                            recordWaitHandler!!.postDelayed(recordRunnable!!, IConstants.ONE.toLong())
                        }
                        hideEditTextLayout()
                    }
                }

                override fun onCancel() {
                    if (mRecorder != null && Utils.isEmpty(
                            Objects.requireNonNull(
                                newMessage!!.text
                            ).toString().trim { it <= ' ' })
                    ) {
                        recordingStop(IConstants.FALSE)
                        screens!!.showToast(R.string.recording_cancelled)
                    }
                }

                override fun onFinish(recordTime: Long, limitReached: Boolean) {
                    try {
                        if (recordWaitHandler != null && Objects.requireNonNull(
                                newMessage!!.text
                            ).toString().trim { it <= ' ' }.isEmpty()
                        ) recordWaitHandler!!.removeCallbacks(
                            recordRunnable!!
                        )
                        if (mRecorder != null && Objects.requireNonNull(newMessage!!.text).toString()
                                .trim { it <= ' ' }
                                .isEmpty()
                        ) {
                            recordingStop(IConstants.TRUE)
                        }
                    } catch (ignored: Exception) {
                    }
                    showEditTextLayout()
                }

                override fun onLessThanSecond() {
                    showEditTextLayout()
                }
            })
            recordView!!.setRecordPermissionHandler {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return@setRecordPermissionHandler true
                }
                if (recordPermissionsAvailable()) {
                    return@setRecordPermissionHandler true
                } else {
                    permissionRecording()
                }
                false
            }
            recordView!!.setOnBasketAnimationEndListener { showEditTextLayout() }


        }catch (e: Exception){
            Toast.makeText(mActivity, e.message, Toast.LENGTH_SHORT).show()
        }








    }

    private fun showEditTextLayout() {
        try {

            if (isStart) {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    imgAttachmentEmoji?.visibility = View.VISIBLE
                    newMessage!!.visibility = View.VISIBLE
                    imgAddAttachment?.visibility = View.VISIBLE
                    imgCamera?.visibility = View.VISIBLE
                }, 10)
            }
            isStart = false

        }catch (e: Exception){
                Toast.makeText(mActivity, "showEdittext"+e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun hideEditTextLayout() {
        try{
        isStart = true
        imgAttachmentEmoji?.visibility = View.GONE
        newMessage!!.visibility = View.INVISIBLE
        imgAddAttachment?.visibility = View.GONE
        imgCamera?.visibility = View.GONE

        }catch (e: Exception){
            Toast.makeText(mActivity, "showEdittext"+e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun clickToSend() {

        try{
        if (TextUtils.isEmpty(
                Objects.requireNonNull(newMessage!!.text).toString().trim { it <= ' ' })
        ) {
            screens!!.showToast(R.string.strEmptyMsg)
        } else {
            sendMessage(
                IConstants.TYPE_TEXT,
                Objects.requireNonNull(newMessage!!.text).toString().trim { it <= ' ' },
                null
            )
        }
        newMessage!!.setText(IConstants.EMPTY)

        }catch (e: Exception){
            Toast.makeText(mActivity, "showEdittext"+e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(view: View) {

        try{

        val id = view.id
        if (id == R.id.recordButton) {
            hideAttachmentView()
            clickToSend()
        } else if (id == R.id.imgAttachmentEmoji) {
            emojiIcon!!.toggle()
        } else if (id == R.id.imgAddAttachment) {
            if (!blockUnblockCheckBeforeSend()) {
                fileUri = null
                imgUri = null
                Utils.closeKeyboard(mActivity!!, view)
                if (mainAttachmentLayout!!.visibility == View.VISIBLE) {
                    hideAttachmentView()
                } else {
                    showAttachmentView()
                }
            }
        } else if (id == R.id.imgCamera) {
            if (!blockUnblockCheckBeforeSend()) {
                fileUri = null
                imgUri = null
                hideAttachmentView()
                openCamera()
            }
        } else if (id == R.id.btnAttachmentGallery) {
            hideAttachmentView()
            openImage()
        } else if (id == R.id.btnAttachmentAudio) {
            hideAttachmentView()
            openAudioPicker()
        } else if (id == R.id.btnAttachmentLocation) {
            hideAttachmentView()
            openPlacePicker()
        } else if (id == R.id.btnAttachmentVideo) {
            hideAttachmentView()
            openVideoPicker()
        } else if (id == R.id.btnAttachmentDocument) {
            hideAttachmentView()
            openDocumentPicker()
        } else if (id == R.id.btnAttachmentContact) {
            hideAttachmentView()
            openContactPicker()
        } else if (id == R.id.attachmentBGView) {
            hideAttachmentView()
        }

    }catch (e: Exception){
        Toast.makeText(mActivity, "view"+e.message, Toast.LENGTH_SHORT).show()
    }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            fileUri = Utils.createImageFile(mActivity!!)
            intent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                Utils.getUriForFileProvider(mActivity!!, fileUri)
            )
        } catch (ignored: Exception) {
        }
        intentLauncher.launch(intent)
    }

    private fun openImage() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        intentLauncher.launch(intent)
    }

    private fun openAudioPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            val target = FileUtils.audioIntent
            val intent = Intent.createChooser(target, getString(R.string.choose_file))
            try {
                pickerLauncher.launch(intent)
            } catch (ignored: Exception) {
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, IConstants.PERMISSION_AUDIO)
        }
    }

    private fun openVideoPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            val target = FileUtils.videoIntent
            val intent = Intent.createChooser(target, getString(R.string.choose_file))
            try {
                pickerLauncher.launch(intent)
            } catch (ignored: Exception) {
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, IConstants.PERMISSION_VIDEO)
        }
    }

    fun openDocumentPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            val target = FileUtils.documentIntent
            val intent = Intent.createChooser(target, getString(R.string.choose_file))
            try {
                pickerLauncher.launch(intent)
            } catch (ignored: ActivityNotFoundException) {
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissionsStorage,
                IConstants.PERMISSION_DOCUMENT
            )
        }
    }

    private fun openContactPicker() {
        if (permissionsAvailable(permissionsContact)) {
            MultiContactPicker.Builder(mActivity!!) //Activity/fragment context
                .theme(R.style.MyCustomPickerTheme)
                .setTitleText(getString(R.string.choose_contact))
                .setChoiceMode(MultiContactPicker.CHOICE_MODE_SINGLE) //Optional - default: CHOICE_MODE_MULTIPLE
                .handleColor(
                    ContextCompat.getColor(
                        mActivity!!,
                        R.color.colorPrimaryDark
                    )
                ) //Optional - default: Azure Blue
                .bubbleColor(
                    ContextCompat.getColor(
                        mActivity!!,
                        R.color.colorPrimaryDark
                    )
                ) //Optional - default: Azure Blue
                .setLoadingType(MultiContactPicker.LOAD_ASYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                .limitToColumn(LimitColumn.PHONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                .setActivityAnimations(
                    android.R.anim.fade_in, android.R.anim.fade_out,
                    android.R.anim.fade_in, android.R.anim.fade_out
                ) //Optional - default: No animation overrides
                .showPickerForResult(IConstants.REQUEST_CODE_CONTACT)
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissionsContact,
                IConstants.PERMISSION_CONTACT
            )
        }
    }

    private fun openPlacePicker() {
        if (!Utils.isGPSEnabled(mActivity!!)) {
            screens!!.openGPSSettingScreen()
        } else {
            val builder = PingPlacePicker.IntentBuilder()
            builder.setAndroidApiKey(getString(R.string.key_android))
                .setMapsApiKey(getString(R.string.key_maps))
            try {
                val placeIntent: Intent? = mActivity?.let { builder.build(it) }
                placeLauncher.launch(placeIntent)
            } catch (ex: Exception) {
                // Google Play services is not available...
                Utils.getErrors(ex)
                try {
                    val googleApiAvailability = GoogleApiAvailability.getInstance()
                    googleApiAvailability.showErrorDialogFragment(
                        this,
                        googleApiAvailability.isGooglePlayServicesAvailable(this),
                        IConstants.REQUEST_CODE_PLAY_SERVICES
                    )
                } catch (ignored: Exception) {
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            IConstants.PERMISSION_CONTACT -> if (permissionsAvailable(permissions)) openContactPicker()
            IConstants.PERMISSION_AUDIO -> if (permissionsAvailable(permissions)) openAudioPicker()
            IConstants.PERMISSION_DOCUMENT -> if (permissionsAvailable(permissions)) openDocumentPicker()
            IConstants.PERMISSION_VIDEO -> if (permissionsAvailable(permissions)) openVideoPicker()
            IConstants.REQUEST_PERMISSION_RECORD -> if (permissionsAvailable(permissions)) {
                try {
                    if (messageAdapters != null) messageAdapters!!.notifyDataSetChanged()
                } catch (ignored: Exception) {
                }
            }
        }
    }

    /*
     * Intent launcher to get Image Uri from storage
     * */
    val intentLauncher =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imgUri = if (fileUri != null) { // Image Capture
                    Uri.fromFile(fileUri)
                } else { // Pick from Gallery
                    val data = result.data!!
                    data.data
                }
                try {
                    CropImage.activity(imgUri)
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setFixAspectRatio(true)
                        .start(mActivity!!)
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
            }
        }
    val pickerLauncher =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                val data = result.data!!
                val uriData = data.data
                Utils.sout("PickerManager uri: " + uriData.toString())
                if (uriData != null) {
                    pickerManager!!.getPath(
                        uriData,
                        Build.VERSION.SDK_INT
                    )
                } /* {@link PickerManagerOnCompleteListener }*/
                //                    newFileUploadTask(data.getDataString(), AttachmentTypes.DOCUMENT, null);
            }
        }
    val placeLauncher =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val data = result.data!!
                    val place = PingPlacePicker.getPlace(data)!!
                    val name = if (Utils.isEmpty(
                            place.name
                        )
                    ) IConstants.EMPTY else place.name!!
                    val locationAddress = Objects.requireNonNull(place.latLng)?.let {
                        Objects.requireNonNull(
                            place.latLng
                        )?.let { it1 ->
                            place.address?.let { it2 ->
                                LocationAddress(
                                    name, it2, it1.latitude, it.longitude
                                )
                            }
                        }
                    }
                    val attachment = Attachment()
                    attachment.data = Gson().toJson(locationAddress)
                    attachment.fileName = name
                    attachment.name = name
                    sendMessage(
                        AttachmentTypes.getTypeName(AttachmentTypes.LOCATION),
                        place.address,
                        attachment
                    )
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
            }
        }

    private fun blockUnblockCheckBeforeSend(): Boolean {
        var isBlock = false
        if (isBlocked) {
            //screens.showToast(R.string.msgUnblockToSend);
            Utils.showOKDialog(
                mActivity!!, IConstants.EMPTY, getString(R.string.msgUnblockToSend, userName),
                R.string.strUnblock, R.string.strCancel
            ) { unblockUser() }
            isBlock = true
        }
        if (isOppBlocked) {
            //screens.showToast(R.string.msgBlockForNotSend);
            Utils.showOKDialog(
                mActivity!!,
                IConstants.EMPTY,
                getString(R.string.msgBlockForNotSend)
            ) {}
            isBlock = true
        }
        if (!isBlocked && !isOppBlocked) {
            isBlock = false
        }
        return isBlock
    }

    private fun sendMessage(type: String, message: String?, attachment: Attachment?) {
        if (blockUnblockCheckBeforeSend()) {
            return
        }
        notify = true
        val defaultMsg: String
        val sender = currentId
        val receiver = userId
        val reference = FirebaseDatabase.getInstance().reference
        val hashMap = HashMap<String, Any?>()
        hashMap[IConstants.EXTRA_SENDER] = sender
        hashMap[IConstants.EXTRA_RECEIVER] = receiver
        hashMap[IConstants.EXTRA_MESSAGE] = message
        hashMap[IConstants.EXTRA_ATTACH_TYPE] = type
        //        hashMap.put(EXTRA_TYPE, type);
        hashMap[IConstants.EXTRA_TYPE] =
            IConstants.TYPE_TEXT //This is for older version users(Default TEXT, all other set as IMAGE)
        try {
            if (!type.equals(
                    IConstants.TYPE_TEXT,
                    ignoreCase = true
                ) && !type.equals(IConstants.TYPE_IMAGE, ignoreCase = true)
            ) {
                defaultMsg = Utils.defaultMessage
                hashMap[IConstants.EXTRA_MESSAGE] = defaultMsg
            }
        } catch (ignored: Exception) {
        }
        try {
            if (type.equals(IConstants.TYPE_TEXT, ignoreCase = true)) {
                //No need to do anything here.
            } else if (type.equals(IConstants.TYPE_IMAGE, ignoreCase = true)) {
                hashMap[IConstants.EXTRA_TYPE] = IConstants.TYPE_IMAGE
                hashMap[IConstants.EXTRA_IMGPATH] = message
            } else {
                hashMap[IConstants.EXTRA_ATTACH_PATH] = message
                try {
                    if (attachment != null) {
                        hashMap[IConstants.EXTRA_ATTACH_NAME] = attachment.name
                        hashMap[IConstants.EXTRA_ATTACH_FILE] = attachment.fileName
                        hashMap[IConstants.EXTRA_ATTACH_SIZE] = attachment.bytesCount
                        if (attachment.data != null) {
                            hashMap[IConstants.EXTRA_ATTACH_DATA] = attachment.data
                        }
                        if (attachment.duration != null) {
                            hashMap[IConstants.EXTRA_ATTACH_DURATION] = attachment.duration
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
        } catch (ignored: Exception) {
        }
        hashMap[IConstants.EXTRA_SEEN] = IConstants.FALSE
        hashMap[IConstants.EXTRA_DATETIME] = Utils.dateTime
        val key = Utils.chatUniqueId
        reference.child(IConstants.REF_CHATS).child(strSender!!).child(key!!).setValue(hashMap)
        reference.child(IConstants.REF_CHATS).child(strReceiver!!).child(key).setValue(hashMap)
        Utils.chatSendSound(applicationContext)
        try {
            var msg = message
            if (!type.equals(
                    IConstants.TYPE_TEXT,
                    ignoreCase = true
                ) && !type.equals(IConstants.TYPE_IMAGE, ignoreCase = true)
            ) {
                msg = try {
                    val firstCapital =
                        type.substring(0, 1).uppercase(Locale.getDefault()) + type.substring(1)
                            .lowercase(
                                Locale.getDefault()
                            )
                    if (attachment != null) {
                        "New " + firstCapital + "(" + attachment.name + ")"
                    } else {
                        firstCapital
                    }
                } catch (e: Exception) {
                    message
                }
            }
            if (notify) {
                sendNotification(receiver, strUsername, msg, type)
            }
            notify = false
        } catch (ignored: Exception) {
        }
    }

    private fun sendNotification(
        receiver: String?,
        username: String?,
        message: String?,
        type: String
    ) {
        val tokenRef = FirebaseDatabase.getInstance().getReference(IConstants.REF_TOKENS)
        val query = tokenRef.orderByKey().equalTo(receiver)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (snapshot in dataSnapshot.children) {
                        val token = snapshot.getValue(
                            Token::class.java
                        )
                        val data =
                            Data(
                                currentId,
                                R.drawable.ic_stat_ic_notification,
                                username,
                                message,
                                getString(R.string.strNewMessage),
                                userId,
                                type
                            )

                        assert(token != null)
                        val sender = token!!.token?.let { Sender(data, it) }
                        apiService!!.sendNotification(sender)!!
                            .enqueue(object : Callback<MyResponse?> {
                                override fun onResponse(
                                    call: Call<MyResponse?>,
                                    response: Response<MyResponse?>
                                ) {
                                    assert(response.code() != 200 || response.body() != null)
                                }

                                override fun onFailure(call: Call<MyResponse?>, t: Throwable) {}
                            })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun readMessages(imageUrl: String) {
        chats = ArrayList()
        reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_CHATS).child(
            strReceiver!!
        )
        reference!!.keepSynced(true)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chats!!.clear()
                if (dataSnapshot.hasChildren()) {
                    for (snapshot in dataSnapshot.children) {
                        try {
                            val chat = snapshot.getValue(Chat::class.java)!!
                            if (!Utils.isEmpty(
                                    chat.message
                                )
                            ) {
                                chat.id = snapshot.key
                                chats!!.add(chat)
                            }
                        } catch (ignored: Exception) {
                        }
                    }
                }
                try {
                    messageAdapters =
                        MessageAdapters(mActivity!!, chats!!, userName, strCurrentImage!!, imageUrl)
                    mRecyclerView?.adapter = messageAdapters
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun seenMessage() {
        seenReferenceSender =
            FirebaseDatabase.getInstance().getReference(IConstants.REF_CHATS).child(
                strSender!!
            ).orderByChild(IConstants.EXTRA_SEEN).equalTo(false)
        seenListenerSender =
            seenReferenceSender!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (snapshot in dataSnapshot.children) {
                            try {
                                val chat = snapshot.getValue(Chat::class.java)!!
                                if (!Utils.isEmpty(
                                        chat.message
                                    )
                                ) {
                                    val hashMap = HashMap<String, Any>()
                                    hashMap[IConstants.EXTRA_SEEN] = IConstants.TRUE
                                    snapshot.ref.updateChildren(hashMap)
                                }
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    var itemBlockUnblock: MenuItem? = null
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_groups, menu)
        val itemViewUser = menu.findItem(R.id.itemGroupInfo)
        itemBlockUnblock = menu.findItem(R.id.itemBlockUnblock)
        val itemAdd = menu.findItem(R.id.itemAddGroup)
        val itemEdit = menu.findItem(R.id.itemEditGroup)
        val itemLeave = menu.findItem(R.id.itemLeaveGroup)
        val itemDelete = menu.findItem(R.id.itemDeleteGroup)
        itemAdd.isVisible = false
        itemEdit.isVisible = false
        itemLeave.isVisible = false
        itemDelete.isVisible = false
        itemViewUser.setTitle(R.string.strUserInfo)
        checkUserIsBlock()
        blockedByOpponent()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.itemGroupInfo) {
            screens!!.openViewProfileActivity(userId)
        } else if (itemId == R.id.itemClearMyChats) {
            Utils.showYesNoDialog(
                mActivity!!,
                R.string.strDelete,
                R.string.strDeleteOwnChats
            ) { deleteOwnChats() }
        } else if (itemId == R.id.itemBlockUnblock) {
            if (itemBlockUnblock!!.title.toString()
                    .equals(getString(R.string.strBlock), ignoreCase = true)
            ) {
                blockUser()
            } else {
                unblockUser()
            }
        }
        return true
    }

    private var isBlocked = false
    private var isOppBlocked = false
    private fun checkUserIsBlock() {
        val ref = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS)
        ref.child(currentId!!).child(IConstants.REF_BLOCK_USERS).orderByChild(IConstants.EXTRA_ID)
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        if (ds.exists()) {
                            itemBlockUnblock!!.setTitle(R.string.strUnblock)
                            isBlocked = true
                        } else {
                            isBlocked = false
                            itemBlockUnblock!!.setTitle(R.string.strBlock)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun blockedByOpponent() {
        val ref = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS)
        ref.child(userId!!).child(IConstants.REF_BLOCK_USERS).orderByChild(IConstants.EXTRA_ID)
            .equalTo(currentId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        isOppBlocked = ds.exists()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun blockUser() {
        try {
            showProgress()
            val hashMap = HashMap<String, String?>()
            hashMap[IConstants.EXTRA_ID] = userId
            val ref = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS)
            ref.child(currentId!!).child(IConstants.REF_BLOCK_USERS).child(userId!!)
                .setValue(hashMap)
                .addOnSuccessListener { aVoid: Void? ->
                    hideProgress()
                    isBlocked = true
                    screens!!.showToast(R.string.msgBlockSuccessfully)
                    itemBlockUnblock!!.setTitle(R.string.strUnblock)
                }.addOnFailureListener { e: Exception ->
                    hideProgress()
                    screens!!.showToast(e.message)
                }
        } catch (e: Exception) {
            hideProgress()
            Utils.getErrors(e)
        }
    }

    private fun unblockUser() {
        try {
            showProgress()
            val ref = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS)
            ref.child(currentId!!).child(IConstants.REF_BLOCK_USERS)
                .orderByChild(IConstants.EXTRA_ID).equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dataSnapshot in snapshot.children) {
                            if (dataSnapshot.exists()) {
                                hideProgress()
                                dataSnapshot.ref.removeValue()
                                    .addOnSuccessListener { aVoid: Void? ->
                                        isBlocked = false
                                        screens!!.showToast(R.string.msgUnblockSuccessfully)
                                        itemBlockUnblock!!.setTitle(R.string.strBlock)
                                    }
                                    .addOnFailureListener { e: Exception -> screens!!.showToast(e.message) }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        hideProgress()
                    }
                })
        } catch (e: Exception) {
            hideProgress()
            Utils.getErrors(e)
        }
    }

    /**
     * False means don't close current screen, just delete my own chats
     * True  means close current screen, cause first we leave from group and than delete own chats
     */
    private fun deleteOwnChats() {
        showProgress()
        val chatsSender = FirebaseDatabase.getInstance().getReference(IConstants.REF_CHATS).child(
            strSender!!
        ).orderByChild(IConstants.EXTRA_SENDER).equalTo(currentId)
        chatsSender.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val chat = snapshot.getValue(Chat::class.java)!!
                            if (!Utils.isEmpty(
                                    chat.attachmentType
                                )
                            ) {
                                storage?.let { Utils.deleteUploadedFilesFromCloud(it, chat) }
                                snapshot.ref.removeValue()
                            }
                        }
                    }
                    hideProgress()
                } catch (ignored: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        val chatsReceiver = FirebaseDatabase.getInstance().getReference(IConstants.REF_CHATS).child(
            strReceiver!!
        ).orderByChild(IConstants.EXTRA_SENDER).equalTo(currentId)
        chatsReceiver.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val chat = snapshot.getValue(Chat::class.java)!!
                            if (!Utils.isEmpty(
                                    chat.attachmentType
                                )
                            ) {
                                storage?.let { Utils.deleteUploadedFilesFromCloud(it, chat) }
                            }
                            snapshot.ref.removeValue()
                        }
                    }
                } catch (ignored: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun readTyping() {
        reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_OTHERS).child(
            currentId!!
        )
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if (dataSnapshot.hasChildren()) {
                        val user = dataSnapshot.getValue(Others::class.java)!!
                        if (user.isTyping && user.typingwith.equals(userId, ignoreCase = true)) {
                            txtTyping?.text = getString(R.string.strTyping)
                        } else {
                            txtTyping?.text = onlineStatus
                        }
                    }
                } catch (ignored: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun typingListening() {
        newMessage!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    if (s.length == 0) {
                        stopTyping()
                        recordButton!!.isListenForRecord = true
                        recordButton!!.setImageResource(R.drawable.recv_ic_mic_white)
                    } else if (s.length > 0) {
                        startTyping()
                        idleTyping(s.length)
                        recordButton!!.isListenForRecord = false
                        recordButton!!.setImageResource(R.drawable.ic_send)
                    }
                } catch (e: Exception) {
                    stopTyping()
                    recordButton!!.isListenForRecord = true
                    recordButton!!.setImageResource(R.drawable.recv_ic_mic_white)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun idleTyping(currentLen: Int) {
        try {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                val newLen = Objects.requireNonNull(newMessage!!.text)?.length
                if (currentLen == newLen) {
                    stopTyping()
                }
            }, IConstants.EXTRA_TYPING_DELAY.toLong())
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    private fun startTyping() {
        typingStatus(IConstants.TRUE)
    }

    private fun stopTyping() {
        typingStatus(IConstants.FALSE)
    }

    /**
     * typingStatus - Update typing and userId with db
     * isTyping = True means 'startTyping' method called
     * isTyping = False means 'stopTyping' method called
     */
    private fun typingStatus(isTyping: Boolean) {
        try {
            reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_OTHERS).child(
                userId!!
            )
            val hashMap = HashMap<String, Any?>()
            hashMap[IConstants.EXTRA_TYPINGWITH] = currentId
            hashMap[IConstants.EXTRA_TYPING] = isTyping
            reference!!.updateChildren(hashMap)
        } catch (ignored: Exception) {
        }
    }

    //    private File myFile = null;
    private fun uploadImage() {
        val pd = ProgressDialog(mActivity)
        pd.setMessage(getString(R.string.msg_image_upload))
        pd.show()
        if (imageUri != null) {
            val fileReference = storageReference!!.child(
                System.currentTimeMillis().toString() + "." + Utils.getExtension(
                    mActivity!!,
                    imageUri
                )
            )
            uploadTask = fileReference.putFile(imageUri!!)
            (uploadTask as UploadTask).continueWithTask(Continuation { task: Task<UploadTask.TaskSnapshot?> ->
                if (!task.isSuccessful) {
                    throw Objects.requireNonNull(task.exception)!!
                }
                fileReference.downloadUrl
            } as Continuation<UploadTask.TaskSnapshot?, Task<Uri>>)
                .addOnCompleteListener((OnCompleteListener { task: Task<Uri> ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val mUrl = downloadUri.toString()
                        sendMessage(IConstants.TYPE_IMAGE, mUrl, null)
                        //                                Utils.deleteRecursive(myFile);
                    } else {
                        screens!!.showToast(R.string.msgFailedToUpload)
                    }
                    pd.dismiss()
                } as OnCompleteListener<Uri>)).addOnFailureListener { e: Exception ->
                    Utils.getErrors(e)
                    screens!!.showToast(e.message)
                    pd.dismiss()
                }
        } else {
            screens!!.showToast(R.string.msgNoImageSelected)
        }
    }

    private fun uploadThumbnail(filePath: String?) {
        if (mainAttachmentLayout!!.visibility == View.VISIBLE) {
            mainAttachmentLayout!!.visibility = View.GONE
            attachmentBGView!!.visibility = View.GONE
            imgAddAttachment?.animate()?.setDuration(400)?.rotationBy(-45f)?.start()
        }
        val pd = ProgressDialog(mActivity)
        pd.setMessage(getString(R.string.msg_image_upload))
        pd.show()
        val file = File(filePath)
        val storageReference =
            storageAttachment!!.child(AttachmentTypes.getTypeName(AttachmentTypes.VIDEO) + IConstants.SLASH + IConstants.REF_VIDEO_THUMBS)
                .child(currentId + "_" + file.name + ".jpg")
        storageReference.downloadUrl.addOnSuccessListener { uri: Uri ->
            //If thumbnail exists
            pd.dismiss()
            val attachment = Attachment()
            attachment.data = uri.toString()
            myFileUploadTask(filePath, AttachmentTypes.VIDEO, attachment)
            Utils.deleteRecursive(Utils.getCacheFolder(mActivity!!))
        }.addOnFailureListener {
            val baseTask: BaseTask<*> = object : BaseTask<Any?>() {
                override fun setUiForLoading() {
                    super.setUiForLoading()
                }

                override fun call(): Any? {
                    return ThumbnailUtils.createVideoThumbnail(
                        filePath!!,
                        MediaStore.Video.Thumbnails.MINI_KIND
                    )
                }

                override fun setDataAfterLoading(result: Any?) {
                    val bitmap = result as Bitmap
                    if (bitmap != null) {
                        //Upload thumbnail and then upload video
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        val uploadTask = storageReference.putBytes(data)
                        uploadTask.continueWithTask { task: Task<UploadTask.TaskSnapshot?> ->
                            if (!task.isSuccessful) {
                                throw Objects.requireNonNull(task.exception)!!
                            }
                            storageReference.downloadUrl
                        }.addOnCompleteListener { task: Task<Uri> ->
                            pd.dismiss()
                            if (task.isSuccessful) {
                                val downloadUri = task.result
                                val attachment = Attachment()
                                attachment.data = downloadUri.toString()
                                myFileUploadTask(filePath, AttachmentTypes.VIDEO, attachment)
                            } else {
                                myFileUploadTask(filePath, AttachmentTypes.VIDEO, null)
                            }
                            Utils.deleteRecursive(Utils.getCacheFolder(mActivity!!))
                        }.addOnFailureListener { e1: Exception? ->
                            pd.dismiss()
                            myFileUploadTask(filePath, AttachmentTypes.VIDEO, null)
                            Utils.deleteRecursive(Utils.getCacheFolder(mActivity!!))
                        }
                    } else {
                        pd.dismiss()
                        myFileUploadTask(filePath, AttachmentTypes.VIDEO, null)
                        Utils.deleteRecursive(Utils.getCacheFolder(mActivity!!))
                    }
                }
            }
            val thumbnailTask = TaskRunner()
            thumbnailTask.executeAsync(baseTask as CustomCallable<R?>)
        }
    }

    private fun myFileUploadTask(
        filePath: String?,
        @AttachmentType attachmentType: Int,
        attachment: Attachment?
    ) {
        hideAttachmentView()
        val pd = ProgressDialog(mActivity)
        pd.setMessage(getString(R.string.msg_image_upload))
        pd.setCancelable(false)
        pd.show()
        val mFileUpload = File(filePath)
        val fileName = Utils.getUniqueFileName(mFileUpload, attachmentType)
        val fileToUpload = File(
            Objects.requireNonNull(
                Utils.moveFileToFolder(
                    mActivity!!,
                    true,
                    fileName,
                    mFileUpload,
                    attachmentType
                )
            ).toString(), fileName
        )
        Utils.sout("newFileUploadTask::: " + fileName + " :Exist: " + fileToUpload.exists() + " >>> " + fileToUpload)
        val storageReference =
            storageAttachment!!.child(AttachmentTypes.getTypeName(attachmentType))
                .child(currentId + "_" + fileName)
        storageReference.downloadUrl.addOnSuccessListener { uri: Uri ->
            //If file is already uploaded
            var myAttachment: Attachment? = null
            try {
                myAttachment = attachment
                if (myAttachment == null) myAttachment = Attachment()
                if (attachmentType == AttachmentTypes.CONTACT) {
                } else {
                    myAttachment.name = fileToUpload.name
                    myAttachment.fileName = fileName
                    myAttachment.duration = Utils.getVideoDuration(mActivity!!, fileToUpload)
                }
                myAttachment.url = uri.toString()
                myAttachment.bytesCount = fileToUpload.length()
            } catch (ignored: Exception) {
            }
            sendMessage(AttachmentTypes.getTypeName(attachmentType), uri.toString(), myAttachment)
            pd.dismiss()
            //Utils.deleteRecursive(new File(dir));
            Utils.deleteRecursive(Utils.getCacheFolder(mActivity!!))
        }.addOnFailureListener { exception: Exception? ->
            //Else upload and then send message
            val firebaseUploader = FirebaseUploader(storageReference, object : UploadListener {
                override fun onUploadFail(message: String?) {
                    Utils.sout("onUploadFail::: $message")
                    pd.dismiss()
                }

                override fun onUploadSuccess(downloadUrl: String?) {
                    var myAttachment: Attachment? = null
                    try {
                        myAttachment = attachment
                        if (myAttachment == null) myAttachment = Attachment()
                        if (attachmentType == AttachmentTypes.CONTACT) {
                        } else {
                            myAttachment!!.name = mFileUpload.name
                            myAttachment!!.fileName = fileName // fileToUpload.getName()
                            try {
                                myAttachment!!.duration =
                                    Utils.getVideoDuration(mActivity!!, fileToUpload)
                            } catch (e: Exception) {
                                Utils.getErrors(e)
                            }
                        }
                        myAttachment!!.url = downloadUrl
                        myAttachment!!.bytesCount = fileToUpload.length()
                    } catch (e: Exception) {
                        Utils.getErrors(e)
                    }
                    sendMessage(
                        AttachmentTypes.getTypeName(attachmentType),
                        downloadUrl,
                        myAttachment
                    )
                    pd.dismiss()
                    try {
                        Utils.deleteRecursive(Utils.getCacheFolder(mActivity!!))
                    } catch (e: Exception) {
                        Utils.getErrors(e)
                    }
                }

                override fun onUploadProgress(progress: Int) {
                    try {
                        pd.setMessage("Uploading $progress%...")
                    } catch (ignored: Exception) {
                    }
                }

                override fun onUploadCancelled() {
                    pd.dismiss()
                }
            })
            firebaseUploader.uploadFile(fileToUpload)
        }
    }

    private fun getSendVCard(results: List<ContactResult>) {
        try {
            displayName = results[0].displayName
            phoneNumber = results[0].phoneNumbers[0].number
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
        val baseTask: BaseTask<*> = object : BaseTask<Any?>() {
            override fun setUiForLoading() {
                super.setUiForLoading()
            }

            override fun call(): Any? {
                val cursor = Utils.contactsCursor(mActivity!!, phoneNumber)
                var toSend = Utils.getSentDirectory(
                    mActivity!!,
                    IConstants.TYPE_CONTACT
                ) //Looks like this : AppName/Contact/.sent/
                if (cursor != null && !cursor.isClosed) {
                    cursor.count
                    if (cursor.moveToFirst()) {
                        @SuppressLint("Range") val lookupKey =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                        val uri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_VCARD_URI,
                            lookupKey
                        )
                        try {
                            val assetFileDescriptor =
                                contentResolver.openAssetFileDescriptor(uri, "r")
                            if (assetFileDescriptor != null) {
                                val inputStream = assetFileDescriptor.createInputStream()
                                var dirExists = toSend.exists()
                                if (!dirExists) dirExists = toSend.mkdirs()
                                if (dirExists) {
                                    try {
                                        toSend = Utils.getSentFile(toSend, IConstants.EXT_VCF)
                                        var fileExists = toSend.exists()
                                        if (!fileExists) fileExists = toSend.createNewFile()
                                        if (fileExists) {
                                            val stream: OutputStream = BufferedOutputStream(
                                                FileOutputStream(toSend, false)
                                            )
                                            val buffer = Utils.readAsByteArray(inputStream)
                                            vCardData = String(buffer)
                                            stream.write(buffer)
                                            stream.close()
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Utils.getErrors(e)
                        } finally {
                            cursor.close()
                        }
                    }
                }
                return toSend
            }

            override fun setDataAfterLoading(result: Any?) {
                val f = result as File
                if (f != null && !TextUtils.isEmpty(vCardData)) {
                    val attachment = Attachment()
                    attachment.data = vCardData
                    try {
                        attachment.name = displayName
                        attachment.fileName = displayName
                        attachment.duration = phoneNumber
                    } catch (ignored: Exception) {
                    }
                    myFileUploadTask(f.absolutePath, AttachmentTypes.CONTACT, attachment)
                }
            }
        }
        val taskRunner = TaskRunner()
        taskRunner.executeAsync(baseTask as CustomCallable<R?>)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                assert(result != null)
                imageUri = result!!.uri
                if (uploadTask != null && uploadTask!!.isInProgress) {
                    screens!!.showToast(R.string.msgUploadInProgress)
                } else {
                    uploadImage()
                }
            }
        }
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IConstants.REQUEST_CODE_CONTACT -> try {
                    assert(data != null)
                    val results: List<ContactResult> = MultiContactPicker.obtainResult(data)
                    getSendVCard(results)
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
                IConstants.REQUEST_CODE_PLAY_SERVICES -> openPlacePicker()
            }
        }
    }

    private fun hideAttachmentView() {
        if (mainAttachmentLayout!!.visibility == View.VISIBLE) {
            mainAttachmentLayout!!.visibility = View.GONE
            attachmentBGView!!.visibility = View.GONE
            imgAddAttachment?.animate()?.setDuration(400)?.rotationBy(-45f)?.start()
        }
    }

    private fun showAttachmentView() {
        mainAttachmentLayout!!.visibility = View.VISIBLE
        attachmentBGView!!.visibility = View.VISIBLE
        imgAddAttachment?.animate()?.setDuration(400)?.rotationBy(45f)?.start()
        emojiIcon!!.dismiss()
    }

    private fun recordingStop(send: Boolean) {
        mRecorder = try {
            mRecorder!!.stop()
            mRecorder!!.release()
            null
        } catch (ex: Exception) {
            null
        }
        recordTimerStop()
        if (send) {
            myFileUploadTask(recordFilePath, AttachmentTypes.RECORDING, null)
        } else {
            try {
                File(recordFilePath).delete()
            } catch (ignored: Exception) {
            }
        }
    }

    private fun permissionRecording() {
        if (!recordPermissionsAvailable()) {
            ActivityCompat.requestPermissions(
                mActivity!!,
                permissionsRecord,
                IConstants.REQUEST_PERMISSION_RECORD
            )
        }
    }

    private fun recordingStart() {
        if (blockUnblockCheckBeforeSend()) {
            return
        }
        if (recordPermissionsAvailable()) {
            var recordFile = Utils.getSentDirectory(
                mActivity!!,
                IConstants.TYPE_RECORDING
            ) //Looks like this : AppName/RECORDING/Sent/
            var dirExists = recordFile.exists()
            if (!dirExists) dirExists = recordFile.mkdirs()
            if (dirExists) {
                try {
                    recordFile = Utils.getSentFile(
                        cacheDir, IConstants.EXT_MP3
                    )
                    if (!recordFile.exists()) recordFile.createNewFile()
                    recordFilePath = recordFile.absolutePath
                    Utils.sout("RecordingStart Path: $recordFilePath")
                    mRecorder = MediaRecorder()
                    mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                    mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    mRecorder!!.setOutputFile(recordFilePath)
                    mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    mRecorder!!.prepare()
                    mRecorder!!.start()
                    recordTimerStart()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    mRecorder = null
                }
            }
        } else {
            permissionRecording()
        }
    }

    private fun recordTimerStart() {
        screens!!.showToast(R.string.recording)
        try {
            recordTimerRunnable = object : Runnable {
                override fun run() {
                    recordTimerHandler!!.postDelayed(this, IConstants.DELAY_ONE_SEC.toLong())
                }
            }
            if (recordTimerHandler == null) recordTimerHandler = Handler(Looper.getMainLooper())
            recordTimerHandler!!.post(recordTimerRunnable as Runnable)
        } catch (ignored: Exception) {
        }
        Utils.setVibrate(mActivity!!, IConstants.VIBRATE_HUNDRED.toLong())
    }

    private fun recordTimerStop() {
        try {
            recordTimerHandler!!.removeCallbacks(recordTimerRunnable!!)
            Utils.setVibrate(mActivity!!, IConstants.VIBRATE_HUNDRED.toLong())
        } catch (ignored: Exception) {
        }
    }

    private fun recordPermissionsAvailable(): Boolean {
        var available = true
        for (permission in permissionsRecord) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                available = false
                break
            }
        }
        return available
    }

    private val positionList = ArrayList<Int>()

    //Download complete listener
    private val downloadCompleteReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent != null && intent.action != null) if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                if (positionList.size > IConstants.ZERO && messageAdapters != null) {
                    for (pos in positionList) {
                        if (pos != -1) {
//                                Uncomment to play recording directly once download completed
//                                But before that please stop the current playing audio if playing
//                                try {
//                                    chats.get(pos).setDownloadProgress(COMPLETED);
//                                } catch (Exception ignored) {
//                                }
                            messageAdapters!!.notifyItemChanged(pos)
                        }
                    }
                }
                positionList.clear()
            }
        }
    }

    fun downloadFile(downloadFileEvent: DownloadFileEvent) {
        if (permissionsAvailable(permissionsStorage)) {
            DownloadUtil().loading(this, downloadFileEvent)
            positionList.add(downloadFileEvent.position)
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 47)
        }
    }

    //Download event listener
    private val downloadEventReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadFileEvent =
                intent.getSerializableExtra(IConstants.DOWNLOAD_DATA) as DownloadFileEvent?
            try {
                downloadFileEvent?.let { downloadFile(it) }
            } catch (ignored: Exception) {
            }
        }
    }

    //
    //  PickerManager Listeners
    //
    //  The listeners can be used to display a Dialog when a file is selected from Dropbox/Google Drive or OnDrive.
    //  The listeners are callbacks from an AsyncTask that creates a new File of the original in /storage/emulated/0/Android/data/your.package.name/files/Temp/
    //
    //  PickerManagerOnUriReturned()
    //  When selecting a file from Google Drive, for example, the Uri will be returned before the file is available(if it has not yet been cached/downloaded).
    //  Google Drive will first have to download the file before we have access to it.
    //  This can be used to let the user know that we(the application), are waiting for the file to be returned.
    //
    //  PickerManagerOnStartListener()
    //  This will be call once the file creations starts and will only be called if the selected file is not local
    //
    //  PickerManagerOnProgressUpdate(int progress)
    //  This will return the progress of the file creation (in percentage) and will only be called if the selected file is not local
    //
    //  PickerManagerOnCompleteListener(String path, boolean wasDriveFile)
    //  If the selected file was from Dropbox/Google Drive or OnDrive, then this will be called after the file was created.
    //  If the selected file was a local file then this will be called directly, returning the path as a String
    //  Additionally, a boolean will be returned letting you know if the file selected was from Dropbox/Google Drive or OnDrive.
    private var percentText: TextView? = null
    private var mProgressBar: ProgressBar? = null
    private var mdialog: AlertDialog? = null
    private var progressBar: ProgressDialog? = null
    override fun PickerManagerOnUriReturned() {
        progressBar = ProgressDialog(this)
        progressBar!!.setMessage(getString(R.string.msgWaitingForFile))
        progressBar!!.setCancelable(false)
        progressBar!!.show()
    }

    override fun PickerManagerOnStartListener() {
        val mPickHandler: Handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(message: Message) {
                // This is where you do your work in the UI thread. Your worker tells you in the message what to do.
                if (progressBar!!.isShowing) {
                    progressBar!!.cancel()
                }
                val mPro = AlertDialog.Builder(ContextThemeWrapper(mActivity, R.style.myDialog))
                @SuppressLint("InflateParams") val mPView =
                    LayoutInflater.from(mActivity).inflate(R.layout.dailog_layout, null)
                percentText = mPView.findViewById(R.id.percentText)
                percentText?.setOnClickListener(object : SingleClickListener() {
                    override fun onClickView(view: View?) {
                        pickerManager!!.cancelTask()
                        if (mdialog != null && mdialog!!.isShowing) {
                            mdialog!!.cancel()
                        }
                    }
                })
                mProgressBar = mPView.findViewById(R.id.mProgressBar)
                mProgressBar?.setMax(100)
                mPro.setView(mPView)
                mdialog = mPro.create()
                mdialog!!.show()
            }
        }
        mPickHandler.sendEmptyMessage(IConstants.ZERO)
    }

    override fun PickerManagerOnProgressUpdate(progress: Int) {
        try {
            val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(message: Message) {
                    val progressPlusPercent = "$progress%"
                    percentText!!.text = progressPlusPercent
                    mProgressBar!!.progress = progress
                }
            }
            mHandler.sendEmptyMessage(IConstants.ZERO)
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    //REQUEST_PICK_AUDIO, REQUEST_PICK_VIDEO, REQUEST_PICK_DOCUMENT
    override fun PickerManagerOnCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {

        try{

        if (mdialog != null && mdialog!!.isShowing) {
            mdialog!!.cancel()
        }
        Utils.sout("Picker Path :: " + File(path).exists() + " >> " + path + " :drive: " + wasDriveFile + " :<Success>: " + wasSuccessful)
        var fileType = 0
        try {
            fileType = Objects.requireNonNull(path?.let { MediaFile.getFileType(it) })?.fileType!!
        } catch (e: Exception) {
            //Utils.getErrors(e);
        }
        if (wasSuccessful) {
            //Utils.sout("Was Successfully::: " + wasSuccessful);
            val file_size = (File(path).length() / 1024).toString().toInt()
            if (MediaFile.isAudioFileType(fileType)) {
                if (file_size > Utils.audioSizeLimit) {
                    screens!!.showToast(
                        String.format(
                            getString(R.string.msgFileTooBig),
                            Utils.MAX_SIZE_AUDIO
                        )
                    )
                } else {
                    myFileUploadTask(path, AttachmentTypes.AUDIO, null)
                }
            } else if (MediaFile.isVideoFileType(fileType)) {
                if (file_size > Utils.videoSizeLimit) {
                    screens!!.showToast(
                        String.format(
                            getString(R.string.msgFileTooBig),
                            Utils.MAX_SIZE_VIDEO
                        )
                    )
                } else {
                    uploadThumbnail(Uri.parse(path).path)
                }
            } else {
                if (file_size > Utils.documentSizeLimit) {
                    screens!!.showToast(
                        String.format(
                            getString(R.string.msgFileTooBig),
                            Utils.MAX_SIZE_DOCUMENT
                        )
                    )
                } else {
                    myFileUploadTask(path, AttachmentTypes.DOCUMENT, null)
                }
            }
        } else {
            screens!!.showToast(R.string.msgChooseFileFromOtherLocation)
        }


    }catch (e: Exception){
        Toast.makeText(mActivity, "pickermanag"+e.message, Toast.LENGTH_SHORT).show()
    }
    }

    override fun onResume() {
        super.onResume()
        try {
            registerReceiver(
                downloadCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
            LocalBroadcastManager.getInstance(this).registerReceiver(
                downloadEventReceiver,
                IntentFilter(IConstants.BROADCAST_DOWNLOAD_EVENT)
            )
        } catch (ignored: Exception) {
        }
    }

    override fun onStart() {
        super.onStart()
        try{

        Utils.readStatus(IConstants.STATUS_ONLINE)
        }catch (e: Exception){
            Toast.makeText(mActivity, "start"+e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(downloadCompleteReceiver)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadEventReceiver)
        } catch (ignored: Exception) {
        }
        try {
            if (messageAdapters != null) {
                messageAdapters!!.stopAudioFile()
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onBackPressed() {
        try {
            pickerManager!!.deleteTemporaryFile(this)
        } catch (ignored: Exception) {
        }
        if (mainAttachmentLayout!!.visibility == View.VISIBLE) {
            hideAttachmentView()
        } else {
            finish()
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            seenReferenceSender!!.removeEventListener(seenListenerSender!!)
            stopTyping()
        } catch (ignored: Exception) {
        }
        try {
            if (!isChangingConfigurations) {
                pickerManager!!.deleteTemporaryFile(this)
            }
        } catch (ignored: Exception) {
        }
    }
}