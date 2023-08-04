package com.innovativetools.firebase.chat.activities.models

import com.innovativetools.firebase.chat.activities.models.Chat
import java.io.Serializable

class DownloadFileEvent(var attachment: Chat, var position: Int) : Serializable {

    override fun toString(): String {
        return "DownloadFileEvent{" +
                "attachment=" + attachment +
                ", position=" + position +
                '}'
    }
}