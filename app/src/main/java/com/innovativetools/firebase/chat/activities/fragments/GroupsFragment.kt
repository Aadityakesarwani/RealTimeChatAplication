package com.innovativetools.firebase.chat.activities.fragments

import android.widget.RelativeLayout
import com.innovativetools.firebase.chat.activities.adapters.GroupsAdapters
import android.os.Bundle
import android.view.*
import com.innovativetools.firebase.chat.activities.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.google.firebase.database.*
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.Groups
import java.lang.Exception
import java.util.ArrayList
import java.util.HashMap

class GroupsFragment : BaseFragment() {
    private var groupList: ArrayList<String?>? = null
    private var imgNoMessage: RelativeLayout? = null
    private var groupsAdapters: GroupsAdapters? = null
    private var mGroups: ArrayList<Groups>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_groups, container, false)
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
        groupList = ArrayList()
        assert(firebaseUser != null)
        val query: Query = FirebaseDatabase.getInstance()
            .getReference(IConstants.REF_GROUP_MEMBERS_S + firebaseUser!!.uid + IConstants.EXTRA_GROUPS_IN_BOTH)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                groupList!!.clear()
                if (dataSnapshot.hasChildren()) {
                    for (snapshot in dataSnapshot.children) {
                        val strGroupId = snapshot.getValue(String::class.java)
                        groupList!!.add(strGroupId)
                    }
                }
                if (groupList!!.size > 0) {
                    imgNoMessage?.setVisibility(View.GONE)
                    readGroups()
                    mRecyclerView?.setVisibility(View.VISIBLE)
                } else {
                    imgNoMessage?.setVisibility(View.VISIBLE)
                    mRecyclerView?.setVisibility(View.GONE)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        return view
    }

    private fun readGroups() {
        mGroups = ArrayList()
        val reference: Query = FirebaseDatabase.getInstance().getReference(IConstants.REF_GROUPS)
        reference.keepSynced(true)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mGroups!!.clear()
                if (dataSnapshot.hasChildren()) {
                    var uList: MutableMap<String?, Groups?> = HashMap()
                    try {
                        for (id in groupList!!) {
                            for (snapshot in dataSnapshot.children) {
                                val groups = snapshot.getValue(
                                    Groups::class.java
                                )!!
                                if (!Utils.isEmpty(
                                        groups.id
                                    )
                                ) {
                                    if (groups.id.equals(
                                            id,
                                            ignoreCase = true
                                        ) && groups.isActive
                                    ) {
                                        uList[groups.id] = groups
                                        break
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Utils.getErrors(e)
                    }
                    if (uList.size > 0) {
                        uList = Utils.sortByGroupDateTime(uList, false) as MutableMap<String?, Groups?>
                        mGroups!!.addAll(uList.values as MutableCollection<Groups>)
                    }
                    groupsAdapters = GroupsAdapters(context!!, mGroups!!)
                    mRecyclerView!!.adapter = groupsAdapters
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }
}