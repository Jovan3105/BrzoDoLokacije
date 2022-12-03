package imi.projekat.hotspot.UI.Profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.auth0.android.jwt.JWT
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
        viewModel.getUserWithID(args.userID)

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.UserInfoResponsee.collectLatest{
                if(it is BaseResponse.Error){
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    username=view.findViewById(binding.usernameText.id)
                    email=view.findViewById(binding.emailText.id)
                    username.text=it.data!!.username
                    email.text=it.data!!.email
                    var photoPath = it.data!!.photo
                    if(!photoPath.isNullOrEmpty()){
                        var pom2=photoPath.split("\\")
                        if(pom2.size==1)
                            pom2=photoPath.split("/")

                        viewModel.dajSliku(profileImage,"ProfileImages/"+pom2[2],requireContext())
                    }
                }
            }
        }


//        val token= MenadzerSesije.getToken(requireContext())
//        if(token != null)
//        {
//            jwt= JWT(token)
//            val usernameToken=jwt.getClaim("username").asString()
//            val emailToken=jwt.getClaim("email").asString()
//            username=view.findViewById(binding.usernameText.id)
//            email=view.findViewById(binding.emailText.id)
//            username.text=usernameToken
//            email.text=emailToken
//            var photoPath = jwt.getClaim("photo").asString()!!
//            if(!photoPath.isNullOrEmpty()){
//                var pom2=photoPath.split("\\")
//                if(pom2.size==1)
//                    pom2=photoPath.split("/")
//
//                viewModel.dajSliku(profileImage,"ProfileImages/"+pom2[2],this.requireContext())
//            }
//
//
//        }
        return view
    }

}