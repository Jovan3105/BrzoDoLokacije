package imi.projekat.hotspot.UI.Profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.auth0.android.jwt.JWT
import com.google.android.material.textfield.TextInputLayout
import imi.projekat.hotspot.LoginActivity
import imi.projekat.hotspot.ModeliZaZahteve.loginDTS
import imi.projekat.hotspot.ModeliZaZahteve.signUpDTS
import imi.projekat.hotspot.Ostalo.APIservis
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.R
import imi.projekat.hotspot.databinding.FragmentLoginAndRegisterBinding
import imi.projekat.hotspot.databinding.FragmentMyProfileBinding

class MyProfileFragment : Fragment() {
    private lateinit var binding: FragmentMyProfileBinding
    private lateinit var profileImage:ImageView
    private lateinit var username:TextView
    private lateinit var email:TextView
    private lateinit var jwt: JWT


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMyProfileBinding.inflate(inflater)
        val view=inflater.inflate(R.layout.fragment_my_profile,container,false)
        val token=MenadzerSesije.getToken(requireContext())
        if(token != null)
        {
            jwt= JWT(token)
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
        binding= FragmentMyProfileBinding.bind(view)
        val ltrAnimacija= AnimationUtils.loadAnimation(this.requireContext(),R.anim.left_to_right)
        val rtlAnimacija= AnimationUtils.loadAnimation(this.requireContext(),R.anim.rigth_to_left)
        val btpAnimacija= AnimationUtils.loadAnimation(this.requireContext(),R.anim.bot_to_top)
        binding.button.setOnClickListener {
            Log.d("brzi","kocka")
        }

        binding.imageView2.setOnClickListener{
            findNavController().navigate(R.id.action_myProfileFragment_to_editProfileFragment)
        }


        binding.textView2.setOnClickListener{
            Toast.makeText(this.requireContext(), "Logout", Toast.LENGTH_SHORT).show()
            MenadzerSesije.clearData(this.requireContext())
            val intent = Intent(this.requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }


}