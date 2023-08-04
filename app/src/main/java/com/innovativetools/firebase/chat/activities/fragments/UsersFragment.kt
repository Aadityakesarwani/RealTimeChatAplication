package com.innovativetools.firebase.chat.activities.fragments

import com.innovativetools.firebase.chat.activities.fragments.BaseFragment
import android.widget.RelativeLayout
import android.os.Bundle
import com.innovativetools.firebase.chat.activities.R
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import android.text.TextWatcher
import android.text.Editable
import android.view.*
import com.innovativetools.firebase.chat.activities.adapters.UserAdapters
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import com.innovativetools.firebase.chat.activities.constants.IFilterListener
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.User
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import java.lang.Exception
import java.util.*

class UsersFragment : BaseFragment() {
    private var txtSearch: AppCompatEditText? = null
    private var imgClear: ImageView? = null
    private var imgNoUsers: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_users, container, false)
        imgNoUsers = view.findViewById(R.id.imgNoUsers)
        imgNoUsers?.setVisibility(View.GONE)
        imgClear = view.findViewById(R.id.imgClear)
        txtSearch = view.findViewById(R.id.txtSearch)
        mRecyclerView = view.findViewById(R.id.recyclerView)
        mRecyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        mRecyclerView?.setLayoutManager(layoutManager)
        val dividerItemDecoration =
            DividerItemDecoration(mRecyclerView?.getContext(), layoutManager.orientation)
        mRecyclerView?.addItemDecoration(dividerItemDecoration)
        mUsers = ArrayList()
        readUsers()
        txtSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchUsers(s.toString().lowercase(Locale.getDefault()))
                if (count > 0) {
                    imgClear?.setVisibility(View.VISIBLE)
                } else {
                    imgClear?.setVisibility(View.GONE)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        imgClear?.setVisibility(View.GONE)
        imgClear?.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                txtSearch?.setText("")
                txtSearch?.requestFocus()
            }
        })
        return view
    }

    private fun showUsers() {
        if (mUsers!!.size > 0) {
            imgNoUsers!!.visibility = View.GONE
            userAdapters = UserAdapters(requireContext(), mUsers, IConstants.FALSE)
            mRecyclerView!!.adapter = userAdapters
            mRecyclerView!!.visibility = View.VISIBLE
        } else {
            imgNoUsers!!.visibility = View.VISIBLE
            mRecyclerView!!.visibility = View.GONE
        }
    }

    private fun searchUsers(search: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val query = Utils.querySortBySearch
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers!!.clear()
                if (dataSnapshot.hasChildren()) {
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(
                            User::class.java
                        )!!
                        assert(firebaseUser != null)
                        if (!user.id.equals(
                                firebaseUser!!.uid,
                                ignoreCase = true
                            ) && user.isActive
                        ) {
                            if (user.search?.contains(search) == true) {
                                onlineOptionFilter(user)
                            }
                        }
                    }
                }
                showUsers()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun readUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        //        Query query = FirebaseDatabase.getInstance().getReference(REF_USERS).orderByChild(EXTRA_SEARCH).startAt(search).endAt(search + "\uf8ff");
        val query = Utils.querySortBySearch
        query.keepSynced(true)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers!!.clear()
                if (dataSnapshot.hasChildren()) {
                    if (txtSearch!!.text.toString().trim { it <= ' ' }
                            .equals("", ignoreCase = true)) {
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
                                onlineOptionFilter(user)
                            }
                        }
                    }
                }
                showUsers()
                try {
                    val searchHint =
                        String.format(getString(R.string.strSearchWithCount), mUsers!!.size)
                    txtSearch!!.hint = searchHint
                } catch (ignored: Exception) {
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
                requireActivity()
            ) { readUsers() }
            true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
}