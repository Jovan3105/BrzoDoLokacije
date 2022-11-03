package imi.projekat.hotspot.UI.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.LoginActivity
import imi.projekat.hotspot.ModeliZaZahteve.ForgotPasswordResponse
import imi.projekat.hotspot.ModeliZaZahteve.NewPasswordDTS
import imi.projekat.hotspot.ModeliZaZahteve.VerificationCodeDTS
import imi.projekat.hotspot.Ostalo.APIservis
import imi.projekat.hotspot.R
import imi.projekat.hotspot.databinding.ActivityForgottenPasswordBinding
import imi.projekat.hotspot.databinding.FragmentForgotPasswordBinding
import imi.projekat.hotspot.databinding.FragmentProfileBinding
import kotlinx.android.synthetic.main.forgottenpasswordform1.view.*
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ForgotPassword : Fragment() {
    private lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var userName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentForgotPasswordBinding.bind(view)
        forma1.recoverButton.setOnClickListener {
            sendData()

        }
        binding.forma2.sendButton.setOnClickListener {
            sendVerCode()
        }

        binding.forma3.confirmButton.setOnClickListener {
            sendNewPassword()
        }

    }

    private fun sendData(){
        if(binding.forma1.EmailRecover.text.toString().isBlank()){
            //binding.logInLayout.Password.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            Toast.makeText(this.requireContext(), R.string.InsertYourEmailOrUsername, Toast.LENGTH_SHORT).show()
            return
        }
        val obj=binding.forma1.EmailRecover.text.toString()
        val retrofitData= APIservis.Servis.ForgotPasswordCall(obj)

        retrofitData.enqueue(object : Callback<ForgotPasswordResponse?> {
            override fun onResponse(call: Call<ForgotPasswordResponse?>, response: Response<ForgotPasswordResponse?>) {
                val responseBody=response.body()
                //val myStringBuilder=StringBuilder()
                if(response.code()!=200){
                    val gson = Gson()
                    val type = object : TypeToken<ForgotPasswordResponse>() {}.type

                    var errorResponse: ForgotPasswordResponse = gson.fromJson(response.errorBody()!!.charStream(), type)
//                    val resourceID = MainActivity.companion.getContext().resources.getIdentifier(
//                        errorResponse.message,
//                        "string",
//                        MainActivity.companion.getContext().packageName
//                    )
                    Toast.makeText(requireContext(),errorResponse.message, Toast.LENGTH_SHORT).show()
                    return
                }
                else if(responseBody!=null){
                    Toast.makeText(requireContext(),responseBody.toString(), Toast.LENGTH_SHORT).show()
                    binding.forma1.root.visibility= View.GONE
                    binding.forma2.root.visibility=View.VISIBLE
                    userName=obj
                    return
                }

            }

            override fun onFailure(call: Call<ForgotPasswordResponse?>, t: Throwable) {
                Toast.makeText(requireContext(),t.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun sendVerCode(){
        if(binding.forma2.VerificationCode.text.toString().isBlank()){
            Log.d("Tag",userName)
            //binding.logInLayout.Password.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            Toast.makeText(this.requireContext(), R.string.InsertYourVerificationCode, Toast.LENGTH_SHORT).show()
            return
        }
        val obj= VerificationCodeDTS(username = userName, code = binding.forma2.VerificationCode.text.toString())
        val retrofitData= APIservis.Servis.SendVerificationCode(obj)

        retrofitData.enqueue(object : Callback<ForgotPasswordResponse?> {
            override fun onResponse(call: Call<ForgotPasswordResponse?>, response: Response<ForgotPasswordResponse?>) {
                val responseBody=response.body()
                //val myStringBuilder=StringBuilder()
                if(response.code()!=200){
                    val gson = Gson()
                    val type = object : TypeToken<ForgotPasswordResponse>() {}.type

                    var errorResponse: ForgotPasswordResponse = gson.fromJson(response.errorBody()!!.charStream(), type)
//                    val resourceID = MainActivity.companion.getContext().resources.getIdentifier(
//                        errorResponse.message,
//                        "string",
//                        MainActivity.companion.getContext().packageName
//                    )
                    Toast.makeText(requireContext(),errorResponse.message, Toast.LENGTH_SHORT).show()
                    return
                }
                else if(responseBody!=null){
                    Toast.makeText(requireContext(),responseBody.toString(), Toast.LENGTH_SHORT).show()
                    binding.forma2.root.visibility=View.GONE
                    binding.forma3.root.visibility=View.VISIBLE
                    return
                }

            }

            override fun onFailure(call: Call<ForgotPasswordResponse?>, t: Throwable) {
                Toast.makeText(requireContext(),t.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun sendNewPassword(){
        if(binding.forma3.newPassword.text.toString().isBlank()){
            //binding.logInLayout.Password.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            Toast.makeText(this.requireContext(), R.string.InsertYourNewPassword, Toast.LENGTH_SHORT).show()
            return
        }

        if(binding.forma3.ConfirmNewPassword.text.toString().isBlank()){
            Toast.makeText(this.requireContext(), R.string.PasswordsAreNotTheSame, Toast.LENGTH_SHORT).show()
            return
        }

        if(!binding.forma3.newPassword.text.toString().equals(binding.forma3.ConfirmNewPassword.text.toString())){
            Toast.makeText(this.requireContext(), R.string.ConfirmPasswordError, Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("Tag",binding.forma3.newPassword.text.toString())
        val obj= NewPasswordDTS(username = userName, newpassword = binding.forma3.newPassword.text.toString())
        val retrofitData= APIservis.Servis.SendNewPassword(obj)

        retrofitData.enqueue(object : Callback<ForgotPasswordResponse?> {
            override fun onResponse(call: Call<ForgotPasswordResponse?>, response: Response<ForgotPasswordResponse?>) {
                val responseBody=response.body()
                //val myStringBuilder=StringBuilder()
                if(response.code()!=200){
                    val gson = Gson()
                    val type = object : TypeToken<ForgotPasswordResponse>() {}.type

                    var errorResponse: ForgotPasswordResponse = gson.fromJson(response.errorBody()!!.charStream(), type)
//                    val resourceID = MainActivity.companion.getContext().resources.getIdentifier(
//                        errorResponse.message,
//                        "string",
//                        MainActivity.companion.getContext().packageName
//                    )
                    Toast.makeText(requireContext(),errorResponse.message, Toast.LENGTH_SHORT).show()
                    return
                }
                else if(responseBody!=null){
                    Toast.makeText(requireContext(),responseBody.toString(), Toast.LENGTH_SHORT).show()
                    startLoginActivity()
                    return
                }

            }

            override fun onFailure(call: Call<ForgotPasswordResponse?>, t: Throwable) {
                Toast.makeText(requireContext(),t.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun startLoginActivity() {
        findNavController().navigate(R.id.loginAndRegister)
    }
}