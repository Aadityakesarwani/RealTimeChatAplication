package com.innovativetools.firebase.chat.activities.managers


import android.annotation.SuppressLint
import com.innovativetools.firebase.chat.activities.models.AttachmentTypes.getTypeName
import com.innovativetools.firebase.chat.activities.models.AttachmentTypes.getDirectoryByType
import com.innovativetools.firebase.chat.activities.models.AttachmentTypes.getExtension
import com.innovativetools.firebase.chat.activities.models.AttachmentTypes.getTargetUri
import com.innovativetools.firebase.chat.activities.constants.IConstants
import android.text.TextUtils
import kotlin.Throws
import android.text.format.DateUtils
import com.bumptech.glide.Glide
import com.innovativetools.firebase.chat.activities.R
import com.bumptech.glide.load.engine.DiskCacheStrategy
import jp.wasabeef.glide.transformations.BlurTransformation
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.jvm.JvmOverloads
import android.app.Activity
import android.content.res.Resources.NotFoundException
import android.os.Build.VERSION
import com.google.firebase.storage.FirebaseStorage
import androidx.cardview.widget.CardView
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import com.innovativetools.firebase.chat.activities.constants.IDialogListener
import com.innovativetools.firebase.chat.activities.constants.IFilterListener
import android.media.MediaPlayer
import com.innovativetools.firebase.chat.activities.views.customimage.ColorGenerator
import android.webkit.MimeTypeMap
import com.innovativetools.firebase.chat.activities.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import android.content.pm.PackageManager
import android.database.Cursor
import com.innovativetools.firebase.chat.activities.constants.ISendMessage
import android.text.Html
import android.provider.MediaStore
import android.media.MediaMetadataRetriever
import android.provider.ContactsContract
import android.location.LocationManager
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.text.format.Time
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.Query
import com.innovativetools.firebase.chat.activities.BuildConfig
import com.innovativetools.firebase.chat.activities.fcmmodels.Token
import com.innovativetools.firebase.chat.activities.models.*
import com.innovativetools.firebase.chat.activities.views.SingleClickListener
import com.innovativetools.firebase.chat.activities.views.files.FileUtils
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object Utils {

    const val IS_TRIAL = false
    private const val DEFAULT_VIBRATE = 500
    var online = true
    var offline = true
    var male = true
    var female = true
    var notset = true
    var withPicture = true
    var withoutPicture = true
    private var strSelectedGender = IConstants.GEN_UNSPECIFIED
    private var settingIndex = IConstants.SETTING_ALL_PARTICIPANTS
    const val ONE_MB = 1024
    var MAX_SIZE_AUDIO = 10 // 10 MB Maximum
    var MAX_SIZE_VIDEO = 15 // 15 MB Maximum
    var MAX_SIZE_DOCUMENT = 5 // 5 MB Maximum
    const val DEF_TEXT = "Please update your app to get attachment options and many new features."
    var UPDATE_TEXT = ""
    val defaultMessage: String
        get() = if (isEmpty(UPDATE_TEXT)) {
            DEF_TEXT
        } else {
            UPDATE_TEXT
        }
    val audioSizeLimit: Int
        get() = MAX_SIZE_AUDIO * ONE_MB
    val videoSizeLimit: Int
        get() = MAX_SIZE_VIDEO * ONE_MB
    val documentSizeLimit: Int
        get() = MAX_SIZE_DOCUMENT * ONE_MB

    @JvmStatic
    fun sout(msg: String) {
        if (IS_TRIAL) {
            println("Pra :: $msg")
        }
    }

    @JvmStatic
    fun isEmpty(s: Any?): Boolean {
        if (s == null) {
            return true
        }
        if (s is String && s.trim { it <= ' ' }.length == 0) {
            return true
        }
        if (s is Map<*, *>) {
            return s.isEmpty()
        }
        if (s is List<*>) {
            return s.isEmpty()
        }
        return if (s is Array<*>) {
            (s as Array<*>).size == 0
        } else false
    }

    @JvmStatic
    fun getErrors(e: Exception?) {
        if (IS_TRIAL) {
            val stackTrace = "Pra ::" + Log.getStackTraceString(e)
            println(stackTrace)
        }
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    val dateTime: String
        @SuppressLint("SimpleDateFormat")
        get() {
            val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = Date()
            return dateFormat.format(date)
        }

    //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    val dateTimeStampName: String
        get() {
            val dateFormat: DateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            val date = Date()
            return dateFormat.format(date)
        }

    fun getCapsWord(name: String?): String {
        val sb = name?.let { StringBuilder(it) }
        sb?.get(0)?.let { sb.setCharAt(0, it.uppercaseChar()) }
        return sb.toString()
    }

    /**
     * Gets timestamp in millis and converts it to HH:mm (e.g. 16:44).
     */
    fun formatDateTime(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(timeInMillis)
    }

    /**
     * Gets timestamp in millis and converts it to HH:mm (e.g. 16:44).
     */
    fun formatTime(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(timeInMillis)
    }

    /**
     * Gets timestamp in millis and converts it to HH:mm (e.g. 16:44).
     */
    fun formatLocalTime(timeInMillis: Long): String {
        val dateFormatUTC = SimpleDateFormat("hh:mm a", Locale.getDefault())
        dateFormatUTC.timeZone = TimeZone.getTimeZone("UTC")
        var date: Date? = null
        try {
            date = dateFormatUTC.parse(formatTime(timeInMillis))
        } catch (ignored: Exception) {
        }
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        return if (date == null) {
            dateFormat.format(timeInMillis)
        } else dateFormat.format(date)
    }

    fun formatLocalFullTime(timeInMillis: Long): String {
        val dateFormatUTC = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormatUTC.timeZone = TimeZone.getTimeZone("UTC")
        var date: Date? = null
        try {
            date = dateFormatUTC.parse(formatDateTime(timeInMillis))
        } catch (e: Exception) {
            getErrors(e)
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        return if (date == null) {
            dateFormat.format(timeInMillis)
        } else dateFormat.format(date)
    }

    fun formatDateTime(context: Context?, timeInMillis: String?): String {
        var localTime = 0L
        try {
            localTime = dateToMillis(formatLocalFullTime(dateToMillis(timeInMillis)))
        } catch (e: Exception) {
            getErrors(e)
        }
        return if (isToday(localTime)) {
            formatTime(context, localTime)
        } else {
            formatDateNew(localTime)
        }
    }

    @Throws(ParseException::class)
    fun dateToMillis(dateString: String?): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateString?.let { sdf.parse(it) }!!
        return date.time
    }

    fun formatFullDate(timeString: String?): String {
        var timeInMillis: Long = 0
        try {
            timeInMillis = dateToMillis(timeString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(timeInMillis).uppercase(Locale.getDefault())
    }

    /**
     * Formats timestamp to 'date month' format (e.g. 'February 3').
     */
    fun formatDateNew(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd yy HH:mm", Locale.getDefault())
        return dateFormat.format(timeInMillis)
    }

    /**
     * Returns whether the given date is today, based on the user's current locale.
     */
    fun isToday(timeInMillis: Long): Boolean {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        val date = dateFormat.format(timeInMillis)
        return date == dateFormat.format(Calendar.getInstance().timeInMillis)
    }

    /**
     * Checks if two dates are of the same day.
     *
     * @param millisFirst  The time in milliseconds of the first date.
     * @param millisSecond The time in milliseconds of the second date.
     * @return Whether {@param millisFirst} and {@param millisSecond} are off the same day.
     */
    fun hasSameDate(millisFirst: Long, millisSecond: Long): Boolean {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return dateFormat.format(millisFirst) == dateFormat.format(millisSecond)
    }

    fun formatLocalTime(context: Context?, `when`: Long): String {
        val then = Time()
        then.set(`when`)
        val now = Time()
        now.setToNow()
        var flags =
            DateUtils.FORMAT_NO_NOON or DateUtils.FORMAT_NO_MIDNIGHT or DateUtils.FORMAT_ABBREV_ALL
        flags = if (then.year != now.year) {
            flags or (DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE)
        } else if (then.yearDay != now.yearDay) {
            flags or DateUtils.FORMAT_SHOW_DATE
        } else {
            flags or DateUtils.FORMAT_SHOW_TIME
        }
        return DateUtils.formatDateTime(context, `when`, flags)
    }

    fun formatTime(context: Context?, `when`: Long): String {
        val then = Time()
        then.set(`when`)
        val now = Time()
        now.setToNow()
        var flags =
            DateUtils.FORMAT_NO_NOON or DateUtils.FORMAT_NO_MIDNIGHT or DateUtils.FORMAT_ABBREV_ALL
        flags = if (then.year != now.year) {
            flags or (DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE)
        } else if (then.yearDay != now.yearDay) {
            flags or DateUtils.FORMAT_SHOW_DATE
        } else {
            flags or DateUtils.FORMAT_SHOW_TIME
        }
        return DateUtils.formatDateTime(context, `when`, flags)
    }

    fun <T> removeDuplicates(list: ArrayList<User>?): ArrayList<User>? {
        // Create a new LinkedHashSet

        // Add the elements to set
        val set: LinkedHashSet<User> = LinkedHashSet(list)

        // Clear the list
        if (list != null) {
            list.clear()
        }

        // add the elements of set
        // with no duplicates to the list
        list?.addAll(set)

        // return the list
        return list
    }

    fun setProfileImage(context: Context?, imgUrl: String, mImageView: ImageView?) {
        try {
            if (!imgUrl.equals(IConstants.IMG_DEFAULTS, ignoreCase = true)) {
//                Picasso.get().load(imgUrl).fit().placeholder(R.drawable.profile_avatar).into(mImageView);
                Glide.with(context!!).load(imgUrl).placeholder(R.drawable.profile_avatar)
                    .thumbnail(0.5f)
                    .into(mImageView!!)
            } else {
//                Picasso.get().load(R.drawable.profile_avatar).fit().into(mImageView);
                Glide.with(context!!).load(R.drawable.profile_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(
                    mImageView!!
                )
            }
        } catch (ignored: Exception) {
        }
    }

    fun setProfileBlurImage(context: Context?, imgUrl: String, mImageView: ImageView?) {
        try {
//            BlurTransformation blur = new BlurTransformation(context, 25, 1);
            val blur = BlurTransformation(25, 1)
            if (!imgUrl.equals(IConstants.IMG_DEFAULTS, ignoreCase = true)) {

//                Picasso.get().load(imgUrl).transform(blur).placeholder(R.drawable.profile_avatar).into(mImageView);
                Glide.with(context!!).load(imgUrl).placeholder(R.drawable.profile_avatar)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.bitmapTransform(blur))
                    .into(mImageView!!)
            } else {

//                Picasso.get().load(R.drawable.profile_avatar).transform(blur).placeholder(R.drawable.profile_avatar).into(mImageView);
                Glide.with(context!!).load(R.drawable.profile_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.bitmapTransform(blur))
                    .into(mImageView!!)
            }
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun setGroupImage(mContext: Context?, imgUrl: String, mImageView: ImageView?) {
        try {
            if (!imgUrl.equals(IConstants.IMG_DEFAULTS, ignoreCase = true)) {
//                Picasso.get().load(imgUrl).fit().placeholder(R.drawable.img_group_default).into(mImageView);
                Glide.with(mContext!!).load(imgUrl).placeholder(R.drawable.img_group_default)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(mImageView!!)
            } else {
//                Picasso.get().load(R.drawable.img_group_default).fit().into(mImageView);
                Glide.with(mContext!!).load(R.drawable.img_group_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(
                    mImageView!!
                )
            }
        } catch (ignored: Exception) {
        }
    }

    fun setGroupParticipateImage(mContext: Context?, imgUrl: String, mImageView: ImageView?) {
        try {
            if (!imgUrl.equals(IConstants.IMG_DEFAULTS, ignoreCase = true)) {
//                Picasso.get().load(imgUrl).placeholder(R.drawable.img_group_default).into(mImageView);
                Glide.with(mContext!!).load(imgUrl).placeholder(R.drawable.img_group_default)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(mImageView!!)
            } else {
//                Picasso.get().load(R.drawable.img_group_default).into(mImageView);
                Glide.with(mContext!!).load(R.drawable.img_group_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(
                    mImageView!!
                )
            }
        } catch (ignored: Exception) {
        }
    }

    fun setChatImage(mContext: Context?, imgUrl: String, mImageView: ImageView?) {
        try {
            val roundedCorner = 16
            val gCorner = GranularRoundedCorners(
                roundedCorner.toFloat(),
                roundedCorner.toFloat(),
                roundedCorner.toFloat(),
                roundedCorner.toFloat()
            )
            if (!imgUrl.equals(IConstants.IMG_DEFAULTS, ignoreCase = true)) {
//                Picasso.get().load(imgUrl).placeholder(R.drawable.image_load).fit().centerCrop().into(mImageView);
                Glide.with(mContext!!).load(imgUrl).placeholder(R.drawable.image_load)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .transform(CenterCrop(), gCorner)
                    .into(mImageView!!)
            } else {
//                Picasso.get().load(R.drawable.image_load).fit().centerCrop().into(mImageView);
                Glide.with(mContext!!).load(R.drawable.image_load)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(CenterCrop(), gCorner)
                    .into(mImageView!!)
            }
        } catch (ignored: Exception) {
        }
    }

    fun uploadToken(referenceToken: String?) {
        try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                val reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_TOKENS)
                val token = Token(referenceToken)
                reference.child(firebaseUser.uid).setValue(token)
            }
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun uploadTypingStatus() {
        try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                val reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_OTHERS)
                val token = Others(IConstants.FALSE)
                reference.child(firebaseUser.uid).setValue(token)
            }
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun setWindow(w: Window) {
        //make status bar transparent
//        w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        w.statusBarColor = ContextCompat.getColor(w.context, R.color.black)
        w.navigationBarColor = ContextCompat.getColor(w.context, R.color.black)
    }

    fun RTLSupport(window: Window) {
        try {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    @JvmOverloads
    fun shareApp(
        mActivity: Activity,
        title: String? = mActivity.resources.getString(R.string.strShareTitle)
    ) {
        try {
            val app_name = Html.fromHtml(title).toString()
            val share_text =
                Html.fromHtml(mActivity.resources.getString(R.string.strShareContent)).toString()
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT, """
     $app_name
     
     $share_text
     
     https://play.google.com/store/apps/details?id=${mActivity.packageName}
     """.trimIndent()
            )
            sendIntent.type = "text/plain"
            mActivity.startActivity(sendIntent)
        } catch (e: NotFoundException) {
            getErrors(e)
        }
    }

    fun sortByUser(mUsers: ArrayList<User>): ArrayList<User> {
        Collections.sort(mUsers) { s1, s2 -> // notice the cast to (Integer) to invoke compareTo
            s1.username!!.compareTo(s2.username!!)
        }
        return mUsers
    }

    fun rateApp(mActivity: Activity) {
        val appName = mActivity.packageName
        try {
            mActivity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(IConstants.DEFAULT_UPDATE_URL_2 + appName)
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            mActivity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(IConstants.DEFAULT_UPDATE_URL + appName)
                )
            )
        }
    }

    fun sortByUser(unsortMap: MutableMap<String, User?>?, order: Boolean): Map<String, User> {
        val list: List<Map.Entry<String, User>> = unsortMap?.entries?.let { LinkedList(it) } as List<Map.Entry<String, User>>
        Collections.sort(list, Comparator<Map.Entry<String?, User>> { (_, value), (_, value1) ->
            try {
                return@Comparator value.username!!.compareTo(value1.username!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            0
        })
        val sortedMap: MutableMap<String, User> = LinkedHashMap()
        for ((key, value) in list) {
            sortedMap[key] = value
        }
        return sortedMap
    }

    fun sortByString(unsortMap: Map<String, String>, order: Boolean): Map<String, String> {
        val list: List<Map.Entry<String, String>> = LinkedList(unsortMap.entries)
        Collections.sort(list, object : Comparator<Map.Entry<String?, String?>> {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            override fun compare(
                o1: Map.Entry<String?, String?>,
                o2: Map.Entry<String?, String?>
            ): Int {
                try {
                    return if (order) {
                        dateFormat.parse(o1.value).compareTo(dateFormat.parse(o2.value))
                    } else {
                        dateFormat.parse(o2.value).compareTo(dateFormat.parse(o1.value))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return 0
            }
        })
        val sortedMap: MutableMap<String, String> = LinkedHashMap()
        for ((key, value) in list) {
            sortedMap[key] = value
        }
        return sortedMap
    }

    fun printMap(map: Map<String, Chat>) {
        for ((key, value) in map) {
            sout("Key : " + key + " Value : " + value.message + " >> " + value.datetime)
        }
    }

    fun sortByChatDateTime(unsortMap: MutableMap<String?, Chat?>, order: Boolean): Map<String, Chat> {
        val list: List<Map.Entry<String, Chat>> = LinkedList(unsortMap.entries) as List<Map.Entry<String, Chat>>
        Collections.sort(list, object : Comparator<Map.Entry<String?, Chat>> {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            override fun compare(o1: Map.Entry<String?, Chat>, o2: Map.Entry<String?, Chat>): Int {
                try {
                    return if (order) {
                        dateFormat.parse(o1.value.datetime)
                            .compareTo(dateFormat.parse(o2.value.datetime))
                    } else {
                        dateFormat.parse(o2.value.datetime)
                            .compareTo(dateFormat.parse(o1.value.datetime))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return 0
            }
        })
        val sortedMap: MutableMap<String, Chat> = LinkedHashMap()
        for ((key, value) in list) {
            sortedMap[key] = value
        }
        return sortedMap
    }

    fun sortByGroupDateTime(unsortMap: Map<String?, Groups?>, order: Boolean): Map<String, Groups> {
        val list: LinkedList<Map.Entry<String?, Groups?>> = LinkedList(unsortMap.entries)
        Collections.sort(list, object : Comparator<Map.Entry<String?, Groups?>> {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            override fun compare(
                o1: Map.Entry<String?, Groups?>,
                o2: Map.Entry<String?, Groups?>
            ): Int {
                try {
                    return if (order) {
                        dateFormat.parse(o1.value?.lastMsgTime)
                            .compareTo(dateFormat.parse(o2.value?.lastMsgTime))
                    } else {
                        dateFormat.parse(o2.value?.lastMsgTime)
                            .compareTo(dateFormat.parse(o1.value?.lastMsgTime))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return 0
            }
        })
        val sortedMap: MutableMap<String, Groups> = LinkedHashMap()
        for ((key, value) in list) {
            sortedMap[key!!] = value!!
        }
        return sortedMap
    }

    fun setVibrate(mContext: Context) {
        // Vibrate for 500 milliseconds
        setVibrate(mContext, DEFAULT_VIBRATE.toLong())
    }

    fun setVibrate(mContext: Context, vibrate: Long) {
        try {
            val vib = mContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(
                    VibrationEffect.createOneShot(
                        vibrate,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vib.vibrate(vibrate) //deprecated in API 26
            }
        } catch (ignored: Exception) {
        }
    }

    fun deleteUploadedFilesFromCloud(storage: FirebaseStorage, chat: Chat) {
        try {
            if (!isEmpty(chat.attachmentType)) {
                var url: String? = ""
                val type = chat.attachmentType
                when (type) {
                    IConstants.TYPE_IMAGE -> url = chat.imgPath
                    IConstants.TYPE_RECORDING, IConstants.TYPE_DOCUMENT, IConstants.TYPE_AUDIO, IConstants.TYPE_CONTACT, IConstants.TYPE_VIDEO -> url =
                        chat.attachmentPath
                }
                if (!type.equals(IConstants.TYPE_LOCATION, ignoreCase = true) || type.equals(
                        IConstants.TYPE_TEXT,
                        ignoreCase = true
                    )
                ) {
                    sout("AttachmentDelete:::  $url")
                    val removeRef = storage.getReferenceFromUrl(url!!)
                    removeRef.delete()
                    if (type.equals(IConstants.TYPE_VIDEO, ignoreCase = true)) {
                        val removeThumbnail = storage.getReferenceFromUrl(chat.attachmentData!!)
                        removeThumbnail.delete()
                    }
                }
            }
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    private var bottomDialog: Dialog? = null
    fun selectGenderPopup(mContext: Activity, userId: String?, selectGender: Int) {
//        String[] strArray = mContext.getResources().getStringArray(R.array.arrGender);
        var index = IConstants.GEN_UNSPECIFIED
        if (selectGender != IConstants.GEN_UNSPECIFIED) {
//            index = Arrays.asList(strArray).indexOf(selectGender);
            index = selectGender
            strSelectedGender = selectGender
        }
        val view = mContext.layoutInflater.inflate(R.layout.dialog_gender, null) as CardView
        if (SessionManager.get()!!.isRTLOn) {
            view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val radioGenderGroup = view.findViewById<RadioGroup>(R.id.rdoGroupGender)
        val radioMale = view.findViewById<RadioButton>(R.id.rdoMale)
        val radioFemale = view.findViewById<RadioButton>(R.id.rdoFemale)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnDone = view.findViewById<AppCompatButton>(R.id.btnDone)
        if (bottomDialog != null) {
            bottomDialog!!.dismiss()
        }
        bottomDialog = Dialog(mContext, R.style.BottomDialog)
        bottomDialog!!.setContentView(view)
        if (index == IConstants.GEN_MALE) {
            radioMale.isChecked = true
            radioFemale.isChecked = false
        } else if (index == IConstants.GEN_FEMALE) {
            radioMale.isChecked = false
            radioFemale.isChecked = true
        } else {
            radioMale.isChecked = false
            radioFemale.isChecked = false
        }
        radioGenderGroup.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            if (null != rb && checkedId > -1) {
                if (checkedId == R.id.rdoMale) {
                    strSelectedGender = IConstants.GEN_MALE
                } else {
                    strSelectedGender = IConstants.GEN_FEMALE
                }
            }
        }

        //===================== START
        btnCancel.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                bottomDialog!!.dismiss()
            }
        })
        btnDone.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                if (isEmpty(strSelectedGender)) {
                    val screens = Screens(mContext)
                    screens.showToast(R.string.msgSelectGender)
                } else {
                    updateGender(userId, strSelectedGender)
                }
                bottomDialog!!.dismiss()
            }
        })
        val layoutParams = view.layoutParams
        layoutParams.width = mContext.resources.displayMetrics.widthPixels
        view.layoutParams = layoutParams

        //https://github.com/jianjunxiao/BottomDialog
//        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
//        params.width = mContext.getResources().getDisplayMetrics().widthPixels - CompatUtils.dp2px(mContext, 16f);
//        params.bottomMargin = CompatUtils.dp2px(mContext, 8f);
//        view.setLayoutParams(params);
        bottomDialog!!.window!!.setGravity(Gravity.BOTTOM)
        bottomDialog!!.setCanceledOnTouchOutside(false)
        bottomDialog!!.setCancelable(false)
        bottomDialog!!.window!!.setWindowAnimations(R.style.BottomDialog_Animation)
        bottomDialog!!.show()
        //=====================
    }

    fun showYesNoDialog(
        mActivity: Activity,
        title: Int,
        message: Int,
        iDialogListener: IDialogListener
    ) {
        showYesNoDialog(
            mActivity,
            if (title == IConstants.ZERO) "" else mActivity.getString(title),
            mActivity.getString(message),
            iDialogListener
        )
    }

    fun showYesNoDialog(
        mActivity: Activity,
        title: String?,
        message: Int,
        iDialogListener: IDialogListener
    ) {
        showYesNoDialog(mActivity, title, mActivity.getString(message), iDialogListener)
    }

    fun showYesNoDialog(
        mActivity: Activity,
        title: String?,
        message: String?,
        iDialogListener: IDialogListener
    ) {
        val builder = AlertDialog.Builder(mActivity)
        val view = mActivity.layoutInflater.inflate(R.layout.dialog_custom, null) as CardView
        if (SessionManager.get()!!.isRTLOn) {
            view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val txtMessage = view.findViewById<TextView>(R.id.txtMessage)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnDone = view.findViewById<AppCompatButton>(R.id.btnDone)
        if (isEmpty(title)) {
            txtTitle.visibility = View.GONE
        } else {
            txtTitle.visibility = View.VISIBLE
            txtTitle.text = title
        }
        txtMessage.text = message
        builder.setView(view)
        val alert = builder.create()
        btnCancel.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                alert.dismiss()
            }
        })
        btnDone.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                iDialogListener.yesButton()
                alert.dismiss()
            }
        })
        alert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alert.setCanceledOnTouchOutside(false)
        alert.setCancelable(false)
        alert.show()
    }

    fun showOKDialog(
        mActivity: Activity,
        title: Int,
        message: Int,
        iDialogListener: IDialogListener
    ) {
        showOKDialog(
            mActivity,
            mActivity.getString(title),
            mActivity.getString(message),
            iDialogListener
        )
    }

    fun showOKDialog(
        mActivity: Activity,
        title: String?,
        message: String?,
        iDialogListener: IDialogListener
    ) {
        val builder = AlertDialog.Builder(mActivity)
        val view = mActivity.layoutInflater.inflate(R.layout.dialog_custom, null) as CardView
        if (SessionManager.get()!!.isRTLOn) {
            view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val txtMessage = view.findViewById<TextView>(R.id.txtMessage)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnDone = view.findViewById<AppCompatButton>(R.id.btnDone)
        if (isEmpty(title)) {
            txtTitle.visibility = View.GONE
        } else {
            txtTitle.visibility = View.VISIBLE
            txtTitle.text = title
        }
        txtMessage.text = message
        builder.setView(view)
        val alert = builder.create()
        btnCancel.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                alert.dismiss()
            }
        })
        btnCancel.visibility = View.GONE
        btnDone.setText(R.string.strOK)
        btnDone.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                iDialogListener.yesButton()
                alert.dismiss()
            }
        })
        alert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alert.setCanceledOnTouchOutside(false)
        alert.setCancelable(false)
        alert.show()
    }

    fun showOKDialog(
        mActivity: Activity,
        title: String?,
        message: String?,
        strOk: Int,
        strCancel: Int,
        iDialogListener: IDialogListener
    ) {
        showOKDialog(
            mActivity,
            title,
            message,
            mActivity.getString(strOk),
            mActivity.getString(strCancel),
            iDialogListener
        )
    }

    fun showOKDialog(
        mActivity: Activity,
        title: String?,
        message: String?,
        strOk: String?,
        strCancel: String?,
        iDialogListener: IDialogListener
    ) {
        val builder = AlertDialog.Builder(mActivity)
        val view = mActivity.layoutInflater.inflate(R.layout.dialog_custom, null) as CardView
        if (SessionManager.get()!!.isRTLOn) {
            view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val txtMessage = view.findViewById<TextView>(R.id.txtMessage)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnDone = view.findViewById<AppCompatButton>(R.id.btnDone)
        if (isEmpty(title)) {
            txtTitle.visibility = View.GONE
        } else {
            txtTitle.visibility = View.VISIBLE
            txtTitle.text = title
        }
        if (!isEmpty(strOk)) {
            btnDone.text = strOk
        } else {
            btnDone.setText(R.string.strOK)
        }
        if (!isEmpty(strCancel)) {
            btnCancel.text = strCancel
        }
        txtMessage.text = message
        builder.setView(view)
        val alert = builder.create()
        btnCancel.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                alert.dismiss()
            }
        })
        btnDone.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                iDialogListener.yesButton()
                alert.dismiss()
            }
        })
        alert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alert.setCanceledOnTouchOutside(false)
        alert.setCancelable(false)
        alert.show()
    }

    fun updateOnlineStatus(userId: String?, status: Int) {
        try {
            val reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(
                userId!!
            )
            val hashMap = HashMap<String, Any>()
            hashMap[IConstants.EXTRA_IS_ONLINE] = status
            hashMap[IConstants.EXTRA_VERSION_CODE] = BuildConfig.VERSION_CODE
            hashMap[IConstants.EXTRA_VERSION_NAME] = BuildConfig.VERSION_NAME
            if (status == IConstants.STATUS_OFFLINE) hashMap[IConstants.EXTRA_LASTSEEN] = dateTime
            reference.updateChildren(hashMap)
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun updateOfflineStatus(userId: String?, status: Int) {
        try {
            val reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(
                userId!!
            )
            val hashMap = HashMap<String, Any>()
            hashMap[IConstants.EXTRA_IS_ONLINE] = status
            reference.updateChildren(hashMap)
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun updateGender(userId: String?, strGender: Int) {
        try {
            val reference = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(
                userId!!
            )
            val hashMap = HashMap<String, Any>()
            hashMap[IConstants.EXTRA_GENDER] = strGender
            reference.updateChildren(hashMap)
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun updateUserActive(userId: String?) {
        try {
            val referenceUpdate =
                FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(
                    userId!!
                )
            val hashMap = HashMap<String, Any>()
            hashMap[IConstants.EXTRA_ACTIVE] = IConstants.FALSE
            referenceUpdate.updateChildren(hashMap)
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    @JvmStatic
    fun updateGenericUserField(userId: String?, fieldKey: String, fieldValue: Any) {
        try {
            val referenceUpdate =
                FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS).child(
                    userId!!
                )
            val hashMap = HashMap<String, Any>()
            hashMap[fieldKey] = fieldValue
            referenceUpdate.updateChildren(hashMap)
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun filterPopup(context: Activity, filterListener: IFilterListener) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getText(R.string.strFilterTitle))
        val view =
            context.layoutInflater.inflate(R.layout.dialog_search_filter, null) as LinearLayout
        if (SessionManager.get()!!.isRTLOn) {
            view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        builder.setView(view)
        val mOnlineChk = view.findViewById<CheckBox>(R.id.chkOnline)
        val mOfflineChk = view.findViewById<CheckBox>(R.id.chkOffline)
        val mMaleChk = view.findViewById<CheckBox>(R.id.chkMale)
        val mFemaleChk = view.findViewById<CheckBox>(R.id.chkFemale)
        val mNotSetChk = view.findViewById<CheckBox>(R.id.chkNotSet)
        val mWithPicture = view.findViewById<CheckBox>(R.id.chkWithPicture)
        val mWithoutPicture = view.findViewById<CheckBox>(R.id.chkWithoutPicture)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnDone = view.findViewById<AppCompatButton>(R.id.btnDone)
        mOnlineChk.isChecked = online
        mOfflineChk.isChecked = offline
        mMaleChk.isChecked = male
        mFemaleChk.isChecked = female
        mNotSetChk.isChecked = notset
        mWithPicture.isChecked = withPicture
        mWithoutPicture.isChecked = withoutPicture
        val alert = builder.create()
        btnCancel.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                alert.dismiss()
            }
        })
        val screens = Screens(context)
        btnDone.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                if (!mOfflineChk.isChecked && !mOnlineChk.isChecked) {
                    screens.showToast(context.getString(R.string.msgErrorUserOnline))
                    return
                }
                if (!mMaleChk.isChecked && !mFemaleChk.isChecked && !mNotSetChk.isChecked) {
                    screens.showToast(context.getString(R.string.msgErrorGender))
                    return
                }
                if (!mWithPicture.isChecked && !mWithoutPicture.isChecked) {
                    screens.showToast(context.getString(R.string.msgErrorProfilePicture))
                    return
                }
                online = mOnlineChk.isChecked
                offline = mOfflineChk.isChecked
                male = mMaleChk.isChecked
                female = mFemaleChk.isChecked
                notset = mNotSetChk.isChecked
                withPicture = mWithPicture.isChecked
                withoutPicture = mWithoutPicture.isChecked
                filterListener.showFilterUsers()
                alert.dismiss()
            }
        })
        alert.setCanceledOnTouchOutside(false)
        alert.setCancelable(false)
        if (SessionManager.get()!!.isRTLOn) {
            alert.window!!.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            alert.window!!.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        alert.show()
    }

    fun chatSendSound(context: Context) {
        try {
            val mediaPlayer = MediaPlayer()
            val afd = context.assets.openFd("chat_tone.mp3")
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    val querySortBySearch: Query
        get() = FirebaseDatabase.getInstance().getReference(IConstants.REF_USERS)
            .orderByChild(IConstants.EXTRA_SEARCH).startAt("").endAt("" + "\uf8ff")
    val groupUniqueId: String?
        get() = FirebaseDatabase.getInstance().reference.child(IConstants.REF_GROUPS).child("")
            .push().key
    val chatUniqueId: String?
        get() = FirebaseDatabase.getInstance().reference.child(IConstants.REF_CHATS).child("")
            .push().key

    @SuppressLint("SuspiciousIndentation")
    fun getImageColor(strName: String?): Int?{
        val generator = ColorGenerator.DEFAULT
            return generator?.getColor(strName!!)
    }

    fun getExtension(context: Context, uri: Uri?): String? {
        val contentResolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun readStatus(status: Int) {
        try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                updateOnlineStatus(firebaseUser.uid, status)
            }
        } catch (ignored: Exception) {
        }
    }

    fun logout(mActivity: Activity) {
        Handler(Looper.getMainLooper()).postDelayed({
            showYesNoDialog(mActivity, R.string.logout_title, R.string.logout_message) {
                revokeGoogle(mActivity)
                val screens = Screens(mActivity)
                readStatus(IConstants.STATUS_OFFLINE)
                FirebaseAuth.getInstance().signOut()
                screens.showClearTopScreen(LoginActivity::class.java)
            }
        }, IConstants.CLICK_DELAY_TIME)
    }

    private fun revokeGoogle(context: Context) {
        try {
            // [START config_signin]
            // Configure Google Sign In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            // [END config_signin]
            val mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
            mGoogleSignInClient.signOut()
            sout("Sign out Google")
            //mGoogleSignInClient.revokeAccess();
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun getAppVersionCode(context: Context): Int {
        var appVersionDetails: Long = 1
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            appVersionDetails =
                PackageInfoCompat.getLongVersionCode(packageInfo) //packageInfo.versionCode;
        } catch (e: PackageManager.NameNotFoundException) {
            getErrors(e)
        }
        return appVersionDetails.toInt()
    }

    fun closeKeyboard(context: Context, view: View?) {
        if (view != null) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @Throws(IOException::class)
    fun readAsByteArray(ios: InputStream?): ByteArray {
        var ous: ByteArrayOutputStream? = null
        try {
            val buffer = ByteArray(4096)
            ous = ByteArrayOutputStream()
            var read: Int
            while (ios!!.read(buffer).also { read = it } != -1) {
                ous.write(buffer, 0, read)
            }
        } finally {
            try {
                ous?.close()
            } catch (ignored: IOException) {
            }
            try {
                ios?.close()
            } catch (ignored: IOException) {
            }
        }
        return ous!!.toByteArray()
    }

    fun getSettingString(mContext: Activity, `val`: Int): String {
        return if (`val` == IConstants.SETTING_ALL_PARTICIPANTS) {
            mContext.getString(R.string.lblAllParticipants)
        } else {
            mContext.getString(R.string.lblOnlyAdmin)
        }
    }

    fun getSettingValue(mContext: Activity, `val`: String?): Int {
        return if (`val`.equals(
                mContext.getString(R.string.lblAllParticipants),
                ignoreCase = true
            )
        ) {
            IConstants.SETTING_ALL_PARTICIPANTS
        } else {
            IConstants.SETTING_ONLY_ADMIN
        }
    }

    private var settingValue: String? = null
    fun selectSendMessages(
        mContext: Activity,
        groupId: String?,
        selectSetting: Int,
        iSendMessage: ISendMessage
    ) {
        val builder = AlertDialog.Builder(mContext)
        val view = mContext.layoutInflater.inflate(R.layout.dialog_send_messages, null) as CardView
        if (SessionManager.get()!!.isRTLOn) {
            view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val radioGroup = view.findViewById<RadioGroup>(R.id.rdoGroup)
        val radioParticipants = view.findViewById<RadioButton>(R.id.rdoAllParticipants)
        val radioAdmin = view.findViewById<RadioButton>(R.id.rdoOnlyAdmins)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnDone = view.findViewById<AppCompatButton>(R.id.btnDone)
        builder.setView(view)
        settingIndex = selectSetting
        if (selectSetting == IConstants.SETTING_ALL_PARTICIPANTS) {
            radioParticipants.isChecked = true
            radioAdmin.isChecked = false
            settingValue = mContext.getString(R.string.lblAllParticipants)
        } else if (selectSetting == IConstants.SETTING_ONLY_ADMIN) {
            radioParticipants.isChecked = false
            radioAdmin.isChecked = true
            settingValue = mContext.getString(R.string.lblOnlyAdmin)
        } else {
            radioParticipants.isChecked = true
            radioAdmin.isChecked = false
            settingValue = mContext.getString(R.string.lblAllParticipants)
        }
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            if (null != rb && checkedId > -1) {
                settingValue = rb.text.toString()
                settingIndex = getSettingValue(mContext, settingValue)
            }
        }
        val alert = builder.create()
        btnCancel.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                alert.dismiss()
            }
        })
        btnDone.setOnClickListener(object : SingleClickListener() {
            override fun onClickView(v: View?) {
                updateSendMessageSetting(groupId, settingIndex)
                alert.dismiss()
                iSendMessage.sendSetting(settingValue)
            }
        })
        alert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alert.setCanceledOnTouchOutside(false)
        alert.setCancelable(false)
        alert.show()
    }

    fun updateSendMessageSetting(groupId: String?, value: Int) {
        try {
            val reference =
                FirebaseDatabase.getInstance().getReference(IConstants.REF_GROUPS).child(
                    groupId!!
                )
            val hashMap = HashMap<String, Any>()
            hashMap[IConstants.EXTRA_SEND_MESSAGES] = value
            reference.updateChildren(hashMap)
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun setHTMLMessage(lblName: TextView, strMsg: String?) {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            lblName.text = Html.fromHtml(strMsg, Html.FROM_HTML_MODE_LEGACY)
        } else {
            lblName.text = Html.fromHtml(strMsg)
        }
    }

    fun getReceiveDirectory(context: Context, type: String?): File {
        val directoryName = getTypeName(type)
        val mainPath =
            IConstants.SLASH + context.getString(R.string.app_name) + IConstants.SLASH + directoryName
        val file: File
        file = if (isAboveQ) {
            File(IConstants.SDPATH + getDirectoryByType(type), mainPath)
        } else {
            File(
                Environment.getExternalStorageDirectory(),
                IConstants.SLASH + getDirectoryByType(type) + mainPath
            )
        }
        return file
    }

    fun getSentDirectory(context: Context, type: String?): File {
        val directoryName = getTypeName(type)
        val systemFolder =
            getDirectoryByType(directoryName) // Audio, Movie or Download(System Folders)
        val file: File
        if (isAboveQ) {
            file = File(
                IConstants.SDPATH + systemFolder,
                IConstants.SLASH + context.getString(R.string.app_name) + IConstants.SLASH + directoryName + IConstants.SENT_FILE
            )
        } else {
            file = File(
                Environment.getExternalStorageDirectory(),
                IConstants.SLASH + systemFolder + IConstants.SLASH + context.getString(R.string.app_name) + IConstants.SLASH + directoryName + IConstants.SENT_FILE
            )
            if (!file.exists()) {
                file.mkdirs()
            }
        }
        return file
    }

    fun getSentFile(directory: File?, extension: String): File {
//        final String ext = "_" + Utils.getDateTimeName() + extension;
        if (extension.equals(IConstants.EXT_MP3, ignoreCase = true)) {
            return File(directory, "REC$extension")
        } else if (extension.equals(IConstants.EXT_VCF, ignoreCase = true)) {
            return File(directory, "CONT$extension")
        }
        return File(directory, dateTimeStampName + extension)
    }

    fun getDownloadDirectory(context: Context, type: String): File {
        var directoryName = type
        if (type.equals(IConstants.TYPE_RECORDING, ignoreCase = true)) {
            directoryName = getTypeName(AttachmentTypes.RECORDING)
        }
        sout("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^Download Directory::: $type == $directoryName")
        return File(
            getDirectoryByType(type),
            IConstants.SLASH + context.getString(R.string.app_name) + IConstants.SLASH + directoryName
        )
    }

    fun getUniqueFileName(fileToUpload: File?, attachmentType: Int): String {
        var pathSegment = Uri.fromFile(fileToUpload).lastPathSegment
        val fileExtension = FileUtils.getExtension(pathSegment)
        if (!isEmpty(fileExtension)) {
            pathSegment = fileExtension?.toRegex()?.let {
                pathSegment!!.replace(
                    it,
                    getExtension(fileExtension, attachmentType)
                )
            }
        }

//        String end = "_" + System.currentTimeMillis() + fileExtension;
        val end = "_" + dateTimeStampName + fileExtension
        //        Utils.sout("----New File Name:: " + file + " >>> " + end);
        return pathSegment!!.replace(fileExtension!!.toRegex(), end)
    }

    val musicFolder: String
        get() = Environment.DIRECTORY_MUSIC
    val moviesFolder: String
        get() = Environment.DIRECTORY_MOVIES
    val downloadFolder: String
        get() = Environment.DIRECTORY_DOWNLOADS

    /**
     * SAF = Storage Access Framework (Scoped Storage)
     * It is only work for Android SDK >= 29 (Android ver >= 10 -> Android Q)
     * This devices didn't use WRITE_EXT_STORAGE Persmission and use new SAF
     */
     var isAboveQ: Boolean = false
        get() = VERSION.SDK_INT >= Build.VERSION_CODES.Q

    /**
     * isStoreFile = True -> It is storing the file via InputStream, False means do not store the file, just put the entry in contentResolver
     */
    fun moveFileToFolder(
        mContext: Context,
        isStoreFile: Boolean,
        newFileName: String?,
        sourceFile: File?,
        attachmentType: Int
    ): File? {
        try {
            val resolver = mContext.contentResolver
            val contentValues = ContentValues()
            val type = getTypeName(attachmentType)
            val dest = if (isStoreFile) getSentDirectory(
                mContext,
                type
            ) else getDownloadDirectory(mContext, type)
            val target: Uri
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
            try {
                val mimeType = FileUtils.getMimeType(File(newFileName))
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                //Utils.sout("OKNew File to Folder::: " + isStoreFile + " >> " + newFileName + " >>> " + type + " >dest> " + dest + " ::mimeType:: " + mimeType);
            } catch (ignored: Exception) {
            }
            target = if (isAboveQ) {
                val tempDest = dest.toString().replace(IConstants.SDPATH.toRegex(), "")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, tempDest)
                getTargetUri(type) //MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            } else {
                contentValues.put(MediaStore.MediaColumns.DATA, dest.toString())
                MediaStore.Files.getContentUri("external")
            }
            val uri = resolver.insert(target, contentValues)
            if (isStoreFile) {
                try {
                    if (isAboveQ) {
                        try {
                            val `is`: InputStream = FileInputStream(sourceFile)
                            val os = resolver.openOutputStream(uri!!, "rwt")
                            val buffer = ByteArray(1024)
                            var r: Int
                            while (`is`.read(buffer).also { r = it } != -1) {
                                os!!.write(buffer, 0, r)
                            }
                            os!!.flush()
                            os.close()
                            `is`.close()
                        } catch (e: Exception) {
                            getErrors(e)
                        }
                    } else {
                        val newFile = File(dest, newFileName)
                        FileUtils.copyFileToDest(sourceFile!!, newFile)
                    }
                } catch (e: Exception) {
                    getErrors(e)
                }
            }
            //Utils.sout("moved File successfully:: " + dest.toString());
            return dest
        } catch (e: Exception) {
            getErrors(e)
        }
        return null
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        var mimeType: String? = null
        try {
            mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                val cr = context.contentResolver
                cr.getType(uri)
            } else {
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
            }
        } catch (e: Exception) {
            getErrors(e)
        }
        return mimeType
    }

    fun getFileSize(size: Long): String {
        return try {
            val BYTES_IN_KILOBYTES = 1024
            val dec = DecimalFormat("###.#")
            val KILOBYTES = " KB"
            val MEGABYTES = " MB"
            val GIGABYTES = " GB"
            var fileSize = 0f
            var suffix = KILOBYTES
            if (size > BYTES_IN_KILOBYTES) {
                fileSize = (size / BYTES_IN_KILOBYTES).toFloat()
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES
                    if (fileSize > BYTES_IN_KILOBYTES) {
                        fileSize = fileSize / BYTES_IN_KILOBYTES
                        suffix = GIGABYTES
                    } else {
                        suffix = MEGABYTES
                    }
                }
            }
            dec.format(fileSize.toDouble()) + suffix
        } catch (e: Exception) {
            ""
        }
    }

    //Extract file extension from full path
    fun getFileExtensionFromPath(string: String): String {
        val index = string.lastIndexOf(".")
        return string.substring(index + 1)
    }

    //Used to open the file by system
    @JvmStatic
    fun getOpenFileIntent(context: Context, path: String): Intent {
        val fileExtension = getFileExtensionFromPath(path)
        val toInstall = File(path)

        //if it's apk make the system open apk installer
        return if (fileExtension.equals("apk", ignoreCase = true)) {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val apkUri =
                    getUriForFileProvider(
                        context,
                        toInstall
                    )
                val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
                intent.data = apkUri
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent
            } else {
                val apkUri = Uri.fromFile(toInstall)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent
            }
        } else { //else make the system open an app that can handle given type
            if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val newIntent = Intent(Intent.ACTION_VIEW)
                newIntent.setDataAndType(
                    getUriForFileProvider(
                        context,
                        toInstall
                    ),
                    getMimeType(
                        context,
                        Uri.fromFile(toInstall)
                    )
                )
                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                newIntent
            } else {
                val newIntent = Intent(Intent.ACTION_VIEW)
                newIntent.setDataAndType(
                    getUriForFileProvider(
                        context,
                        toInstall
                    ),
                    getMimeType(
                        context,
                        Uri.fromFile(toInstall)
                    )
                )
                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                newIntent
            }
        }
    }

    fun openPlayingVideo(context: Context, file: File?) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                getUriForFileProvider(context, file),
                getMimeType(context, Uri.fromFile(file))
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun getUriForFileProvider(context: Context, file: File?): Uri {
        return FileProvider.getUriForFile(context, context.getString(R.string.authority), file!!)
    }

    private fun getVideoDurationValidation(context: Context, file: File): Long {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.fromFile(file))
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = time!!.toLong()
            retriever.release()
            return durationMs
        } catch (ignored: Exception) {
        }
        return 0
    }

    fun getVideoDuration(context: Context, file: File): String {
        return convertSecondsToHMmSs(getVideoDurationValidation(context, file))
    }

    @JvmStatic
    fun convertSecondsToHMmSs(mySec: Long): String {
        val seconds = mySec / 1000
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / (60 * 60) % 24
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    fun contactsCursor(context: Context, searchText: String?): Cursor? {
        return try {
            val search = if (isEmpty(searchText)) null else Uri.encode(searchText)
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, search)
            context.contentResolver.query(uri, null, null, null, null)
        } catch (e: Exception) {
            null
        }
    }

    fun openCallIntent(context: Context, number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$number")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (ignored: Exception) {
        }
    }

    fun isGPSEnabled(context: Context): Boolean {
        val locationManager =
            (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private const val staticMap =
        "https://maps.googleapis.com/maps/api/staticmap?key=%s&center=%s,%s&zoom=18&size=280x160&scale=2&format=jpg&markers=color:red|%s,%s|scale:4"

    fun showStaticMap(
        mContext: Context,
        locationAddress: LocationAddress,
        topLeft: Int,
        topRight: Int,
        imgLocation: ImageView?
    ) {
        Glide.with(mContext).load(
            String.format(
                staticMap,
                mContext.getString(R.string.key_maps),
                locationAddress.latitude,
                locationAddress.longitude,
                locationAddress.latitude,
                locationAddress.longitude
            )
        )
            .transform(
                CenterInside(), GranularRoundedCorners(
                    topLeft.toFloat(), topRight.toFloat(), 4F, 4F
                )
            )
            .into(imgLocation!!)
    }

    fun openMapWithAddress(mContext: Context, locationAddress: LocationAddress) {
        try {
            val gmmIntentUri = Uri.parse(
                "geo:" + locationAddress.latitude + "," + locationAddress.longitude + "?q=" + Uri.encode(
                    locationAddress.address
                )
            )
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(mContext.packageManager) != null) {
                mContext.startActivity(mapIntent)
            }
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        val timeStamp = dateTimeStampName
        val imageFileName = "PIC_$timeStamp"
        val image: File
        val storageDir: File?
        if (isAboveQ) {
            storageDir =
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES + File.separator + IConstants.IMG_FOLDER)
            image = File.createTempFile(imageFileName, ".jpg", storageDir)
            //            String currentPhotoPath = image.getAbsolutePath();
        } else {
            storageDir =
                File(Environment.getExternalStorageDirectory().absolutePath + File.separator + IConstants.IMG_FOLDER)
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            image = File(storageDir, "$imageFileName.jpg")
            image.createNewFile()
        }
        return image
    }

    fun getCacheFolder(context: Context): File? {
        return context.getExternalFilesDir(null)
    }

    fun deleteRecursive(fileOrDirectory: File?) {
        try {
            if (fileOrDirectory != null) {
                if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
                    child
                )
                fileOrDirectory.delete()
            }
        } catch (e: Exception) {
            //Utils.getErrors(e);
        }
    }

    fun showOnlineOffline(context: Context, status: Int): String {
        return if (status == IConstants.STATUS_ONLINE) {
            context.getString(R.string.strOnline)
        } else context.getString(R.string.strOffline)
    }

    @JvmStatic
    fun getRegularFont(context: Context?): Typeface? {
        return ResourcesCompat.getFont(context!!, R.font.roboto_regular)
    }

    fun getBoldFont(context: Context?): Typeface? {
        return ResourcesCompat.getFont(context!!, R.font.roboto_bold)
    }

    fun isTypeEmail(strSignUpType: String): Boolean {
        return isEmpty(strSignUpType) || strSignUpType.equals(
            IConstants.TYPE_EMAIL,
            ignoreCase = true
        )
    }
}