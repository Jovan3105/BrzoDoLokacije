package imi.projekat.hotspot.UI.Profile

import android.content.Intent
import android.os.Bundle
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.auth0.android.jwt.JWT
import imi.projekat.hotspot.LoginActivity
import imi.projekat.hotspot.MainActivity
import imi.projekat.hotspot.MapsActivity
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentMyProfileBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MyProfileFragment : Fragment() {
    private lateinit var binding: FragmentMyProfileBinding
    private lateinit var profileImage:ImageView
    private lateinit var username:TextView
    private lateinit var email:TextView
    private lateinit var jwt: JWT
    private lateinit var listaPostova:ArrayList<singlePost>
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var klikNaDugmeMyPosts=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.MyPostsResponse.collectLatest {
                    if(it is BaseResponse.Success){
                        if(it.data==null){
                            return@collectLatest
                        }
                        listaPostova= arrayListOf<singlePost>()
                        listaPostova.clear()
                        listaPostova= it.data as ArrayList<singlePost>
                        (activity as MainActivity?)!!.endLoadingDialog()
                        Log.d("BROJ Mojih POSTOVA", listaPostova.size.toString())

                        if(klikNaDugmeMyPosts){
                            Log.d("IDEM NA","MAPU")
                            val intent = Intent(requireContext(), MapsActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    if(it is BaseResponse.Error){
                        Log.d("MyPostsErr", it.toString())
                        (activity as MainActivity?)!!.endLoadingDialog()
                        Toast.makeText(requireContext(), "Error while loading posts", Toast.LENGTH_SHORT).show()
                    }
                    if(it is BaseResponse.Loading){

                    }
                }
            }
        }
        viewModel.getMyPosts()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMyProfileBinding.inflate(inflater)
        val view=inflater.inflate(R.layout.fragment_my_profile,container,false)
        profileImage=view.findViewById(binding.profileImage.id)

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
            var photoPath = jwt.getClaim("photo").asString()!!
            if(!photoPath.isNullOrEmpty()){

                var pom2=photoPath.split("\\")
                if(pom2.size==1)
                    pom2=photoPath.split("/")

                viewModel.dajSliku(profileImage,"ProfileImages/"+pom2[2],this.requireContext())
            }


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

        binding.PostsOnMapButton.setOnClickListener{
            if (!(this::listaPostova.isInitialized)) {
                (activity as MainActivity?)!!.startLoadingDialog()
                return@setOnClickListener
            }
            if(listaPostova.size==0){
                Toast.makeText(requireContext(), "You have no posts", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(requireContext(), MapsActivity::class.java)
            startActivity(intent)

        }


    }





}