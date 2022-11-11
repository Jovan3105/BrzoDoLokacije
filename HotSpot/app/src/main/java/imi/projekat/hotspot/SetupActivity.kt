package imi.projekat.hotspot

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import imi.projekat.hotspot.Dialogs.LoadingDialog
import imi.projekat.hotspot.ModeliZaZahteve.refreshTokenDTS
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.ViewModeli.SetupActivityViewModel
import imi.projekat.hotspot.databinding.ActivitySetupBinding
import java.util.*


class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding
    private val viewModel by viewModels<SetupActivityViewModel>()
    val dijalog= LoadingDialog(this)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding= ActivitySetupBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_setup)
        Companion.setContext(this.application)
        val token = MenadzerSesije.getToken(this)
        if (token.isNullOrBlank()) {
            val intent = Intent(this@SetupActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
            finish()
            return
        }
        val jwt:JWT= JWT(token)
        viewModel.liveRefreshTokenResponse.observe(this){
            dijalog.isDismiss()
            when(it){
                is BaseResponse.Loading->{
                    dijalog.startLoading()
                }
                is BaseResponse.Success->{
                    MenadzerSesije.saveAuthToken(applicationContext,it.data?.token.toString())
                    val intent = Intent(this@SetupActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is BaseResponse.Error->{
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),applicationContext)
                    Toast.makeText(this@SetupActivity, id, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SetupActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

//        if (jwt.expiresAt!!.before(Date())) {
//            var refToken=MenadzerSesije.getRefreshToken(applicationContext).toString()
//            if(refToken!=null){
//                var zahtev = refreshTokenDTS(token,refToken)
//                viewModel.refreshToken(zahtev)
//            }
//
//        }

        val usernameToken=jwt.getClaim("username").asString()
        val emailToken=jwt.getClaim("email").asString()
        val intent = Intent(this@SetupActivity, MainActivity::class.java)
        startActivity(intent)
        finish()

    }


    object Companion{

        private lateinit var aplikacija: Application

        fun setContext(app: Application) {
            aplikacija=app
        }
        fun getContext(): Context {
            return aplikacija.applicationContext
        }
    }
}