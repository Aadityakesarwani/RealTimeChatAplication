package com.innovativetools.firebase.chat.activities.models

import com.innovativetools.firebase.chat.activities.constants.IConstants

class Chat {
    var id: String? = null
    var sender: String? = null
    var receiver: String? = null
    var message: String? = null
    var isMsgseen = false
    var datetime: String? = null
    var type //IMAGE
            : String? = null
    var imgPath //Full Image Path
            : String? = null

    //For uploading Recording
    var attachmentType: String? = null
    var attachmentName: String? = null
    var attachmentFileName: String? = null
    var attachmentPath: String? = null
    var attachmentData: String? = null
    var attachmentDuration: String? = null
    var attachmentSize: Long = 0

    //Default STARTED, once downloaded file completed, notify with COMPLETED : This variable not stored in Firebase DB
    var downloadProgress = IConstants.STARTED
    override fun toString(): String {
        return "Chat{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", message='" + message + '\'' +
                ", msgseen=" + isMsgseen +
                ", datetime='" + datetime + '\'' +
                ", type='" + type + '\'' +
                ", imgPath='" + imgPath + '\'' +
                ", attachmentFileName='" + attachmentFileName + '\'' +
                ", attachmentName='" + attachmentName + '\'' +
                ", attachmentPath='" + attachmentPath + '\'' +
                '}'
    }
}