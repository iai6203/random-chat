package com.devjs.randomchat.firebase.database

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object DatabaseUtil {
    private val database: FirebaseDatabase = Firebase.database

    val roomDB: DatabaseReference = database.reference.child("Rooms")
    val chatDB: DatabaseReference = database.reference.child("Chats")
}