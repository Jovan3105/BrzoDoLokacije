package imi.projekat.hotspot.UI.Profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.auth0.android.jwt.JWT
import imi.projekat.hotspot.LoginActivity
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentMyProfileBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.FileInputStream
import kotlin.properties.Delegates


class MyProfileFragment : Fragment() {
    private lateinit var binding: FragmentMyProfileBinding
    private lateinit var profileImage:ImageView
    private lateinit var username:TextView
    private lateinit var email:TextView
    private lateinit var jwt: JWT
    private var Photobitmap:Bitmap?=null
    private var followingNumber:Int = 0
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.getPhoto()
        binding=FragmentMyProfileBinding.inflate(inflater)
        val view=inflater.inflate(R.layout.fragment_my_profile,container,false)
        profileImage=view.findViewById(binding.profileImage.id)
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.liveProfilePhotoResponse.collectLatest{
                if(it is BaseResponse.Error){

                }
                if(it is BaseResponse.Success){
                    Log.d("SES","SESESSE")
                    if(it.data!=null)// da se istestira
                    {
                        val content = it.data!!.charStream().readText()
                        Log.d("slika",content)
                        val imageBytes = Base64.decode(content, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        profileImage.setImageBitmap(decodedImage)
                    }


                }
            }
        }
        val token=MenadzerSesije.getToken(requireContext())
        if(token != null)
        {
            jwt= JWT(token)
            val usernameToken=jwt.getClaim("username").asString()
            val emailToken=jwt.getClaim("email").asString()
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
        binding.followingButton.setOnClickListener {
            Log.d("brzi","kocka")
            findNavController().navigate(R.id.action_myProfileFragment_to_followingProfilesFragment)
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