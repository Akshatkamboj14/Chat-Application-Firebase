package com.example.chatapplication.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.adapter.UserAdapter
import com.example.chatapplication.databinding.FragmentUserBinding
import com.example.chatapplication.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserFragment : Fragment() {

    private lateinit var binding: FragmentUserBinding
    private lateinit var list: ArrayList<User>
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserBinding.inflate(inflater)
        list = arrayListOf()
        getData()
        return binding.root
    }

    private fun getData() {

        FirebaseDatabase.getInstance().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()){
                        list.clear()
                        for (data in snapshot.children){
                            val model = data.getValue(User::class.java)
                            if (model?.uid != FirebaseAuth.getInstance().currentUser?.uid){
                                list.add(model!!)
                            }
                        }
                    }
                    list.shuffle()
                    binding.userRcy.apply {
                        userAdapter = UserAdapter(requireContext(), list)
                        adapter = userAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }

            })

    }
}