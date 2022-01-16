package com.devjs.randomchat.model

data class Chat(
    val id: String,
    val type: String,
    var message: String,
    val createdAt: Long,
) {
    constructor() : this("", "", "", 0L)
}
