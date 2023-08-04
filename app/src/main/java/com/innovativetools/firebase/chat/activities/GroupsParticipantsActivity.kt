package com.innovativetools.firebase.chat.activities


import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.innovativetools.firebase.chat.activities.adapters.GroupsParticipantsAdapters
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.innovativetools.firebase.chat.activities.views.profileview.HeaderView
import com.google.firebase.storage.FirebaseStorage
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import android.content.Intent
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.google.firebase.auth.FirebaseAuth
import android.widget.RelativeLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.cardview.widget.CardView
import com.innovativetools.firebase.chat.activities.constants.ISendMessage
import com.innovativetools.firebase.chat.activities.constants.IDialogListener
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.innovativetools.firebase.chat.activities.models.Chat
import com.google.firebase.database.DatabaseError
import android.app.Activity
import android.renderscript.ScriptGroup.Binding
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.Task
import com.innovativetools.firebase.chat.activities.GroupsAddActivity
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.Groups
import com.innovativetools.firebase.chat.activities.models.User
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.lang.Exception
import java.util.ArrayList

class GroupsParticipantsActivity : BaseActivity(), OnOffsetChangedListener {
    private var mUsers: ArrayList<User>? = null
    private var userAdapters: GroupsParticipantsAdapters? = null
    private var mRecyclerView: RecyclerView? = null
    private var mineUser = User()
    private var groupId: String? = null
    private var groups: Groups? = null
    private var groupName = ""
    private var lblParticipants: TextView? = null
    private var imgGroupBackground: ImageView? = null
    private var toolbarHeaderView: HeaderView? = null
    private var floatHeaderView: HeaderView? = null
    private var isHideToolbarView = false
    private var storage: FirebaseStorage? = null
    private var setting = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_participants)
        toolbarHeaderView = findViewById(R.id.toolbar_header_view)
        floatHeaderView = findViewById(R.id.float_header_view)
        val appBarLayout = findViewById<AppBarLayout>(R.id.appbar)
        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle("")
        mToolbar.setNavigationOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                onBackPressed()
            }
        })
        lblParticipants = findViewById(R.id.lblParticipants)
        val txtExitGroup = findViewById<TextView>(R.id.txtExitGroup)
        val intent = intent
        groups = intent.getSerializableExtra(IConstants.EXTRA_OBJ_GROUP) as Groups?
        groupName = groups!!.groupName.toString()
        groupId = groups!!.id
        storage = FirebaseStorage.getInstance()
        //final StorageReference storageReference = storage.getReference(REF_GROUP_PHOTO_UPLOAD + SLASH + groupId);
        firebaseUser = FirebaseAuth.getInstance().currentUser
        imgGroupBackground = findViewById(R.id.imgGroupBackground)
        groups!!.groupImg?.let { Utils.setGroupParticipateImage(mActivity, it, imgGroupBackground) }
        imgGroupBackground?.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                //final Screens screens = new Screens(mActivity);
                screens!!.openFullImageViewActivity(v, groups!!.groupImg, groupName, "")
            }
        })
        val imgNoMessage = findViewById<RelativeLayout>(R.id.imgNoMessage)
        imgNoMessage.visibility = View.GONE
        mRecyclerView = findViewById(R.id.recyclerView)
        val layoutGroupAdminSetting = findViewById<LinearLayout>(R.id.layoutGroupAdminSetting)
        val lblSettingOption = findViewById<TextView>(R.id.lblSettingOption)
        val layoutManager = LinearLayoutManager(mActivity)
        val dividerItemDecoration =
            DividerItemDecoration(mRecyclerView?.getContext(), layoutManager.orientation)
        mRecyclerView?.setHasFixedSize(true)
        mRecyclerView?.setLayoutManager(layoutManager)
        mRecyclerView?.setNestedScrollingEnabled(false)
        mRecyclerView?.addItemDecoration(dividerItemDecoration)
        appBarLayout.addOnOffsetChangedListener(this)
        readGroupTitle()

        //Admin Message
        val cardView = findViewById<CardView>(R.id.cardViewSendMessage)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (groups!!.admin.equals(firebaseUser!!.uid, ignoreCase = true)) {
            cardView.visibility = View.VISIBLE
        } else {
            cardView.visibility = View.GONE
        }
        setting = groups!!.sendMessageSetting
        lblSettingOption.text = Utils.getSettingString(mActivity!!, setting)
        layoutGroupAdminSetting.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                Utils.selectSendMessages(mActivity!!, groupId, setting) { value: String? ->
                    lblSettingOption.text = value
                    setting = Utils.getSettingValue(mActivity!!, value)
                    groups!!.sendMessageSetting = setting
                }
            }
        })
        txtExitGroup.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                Utils.showYesNoDialog(mActivity!!, R.string.strLeave, R.string.strLeaveFromGroup) {
                    showProgress()
                    if (isAdmin) {
                        if (groups!!.members?.size!!  >= IConstants.TWO) { //Make other Person to Admin for this group cause more than 2 person available.
                            var newAdminId =
                                groups!!.members?.get(1) //Default set from 1st position to Make as Admin.
                            for (i in groups!!.members?.indices!!) {
                                if (!groups!!.members?.get(i)?.equals(
                                        firebaseUser!!.uid,
                                        ignoreCase = true
                                    )!!
                                ) {
                                    newAdminId = groups?.members!![i]//Assign Admin Role to next USER.
                                    break
                                }
                            }
                            groups!!.admin = newAdminId
                            groups!!.members?.remove(firebaseUser!!.uid)
                            leaveFromGroup(IConstants.TRUE) //True means close current screen, cause first we leave from group and than delete own chats
                        } else { //You are alone in this Groups. So Delete group and its DATA.
                            deleteWholeGroupsData() // In this case only groups have Single User and can delete whole groups data.
                        }
                    } else {
                        val removeId = groups!!.members
                        removeId?.remove(firebaseUser!!.uid)
                        groups!!.members = removeId
                        leaveFromGroup(IConstants.TRUE) //True means close current screen, cause first we leave from group and than delete own chats
                    }
                }
            }
        })
    }

    private fun leaveFromGroup(isFinishActivity: Boolean) {
        //Remove from Main Group info /groupId/members/<removeId>
        FirebaseDatabase.getInstance().reference.child(IConstants.REF_GROUPS_S + groupId)
            .setValue(groups).addOnCompleteListener { task: Task<Void?>? ->

            //Remove from MembersGroup/groupsIn/<groupId>
            FirebaseDatabase.getInstance().reference.child(IConstants.REF_GROUP_MEMBERS_S + firebaseUser!!.uid + IConstants.EXTRA_GROUPS_IN_BOTH + groupId)
                .removeValue()
                .addOnCompleteListener { task1: Task<Void?>? ->
                    deleteOwnChats(isFinishActivity) //True means close current screen, cause first we leave from group and than delete own chats
                }
        }.addOnFailureListener { e: Exception? -> hideProgress() }
    }

    /**
     * False means don't close current screen, just delete my own chats
     * True  means close current screen, cause first we leave from group and than delete own chats
     */
    private fun deleteOwnChats(isFinishActivity: Boolean) {
        FirebaseDatabase.getInstance().reference.child(IConstants.REF_GROUPS_MESSAGES + IConstants.SLASH + groupId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            for (snapshot in dataSnapshot.children) {
                                val chat = snapshot.getValue(Chat::class.java)!!
                                if (chat.sender.equals(firebaseUser!!.uid, ignoreCase = true)) {
                                    storage?.let { Utils.deleteUploadedFilesFromCloud(it, chat) }
                                    snapshot.ref.removeValue()
                                }
                            }
                        }
                    } catch (ignored: Exception) {
                    }
                    hideProgress()
                    if (isFinishActivity) {
                        goBack()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun goBack() {
        val data = Intent()
        data.putExtra(IConstants.EXTRA_OBJ_GROUP, groups)
        setResult(RESULT_FIRST_USER, data)
        finish()
    }

    private fun deleteWholeGroupsData() {
        val members = groups!!.members?.size!!
        FirebaseDatabase.getInstance().reference.child(IConstants.REF_GROUPS_S + groupId)
            .removeValue().addOnCompleteListener { task: Task<Void?>? ->
            for (i in 0 until members) {
                FirebaseDatabase.getInstance().reference.child(IConstants.REF_GROUP_MEMBERS_S + (groups!!.members?.get(i)) + IConstants.EXTRA_GROUPS_IN_BOTH + groupId)
                    .removeValue().addOnCompleteListener { task12: Task<Void?>? -> }
                if (i == members - 1) {
                    hideProgress()
                    goBack()
                }
            }
            FirebaseDatabase.getInstance().reference.child(IConstants.REF_GROUPS_MESSAGES + IConstants.SLASH + groupId)
                .removeValue().addOnCompleteListener { task1: Task<Void?>? -> }
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        val maxScroll = appBarLayout.totalScrollRange
        val percentage = Math.abs(offset).toFloat() / maxScroll.toFloat()
        if (percentage == 1f && isHideToolbarView) {
            toolbarHeaderView!!.visibility = View.VISIBLE
            isHideToolbarView = !isHideToolbarView
        } else if (percentage < 1f && !isHideToolbarView) {
            toolbarHeaderView!!.visibility = View.GONE
            isHideToolbarView = !isHideToolbarView
        }
    }

    private fun readUsers() {
        mUsers = ArrayList()
        mineUser = User()
        val reference = Utils.querySortBySearch
        reference.keepSynced(true)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers!!.clear()
                if (dataSnapshot.hasChildren()) {
                    try {
                        for (id in groups!!.members!!) {
                            for (snapshot in dataSnapshot.children) {
                                val user = snapshot.getValue(
                                    User::class.java
                                )!!
                                if (user.id.equals(id, ignoreCase = true) && user.isActive) {
                                    if (groups!!.admin.equals(user.id, ignoreCase = true)) {
                                        user.isAdmin = IConstants.TRUE
                                    } else {
                                        user.isAdmin = IConstants.FALSE
                                    }
                                    if (!user.id.equals(firebaseUser!!.uid, ignoreCase = true)) {
                                        mUsers!!.add(user)
                                    } else {
                                        user.username = getString(R.string.strYou)
                                        mineUser = user
                                    }
                                    break
                                }
                            }
                        }
                    } catch (ignored: Exception) {
                    }
                    if (mUsers!!.size > 0) {
                        mUsers = Utils.sortByUser(mUsers!!)
                        //                        if (mineUser.isAdmin()) {
//                            mUsers.add(0, mineUser);
//                        } else {
                        (mUsers as ArrayList<User>?)?.add(mineUser)
                        //                        }
                    }
                    userAdapters = GroupsParticipantsAdapters(mActivity!!, mUsers)
                    mRecyclerView!!.adapter = userAdapters
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_group_add, menu)
        val item = menu.findItem(R.id.itemGroupSave)
        item.setIcon(R.drawable.ic_group_add)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemGroupSave) {
            if (isAdmin) {
                val intent = Intent(mActivity, GroupsAddActivity::class.java)
                intent.putExtra(IConstants.EXTRA_GROUP_ID, groupId)
                intent.putExtra(IConstants.EXTRA_OBJ_GROUP, groups)
                intentLauncher.launch(intent)
            } else {
                screens!!.showToast(R.string.msgOnlyAdminEdit)
            }
        }
        return true
    }

    private fun readGroupTitle() {
        val lastSeen = String.format(
            getString(R.string.strCreatedOn),
            Utils.formatDateTime(mActivity, groups!!.lastMsgTime)
        )
        groupName = groups!!.groupName.toString()
        toolbarHeaderView!!.bindTo(groupName, lastSeen)
        floatHeaderView!!.bindTo(groupName, lastSeen)
        lblParticipants!!.text =
            groups!!.members?.let { String.format(getString(R.string.strParticipants), it.size) }
        groups!!.groupImg?.let { Utils.setGroupParticipateImage(mActivity, it, imgGroupBackground) }
        readUsers()
    }

    /*
     * Intent launcher to get Image Uri from storage
     * */
    val intentLauncher =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                groups = data!!.getSerializableExtra(IConstants.EXTRA_OBJ_GROUP) as Groups?
                readGroupTitle()
            }
        }
    private val isAdmin: Boolean
        private get() = if (groups!!.admin.equals(firebaseUser!!.uid, ignoreCase = true)) {
            IConstants.TRUE
        } else IConstants.FALSE
}