package imi.projekat.hotspot

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.databinding.ActivitySetupBinding

class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding

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
        val intent = Intent(this@SetupActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
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