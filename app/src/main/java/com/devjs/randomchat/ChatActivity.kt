package com.devjs.randomchat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.devjs.randomchat.adapter.ChatAdapter
import com.devjs.randomchat.databinding.ActivityChatBinding
import com.devjs.randomchat.firebase.database.DatabaseUtil.chatDB
import com.devjs.randomchat.model.Chat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding

    private lateinit var id: String
    private lateinit var type: String

    private lateinit var uid: String

    private lateinit var chatEventListener: ValueEventListener

    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVariables()
        bindViews()
    }

    override fun onDestroy() {
        super.onDestroy()

        chatDB.removeEventListener(chatEventListener)
    }

    private fun initVariables() {
        val id = intent.getStringExtra("id")
        val type = intent.getStringExtra("type")
        if (id == null || type == null) {
            Toast.makeText(this, "채팅방을 구성하는 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            this.id = id
            this.type = type
        }

        uid = UUID.randomUUID().toString()

        chatAdapter = ChatAdapter(uid)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = chatAdapter

        initChatEventListener()
    }

    private fun bindViews() {
        binding.sendButton.setOnClickListener {
            val chat = Chat(
                id = System.currentTimeMillis().toString(),
                type = this.uid,
                message = binding.chatEditText.text.toString(),
                createdAt = System.currentTimeMillis()
            )
            uploadChat(chat) {
                binding.chatEditText.text.clear()
            }
        }
    }

    private fun initChatEventListener() {
        chatEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList: List<Chat> = snapshot.children.map { it.getValue(Chat::class.java)!! }
                chatAdapter.submitList(chatList)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        chatDB.child(id).addValueEventListener(chatEventListener)
    }

    private fun uploadChat(chat: Chat, successHandler: () -> Unit) {
        chatDB.child(id).push().setValue(chat)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    successHandler()
                } else {
                    Toast.makeText(this, "채팅 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "채팅 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, it.toString())
            }
    }

    companion object {
        private const val TAG = "ChatActivity"
    }
}