package com.innovativetools.firebase.chat.activities.managers

import android.net.Uri
import com.innovativetools.firebase.chat.activities.managers.Utils.getErrors
import com.innovativetools.firebase.chat.activities.managers.Utils.sout
import com.google.firebase.storage.StorageReference
import com.innovativetools.firebase.chat.activities.managers.FirebaseUploader.UploadListener
import com.google.firebase.storage.UploadTask
import com.innovativetools.firebase.chat.activities.async.BaseTask
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import kotlin.Throws
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.storage.OnProgressListener
import com.innovativetools.firebase.chat.activities.R
import com.innovativetools.firebase.chat.activities.async.CustomCallable
import com.innovativetools.firebase.chat.activities.async.TaskRunner
import java.io.File
import java.lang.Exception

class FirebaseUploader(
    private val uploadRef: StorageReference,
    private val uploadListener: UploadListener
) {
    private var uploadTask: UploadTask? = null
    private var fileUri: Uri? = null
    fun uploadFile(file: File?) {
        val baseTask: BaseTask<*> = object : BaseTask<Any?>() {
            override fun setUiForLoading() {
                super.setUiForLoading()
            }

            override fun call(): Any? {
                try {
                    fileUri = Uri.fromFile(file)
                } catch (e: Exception) {
                    getErrors(e)
                }
                checkIfExists()
                return ""
            }

            override fun setDataAfterLoading(result: Any?) {}
        }
        val runner = TaskRunner()
        runner.executeAsync(baseTask as CustomCallable<R?>)
    }

    private fun checkIfExists() {
        uploadRef.downloadUrl.addOnSuccessListener { uri -> uploadListener.onUploadSuccess(uri.toString()) }
            .addOnFailureListener { upload() }
    }

    private fun upload() {
        sout("Upload fileURI:::: $fileUri")
        uploadTask = uploadRef.putFile(fileUri!!)
        uploadTask!!.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            uploadRef.downloadUrl
        }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    uploadListener.onUploadSuccess(downloadUri.toString())
                } else {
                    uploadListener.onUploadFail(task.exception!!.message)
                }
            }
            .addOnFailureListener { e -> uploadListener.onUploadFail(e.message) }
        uploadTask!!.addOnProgressListener { taskSnapshot ->
            val progress = 100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            uploadListener.onUploadProgress(progress.toInt())
        }
    }

    fun cancelUpload() {
        if (uploadTask != null && uploadTask!!.isInProgress) {
            uploadTask!!.cancel()
            uploadListener.onUploadCancelled()
        }
    }

    interface UploadListener {
        fun onUploadFail(message: String?)
        fun onUploadSuccess(downloadUrl: String?)
        fun onUploadProgress(progress: Int)
        fun onUploadCancelled()
    }
}