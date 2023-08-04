package com.innovativetools.firebase.chat.activities


import com.innovativetools.firebase.chat.activities.constants.IGroupListener
import androidx.recyclerview.widget.RecyclerView
import android.widget.RelativeLayout
import android.widget.EditText
import com.google.firebase.storage.StorageReference
import android.os.Bundle
import com.google.firebase.storage.FirebaseStorage
import com.innovativetools.firebase.chat.activities.constants.IConstants
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.innovativetools.firebase.chat.activities.adapters.GroupsUserAdapters
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import android.app.ProgressDialog
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.Groups
import com.innovativetools.firebase.chat.activities.models.User
import java.lang.Exception
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

class GroupsAddActivity : BaseActivity(), IGroupListener, View.OnClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var imgNoUsers: RelativeLayout? = null
    private var mUsers: ArrayList<User>? = null
    private var mSelectedUsers: ArrayList<User>? = null
    private var mSelectedMembersId: MutableList<String>? = null
    private var mDeletedMembersId: Set<String>? = null
    private var txtGroupName: EditText? = null
    private var isEditGroup = false
    private var groups: Groups? = null
    private var groupId: String? = null
    private var groupImg = ""
    private var lastMsg = ""
    private var msgType = ""
    private var imageUri: Uri? = null
    private var storageReference: StorageReference? = null
    private var imgAvatar: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)
        val storage = FirebaseStorage.getInstance()
        storageReference = storage.getReference(IConstants.REF_GROUP_UPLOAD)
        imgAvatar = findViewById(R.id.imgAvatar)
        imgNoUsers = findViewById(R.id.imgNoUsers)
        imgNoUsers?.setVisibility(View.GONE)
        txtGroupName = findViewById(R.id.txtGroupName)
        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        mRecyclerView?.setLayoutManager(layoutManager)
        val dividerItemDecoration =
            DividerItemDecoration(mRecyclerView?.getContext(), layoutManager.orientation)
        mRecyclerView?.addItemDecoration(dividerItemDecoration)
        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)
        val myIntent = intent
        if (myIntent.getStringExtra(IConstants.EXTRA_GROUP_ID) != null) {
            isEditGroup = IConstants.TRUE
            groups = myIntent.getSerializableExtra(IConstants.EXTRA_OBJ_GROUP) as Groups?
            groupId = groups!!.id
            val groupName = groups!!.groupName
            groupImg = groups!!.groupImg.toString()
            msgType = groups!!.type.toString()
            lastMsg = groups!!.lastMsg.toString()
            txtGroupName?.setText(groupName)
            groups!!.groupImg?.let { Utils.setGroupImage(mActivity, it, imgAvatar) }
            supportActionBar!!.setTitle(R.string.strEditGroup)
        } else {
            supportActionBar!!.setTitle(R.string.strCreateNewGroup)
            isEditGroup = IConstants.FALSE
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            screens!!.showClearTopScreen(LoginActivity::class.java)
            finish()
        }
        mUsers = ArrayList()
        mSelectedUsers = ArrayList()
        mSelectedMembersId = ArrayList()
        mDeletedMembersId = HashSet()
        mSelectedMembersId?.add(firebaseUser!!.uid)
        readUsers()
        imgAvatar?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.imgAvatar) {
            openImageCropper()
        }
    }

    private fun readUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val query = Utils.querySortBySearch
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers!!.clear()
                if (dataSnapshot.hasChildren()) {
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(
                            User::class.java
                        )
                        assert(firebaseUser != null)
                        assert(user != null)
                        if (!user!!.id.equals(
                                firebaseUser!!.uid,
                                ignoreCase = true
                            ) && user.isActive
                        ) {
                            mUsers!!.add(user)
                        }
                    }
                    showUsers()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun showUsers() {
        if (mUsers!!.size > 0) {
            imgNoUsers!!.visibility = View.GONE
            val groupUserAdapters = GroupsUserAdapters(
                mActivity!!,
                mUsers!!,
                mSelectedUsers!!,
                mSelectedMembersId!!,
                mDeletedMembersId as MutableSet<String>,
                isEditGroup,
                groups!!,
                this
            )
            mRecyclerView!!.adapter = groupUserAdapters
            mRecyclerView!!.visibility = View.VISIBLE
        } else {
            imgNoUsers!!.visibility = View.VISIBLE
            mRecyclerView!!.visibility = View.GONE
        }
    }

    override fun setSubTitle() {
        try {
            val selectedCount = mSelectedUsers!!.size
            supportActionBar!!.subtitle = getString(R.string.strSelected) + " " + selectedCount
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_group_add, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.itemGroupSave) {
            val strGroupName = txtGroupName!!.text.toString().trim { it <= ' ' }
            if (Utils.isEmpty(strGroupName)) {
                screens!!.showToast(R.string.msgEnterGroupName)
                return true
            }
            if (mSelectedMembersId!!.size < IConstants.THREE) {
                screens!!.showToast(R.string.msgGroupMoreThanOne)
                return true
            }
            groupId = if (isEditGroup) {
                groups!!.id
            } else {
                Utils.groupUniqueId
            }
            showProgress()
            groups = Groups()
            val currentDate = Utils.dateTime
            groups!!.id = groupId
            groups!!.groupName = strGroupName
            groups!!.admin = firebaseUser!!.uid
            groups!!.members = mSelectedMembersId
            groups!!.groupImg = if (Utils.isEmpty(groupImg)) IConstants.IMG_DEFAULTS else groupImg
            groups!!.lastMsgTime = currentDate
            groups!!.createdAt = currentDate
            groups!!.lastMsg = if (Utils.isEmpty(lastMsg)) "" else lastMsg
            groups!!.type = if (Utils.isEmpty(msgType)) IConstants.TYPE_TEXT else msgType
            groups!!.isActive = IConstants.TRUE
            FirebaseDatabase.getInstance().reference.child(IConstants.REF_GROUPS_S + groupId)
                .setValue(groups).addOnCompleteListener { task: Task<Void?>? ->
                addedGroupInMembers(
                    groupId,
                    IConstants.ZERO
                )
            }
                .addOnFailureListener { e: Exception? -> hideProgress() }
            return true
        } else if (itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addedGroupInMembers(groupId: String?, index: Int) {
        if (index == mSelectedMembersId!!.size) {
            if (isEditGroup) {
                deleteMembersFromGroups(groupId, IConstants.ZERO)
            } else {
                groupAddedAndFinishScreen()
            }
        } else {
            FirebaseDatabase.getInstance().reference
                .child(IConstants.REF_GROUP_MEMBERS_S + mSelectedMembersId!!.toTypedArray()[index] + IConstants.EXTRA_GROUPS_IN_BOTH + groupId)
                .setValue(groupId)
                .addOnCompleteListener { task: Task<Void?>? ->
                    addedGroupInMembers(
                        groupId,
                        index + 1
                    )
                }
        }
    }

    private fun deleteMembersFromGroups(groupId: String?, userIndex: Int) {
        if (userIndex == mDeletedMembersId!!.size) {
            val data = Intent()
            data.putExtra(IConstants.EXTRA_OBJ_GROUP, groups)
            setResult(RESULT_OK, data)
            groupAddedAndFinishScreen()
        } else {
            FirebaseDatabase.getInstance().reference
                .child(IConstants.REF_GROUP_MEMBERS_S + mDeletedMembersId!!.toTypedArray()[userIndex] + IConstants.EXTRA_GROUPS_IN_BOTH + groupId)
                .removeValue()
                .addOnCompleteListener { task: Task<Void?>? ->
                    deleteMembersFromGroups(
                        groupId,
                        userIndex + 1
                    )
                }
        }
    }

    private fun groupAddedAndFinishScreen() {
        hideProgress()
        if (Utils.isEmpty(imageUri)) {
            finish()
        } else {
            uploadImage()
        }
    }

    private fun openImageCropper() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setFixAspectRatio(true)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(this)
    }


    private fun uploadImage() {
        val pd: ProgressDialog = ProgressDialog(mActivity)
        pd.setMessage(getString(R.string.msg_image_upload))
        pd.show()
        if (imageUri != null) {
            val fileReference: StorageReference = storageReference!!.child(
                System.currentTimeMillis().toString() + "." + mActivity?.let {
                    Utils.getExtension(
                        it,
                        imageUri
                    )
                }
            )
            val uploadTask: StorageTask<UploadTask.TaskSnapshot> = fileReference.putFile(imageUri!!)

            uploadTask.continueWithTask(
                Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }
                    fileReference.downloadUrl
                }
            ).addOnCompleteListener(OnCompleteListener { task: Task<Uri> ->
                if (task.isSuccessful) {
                    val downloadUri: Uri = task.result
                    val mUrl: String = downloadUri.toString()
                    if (!Utils.isEmpty(groupId)) {
                        reference = FirebaseDatabase.getInstance()
                            .getReference(IConstants.REF_GROUPS).child(groupId!!)
                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap.put(IConstants.EXTRA_GROUP_IMG, mUrl)
                        reference!!.updateChildren(hashMap)
                        groups?.groupImg = mUrl
                        val data: Intent = Intent()
                        data.putExtra(IConstants.EXTRA_OBJ_GROUP, groups)
                        setResult(RESULT_OK, data)
                    }
                    finish()
                } else {
                    screens!!.showToast(R.string.msgFailedToUpload)
                }
                pd.dismiss()
            }).addOnFailureListener(OnFailureListener { e: Exception ->
                Utils.getErrors(e)
                screens!!.showToast(e.message)
                pd.dismiss()
            })
        } else {
            screens!!.showToast(R.string.msgNoImageSelected)
        }
    }



    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageUri = result.uri
            }
        }
        if (!Utils.isEmpty(imageUri)) {
            imgAvatar!!.setImageURI(imageUri)
        }
    }

    override fun onBackPressed() {
        finish()
    }
}