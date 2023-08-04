package com.innovativetools.firebase.chat.activities.views.files

interface PickerManagerCallbacks {
    fun PickerManagerOnUriReturned()
    fun PickerManagerOnStartListener()
    fun PickerManagerOnProgressUpdate(progress: Int)
    fun PickerManagerOnCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    )
}