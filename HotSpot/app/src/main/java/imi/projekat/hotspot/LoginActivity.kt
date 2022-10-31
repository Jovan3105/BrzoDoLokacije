package imi.projekat.hotspot


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import imi.projekat.hotspot.ModeliZaZahteve.loginDTS
import imi.projekat.hotspot.ModeliZaZahteve.signUpDTS
import imi.projekat.hotspot.Ostalo.APIservis
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.ViewModeli.LoginActivityViewModel
import imi.projekat.hotspot.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.login_tab_fragment.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel by viewModels<LoginActivityViewModel>()
    val dijalog=LoadingDialog(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val ltrAnimacija=AnimationUtils.loadAnimation(this,R.anim.left_to_right)
        val rtlAnimacija=AnimationUtils.loadAnimation(this,R.anim.rigth_to_left)
        val btpAnimacija=AnimationUtils.loadAnimation(this,R.anim.bot_to_top)

        binding.logInLayout.ForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgottenPasswordActivity::class.java)
            startActivity(intent)
        }

        viewModel.liveLoginResponse.observe(this){
            dijalog.isDismiss()
            when(it){
                is BaseResponse.Loading->{
                    dijalog.startLoading()
                }
                is BaseResponse.Success->{
                    Toast.makeText(this@LoginActivity, it.data.toString(), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, HomePageActivity::class.java)
                    startActivity(intent)
                }
                is BaseResponse.Error->{
                    Toast.makeText(this@LoginActivity, it.poruka, Toast.LENGTH_SHORT).show()
                }
            }

        }


        binding.linearlayout1.startAnimation(btpAnimacija)


        binding.logInLayout.Password.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                binding.logInLayout.passwordWrapper.endIconMode=TextInputLayout.END_ICON_PASSWORD_TOGGLE
            }
        })

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
        }


        binding.signUpLayout.signUpDugme.setOnClickListener {
            signUpData()
        }


    }

    private fun getMyData() {
        var Username:String=binding.logInLayout.Email.text.toString().trim()

        if(Username.isBlank()){
            binding.logInLayout.Email.setError(getString(R.string.InsertYourEmailOrUsername))
            return
        }

        var Password:String=binding.logInLayout.Password.text.toString().trim()
        if(Password.isBlank()){
            binding.logInLayout.passwordWrapper.endIconMode=TextInputLayout.END_ICON_NONE
            binding.logInLayout.Password.setError(getString(R.string.InsertYourPassword))
            return
        }
        val obj= loginDTS(Username, Password)
        viewModel.login(obj)
    }

    private fun signUpData(){
        if(binding.signUpLayout.Username.text.toString().isBlank())
        {
            Toast.makeText(this@LoginActivity, R.string.InsertYourUsername, Toast.LENGTH_SHORT).show()
            return
        }

        if(binding.signUpLayout.Email.text.toString().isBlank())
        {
            Toast.makeText(this@LoginActivity, R.string.InsertYourEmail, Toast.LENGTH_SHORT).show()
            return
        }

        if(binding.signUpLayout.Password.text.toString().isBlank())
        {
            Toast.makeText(this@LoginActivity, R.string.InsertYourPassword, Toast.LENGTH_SHORT).show()
            return
        }

        if(!binding.signUpLayout.Password.text.toString().equals(binding.signUpLayout.ConfirmPassword.text.toString()))
        {
            Toast.makeText(this@LoginActivity, R.string.PasswordsAreNotTheSame, Toast.LENGTH_SHORT).show()
            return
        }





        val obj= signUpDTS(password = binding.signUpLayout.Password.text.toString(), username = binding.signUpLayout.Username.text.toString(), email = binding.signUpLayout.Email.text.toString())

        val retrofitData=APIservis.Servis.signUpCall(obj)
        dijalog.startLoading()
        retrofitData.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                dijalog.isDismiss()
                var responseBody= response.body()?.string()
                //val myStringBuilder=StringBuilder()
                if(response.code()!=200){

                    val content = response.errorBody()!!.charStream().readText()
                    Toast.makeText(this@LoginActivity, content, Toast.LENGTH_SHORT).show()
                    return
                }
                if(responseBody!=null){
                    Toast.makeText(this@LoginActivity, responseBody, Toast.LENGTH_SHORT).show()
                }


            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                dijalog.isDismiss()
                Toast.makeText(this@LoginActivity,getString(R.string.ConnectionError), Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onBackPressed() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
    }
}