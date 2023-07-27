package com.example.chatapplication.ui

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.chatapplication.R
import com.example.chatapplication.adapter.MessageAdapter
import com.example.chatapplication.databinding.ActivityChatBinding
import com.example.chatapplication.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private var senderId: String? = null
    private var chatId: String? = null
    private var receiverId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbarId)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("imageUrl")
        Glide.with(this).load(profile).placeholder(R.drawable.placeholder).into(binding.profileImageId)
        binding.nameId.text = name
        binding.imageViewBackId.setOnClickListener { finish() }
        verifyChatId()

        binding.imageView4.setOnClickListener {
            if (binding.yourMessage.text!!.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            } else {
                storeData(binding.yourMessage.text.toString())
            }
        }
    }

    private fun verifyChatId() {
        receiverId = intent.getStringExtra("uid")
        senderId = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseDatabase.getInstance().reference.child("presence").child(receiverId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status == "Online") {
                            binding.statusId.text = status
                            binding.statusId.visibility = View.VISIBLE
                        }else if(status == "typing..."){
                            binding.statusId.text = status
                            binding.statusId.visibility = View.VISIBLE
                        }
                        else {
                            binding.statusId.visibility = View.GONE
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })

        val handler = Handler()
        binding.yourMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                FirebaseDatabase.getInstance().reference.child("presence").child(senderId!!).setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }
            var userStoppedTyping = Runnable {
                FirebaseDatabase.getInstance().reference.child("presence").child(senderId!!).setValue("Online")
            }
        })
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        chatId = senderId + receiverId
        val reverseChatId = receiverId + senderId


        val reference = FirebaseDatabase.getInstance().getReference("chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(chatId!!)) {
                    getData(chatId)
                } else if (snapshot.hasChild(reverseChatId)) {
                    chatId = reverseChatId
                    getData(chatId)
                }
            }


            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun getData(chatId: String?) {
        FirebaseDatabase.getInstance().getReference("chats")
            .child(chatId!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = arrayListOf<Message>()
                    for (show in snapshot.children) {
                        list.add(show.getValue(Message::class.java)!!)
                    }
                    binding.recyclerView2.adapter = MessageAdapter(this@ChatActivity, list)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun storeData(textMessage: String) {
        val currentTime: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val chat = Message(
            senderId,
            textMessage,
            currentTime
        )

        val reference = FirebaseDatabase.getInstance().getReference("chats").child(chatId!!)
        reference.child(reference.push().key!!).setValue(chat).addOnCompleteListener {
            if (it.isSuccessful) {
                binding.yourMessage.text = null
                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Message not sent", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        FirebaseDatabase.getInstance().reference.child("presence").child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        FirebaseDatabase.getInstance().reference.child("presence").child(currentId!!).setValue("Offline")
    }
}