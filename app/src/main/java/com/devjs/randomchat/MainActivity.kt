package com.devjs.randomchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devjs.randomchat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindViews()
    }

    private fun bindViews() {
        binding.startChatButton.setOnClickListener {
            startFindChatActivity()
        }
    }

    private fun startFindChatActivity() {
        val intent = Intent(this, FindChatActivity::class.java)
        startActivity(intent)
    }
}