package com.devjs.randomchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devjs.randomchat.databinding.ItemChatBoxBinding
import com.devjs.randomchat.model.Chat

class ChatAdapter(private val uid: String) : ListAdapter<Chat, ChatAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemChatBoxBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            if (chat.type != uid) {
                binding.myChatTextView.isVisible = false

                binding.otherNameTextView.text = "상대방"
                binding.otherChatTextView.text = chat.message
            } else {
                binding.otherChatTextView.isVisible = false

                binding.myNameTextView.text = "나"
                binding.myChatTextView.text = chat.message
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Chat>() {
            override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
                return oldItem == newItem
            }
        }
    }
}