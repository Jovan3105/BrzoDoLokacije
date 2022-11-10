package imi.projekat.hotspot.UI.Post

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.LoginActivityViewModel
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentCreatePostBinding
import imi.projekat.hotspot.databinding.FragmentLoginAndRegisterBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreatePostFragment : Fragment() {
    private lateinit var binding:FragmentCreatePostBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentCreatePostBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.KreirajPostResponse.collectLatest{
                if(it is BaseResponse.Error){
                    Toast.makeText(context, it.poruka, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    Log.d("SES","SESESSE")
                    Toast.makeText(context, it.data.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }



        binding.button2.setOnClickListener{
            val token:String= MenadzerSesije.getToken(requireContext()).toString()
            viewModel.KreirajPost("SES")
        }

        binding.button3.setOnClickListener{

        }
    }

}