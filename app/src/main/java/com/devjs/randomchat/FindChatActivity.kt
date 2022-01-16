package com.devjs.randomchat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devjs.randomchat.databinding.ActivityFindChatBinding
import com.devjs.randomchat.firebase.database.DatabaseUtil.roomDB
import com.devjs.randomchat.model.ChatRoom
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FindChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFindChatBinding

    private var id: String? = null

    // database listener
    private lateinit var waitingChatValueEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 입장 가능한 채팅방 찾기
        findChatRoom(
            findRoomHandler = { chatRoom ->
                Log.d(TAG, "입장 가능한 채팅방을 발견했습니다.")
                chatRoom.other = true
                updateChatRoom(
                    chatRoom,
                    successHandler = {
                        startChatActivity(chatRoom.id, "other")
                    },
                    errorHandler = {
                        Toast.makeText(this, "채팅을 시작하는 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            },
            noRoomHandler = {
                Log.d(TAG, "입장 가능한 채팅방이 없습니다.")
                makeChatRoom(
                    successHandler = {
                        Log.d(TAG, "채팅방이 생성되었습니다.")
                        waitingOther(
                            successHandler = {
                                Log.d(TAG, "채팅 상대를 발견했습니다.")
                                this.id?.let {
                                    startChatActivity(it, "owner")
                                }
                            }
                        )
                    },
                    errorHandler = {
                        Toast.makeText(this, "채팅을 시작하는 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        id?.let {
            deleteChatRoom(
                it,
                successHandler = {
                    Toast.makeText(this, "채팅 검색이 종료되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                },
                errorHandler = {}
            )
        }
        roomDB.removeEventListener(waitingChatValueEventListener)
    }

    private fun findChatRoom(findRoomHandler: (ChatRoom) -> Unit, noRoomHandler: () -> Unit) {
        setLoadingStatusText("입장 가능한 채팅방이 있는지 검색중입니다.")
        roomDB.addListenerForSingleValueEvent(object : ValueEventListener {
            private var chatRoom: ChatRoom? = null

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { dataSnapshot ->
                    val chatRoom = dataSnapshot.getValue(ChatRoom::class.java)
                    chatRoom?.let {
                        if (it.other.not()) {
                            this.chatRoom = it
                            return@forEach
                        }
                    }
                }

                if (this.chatRoom != null) {
                    findRoomHandler(this.chatRoom!!)
                } else {
                    noRoomHandler()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun makeChatRoom(successHandler: () -> Unit, errorHandler: () -> Unit) {
        setLoadingStatusText("입장 가능한 채팅방이 없어 채팅방을 생성 중입니다.")

        val id = System.currentTimeMillis().toString()
        val chatRoom = ChatRoom(id, owner = true, other = false)
        roomDB.child(id).setValue(chatRoom)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    this.id = id
                    successHandler()
                } else {
                    Log.e(TAG, "채팅방 생성 중 에러 발생")
                    errorHandler()
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "채팅방 생성 중 에러 발생")
                Log.e(TAG, it.toString())
                errorHandler()
            }
    }

    private fun waitingOther(successHandler: () -> Unit) {
        setLoadingStatusText("상대방을 기다리는 중입니다.")

        this.id?.let { id ->
            waitingChatValueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatRoom = snapshot.getValue(ChatRoom::class.java)

                    chatRoom?.let {
                        if (it.other) {
                            successHandler()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }

            roomDB.child(id).addValueEventListener(waitingChatValueEventListener)
        }
    }

    private fun updateChatRoom(chatRoom: ChatRoom, successHandler: () -> Unit, errorHandler: () -> Unit) {
        roomDB.child(chatRoom.id).setValue(chatRoom)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    successHandler()
                } else {
                    Log.e(TAG, "채팅방을 업데이트 하는 중 에러 발생")
                    errorHandler()
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "채팅방을 업데이트 하는 중 에러 발생")
                Log.e(TAG, it.toString())
                errorHandler()
            }
    }

    private fun deleteChatRoom(id: String, successHandler: () -> Unit, errorHandler: () -> Unit) {
        roomDB.child(id).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "채팅방 삭제 완료")
                    successHandler()
                } else {
                    Log.e(TAG, "채팅방을 삭제하는 중 에러 발생")
                    errorHandler()
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "채팅방을 삭제하는 중 에러 발생")
                Log.e(TAG, it.toString())
                errorHandler()
            }
    }

    private fun setLoadingStatusText(text: String) {
        binding.loadingStatusTextView.text = text
    }

    private fun startChatActivity(id: String, type: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.apply {
            putExtra("id", id)
            putExtra("type", type)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "FindChatActivity"
    }
}