package imi.projekat.hotspot.UI.Profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.auth0.android.jwt.JWT
import imi.projekat.hotspot.LoginActivity
import imi.projekat.hotspot.MainActivity
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.R
import imi.projekat.hotspot.UI.HomePage.SinglePost.SinglePostFragmentArgs
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentDrugiKorisnikBinding
import imi.projekat.hotspot.databinding.FragmentMyProfileBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Drugi_korisnik : Fragment() {
    private lateinit var binding: FragmentDrugiKorisnikBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var profileImage: ImageView
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var jwt: JWT
    private lateinit var dugme:Button
    private val args: Drugi_korisnikArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentDrugiKorisnikBinding.inflate(inflater)
        val view=inflater.inflate(R.layout.fragment_drugi_korisnik,container,false)
        profileImage=view.findViewById(binding.profileImage.id)

        val token=MenadzerSesije.getToken(requireContext())
        jwt= JWT(token!!)
        val idKorisnika= jwt.getClaim("id").asString()!!.toInt()

        if(idKorisnika==args.userID){
            (activity as MainActivity).bottomMenu.getItem(2).setChecked(true)
            (activity as MainActivity).bottomMenu.performIdentifierAction((activity as MainActivity).bottomMenu.getItem(2).itemId,0)
        }


        viewModel.getUserWithID(args.userID)



        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.UnfollowUserResponse.collectLatest{
                if(it is BaseResponse.Error){
                    Log.d("greska","Nije dobar zahtev za follow korisnika")
                }
                if(it is BaseResponse.Success){
                    dugme.text="Follow"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.FollowUserResponse.collectLatest{
                if(it is BaseResponse.Error){
                    Log.d("greska","Nije dobar zahtev za follow korisnika")
                }
                if(it is BaseResponse.Success){
                    dugme.text="Unfollow"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.UserInfoResponsee.collectLatest{
                if(it is BaseResponse.Error){
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    username=view.findViewById(binding.usernameText.id)
                    email=view.findViewById(binding.emailText.id)
                    dugme=view.findViewById(binding.followingButton.id)
                    if(it.data!!.following)
                    {
                        dugme.text="Unfollow"
                    }
                    else
                    {
                        dugme.text="Follow"
                    }
                    username.text=it.data!!.username
                    email.text=it.data!!.email


                    binding.PostsOnMapButton.setOnClickListener {
                        val action: NavDirections = Drugi_korisnikDirections.actionDrugiKorisnikToMapaZaPrikazPostova(args.userID)
                        findNavController().navigate(action)
                    }

                    var photoPath = it.data!!.photo
                    if(!photoPath.isNullOrEmpty()){
                        //var pom2=photoPath.split("\\")
//                        if(pom2.size==1){
//                            pom2=photoPath.split("/")
//                            viewModel.dajSliku(profileImage,"ProfileImages/"+pom2[0],requireContext())
//                        }
                        Log.d("SLIKA KOR",it.toString())
                        viewModel.dajSliku(profileImage,"ProfileImages/"+photoPath,requireContext())
                    }
                }
            }
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentDrugiKorisnikBinding.bind(view)

        binding.followingButton.setOnClickListener{
            FollowOrUnfollow()
        }


    }

    private fun FollowOrUnfollow(){
        if(dugme.text.toString()=="Follow")
        {
            viewModel.followUser(args.userID)
        }
        else
        {
            viewModel.unfollowUser(args.userID)
        }
    }


}