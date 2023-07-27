package com.example.chatapplication.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.chatapplication.R
import com.example.chatapplication.databinding.FragmentPofileBinding
import com.example.chatapplication.model.User
import com.example.chatapplication.ui.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentPofileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPofileBinding.inflate(layoutInflater)

        FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).get()
            .addOnSuccessListener {
                if (it.exists()){
                    val data = it.getValue(User::class.java)

                    binding.name.setText(data!!.name.toString())
                    binding.email.setText(data.email.toString())

                    Glide.with(requireContext()).load(data.imageUrl).placeholder(R.drawable.placeholder).into(binding.userImage)
                }
            }

        binding.logout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), SignInActivity::class.java))
            requireActivity().finish()
        }

        return binding.root
    }


}