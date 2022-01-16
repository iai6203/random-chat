package com.devjs.randomchat.model

data class ChatRoom(
    val id: String,
    var owner: Boolean,
    var other: Boolean,
) {
    constructor() : this("", true, false)
}
