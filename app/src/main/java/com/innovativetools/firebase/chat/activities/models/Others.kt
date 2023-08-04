package com.innovativetools.firebase.chat.activities.models

import java.io.Serializable

class Others : Serializable {
    var isTyping = false
    var typingwith: String? = null

    constructor() {}
    constructor(typing: Boolean) {
        isTyping = typing
    }
}