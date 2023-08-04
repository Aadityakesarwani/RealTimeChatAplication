package com.innovativetools.firebase.chat.activities


import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Bundle
import com.innovativetools.firebase.chat.activities.R
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.innovativetools.firebase.chat.activities.constants.IConstants
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.android.material.tabs.TabLayout
import com.innovativetools.firebase.chat.activities.MainActivity.ViewPageAdapter
import com.innovativetools.firebase.chat.activities.fragments.ChatsFragment
import com.innovativetools.firebase.chat.activities.fragments.GroupsFragment
import com.innovativetools.firebase.chat.activities.fragments.ProfileFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import android.annotation.SuppressLint
import android.content.Context
import com.innovativetools.firebase.chat.activities.UsersActivity
import com.innovativetools.firebase.chat.activities.GroupsAddActivity
import android.view.animation.OvershootInterpolator
import android.os.Looper
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import androidx.fragment.app.FragmentActivity
import com.innovativetools.firebase.chat.activities.MainActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import android.text.SpannableString
import com.innovativetools.firebase.chat.activities.views.CustomTypefaceSpan
import android.text.Spannable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdRequest
import com.innovativetools.firebase.chat.activities.managers.Utils
import com.innovativetools.firebase.chat.activities.models.User
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.util.ArrayList

class MainActivity : BaseActivity() {
    private var mImageView: CircleImageView? = null
    private var mTxtUsername: TextView? = null
    private var mViewPager: ViewPager2? = null
    private var exitTime: Long = 0
    private var fabMain: FloatingActionButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mImageView = findViewById(R.id.imageView)
        mTxtUsername = findViewById(R.id.txtUsername)
        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setTitle("")
        val adView = findViewById<AdView>(R.id.adView)
        if (BuildConfig.ADS_SHOWN) {
            adView.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } else {
            adView.visibility = View.GONE
        }
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(
            firebaseUser!!.uid
        )
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if (dataSnapshot.hasChildren()) {
                        val user = dataSnapshot.getValue(
                            User::class.java
                        )!!
                        mTxtUsername?.setText(user.username)
                        if (user.genders == IConstants.GEN_UNSPECIFIED) {
                            Utils.selectGenderPopup(
                                mActivity!!,
                                firebaseUser!!.uid,
                                IConstants.GEN_UNSPECIFIED
                            )
                        }
                        user.myImg?.let {
                            Utils.setProfileImage(
                                applicationContext, it, mImageView
                            )
                        }
                    }
                } catch (ignored: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        mImageView?.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                mViewPager!!.currentItem = 2
            }
        })
        val mTabLayout = findViewById<TabLayout>(R.id.tabLayout)
        mViewPager = findViewById(R.id.viewPager)
        fabMain = findViewById(R.id.fabMain)
        val viewPageAdapter = ViewPageAdapter(this)
        viewPageAdapter.addFragment(ChatsFragment(), getString(R.string.strChats))
        viewPageAdapter.addFragment(GroupsFragment(), getString(R.string.strGroups))
        viewPageAdapter.addFragment(ProfileFragment(), getString(R.string.strProfile))
        mViewPager?.setAdapter(viewPageAdapter)
        TabLayoutMediator(
            mTabLayout,
            mViewPager!!,
            TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = viewPageAdapter.getTitle(position)
            }).attach()
        mViewPager?.setOffscreenPageLimit(viewPageAdapter.itemCount - 1)
        mTabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            @SuppressLint("RestrictedApi")
            override fun onTabSelected(tab: TabLayout.Tab) {
                fabMain?.setVisibility(View.VISIBLE)
                if (tab.position == IConstants.ZERO) {
                    fabMain?.setImageResource(R.drawable.ic_chat)
                    rotateFabForward()
                } else if (tab.position == IConstants.ONE) {
                    fabMain?.setImageResource(R.drawable.ic_group_add)
                    rotateFabForward()
                } else {
                    fabMain?.setVisibility(View.GONE)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        fabMain?.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                if (mViewPager?.getCurrentItem() == IConstants.ZERO) {
                    screens!!.showCustomScreen(UsersActivity::class.java)
                } else if (mViewPager?.getCurrentItem() == IConstants.ONE) {
                    screens!!.showCustomScreen(GroupsAddActivity::class.java)
                }
            }
        })
    }

    fun rotateFabForward() {
        ViewCompat.animate(fabMain!!)
            .rotation(5.0f)
            .withLayer()
            .setDuration(300L)
            .setInterpolator(OvershootInterpolator(10.0f))
            .start()
        val handler = Handler(Looper.getMainLooper())
        //Write whatever to want to do after delay specified (1 sec)
        handler.postDelayed({ rotateFabBackward() }, 200)
    }

    fun rotateFabBackward() {
        ViewCompat.animate(fabMain!!)
            .rotation(0.0f)
            .withLayer()
            .setDuration(300L)
            .setInterpolator(OvershootInterpolator(10.0f))
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    internal class ViewPageAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        val fragments: ArrayList<Fragment>
        val titles: ArrayList<String>

        init {
            fragments = ArrayList()
            titles = ArrayList()
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }

        override fun getItemCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        fun getTitle(index: Int): String {
            return titles[index]
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        applyFontToMenu(menu, this)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.itemSettings) {
            screens!!.openSettingsActivity()
            return true
        } else if (itemId == R.id.itemLogout) {
            Utils.logout(mActivity!!)
            return true
        }
        return true
    }

    override fun onBackPressed() {
        exitApp()
    }

    private fun exitApp() {
        try {
            if (mViewPager!!.currentItem == IConstants.ZERO) {
                val DEFAULT_DELAY = 2000
                if (System.currentTimeMillis() - exitTime > DEFAULT_DELAY) {
                    try {
                        val mainRootLayout = findViewById<CoordinatorLayout>(R.id.mainRootLayout)
                        val snackbar = Snackbar.make(
                            mainRootLayout,
                            getString(R.string.press_again_to_exit),
                            Snackbar.LENGTH_LONG
                        )
                        val sbView = snackbar.view
                        val textView = sbView.findViewById<TextView>(R.id.snackbar_text)
                        textView.typeface = Utils.getRegularFont(mActivity)
                        textView.setTextColor(Color.YELLOW)
                        snackbar.show()
                    } catch (e: Exception) {
                        screens!!.showToast(R.string.press_again_to_exit)
                    }
                    exitTime = System.currentTimeMillis()
                } else {
                    finish()
                }
            } else {
                mViewPager!!.currentItem = IConstants.ZERO
            }
        } catch (e: Exception) {
            Utils.getErrors(e)
        }
    }

    override fun onStart() {
        super.onStart()
        Utils.readStatus(IConstants.STATUS_ONLINE)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun applyFontToMenu(m: Menu, mContext: Context?) {
            for (i in 0 until m.size()) {
                applyFontToMenuItem(m.getItem(i), mContext)
            }
        }

        fun applyFontToMenuItem(mi: MenuItem, mContext: Context?) {
            val mNewTitle = SpannableString(mi.title)
            mNewTitle.setSpan(
                Utils.getRegularFont(mContext)?.let { CustomTypefaceSpan("", it) },
                IConstants.ZERO,
                mNewTitle.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            mi.title = mNewTitle
        }
    }
}