package imi.projekat.hotspot.UI.LoginRegister

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import imi.projekat.hotspot.Dialogs.LoadingDialog
import imi.projekat.hotspot.LoginActivity
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.LoginActivityViewModel
import imi.projekat.hotspot.databinding.ActivityLoginBinding
import imi.projekat.hotspot.databinding.FragmentConfirmEmailBinding
import kotlinx.android.synthetic.main.custom_dialog.*


class ConfirmEmail : Fragment() {

    private lateinit var binding: FragmentConfirmEmailBinding
    private val args:ConfirmEmailArgs by navArgs()
    private val viewModel:LoginActivityViewModel by activityViewModels()
    private lateinit var textView3:TextView
    private lateinit var dugmeLogin:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentConfirmEmailBinding.inflate(inflater)
        val view=inflater.inflate(R.layout.fragment_confirm_email,container,false)
        textView3=view.findViewById(binding.textView3.id)
        dugmeLogin=view.findViewById(binding.loginButton.id)
        viewModel.liveValidationResponse.observe(viewLifecycleOwner){

            if(it is BaseResponse.Error){
                val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                textView3.text=id
            }

        }
        dugmeLogin.setOnClickListener{
            findNavController().navigate(R.id.action_confirmEmail_to_loginAndRegister)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var token=args.confirmationToken


        (activity as LoginActivity?)?.validateEmail(token)
    }



}