package com.innovativetools.firebase.chat.activities.views.files

import com.innovativetools.firebase.chat.activities.managers.Utils.sout
import com.innovativetools.firebase.chat.activities.views.files.SDUtil.getStorageDirectories
import com.innovativetools.firebase.chat.activities.views.files.FileUtils.fileCopyFromCache
import com.innovativetools.firebase.chat.activities.managers.Utils.getErrors
import com.innovativetools.firebase.chat.activities.views.files.UUtils
import android.annotation.SuppressLint
import android.provider.DocumentsContract
import android.os.Environment
import com.innovativetools.firebase.chat.activities.views.files.SDUtil
import android.content.ContentUris
import android.content.Context
import android.content.CursorLoader
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.lang.Exception

internal object UUtils {
    private var failReason: String? = null
    fun errorReason(): String? {
        return failReason
    }

    @SuppressLint("NewApi")
    fun getRealPathFromURI_API19(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            sout("*** 1")
            if (isExternalStorageDocument(uri)) {
                sout("*** 1.1")
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                return if ("primary".equals(type, ignoreCase = true)) {
                    if (split.size > 1) {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else {
                        Environment.getExternalStorageDirectory().toString() + "/"
                    }
                } else {
                    // Some devices does not allow access to the SD Card using the UID, for example /storage/6551-1152/folder/video.mp4
                    // Instead, we first have to get the name of the SD Card, for example /storage/sdcard1/folder/video.mp4

                    // We first have to check if the device allows this access
                    if (File("storage" + "/" + docId.replace(":", "/")).exists()) {
                        return "/storage/" + docId.replace(":", "/")
                    }
                    // If the file is not available, we have to get the name of the SD Card, have a look at SDUtils
                    val availableExternalStorages = getStorageDirectories(context)
                    var root = ""
                    for (s in availableExternalStorages) {
                        root = if (split[1].startsWith("/")) {
                            s + split[1]
                        } else {
                            s + "/" + split[1]
                        }
                    }
                    if (root.contains(type)) {
                        "storage" + "/" + docId.replace(":", "/")
                    } else {
                        if (root.startsWith("/storage/") || root.startsWith("storage/")) {
                            root
                        } else if (root.startsWith("/")) {
                            "/storage$root"
                        } else {
                            "/storage/$root"
                        }
                    }
                }
            } else if (isRawDownloadsDocument(uri)) {
                sout("*** 1.2")
                val fileName = getFilePath(context, uri)
                val subFolderName = getSubFolders(uri)
                if (fileName != null) {
                    sout("Return from HERE ::::::::::::::::::::::::::: $subFolderName$fileName")
                    //                    return Environment.getExternalStorageDirectory().toString() + "/Download/" + subFolderName + fileName;
                }
                return try {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        id.toLong()
                    )
                    sout("---- fileName:: $subFolderName >> $fileName ==> $id >> $contentUri")
                    getDataColumn(context, contentUri, null, null)
                } catch (e: Exception) {
                    sout("Next Exception ::::::::::::::::::")
                    Environment.getExternalStorageDirectory()
                        .toString() + "/Download/" + subFolderName + fileName
                }
            } else if (isDownloadsDocument(uri)) {
                sout("*** 1.3")
                val fileName = getFilePath(context, uri)
                if (fileName != null) {
                    val path = Environment.getExternalStorageDirectory()
                        .toString() + "/Download/" + fileName
                    sout("*** 1.3 fileName:: $fileName >> $uri")
                    sout(
                        "*** 1.3 uri:: " + DocumentsContract.getDocumentId(uri) + " >Exist file?::: " + File(
                            path
                        ).exists()
                    )
                    //                    if (new File(path).exists()) {
//                    return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
//                    }
                }
                var id = DocumentsContract.getDocumentId(uri)
                sout("*** 1.3 :::id::: $id")
                if (id.startsWith("raw:")) {
                    id = id.replaceFirst("raw:".toRegex(), "")
                    val file = File(id)
                    if (file.exists()) return id
                }
                if (id.startsWith("raw%3A%2F")) {
                    id = id.replaceFirst("raw%3A%2F".toRegex(), "")
                    val file = File(id)
                    if (file.exists()) return id
                }
                //Solution from this link: https://github.com/Javernaut/WhatTheCodec/issues/2#issuecomment-779767009
                if (id.startsWith("msf:")) {
                    id = id.replaceFirst("msf:".toRegex(), "")
                    sout("msf 1: $id")
                    return fileCopyFromCache(context, uri)
                }
                if (id.startsWith("msf%3A%2F")) {
                    id = id.replaceFirst("msf%3A%2F".toRegex(), "")
                    sout("msf 2: $id")
                    return fileCopyFromCache(context, uri)
                }

//                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
//                return getDataColumn(context, contentUri, null, null);
                return try {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        id.toLong()
                    )
                    val data = getDataColumn(context, contentUri, null, null)
                    if (data != null) {
                        sout("*** 1.3 content:: $contentUri")
                        data
                    } else {
                        sout("*** 1.3 fileCopyFromCache")
                        fileCopyFromCache(context, uri)
                    }
                } catch (e: Exception) {
                    getErrors(e)
                    sout("Next Exception 1.3*** ::::::::::::::::::: " + e.message)
                    Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName
                }
            } else if (isMediaDocument(uri)) {
                sout("*** 1.4")
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            sout("*** 2")
            if (isGooglePhotosUri(uri)) {
                return uri.lastPathSegment
            }
            if (getDataColumn(context, uri, null, null) == null) {
                failReason = "dataReturnedNull"
            }
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            sout("*** 3")
            return uri.path
        }
        return null
    }

    private fun getSubFolders(uri: Uri): String {
        val replaceChars =
            uri.toString().replace("%2F", "/").replace("%20", " ").replace("%3A", ":")
        val bits = replaceChars.split("/").toTypedArray()
        val sub5 = bits[bits.size - 2]
        val sub4 = bits[bits.size - 3]
        val sub3 = bits[bits.size - 4]
        val sub2 = bits[bits.size - 5]
        val sub1 = bits[bits.size - 6]
        return if (sub1 == "Download") {
            "$sub2/$sub3/$sub4/$sub5/"
        } else if (sub2 == "Download") {
            "$sub3/$sub4/$sub5/"
        } else if (sub3 == "Download") {
            "$sub4/$sub5/"
        } else if (sub4 == "Download") {
            "$sub5/"
        } else {
            ""
        }
    }

    fun getRealPathFromURI_BelowAPI19(context: Context?, contentUri: Uri?): String {
        val proj = arrayOf(MediaStore.Video.Media.DATA)
        val loader = CursorLoader(context, contentUri, proj, null, null, null)
        val cursor = loader.loadInBackground()
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        cursor.moveToFirst()
        val result = cursor.getString(column_index)
        cursor.close()
        return result
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } catch (e: Exception) {
            failReason = e.message
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun getFilePath(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
        try {
            cursor = context.contentResolver.query(
                uri, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } catch (e: Exception) {
            failReason = e.message
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isRawDownloadsDocument(uri: Uri): Boolean {
        val uriToString = uri.toString()
        return uriToString.contains("com.android.providers.downloads.documents/document/raw")
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}