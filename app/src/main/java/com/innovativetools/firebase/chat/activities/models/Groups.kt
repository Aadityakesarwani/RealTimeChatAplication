package com.innovativetools.firebase.chat.activities.models

import java.io.Serializable

class Groups : Serializable {
    var id: String? = null
    var groupName: String? = null
    var admin: String? = null
    var groupImg: String? = null
    var lastMsg: String? = null
    var lastMsgTime: String? = null
    var type: String? = null
    var members: MutableList<String>? = null
    var createdAt: String? = null
    var isActive = false
    var sendMessageSetting // 0 = All Participants, 1 = Only Admin
            = 0

    override fun toString(): String {
        return "Groups{" +
                "id='" + id + '\'' +
                ", groupName='" + groupName + '\'' +
                ", admin='" + admin + '\'' +
                ", groupImg='" + groupImg + '\'' +
                ", lastMsg='" + lastMsg + '\'' +
                ", lastMsgTime='" + lastMsgTime + '\'' +
                ", members=" + members +
                ", createdAt='" + createdAt + '\'' +
                ", active='" + isActive + '\'' +
                ", sendMessageSetting='" + sendMessageSetting + '\'' +
                '}'
    }
}