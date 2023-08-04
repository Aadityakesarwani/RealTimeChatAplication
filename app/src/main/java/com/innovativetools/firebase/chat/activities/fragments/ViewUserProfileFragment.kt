package com.innovativetools.firebase.chat.activities.fragments

import com.innovativetools.firebase.chat.activities.constants.IConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.innovativetools.firebase.chat.activities.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.view.View
import android.widget.ImageView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.User
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import de.hdodenhof.circleimageview.CircleImageView

class ViewUserProfileFragment : BaseFragment() {
    private var imgAvatar: CircleImageView? = null
    private var imgBlurImage: ImageView? = null
    private var strDescription = ""
    private var strAvatarImg: String? = null
    private var strUsername = ""
    private var viewUserId: String? = ""
    private var strGender = IConstants.GEN_UNSPECIFIED
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_new, container, false)
        val fabChat = view.findViewById<FloatingActionButton>(R.id.fabChat)
        imgAvatar = view.findViewById(R.id.imgAvatar)
        imgBlurImage = view.findViewById(R.id.imgRelativeBlue)
        val txtUsername = view.findViewById<TextView>(R.id.txtUsername)
        val txtEmail = view.findViewById<TextView>(R.id.txtEmail)
        val txtAbout = view.findViewById<TextView>(R.id.txtAbout)
        val txtGender = view.findViewById<TextView>(R.id.txtGender)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val i = requireActivity().intent
        val userId = i.getStringExtra(IConstants.EXTRA_USER_ID)
        viewUserId = if (Utils.isEmpty(userId)) {
            assert(firebaseUser != null)
            firebaseUser!!.uid
        } else {
            userId
        }
        assert(firebaseUser != null)
        if (viewUserId.equals(firebaseUser!!.uid, ignoreCase = true)) {
            fabChat.hide()
        } else {
            fabChat.show()
        }
        imgAvatar?.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                screens!!.openFullImageViewActivity(v, strAvatarImg, strUsername)
            }
        })
        val lblStatus = mActivity!!.getString(R.string.strAboutStatus)
        val lblUnSpecified = getString(R.string.strUnspecified)
        val lblMale = getString(R.string.strMale)
        val lblFemale = getString(R.string.strFemale)
        val msgPrivateEmail = mActivity!!.getString(R.string.msgPrivateEmail)
        val reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(
            viewUserId!!
        )
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    val user = dataSnapshot.getValue(
                        User::class.java
                    )!!
                    strUsername = user.username.toString()
                    txtUsername.text = strUsername
                    var email = user.email
                    strAvatarImg = user.getImageURL()
                    if (!viewUserId.equals(firebaseUser.uid, ignoreCase = true)) {
                        if (user.isHideEmail) {
                            email = msgPrivateEmail
                        }
                        if (user.isHideProfilePhoto) {
                            strAvatarImg = IConstants.IMG_PREVIEW
                        }
                    }
                    strGender = user.genders
                    strDescription = user.about.toString()
                    txtEmail.text = email
                    txtAbout.text = if (Utils.isEmpty(strDescription)) lblStatus else strDescription
                    txtGender.text =
                        if (strGender == IConstants.GEN_UNSPECIFIED) lblUnSpecified else if (strGender == IConstants.GEN_MALE) lblMale else lblFemale
                    strAvatarImg?.let {
                        Utils.setProfileImage(
                            context, it, imgAvatar
                        )
                    }
                    strAvatarImg?.let {
                        Utils.setProfileBlurImage(
                            context, it, imgBlurImage
                        )
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        fabChat.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                screens!!.openUserMessageActivity(viewUserId)
            }
        })
        return view
    }
}