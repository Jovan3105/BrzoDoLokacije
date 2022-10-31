package imi.projekat.hotspot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.ModeliZaZahteve.ForgotPasswordDTS
import imi.projekat.hotspot.ModeliZaZahteve.ForgotPasswordResponse
import imi.projekat.hotspot.ModeliZaZahteve.LoginResponse
import imi.projekat.hotspot.ModeliZaZahteve.loginDTS
import imi.projekat.hotspot.Ostalo.APIservis
import imi.projekat.hotspot.databinding.ActivityForgottenPasswordBinding
import imi.projekat.hotspot.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgottenPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgottenPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityForgottenPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recoverButton.setOnClickListener {
            sendData()

        }


    }
    private fun sendData(){
        if(binding.EmailRecover.text.toString().isBlank()){
            //binding.logInLayout.Password.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            Toast.makeText(this, R.string.InsertYourEmailOrUsername, Toast.LENGTH_SHORT).show()
            return
        }
        val obj=binding.EmailRecover.text.toString()
        Log.d("TAG", obj)
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
                    return
                }

            }

            override fun onFailure(call: Call<ForgotPasswordResponse?>, t: Throwable) {
                Toast.makeText(this@ForgottenPasswordActivity,t.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    override fun onBackPressed() {
        val intent = Intent(this@ForgottenPasswordActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
    }
}