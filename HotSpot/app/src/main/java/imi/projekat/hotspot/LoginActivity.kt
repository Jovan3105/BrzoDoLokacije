package imi.projekat.hotspot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.Interfaces.ApiInterface
import imi.projekat.hotspot.Modeli.LoginResponse
import imi.projekat.hotspot.Modeli.loginDTS
import imi.projekat.hotspot.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.time.Duration.Companion.seconds

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    val dijalog=LoadingDialog(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ltrAnimacija=AnimationUtils.loadAnimation(this,R.anim.left_to_right)
        val rtlAnimacija=AnimationUtils.loadAnimation(this,R.anim.rigth_to_left)
        val btpAnimacija=AnimationUtils.loadAnimation(this,R.anim.bot_to_top)





        binding.linearlayout1.startAnimation(btpAnimacija)


        binding.singUp.setOnClickListener{
            binding.singUp.startAnimation(ltrAnimacija)
            //Toast.makeText(this@LoginActivity, "You clicked on TextView1 'Click Me'.", Toast.LENGTH_SHORT).show()
            binding.singUp.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            binding.singUp.setTextColor(resources.getColor(R.color.white,null))
            binding.logIn.background=null
            binding.logIn.setTextColor(resources.getColor(R.color.pink1,null))
            binding.logInLayout.root.visibility=View.GONE
            binding.signUpLayout.root.visibility=View.VISIBLE

        }

        binding.logIn.setOnClickListener{
            //Toast.makeText(this@LoginActivity, "You clicked on TextView 2'Click Me'.", Toast.LENGTH_SHORT).show()
            binding.logIn.startAnimation(rtlAnimacija)
            binding.logIn.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            binding.logIn.setTextColor(resources.getColor(R.color.white,null))
            binding.singUp.background=null
            binding.singUp.setTextColor(resources.getColor(R.color.pink1,null))
            binding.logInLayout.root.visibility=View.VISIBLE
            binding.signUpLayout.root.visibility=View.GONE

        }

        binding.logInLayout.loginButton.setOnClickListener {
            getMyData()
            //val intent = Intent(this@LoginActivity, MainActivity::class.java)
            //startActivity(intent)
        }


        binding.signUpLayout.signUpDugme.setOnClickListener {

        }

    }

    private fun getMyData() {

        if(binding.logInLayout.Email.text.toString().isBlank()){
            //binding.logInLayout.Password.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            Toast.makeText(this@LoginActivity, "Insert your email or username", Toast.LENGTH_SHORT).show()
            return
        }

        if(binding.logInLayout.Password.text.toString().isBlank()){
            //binding.logInLayout.Password.background=resources.getDrawable(R.drawable.dugme_pozadina,null)
            Toast.makeText(this@LoginActivity, "Insert your password", Toast.LENGTH_SHORT).show()
            return
        }



        val retrofitBuilder=Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://10.0.2.2:5140/")
            .build()
            .create(ApiInterface::class.java)

        val obj=loginDTS(password = binding.logInLayout.Password.text.toString(), username = binding.logInLayout.Email.text.toString())

        val retrofitData=retrofitBuilder.loginCall(obj)
        dijalog.startLoading()
        retrofitData.enqueue(object : Callback<LoginResponse?> {
            override fun onResponse(call: Call<LoginResponse?>, response: Response<LoginResponse?>) {
                dijalog.isDismiss()
                val responseBody=response.body()
                //val myStringBuilder=StringBuilder()
                if(response.code()!=200){
                    val gson = Gson()
                    val type = object : TypeToken<LoginResponse>() {}.type
                    var errorResponse: LoginResponse = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Toast.makeText(this@LoginActivity, errorResponse.message, Toast.LENGTH_SHORT).show()
                    return
                }
                if(responseBody!=null){
                    Toast.makeText(this@LoginActivity, "You clicked on"+responseBody.toString(), Toast.LENGTH_SHORT).show()
                }


            }

            override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                dijalog.isDismiss()
                Toast.makeText(this@LoginActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}