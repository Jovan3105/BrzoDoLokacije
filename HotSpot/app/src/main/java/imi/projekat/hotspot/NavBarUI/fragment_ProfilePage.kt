package imi.projekat.hotspot.NavBarUI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import imi.projekat.hotspot.R


import androidx.fragment.app.Fragment
import com.auth0.android.jwt.JWT
import imi.projekat.hotspot.HomePageActivity
import imi.projekat.hotspot.LoginActivity
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.databinding.FragmentProfileBinding
import kotlinx.android.synthetic.main.fragment_profile.*

class FragmentProfilePage:Fragment() {
    private lateinit var binding:FragmentProfileBinding
    private lateinit var profileImage:ImageView
    private lateinit var username:TextView
    private lateinit var email:TextView
    private lateinit var jwt: JWT
   // private lateinit var usernameToken:String
   // private lateinit var emailToken:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentProfileBinding.inflate(inflater)
        val view=inflater.inflate(R.layout.fragment_profile,container,false)
        val token=MenadzerSesije.getToken(requireContext())
        if(token != null)
        {
            jwt=JWT(token)
            val usernameToken=jwt.getClaim("username").asString()
            val emailToken=jwt.getClaim("email").asString()
            profileImage=view.findViewById(binding.profileImage.id)
            username=view.findViewById(binding.usernameText.id)
            email=view.findViewById(binding.emailText.id)
            username.text=usernameToken
            email.text=emailToken

        }

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.logoutButton.setOnClickListener {
            logout1()
        }
    }

    private fun logout1(){
        MenadzerSesije.clearData(requireContext())
        (activity as HomePageActivity?)?.logout()
    }

}