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
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.ModeliZaZahteve.loginDTS
import imi.projekat.hotspot.ModeliZaZahteve.signUpDTS
import imi.projekat.hotspot.Ostalo.APIservis
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.ViewModeli.LoginActivityViewModel
import imi.projekat.hotspot.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.fragment_login_and_register.view.*
import kotlinx.android.synthetic.main.fragment_login_and_register.view.signUpLayout
import kotlinx.android.synthetic.main.login_tab_fragment.view.*
import kotlinx.android.synthetic.main.login_tab_fragment.view.Email
import kotlinx.android.synthetic.main.login_tab_fragment.view.Password
import kotlinx.android.synthetic.main.login_tab_fragment.view.logInLayout
import kotlinx.android.synthetic.main.login_tab_fragment.view.passwordWrapper
import kotlinx.android.synthetic.main.register_tab_fragment.view.*
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






        viewModel.liveLoginResponse.observe(this){
            dijalog.isDismiss()
            when(it){
                is BaseResponse.Loading->{
                    dijalog.startLoading()
                }
                is BaseResponse.Success->{

                    val id = UpravljanjeResursima.getResourceString(it.data?.message.toString(),applicationContext)
                    Toast.makeText(this@LoginActivity, id, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, HomePageActivity::class.java)
                    startActivity(intent)
                }
                is BaseResponse.Error->{
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),applicationContext)
                    Toast.makeText(this@LoginActivity, id, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.liveRegisterResponse.observe(this){
            dijalog.isDismiss()
            when(it){
                is BaseResponse.Loading->{
                    dijalog.startLoading()
                }
                is BaseResponse.Success->{
                    val content = it.data!!.charStream().readText()
                    val id = UpravljanjeResursima.getResourceString(content,applicationContext)
//                    val gson = Gson()
//                    val type = object : TypeToken<ResponseBody>() {}.type
//                    val errorResponse: ResponseBody = gson.fromJson(it.data!!.charStream(), type)

                    Toast.makeText(this@LoginActivity,id, Toast.LENGTH_SHORT).show()

//                    val intent = Intent(this@LoginActivity, HomePageActivity::class.java)
//                    startActivity(intent)
                }
                is BaseResponse.Error->{
                    Toast.makeText(this@LoginActivity, it.poruka, Toast.LENGTH_SHORT).show()
                }
            }
        }




        binding.fragmentContainerView.startAnimation(btpAnimacija)











    }

    public fun getMyData(obj:loginDTS) {
        viewModel.login(obj)
    }

    public fun signUp(obj:signUpDTS){
        viewModel.signUp(obj)
    }





}