package com.innovativetools.firebase.chat.activities.views.files

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.innovativetools.firebase.chat.activities.managers.Utils.sout
import com.innovativetools.firebase.chat.activities.managers.Utils.getErrors
import com.innovativetools.firebase.chat.activities.async.BaseTask
import android.webkit.MimeTypeMap
import kotlin.Throws
import android.provider.OpenableColumns
import android.text.TextUtils
import java.io.*
import java.lang.Exception
import java.lang.ref.WeakReference

class DownloadBaseTask(private val mUri: Uri, context: Context, callback: CallBackTask) :
    BaseTask<Any?>() {
    private val callback: CallBackTask
    private val mContext: WeakReference<Context>
    private val myExtention: String?
    private var pathPlusName: String? = null
    private var folder: File? = null
    private var returnCursor: Cursor? = null
    private var `is`: InputStream? = null
    private var errorReason: String? = ""

    init {
        val mime = MimeTypeMap.getSingleton()
        myExtention = mime.getExtensionFromMimeType(context.contentResolver.getType(mUri))
        sout("==DownloadBaseTask:: $myExtention >>> $mUri")
        mContext = WeakReference(context)
        this.callback = callback
    }

    @Throws(Exception::class)
    override fun call(): Any? {
        var file: File? = null
        var size = -1
        val context = mContext.get()
        if (context != null) {
            folder = context.getExternalFilesDir("Temp")
            if (folder != null && !folder!!.exists()) {
                if (folder!!.mkdirs()) {
                    sout("==Temp folder created")
                }
            }
            returnCursor = context.contentResolver.query(mUri, null, null, null, null)
            try {
                `is` = context.contentResolver.openInputStream(mUri)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        try {
            // File is now available
            callback.PickerManagerOnPreExecute()
        } catch (e: Exception) {
            getErrors(e)
        }
        try {
            try {
                if (returnCursor != null && returnCursor!!.moveToFirst()) {
                    val fileSize: Long
                    if (mUri.scheme != null) if (mUri.scheme == "content") {
                        val sizeIndex = returnCursor!!.getColumnIndex(OpenableColumns.SIZE)
                        fileSize = returnCursor!!.getLong(sizeIndex)
                        size = fileSize.toInt()
                    } else if (mUri.scheme == "file") {
                        val ff = File(mUri.path)
                        fileSize = ff.length()
                        size = fileSize.toInt()
                    }
                }
            } finally {
                if (returnCursor != null) returnCursor!!.close()
            }
            //            getDisplayNameSize(mContext.get(), mUri);
            var fileName = getFileName(mUri, mContext.get())
            if (!TextUtils.isEmpty(myExtention)) {
                if (!fileName!!.endsWith(myExtention!!)) {
                    fileName = "$fileName.$myExtention"
                }
            }
            pathPlusName = folder.toString() + "/" + fileName
            file = File(folder.toString() + "/" + fileName)
            sout("==pathPlusName + File :: $fileName >>> $pathPlusName >> $file")
            val bis = BufferedInputStream(`is`)
            val fos = FileOutputStream(file)
            val data = ByteArray(1024)
            var total: Long = 0
            var count: Int
            while (bis.read(data).also { count = it } != -1) {
//                if (!isCancelled()) {
                total += count.toLong()
                if (size != -1) {
                    try {
                        val post = (total * 100 / size).toInt()
                        //                            Utils.sout("==Publish Progress:: " + post);
                        callback.PickerManagerOnProgressUpdate(post)
                        //                            publishProgress((int) ((total * 100) / size));
                    } catch (e: Exception) {
//                            Utils.sout("==File size is less than 1 : Progress is 0");
                        callback.PickerManagerOnProgressUpdate(0)
                        //                            publishProgress(0);
                    }
                }
                fos.write(data, 0, count)
                //                }
            }
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            sout("==Exception:: " + e.message)
            getErrors(e)
            errorReason = e.message
        }
        assert(file != null)
        return file!!.absolutePath
    }

    override fun setUiForLoading() {
        callback.PickerManagerOnUriReturned()
        //        listener.showProgressBar(0);
    }

    override fun setDataAfterLoading(result: Any?) {
//        listener.setDataInPageWithResult(result);
//        listener.hideProgressBar();
        if (result == null) {
            callback.PickerManagerOnPostExecute(pathPlusName, true, false, errorReason)
        } else {
            callback.PickerManagerOnPostExecute(pathPlusName, true, true, "")
        }
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri, context: Context?): String? {
        var result: String? = null
        if (uri.scheme != null) {
            if (uri.scheme == "content") {
                val cursor = context!!.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            assert(result != null)
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
}