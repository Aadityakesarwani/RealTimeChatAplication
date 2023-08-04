package com.innovativetools.firebase.chat.activities.views.files

import android.content.Context

import com.innovativetools.firebase.chat.activities.managers.Utils.sout
import com.innovativetools.firebase.chat.activities.managers.Utils.getErrors
import android.webkit.MimeTypeMap
import com.innovativetools.firebase.chat.activities.R
import android.database.DatabaseUtils
import com.innovativetools.firebase.chat.activities.async.BaseTask
import kotlin.Throws
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import com.innovativetools.firebase.chat.activities.async.CustomCallable
import com.innovativetools.firebase.chat.activities.async.TaskRunner
import java.io.*
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*

object FileUtils {
    const val MIME_TYPE_AUDIO = "audio/*"
    const val MIME_TYPE_TEXT = "text/*"
    const val MIME_TYPE_IMAGE = "image/*"
    const val MIME_TYPE_VIDEO = "video/*"
    const val MIME_TYPE_APP = "application/*"
    const val HIDDEN_PREFIX = "."

    /**
     * TAG for log messages.
     */
    const val TAG = "FileUtils"
    private const val DEBUG = false // Set to true to enable logging

    /**
     * File and folder comparator. TODO Expose sorting option method
     */
    var sComparator =
        Comparator<File> { f1, f2 -> // Sort alphabetically by lower case, which is much cleaner
            f1.name.lowercase(Locale.getDefault()).compareTo(
                f2.name.lowercase(Locale.getDefault())
            )
        }

    /**
     * File (not directories) filter.
     */
    var sFileFilter = FileFilter { file ->
        val fileName = file.name
        // Return files only (not directories) and skip hidden files
        file.isFile && !fileName.startsWith(HIDDEN_PREFIX)
    }

    /**
     * Folder (directories) filter.
     */
    var sDirFilter = FileFilter { file ->
        val fileName = file.name
        // Return directories only and skip hidden directories
        file.isDirectory && !fileName.startsWith(HIDDEN_PREFIX)
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg" or ".mp3".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    fun getExtension(uri: String?): String? {
        if (uri == null) {
            return null
        }
        val dot = uri.lastIndexOf(".")
        return if (dot >= 0) {
            uri.substring(dot)
        } else {
            // No extension.
            ""
        }
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file
     * @return
     */
    fun getPathWithoutFilename(file: File?): File? {
        return if (file != null) {
            if (file.isDirectory) {
                // no file to be split off. Return everything
                file
            } else {
                val filename = file.name
                val filepath = file.absolutePath

                // Construct path without file name.
                var pathwithoutname = filepath.substring(
                    0,
                    filepath.length - filename.length
                )
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length - 1)
                }
                File(pathwithoutname)
            }
        } else null
    }

    /**
     * @return The MIME type for the given file.
     */
    fun getMimeType(file: File): String? {
        val extension = getExtension(file.name)
        return if (extension!!.length > 0) MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            extension.substring(1)
        ) else "application/octet-stream"
    }

    const val AUTHORITY = "com.bytesbee.filechoser.documents"

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority.
     */
    fun isLocalStorageDocument(context: Context, uri: Uri): Boolean {
        val authority = context.getString(R.string.authority)
        return authority == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String?>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG) DatabaseUtils.dumpCursor(cursor)
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * Get the file size in a human-readable string.
     *
     * @param size
     * @return
     */
    fun getReadableFileSize(size: Int): String {
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
        return dec.format(fileSize.toDouble()) + suffix
    }

    fun copyFile(src: File?, dst: File?) {
        val baseTask: BaseTask<*> = object : BaseTask<Any?>() {
            override fun setUiForLoading() {
                super.setUiForLoading()
            }

            @Throws(Exception::class)
            override fun call(): Any? {
                try {
                    val inStream = FileInputStream(src)
                    val outStream = FileOutputStream(dst)
                    val inChannel = inStream.channel
                    val outChannel = outStream.channel
                    inChannel.transferTo(0, inChannel.size(), outChannel)
                    inStream.close()
                    outStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return null
            }

            override fun setDataAfterLoading(result: Any?) {}
        }
        val taskRunner = TaskRunner()
        taskRunner.executeAsync(baseTask as CustomCallable<R?>)

//        AsyncTask<File, Void, Void> task = new AsyncTask<File, Void, Void>() {
//            @Override
//            protected Void doInBackground(File... params) {
//                try {
//                    FileInputStream inStream = new FileInputStream(params[0]);
//                    FileOutputStream outStream = new FileOutputStream(params[1]);
//                    FileChannel inChannel = inStream.getChannel();
//                    FileChannel outChannel = outStream.getChannel();
//                    inChannel.transferTo(0, inChannel.size(), outChannel);
//                    inStream.close();
//                    outStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//            }
//        };
//        File[] arr = {src, dst};
//        task.execute(arr);
    }

    val imageIntent: Intent
        get() {
            val mimeTypes = arrayOf("image/jpeg", "image/jpeg", "image/png")
            return pickIntentFromMimeType(mimeTypes)
        }

    /**
     * Supported Formats: avi, mov, mp4, webm, 3gp, mkv, mpeg
     */
    val videoIntent: Intent
        get() {
            val mimeTypes = arrayOf(
                "video/avi",
                "video/mov",
                "video/quicktime",
                "video/mp4",
                "video/webm",
                "video/3gpp",
                "video/x-matroska",
                "video/mpeg"
            )
            return pickIntentFromMimeType(mimeTypes)
        }

    /**
     * Supported formats: aac, amr, awb, mp3, mp4, ogg, opus, wav
     */
    val audioIntent: Intent
        get() {
            val mimeTypes = arrayOf(
                "audio/aac",
                "audio/aac-adts",
                "audio/amr",
                "audio/amr-wb",
                "audio/mpeg",
                "audio/mp4",
                "audio/ogg",
                "audio/x-wav"
            )
            return pickIntentFromMimeType(mimeTypes)
        }// .doc & .docx
    //pdf
    // .xls & .xlsx
    // .ppt & .pptx
    //txt
    //zip
    //epub
    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     * Supported Formats: doc, docx, xls, xlsx, ppt, pdf, epub, txt, zip
     *
     * @return The intent for opening a file with Intent.createChooser()
     */
    val documentIntent: Intent
        get() {
            val mimeTypes = arrayOf(
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .doc & .docx
                "application/pdf",  //pdf
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xls & .xlsx
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // .ppt & .pptx
                "text/plain",  //txt
                "application/zip",  //zip
                "application/epub+zip" //epub
            )
            return pickIntentFromMimeType(mimeTypes)
        }

    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     *
     * @return The intent for opening a file with Intent.createChooser()
     */
    fun pickIntentFromMimeType(mimeTypes: Array<String>?): Intent {
        // Implicitly allow the user to select a particular kind of data
//        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        // The MIME data type filter
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        /* Commented by Prashant Adesara - START API 30 */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
//            if (mimeTypes.length > 0) {
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
//            }
//        } else {
//            String mimeTypesStr = "";
//            for (String mimeType : mimeTypes) {
//                mimeTypesStr += mimeType + "|";
//            }
//            intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
//        }
        /* Commented by Prashant Adesara - END API 30*/

        //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "IMAGE_NAME.pdf")));
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    @JvmStatic
    fun fileCopyFromCache(context: Context, uri: Uri): String? {
        val file = File(context.cacheDir, uri.lastPathSegment)
        try {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (inputStream!!.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                    return file.path
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun copyFileToDest(src: File, dst: File) {
        try {
            sout("copyFileNew:: Source File to copy:: $src >> $dst")
            val input = FileInputStream(src)
            val output = FileOutputStream(dst)
            val buffer = ByteArray(1024)
            var read: Int
            while (input.read(buffer).also { read = it } > 0) {
                output.write(buffer, 0, read)
            }
            //Utils.sout("copyFileNew Dest:: " + dst.exists());
            input.close()
            output.close()
        } catch (e: Exception) {
            getErrors(e)
        }
    }
}