package com.innovativetools.firebase.chat.activities.views.files

interface CallBackTask {
    fun PickerManagerOnUriReturned()
    fun PickerManagerOnPreExecute()
    fun PickerManagerOnProgressUpdate(progress: Int)
    fun PickerManagerOnPostExecute(
        path: String?,
        wasDriveFile: Boolean,
        wasSuccessful: Boolean,
        reason: String?
    )
}