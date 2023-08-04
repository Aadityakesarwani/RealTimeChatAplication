package com.innovativetools.firebase.chat.activities.fragments


import com.innovativetools.firebase.chat.activities.fragments.BaseFragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.innovativetools.firebase.chat.activities.constants.IConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.innovativetools.firebase.chat.activities.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.cardview.widget.CardView
import com.shobhitpuri.custombuttons.GoogleSignInButton
import android.text.TextUtils
import com.innovativetools.firebase.chat.activities.models.Chat
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.innovativetools.firebase.chat.activities.LoginActivity
import com.google.firebase.auth.GoogleAuthProvider
import com.innovativetools.firebase.chat.activities.fragments.ProfileFragment
import com.skydoves.powermenu.PowerMenuItem
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.CircularEffect
import android.view.Gravity
import android.provider.MediaStore
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.UploadTask
import com.innovativetools.firebase.chat.activities.managers.SessionManager
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.Groups
import com.innovativetools.firebase.chat.activities.models.User
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.lang.Exception
import java.util.ArrayList
import java.util.HashMap

class ProfileFragment : BaseFragment(), View.OnClickListener {
    private var imgAvatar: CircleImageView? = null
    private var firebaseUser: FirebaseUser? = null
    private var reference: DatabaseReference? = null
    private var storageReference: StorageReference? = null
    private var imgEditAbout: ImageView? = null
    private var imgEditGender: ImageView? = null
    private var btnDeleteAccount: Button? = null
    private var imageUri: Uri? = null
    private var uploadTask: StorageTask<*>? = null
    private var strDescription = ""
    private var strGender = IConstants.GEN_UNSPECIFIED
    private var strReEmail = ""
    private var strRePassword = ""
    private var strAvatarImg: String? = null
    private var strUsername = ""
    private var strSocialToken = ""
    private var currentId: String? = null
    private var strSignUpType = IConstants.TYPE_EMAIL
    private val userList: MutableList<String?> = ArrayList()
    private val groupAdminList: MutableList<String> = ArrayList()
    private val groupAdminMemberList: MutableList<String> = ArrayList()
    private val groupOthersList: MutableList<String?> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val layoutCameraGallery = view.findViewById<View>(R.id.layoutCameraGallery)
        imgAvatar = view.findViewById(R.id.imgAvatar)
        val txtUsername = view.findViewById<TextView>(R.id.txtUsername)
        val txtEmail = view.findViewById<TextView>(R.id.txtEmail)
        val txtAbout = view.findViewById<TextView>(R.id.txtAbout)
        val txtGender = view.findViewById<TextView>(R.id.txtGender)
        imgEditAbout = view.findViewById(R.id.imgEdit)
        imgEditGender = view.findViewById(R.id.imgEditGender)
        val layoutAbout = view.findViewById<RelativeLayout>(R.id.layoutAbout)
        val layoutGender = view.findViewById<RelativeLayout>(R.id.layoutGender)
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount)
        val storage = FirebaseStorage.getInstance()
        storageReference = storage.getReference(IConstants.REF_UPLOAD)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        assert(firebaseUser != null)
        currentId = firebaseUser!!.uid
        val i = requireActivity().intent
        val userId = i.getStringExtra(IConstants.EXTRA_USER_ID)
        val viewUserId: String?
        if (Utils.isEmpty(userId)) { //
            viewUserId = firebaseUser!!.uid
            showHideViews(View.VISIBLE)
            layoutCameraGallery.setOnClickListener(this)
            imgAvatar?.setOnClickListener(this)
            layoutAbout.setOnClickListener(this)
            layoutGender.setOnClickListener(this)
            btnDeleteAccount?.setOnClickListener(this)
        } else {
            viewUserId = userId
            showHideViews(View.GONE)
        }
        val lblStatus = mActivity!!.getString(R.string.strAboutStatus)
        val lblUnSpecified = getString(R.string.strUnspecified)
        val lblMale = getString(R.string.strMale)
        val lblFemale = getString(R.string.strFemale)
        reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(
            viewUserId!!
        )
        reference!!.keepSynced(true)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    val user = dataSnapshot.getValue(
                        User::class.java
                    )
                    strUsername = user?.username.toString()
                    strSignUpType = user?.signup_type.toString()
                    try {
                        if (user != null) {
                            strSocialToken = if (Utils.isEmpty(
                                    user.social_token
                                )
                            ) "" else user.social_token
                        }
                    } catch (ignored: Exception) {
                    }
                    strAvatarImg = user?.myImg
                    txtUsername.text = strUsername
                    strReEmail = user?.email.toString()
                    strRePassword = user?.password.toString()
                    txtEmail.text = strReEmail
                    strGender = user?.genders!!
                    strDescription = user?.about.toString()
                    txtAbout.text = if (Utils.isEmpty(strDescription)) lblStatus else strDescription
                    txtGender.text =
                        if (strGender == IConstants.GEN_UNSPECIFIED) lblUnSpecified else if (strGender == IConstants.GEN_MALE) lblMale else lblFemale
                    strAvatarImg?.let {
                        Utils.setProfileImage(
                            context, it, imgAvatar
                        )
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        return view
    }

    private fun showHideViews(isShow: Int) {
        imgEditAbout!!.visibility = isShow
        imgEditGender!!.visibility = isShow
        btnDeleteAccount!!.visibility = isShow
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.layoutCameraGallery) {
            if (Utils.isTypeEmail(strSignUpType)) {
                openImageCropper()
            } else {
                screens!!.openFullImageViewActivity(v, strAvatarImg, strUsername)
            }
        } else if (id == R.id.imgAvatar) {
            screens!!.openFullImageViewActivity(v, strAvatarImg, strUsername)
        } else if (id == R.id.layoutAbout) {
            popupForAbout()
        } else if (id == R.id.layoutGender) {
            Utils.selectGenderPopup(mActivity!!, firebaseUser!!.uid, strGender)
        } else if (id == R.id.btnDeleteAccount) {
            openAuthenticatePopup()
        }
    }

    private fun openAuthenticatePopup() {
        val builder = AlertDialog.Builder(mContext)
        val view =
            mActivity!!.layoutInflater.inflate(R.layout.dialog_reauthenticate, null) as CardView
        if (SessionManager.get()!!.isRTLOn) {
            view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        builder.setView(view)
        val layoutEmail = view.findViewById<LinearLayout>(R.id.layoutEmail)
        val txtEmail = view.findViewById<TextView>(R.id.txtEmail)
        val txtPassword = view.findViewById<TextView>(R.id.txtPassword)
        val btnSignup = view.findViewById<Button>(R.id.btnSignUp)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnGoogleSignIn = view.findViewById<GoogleSignInButton>(R.id.btnGoogleSignIn)
        layoutEmail.visibility = View.GONE
        btnGoogleSignIn.visibility = View.GONE
        if (Utils.isTypeEmail(strSignUpType)) {
            layoutEmail.visibility = View.VISIBLE
        } else if (strSignUpType.equals(IConstants.TYPE_GOOGLE, ignoreCase = true)) {
            btnGoogleSignIn.visibility = View.VISIBLE
        }
        val alert = builder.create()
        btnCancel.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                alert.dismiss()
            }
        })
        btnSignup.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                val strEmail = txtEmail.text.toString().trim { it <= ' ' }
                val strPassword = txtPassword.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)) {
                    screens!!.showToast(R.string.strAllFieldsRequired)
                } else if (!strEmail.equals(strReEmail, ignoreCase = true) || !strPassword.equals(
                        strRePassword,
                        ignoreCase = true
                    )
                ) {
                    screens!!.showToast(R.string.strInvalidEmailPassword)
                } else {
                    alert.dismiss()
                    deleteChatsAndOtherData()
                }
            }
        })
        btnGoogleSignIn.setOnClickListener { v: View? ->
            val strEmail = txtEmail.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(strEmail)) {
                screens!!.showToast(mActivity!!.getString(R.string.strEmailFieldsRequired))
            } else if (!strEmail.equals(strReEmail, ignoreCase = true)) {
                screens!!.showToast(mActivity!!.getString(R.string.strInvalidEmail))
            } else {
                alert.dismiss()
                deleteChatsAndOtherData()
            }
        }
        alert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alert.setCanceledOnTouchOutside(false)
        alert.setCancelable(!Utils.isTypeEmail(strSignUpType))
        alert.show()
    }

    private fun deleteInsideUsersChat() {
        for (i in userList.indices) {
            val key = userList[i]
            val query: Query =
                FirebaseDatabase.getInstance().getReference(IConstants.REF_CHATS).child(
                    key!!
                )
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            for (snapshot in dataSnapshot.children) {
                                if (snapshot.key.equals(currentId, ignoreCase = true)) {
                                    snapshot.ref.removeValue()
                                    break
                                }
                            }
                        }
                    } catch (ignored: Exception) {
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    private fun deleteChatsAndOtherData() {
        showProgress()
        userList.clear()
        groupAdminList.clear()
        groupAdminMemberList.clear()
        groupOthersList.clear()
        val chats: Query = FirebaseDatabase.getInstance().getReference(IConstants.REF_CHATS).child(
            currentId!!
        )
        chats.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            userList.add(snapshot.key)
                            snapshot.ref.removeValue()
                        }
                        deleteInsideUsersChat()
                    }
                } catch (ignored: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        deleteGroupData()
    }

    //***************************************************************************************************************************
    //************************************************** GROUPS ADMIN DELETE - START **************************************************
    //***************************************************************************************************************************
    private fun deleteGroupData() {

        //Delete first our groups, where we are admin
        val groupsAdmin = FirebaseDatabase.getInstance().getReference(IConstants.REF_GROUPS)
            .orderByChild(IConstants.EXTRA_ADMIN).equalTo(currentId)
        groupsAdmin.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        try {
                            for (snapshot in dataSnapshot.children) {
                                val grp = snapshot.getValue(
                                    Groups::class.java
                                )!!
                                grp.id?.let { groupAdminList.add(it) }
                                grp.members?.let { groupAdminMemberList.addAll(it) }
                                snapshot.ref.removeValue() // Delete our admin group
                            }
                        } catch (ignored: Exception) {
                        }

                        //Remove group messages where we are Group Admin, Whole Group can be deleted
                        for (i in groupAdminList.indices) {
                            var keyGroupId: String? = null
                            try {
                                keyGroupId = groupAdminList[i]
                                val groupsAdminMessages: Query = FirebaseDatabase.getInstance()
                                    .getReference(IConstants.REF_GROUPS_MESSAGES).child(keyGroupId)
                                groupsAdminMessages.ref.removeValue() // Delete our created Group All Messages
                            } catch (ignored: Exception) {
                            }
                            try {
                                for (j in groupAdminMemberList.indices) {
                                    val keyMem = groupAdminMemberList[j]
                                    val groupsAdminGroupsIn: Query = FirebaseDatabase.getInstance()
                                        .getReference(IConstants.REF_GROUP_MEMBERS)
                                        .child(keyMem + IConstants.EXTRA_GROUPS_IN_BOTH + keyGroupId)
                                    groupsAdminGroupsIn.ref.removeValue() //Remove Group Id from Other member's groupIn, So they are not part of our group because we're deleted
                                }
                            } catch (ignored: Exception) {
                            }
                        }
                        deleteOtherGroupData()
                    } else {
                        deleteOtherGroupData()
                    }
                } catch (ignored: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

//        Utils.updateUserActive(firebaseUser.getUid());
    }

    /**
     * ===================================================================================================
     * =========================== START OTHER GROUPS DATA AND DELETE IF FOUND ===========================
     * ===================================================================================================
     */
    private fun deleteOtherGroupData() {

        //Delete myself from Other groups where added by thier other users.
        val groupsAdminGroupsIn: Query =
            FirebaseDatabase.getInstance().getReference(IConstants.REF_GROUP_MEMBERS)
                .child(currentId + IConstants.SLASH + IConstants.EXTRA_GROUPS_IN)
        groupsAdminGroupsIn.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            groupOthersList.add(snapshot.key)
                            snapshot.ref.removeValue() //Remove from other Group created by other user.
                        }
                    }
                } catch (ignored: Exception) {
                }
                try {
                    //Delete other groups from where I did chat with other guys, So I only deleted my message from that groups.
                    for (i in groupOthersList.indices) {
                        val grpOtherMsg = groupOthersList[i]
                        val groupsOtherMsg: Query = FirebaseDatabase.getInstance()
                            .getReference(IConstants.REF_GROUPS_MESSAGES).child(
                            grpOtherMsg!!
                        )
                        groupsOtherMsg.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {
                                    if (dataSnapshot.exists()) {
                                        for (snapshot in dataSnapshot.children) {
                                            val chat = snapshot.getValue(Chat::class.java)!!
                                            if (chat.sender.equals(currentId, ignoreCase = true)) {
                                                snapshot.ref.removeValue() //Delete mine message only from Other groups
                                            }
                                        }
                                    }
                                } catch (ignored: Exception) {
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })

                        //Delete from Other Groups info where I added inside the 'members' attribute. So delete myself from their.
                        val groupsOther: Query =
                            FirebaseDatabase.getInstance().getReference(IConstants.REF_GROUPS)
                                .child(
                                    grpOtherMsg
                                )
                        groupsOther.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                try {
                                    if (dataSnapshot.exists()) {
                                        val groups = dataSnapshot.getValue(
                                            Groups::class.java
                                        )!!
                                        val list: MutableList<String> = groups.members!!.toMutableList()
                                        list.remove(currentId) //Delete our Id to remove from that group and update it list
                                        val hashMap = HashMap<String, Any>()
                                        hashMap[IConstants.EXTRA_GROUP_MEMBERS] = list
                                        dataSnapshot.ref.updateChildren(hashMap) // Delete/Update myself from members in other groups.
                                    }
                                } catch (ignored: Exception) {
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    }
                } catch (ignored: Exception) {
                }
                deleteTokenOtherData()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    //***************************************************************************************************************************
    //*************************************************** TOKENS DELETE - END ***************************************************
    //***************************************************************************************************************************
    private fun deleteTokenOtherData() {
        val tokens =
            FirebaseDatabase.getInstance().getReference(IConstants.REF_TOKENS).orderByKey().equalTo(
                firebaseUser!!.uid
            )
        tokens.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            snapshot.ref.removeValue()
                        }
                    }
                } catch (ignored: Exception) {
                }
                val others =
                    FirebaseDatabase.getInstance().getReference(IConstants.REF_OTHERS).orderByKey()
                        .equalTo(
                            firebaseUser!!.uid
                        )
                others.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        try {
                            if (dataSnapshot.exists()) {
                                for (snapshot in dataSnapshot.children) {
                                    snapshot.ref.removeValue()
                                }
                            }
                        } catch (ignored: Exception) {
                        }
                        if (Utils.isTypeEmail(strSignUpType)) {
                            deActivateAccount()
                        } else {
                            deActivateAccount(strSignUpType)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun deActivateAccount() {
        //Getting the user instance.
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            //You need to get here the token you saved at logging-in time.
            val token: String? = null
            val credential: AuthCredential

            //This means you didn't have the token because user used like Facebook Sign-in method.
            credential = if (token == null) {
                EmailAuthProvider.getCredential(strReEmail, strRePassword)
            } else {
                //Doesn't matter if it was Facebook Sign-in or others. It will always work using GoogleAuthProvider for whatever the provider.
                //credential = GoogleAuthProvider.getCredential(token, null);
                return
            }


            //We have to reauthenticate user because we don't know how long
            //it was the sign-in. Calling reauthenticate, will update the
            //user login and prevent FirebaseException (CREDENTIAL_TOO_OLD_LOGIN_AGAIN) on user.delete()
            user.reauthenticate(credential)
                .addOnCompleteListener { task: Task<Void?>? ->
                    //Calling delete to remove the user and wait for a result.
                    user.delete().addOnCompleteListener({ task1: Task<Void?> ->
                        if (task1.isSuccessful) {
                            //Ok, user remove
                            try {
                                hideProgress()
                                val reference3: Query = FirebaseDatabase.getInstance()
                                    .getReference(IConstants.REF_USERS).orderByKey()
                                    .equalTo(user.uid)
                                reference3.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (snapshot: DataSnapshot in dataSnapshot.children) {
                                                snapshot.ref.removeValue()
                                                screens!!.showClearTopScreen(LoginActivity::class.java)
                                            }
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                            } catch (e: Exception) {
                                Utils.getErrors(e)
                            }
                        } else {
                            //Handle the exception
                            Utils.getErrors(task1.exception)
                        }
                    })
                }
        }
    }

    fun deActivateAccount(strSignUpType: String) {
        //Getting the user instance.
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            try {
                hideProgress()
                val credential: AuthCredential
                credential = if (strSignUpType.equals(IConstants.TYPE_GOOGLE, ignoreCase = true)) {
                    GoogleAuthProvider.getCredential(strSocialToken, null)
                } else {
                    user.delete()
                    openLoginScreen(user, strSignUpType)
                    return
                }
                user.reauthenticate(credential)
                    .addOnCompleteListener { task: Task<Void?>? ->
                        //Calling delete to remove the user and wait for a result.
                        user.delete().addOnCompleteListener(OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Ok, user remove
                                openLoginScreen(user, strSignUpType)
                            } else {
                                //Handle the exception
                                openLoginScreen(user, strSignUpType)
                                Utils.getErrors(task.exception)
                            }
                        })
                    }
            } catch (e: Exception) {
                openLoginScreen(user, strSignUpType)
                Utils.getErrors(e)
            }
        } else {
            //Handle the exception
        }
    }

    private fun openLoginScreen(user: FirebaseUser, strSignUpType: String) {
        try {
            val reference3 =
                FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).orderByKey()
                    .equalTo(user.uid)
            reference3.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        FirebaseAuth.getInstance().signOut()
                        if (strSignUpType.equals(IConstants.TYPE_GOOGLE, ignoreCase = true)) {
                            revokeGoogle(mContext!!)
                        }
                        for (snapshot in dataSnapshot.children) {
                            snapshot.ref.removeValue()
                            screens!!.showClearTopScreen(LoginActivity::class.java)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    fun popupForAbout() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getText(R.string.strEnterAbout))
        val view = layoutInflater.inflate(R.layout.dialog_description, null) as CardView
        if (SessionManager.get()!!.isRTLOn) {
            view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnDone = view.findViewById<AppCompatButton>(R.id.btnDone)
        builder.setView(view)
        val txtAbout = view.findViewById<EditText>(R.id.txtAbout)
        txtAbout.setText(strDescription)
        val alert = builder.create()
        btnCancel.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                alert.dismiss()
            }
        })
        btnDone.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                try {
                    val strAbout = txtAbout.text.toString().trim { it <= ' ' }
                    if (Utils.isEmpty(strAbout)) {
                        screens!!.showToast(R.string.msgErrorEnterDesc)
                        return
                    }
                    try {
                        val reference =
                            FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(
                                firebaseUser!!.uid
                            )
                        val hashMap = HashMap<String, Any>()
                        hashMap[IConstants.EXTRA_ABOUT] = strAbout
                        reference.updateChildren(hashMap)
                    } catch (e: Exception) {
                        Utils.getErrors(e)
                    }
                } catch (e: Exception) {
                    Utils.getErrors(e)
                }
                alert.dismiss()
            }
        })
        alert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alert.setCanceledOnTouchOutside(false)
        alert.setCancelable(false)
        alert.show()
    }

    private var fileUri: File? = null
    private var imgUri: Uri? = null
    private fun openImageCropper() {
        try {
            fileUri = null
            imgUri = null
            val list: MutableList<PowerMenuItem> = ArrayList()
            list.add(PowerMenuItem(getString(R.string.strGallery), R.drawable.ic_popup_gallery))
            list.add(PowerMenuItem(getString(R.string.strCamera), R.drawable.ic_popup_camera))
            val powerMenu = Utils.getRegularFont(mActivity)?.let {
                PowerMenu.Builder(mActivity!!)
                    .addItemList(list)
                    .setAnimation(MenuAnimation.ELASTIC_CENTER)
                    .setCircularEffect(CircularEffect.BODY)
                    .setTextGravity(Gravity.NO_GRAVITY)
                    .setMenuRadius(10f) // sets the corner radius.
                    .setMenuShadow(10f) // sets the shadow.
                    .setTextTypeface(it)
                    .setTextSize(15)
                    .setSelectedTextColor(Color.WHITE)
                    .setMenuColor(Color.WHITE)
                    .setSelectedEffect(true)
                    .setTextColor(ContextCompat.getColor(mActivity!!, R.color.grey_800))
                    .setSelectedMenuColor(ContextCompat.getColor(mActivity!!, R.color.colorAccent))
                    .setDismissIfShowAgain(true)
                    .setAutoDismiss(true)
                    .setOnMenuItemClickListener { position: Int, item: PowerMenuItem ->
                        if (item.title.toString()
                                .equals(getString(R.string.strGallery), ignoreCase = true)
                        ) {
                            openImage()
                        } else {
                            openCamera()
                        }
                    }
                    .build()
            }
            if (powerMenu != null) {
                powerMenu.showAsAnchorCenter(view)
            }
        } catch (e: Exception) {
            Utils.getErrors(e)
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

    private fun cropImage() {
        try {
            val intent = CropImage.activity(imgUri)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setFixAspectRatio(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .getIntent(mActivity!!)
            cropLauncher.launch(intent)
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    /*
     * Intent launcher to get Image Uri from storage
     * */
    val intentLauncher =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imgUri = if (fileUri != null) { // Image Capture
                    Uri.fromFile(fileUri)
                } else { // Pick from Gallery
                    val data = result.data!!
                    data.data
                }
                Utils.sout("ImageURI:::  $imgUri")
                cropImage()
            }
        }

    /*
     * Intent launcher to get Image Uri from storage
     * */
    val cropLauncher =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val re = CropImage.getActivityResult(data)!!
                imageUri = re.uri
                if (uploadTask != null && uploadTask!!.isInProgress) {
                    screens!!.showToast(R.string.msgUploadInProgress)
                } else {
                    uploadImage()
                }
            }
        }

    private fun uploadImage() {
        val pd = ProgressDialog(context)
        pd.setMessage(getString(R.string.msg_image_upload))
        pd.show()
        if (imageUri != null) {
            val fileReference = storageReference!!.child(
                System.currentTimeMillis().toString() + "." + context?.let {
                    Utils.getExtension(
                        it, imageUri
                    )
                }
            )
            uploadTask = fileReference.putFile(imageUri!!)
            (uploadTask as UploadTask).continueWithTask(Continuation { task: Task<UploadTask.TaskSnapshot?> ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                fileReference.downloadUrl
            } as Continuation<UploadTask.TaskSnapshot?, Task<Uri>>)
                .addOnCompleteListener((OnCompleteListener { task: Task<Uri> ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val mUrl = downloadUri.toString()
                        imgAvatar!!.setImageURI(imageUri)
                        reference =
                            FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS)
                                .child(firebaseUser!!.uid)
                        val hashMap = HashMap<String, Any>()
                        hashMap[IConstants.EXTRA_IMAGEURL] = mUrl
                        reference!!.updateChildren(hashMap)
                    } else {
                        screens!!.showToast(R.string.msgFailedToUpload)
                    }
                    pd.dismiss()
                } as OnCompleteListener<Uri>))
                .addOnFailureListener(OnFailureListener { e: Exception ->
                    Utils.getErrors(e)
                    screens!!.showToast(e.message)
                    pd.dismiss()
                })
        } else {
            screens!!.showToast(R.string.msgNoImageSelected)
        }
    }

    companion object {
        private fun revokeGoogle(context: Context) {
            try {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
                mGoogleSignInClient.revokeAccess()
            } catch (e: Exception) {
                Utils.getErrors(e)
            }
        }
    }
}