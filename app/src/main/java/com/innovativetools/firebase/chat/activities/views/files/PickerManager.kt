package com.innovativetools.firebase.chat.activities.views.files

import com.innovativetools.firebase.chat.activities.managers.Utils.sout
import com.innovativetools.firebase.chat.activities.managers.Utils.getErrors
import com.innovativetools.firebase.chat.activities.views.files.PickerManagerCallbacks
import android.app.Activity
import com.innovativetools.firebase.chat.activities.views.files.CallBackTask
import com.innovativetools.firebase.chat.activities.views.files.UUtils
import android.webkit.MimeTypeMap
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.innovativetools.firebase.chat.activities.async.TaskRunner
import com.innovativetools.firebase.chat.activities.views.files.DownloadBaseTask
import java.io.File
import java.lang.Exception
import java.util.*

class PickerManager(
    private val context: Context,
    private val pickerManagerCallbacks: PickerManagerCallbacks,
    activity: Activity?
) : CallBackTask {
    private var isDriveFile = false
    private var isFromUnknownProvider = false

    //    private DownloadAsyncTask asyntask;
    private var unknownProviderCalledBefore = false
    fun getPath(uri: Uri, APILevel: Int) {
        val returnedPath: String?
        if (APILevel >= 19) {
            // Drive file was selected
            if (isOneDrive(uri) || isDropBox(uri) || isGoogleDrive(uri)) {
                isDriveFile = true
                downloadFile(uri)
            } else {
                returnedPath = UUtils.getRealPathFromURI_API19(context, uri)
                sout("~ ~ ~ ~ ~ RETURNED :::: $returnedPath")

                //Get the file extension
                val mime = MimeTypeMap.getSingleton()
                val subStringExtension =
                    returnedPath.toString().substring(returnedPath.toString().lastIndexOf(".") + 1)
                val extensionFromMime =
                    mime.getExtensionFromMimeType(context.contentResolver.getType(uri))

                // Path is null
                if (returnedPath == null || returnedPath == "") {
                    // This can be caused by two situations
                    // 1. The file was selected from a third party app and the data column returned null (for example EZ File Explorer)
                    // Some file providers (like EZ File Explorer) will return a URI as shown below:
                    // content://es.fileexplorer.filebrowser.ezfilemanager.externalstorage.documents/document/primary%3AFolderName%2FNameOfFile.mp4
                    // When you try to read the _data column, it will return null, without trowing an exception
                    // In this case the file need to copied/created a new file in the temporary folder
                    // 2. There was an error
                    // In this case call PickerManagerOnCompleteListener and get/provide the reason why it failed

                    //We first check if it was called before, avoiding multiple calls
                    if (!unknownProviderCalledBefore) {
                        unknownProviderCalledBefore = true
                        if (uri.scheme != null && uri.scheme == ContentResolver.SCHEME_CONTENT) {
                            //Then we check if the _data colomn returned null
                            if (UUtils.errorReason() != null && UUtils.errorReason() == "dataReturnedNull") {
                                isFromUnknownProvider = true
                                //Copy the file to the temporary folder
                                downloadFile(uri)
                                return
                            } else if (UUtils.errorReason() != null && UUtils.errorReason()
                                    ?.contains("column '_data' does not exist") == true
                            ) {
                                isFromUnknownProvider = true
                                //Copy the file to the temporary folder
                                downloadFile(uri)
                                return
                            } else if (UUtils.errorReason() != null && UUtils.errorReason() == "uri") {
                                isFromUnknownProvider = true
                                //Copy the file to the temporary folder
                                downloadFile(uri)
                                return
                            }
                        }
                    }
                    //Else an error occurred, get/set the reason for the error
                    pickerManagerCallbacks.PickerManagerOnCompleteListener(
                        returnedPath,
                        false,
                        false,
                        false,
                        UUtils.errorReason()
                    )
                } else {
                    // This can be caused by two situations
                    // 1. The file was selected from an unknown provider (for example a file that was downloaded from a third party app)
                    // 2. getExtensionFromMimeType returned an unknown mime type for example "audio/mp4"
                    //
                    // When this is case we will copy/write the file to the temp folder, same as when a file is selected from Google Drive etc.
                    // We provide a name by getting the text after the last "/"
                    // Remember if the extension can't be found, it will not be added, but you will still be able to use the file
                    //Todo: Add checks for unknown file extensions
                    if (subStringExtension != "jpeg" && subStringExtension != extensionFromMime && uri.scheme != null && uri.scheme == ContentResolver.SCHEME_CONTENT) {
                        isFromUnknownProvider = true
                        downloadFile(uri)
                        return
                    }

                    // Path can be returned, no need to make a "copy"
                    //Wrong PATH : Prashant Adesara
                    pickerManagerCallbacks.PickerManagerOnCompleteListener(
                        returnedPath,
                        false,
                        false,
                        true,
                        ""
                    )
                }
            }
        } else {
            //Todo: Test API <19
            returnedPath = UUtils.getRealPathFromURI_BelowAPI19(context, uri)
            pickerManagerCallbacks.PickerManagerOnCompleteListener(
                returnedPath,
                false,
                false,
                true,
                ""
            )
        }
    }

    // Create a new file from the Uri that was selected
    private fun downloadFile(uri: Uri) {
//        asyntask = new DownloadAsyncTask(uri, context, this, mActivity);
//        asyntask.execute();
        val runner = TaskRunner()
        runner.executeAsync(DownloadBaseTask(uri, context, this))
    }

    // End the "copying" of the file
    fun cancelTask() {
//        if (asyntask!=null){
//            asyntask.cancel(true);
//            deleteTemporaryFile(context);
//        }
        try {
            deleteTemporaryFile(context)
        } catch (e: Exception) {
            getErrors(e)
        }
    }

    fun wasLocalFileSelected(uri: Uri): Boolean {
        return !isDropBox(uri) && !isGoogleDrive(uri) && !isOneDrive(uri)
    }

    // Check different providers
    private fun isDropBox(uri: Uri): Boolean {
        return uri.toString().lowercase(Locale.getDefault()).contains("content://com.dropbox.")
    }

    private fun isGoogleDrive(uri: Uri): Boolean {
        return uri.toString().lowercase(Locale.getDefault()).contains("com.google.android.apps")
    }

    private fun isOneDrive(uri: Uri): Boolean {
        return uri.toString().lowercase(Locale.getDefault())
            .contains("com.microsoft.skydrive.content")
    }

    // PickerManager callback Listeners
    override fun PickerManagerOnUriReturned() {
        pickerManagerCallbacks.PickerManagerOnUriReturned()
    }

    override fun PickerManagerOnPreExecute() {
        pickerManagerCallbacks.PickerManagerOnStartListener()
    }

    override fun PickerManagerOnProgressUpdate(progress: Int) {
        pickerManagerCallbacks.PickerManagerOnProgressUpdate(progress)
    }

    override fun PickerManagerOnPostExecute(
        path: String?,
        wasDriveFile: Boolean,
        wasSuccessful: Boolean,
        reason: String?
    ) {
        unknownProviderCalledBefore = false
        if (wasSuccessful) {
            if (isDriveFile) {
                pickerManagerCallbacks.PickerManagerOnCompleteListener(path, true, false, true, "")
            } else if (isFromUnknownProvider) {
                pickerManagerCallbacks.PickerManagerOnCompleteListener(path, false, true, true, "")
            }
        } else {
            if (isDriveFile) {
                pickerManagerCallbacks.PickerManagerOnCompleteListener(
                    path,
                    true,
                    false,
                    false,
                    reason
                )
            } else if (isFromUnknownProvider) {
                pickerManagerCallbacks.PickerManagerOnCompleteListener(
                    path,
                    false,
                    true,
                    false,
                    reason
                )
            }
        }
    }

    // Delete the temporary folder
    fun deleteTemporaryFile(context: Context) {
        val folder = context.getExternalFilesDir("Temp")
        if (folder != null) {
            if (deleteDirectory(folder)) {
                Log.i("PickerManager ", "Prashant  deleteDirectory was called")
            }
        }
    }

    private fun deleteDirectory(path: File): Boolean {
        if (path.exists()) {
            val files = path.listFiles() ?: return false
            for (file in files) {
                if (file.isDirectory) {
                    deleteDirectory(file)
                } else {
                    val wasSuccessful = file.delete()
                    if (wasSuccessful) {
                        Log.i("Deleted ", "Prashant successfully")
                    }
                }
            }
        }
        return path.delete()
    }
}