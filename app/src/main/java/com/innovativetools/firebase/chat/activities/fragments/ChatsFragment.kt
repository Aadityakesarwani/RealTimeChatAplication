package com.innovativetools.firebase.chat.activities.fragments

import android.widget.RelativeLayout
import android.os.Bundle
import android.view.*
import com.innovativetools.firebase.chat.activities.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.google.firebase.messaging.FirebaseMessaging
import com.innovativetools.firebase.chat.activities.models.Chat
import com.innovativetools.firebase.chat.activities.adapters.UserAdapters
import com.google.firebase.database.*
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.User
import java.lang.Exception
import java.util.*

class ChatsFragment : BaseFragment() {
    private var userList: ArrayList<String?>? = null
    private var imgNoMessage: RelativeLayout? = null
    private var currentId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

//        final FloatingActionButton fabChat = view.findViewById(R.id.fabChat);
        imgNoMessage = view.findViewById(R.id.imgNoMessage)
        imgNoMessage?.setVisibility(View.GONE)
        val layoutManager = LinearLayoutManager(activity)
        mRecyclerView = view.findViewById(R.id.recyclerView)
        mRecyclerView?.setHasFixedSize(true)
        mRecyclerView?.setLayoutManager(layoutManager)
        val dividerItemDecoration =
            DividerItemDecoration(mRecyclerView?.getContext(), layoutManager.orientation)
        mRecyclerView?.addItemDecoration(dividerItemDecoration)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        currentId = Objects.requireNonNull(firebaseUser)?.uid
        userList = ArrayList()
        val query: Query = FirebaseDatabase.getInstance().getReference(IConstants.REF_CHATS).child(
            currentId!!
        )
        //        query.addListenerForSingleValueEvent(new ValueEventListener() {
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList!!.clear()
                uList.clear()
                if (dataSnapshot.hasChildren()) {
                    for (snapshot in dataSnapshot.children) {
                        userList!!.add(snapshot.key)
                    }
                }
                if (userList!!.size > 0) {
                    sortChats()
                } else {
                    imgNoMessage?.setVisibility(View.VISIBLE)
                    mRecyclerView?.setVisibility(View.GONE)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        FirebaseMessaging.getInstance().token.addOnSuccessListener { referenceToken: String? ->
            Utils.uploadToken(
                referenceToken
            )
        }
        return view
    }

    var uList: MutableMap<String?, Chat?> = HashMap()
    private fun sortChats() {
        for (i in userList!!.indices) {
            val key = userList!![i]
            val query = FirebaseDatabase.getInstance().getReference(IConstants.REF_CHATS)
                .child(currentId + IConstants.SLASH + key).limitToLast(1)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (snapshot in dataSnapshot.children) {
                            val chat = snapshot.getValue(Chat::class.java)
                            uList[key] = chat
                        }
                    }
                    if (uList.size == userList!!.size) {
                        if (uList.size > 0) {
                            uList = Utils.sortByChatDateTime(uList, false) as MutableMap<String?, Chat?>
                        }
                        userList = ArrayList<String?>(uList.keys)
                        readChats()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    private fun readChats() {
        mUsers = ArrayList()
        val reference = Utils.querySortBySearch
        reference.keepSynced(true)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers!!.clear()
                if (dataSnapshot.hasChildren()) {
                    try {
                        for (id in userList!!) {
                            for (snapshot in dataSnapshot.children) {
                                val user = snapshot.getValue(
                                    User::class.java
                                )!!
                                if (user.id.equals(id, ignoreCase = true) && user.isActive) {
                                    onlineOptionFilter(user)
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        //Utils.getErrors(e);
                    }
                }
                if (mUsers!!.size > 0) {
                    imgNoMessage!!.visibility = View.GONE
                    mRecyclerView!!.visibility = View.VISIBLE
                    userAdapters = UserAdapters(context!!, mUsers, IConstants.TRUE)
                    mRecyclerView!!.adapter = userAdapters
                } else {
                    imgNoMessage!!.visibility = View.VISIBLE
                    mRecyclerView!!.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun onlineOptionFilter(user: User?) {
        try {
            if (user!!.isOnline == IConstants.STATUS_ONLINE) {
                if (Utils.online) profileOptionFilter(user)
            } else if (user.isOnline == IConstants.STATUS_OFFLINE) {
                if (Utils.offline) profileOptionFilter(user)
            } else {
                profileOptionFilter(user)
            }
        } catch (ignored: Exception) {
        }
    }

    private fun profileOptionFilter(user: User?) {
        try {
            if (!user!!.getImageURL().equals(IConstants.IMG_DEFAULTS, ignoreCase = true)) {
                if (Utils.withPicture) levelOptionFilter(user)
            } else if (user.getImageURL().equals(IConstants.IMG_DEFAULTS, ignoreCase = true)) {
                if (Utils.withoutPicture) levelOptionFilter(user)
            } else {
                levelOptionFilter(user)
            }
        } catch (ignored: Exception) {
        }
    }

    private fun levelOptionFilter(user: User?) {
        try {
            if (user!!.genders == IConstants.GEN_UNSPECIFIED) {
                if (Utils.notset) addNewUserDataToList(user)
            } else {
                if (user.genders == IConstants.GEN_MALE) {
                    if (Utils.male) addNewUserDataToList(user)
                } else if (user.genders == IConstants.GEN_FEMALE) {
                    if (Utils.female) addNewUserDataToList(user)
                }
            }
        } catch (e: Exception) {
            addNewUserDataToList(user)
        }
    }

    private fun addNewUserDataToList(user: User?) {
        mUsers!!.add(user!!)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filter, menu)
        val searchItem = menu.findItem(R.id.itemFilter)
        searchItem.setOnMenuItemClickListener { item: MenuItem? ->
            Utils.filterPopup(
                mActivity!!
            ) { readChats() }
            true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
}