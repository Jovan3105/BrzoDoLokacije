package imi.projekat.hotspot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.ModeliZaZahteve.*
import imi.projekat.hotspot.Ostalo.APIservis
import imi.projekat.hotspot.databinding.ActivityForgottenPasswordBinding
import imi.projekat.hotspot.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgottenPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgottenPasswordBinding
    private lateinit var userName:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityForgottenPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.forma1.recoverButton.setOnClickListener {
            sendData()

        }
        binding.forma2.sendButton.setOnClickListener {
            sendVerCode()
        }


    }
    private fun sendData(){
        if(binding.forma1.EmailRecover.text.toString().isBlank()){
            //binding.logInLayout.Password.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            Toast.makeText(this, R.string.InsertYourEmailOrUsername, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@ForgottenPasswordActivity,errorResponse.message, Toast.LENGTH_SHORT).show()
                    return
                }
                else if(responseBody!=null){
                    Toast.makeText(this@ForgottenPasswordActivity,responseBody.toString(), Toast.LENGTH_SHORT).show()
                    binding.forma1.root.visibility= View.GONE
                    binding.forma2.root.visibility=View.VISIBLE
                    userName=obj
                    return
                }

            }

            override fun onFailure(call: Call<ForgotPasswordResponse?>, t: Throwable) {
                Toast.makeText(this@ForgottenPasswordActivity,t.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun sendVerCode(){
        Log.d("Tag",userName)
        if(binding.forma2.VerificationCode.text.toString().isBlank()){
            Log.d("Tag",userName)
            //binding.logInLayout.Password.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            Toast.makeText(this, R.string.InsertYourVerificationCode, Toast.LENGTH_SHORT).show()
            return
        }
        val obj=VerificationCodeDTS(username = userName, code = binding.forma2.VerificationCode.text.toString())
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
                    Toast.makeText(this@ForgottenPasswordActivity,errorResponse.message, Toast.LENGTH_SHORT).show()
                    return
                }
                else if(responseBody!=null){
                    Toast.makeText(this@ForgottenPasswordActivity,responseBody.toString(), Toast.LENGTH_SHORT).show()
                    return
                }

            }

            override fun onFailure(call: Call<ForgotPasswordResponse?>, t: Throwable) {
                Toast.makeText(this@ForgottenPasswordActivity,t.message, Toast.LENGTH_SHORT).show()
            }
        })

    }
}