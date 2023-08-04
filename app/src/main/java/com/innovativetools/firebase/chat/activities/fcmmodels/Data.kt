package com.innovativetools.firebase.chat.activities.fcmmodels

class Data {
    var user: String?
    var icon: Int?
    private val username: String?
    var body: String?
    var title: String?
    var sent: String?
    var groups: String? = null
    var type: String?

    constructor(
        user: String?,
        icon: Int?,
        username: String?,
        body: String?,
        title: String?,
        sent: String?,
        type: String
    ) {
        this.user = user
        this.icon = icon
        this.username = username
        this.body = body
        this.title = title
        this.sent = sent
        this.type = type
    }

    constructor(
        user: String?,
        icon: Int,
        username: String?,
        body: String?,
        title: String?,
        sent: String?,
        groups: String?,
        type: String
    ) {
        this.user = user
        this.icon = icon
        this.username = username
        this.body = body
        this.title = title
        this.sent = sent
        this.groups = groups
        this.type = type
    }
}