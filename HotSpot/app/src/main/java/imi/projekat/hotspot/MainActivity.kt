package imi.projekat.hotspot


import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        Companion.setContext(this.application)
        val token = MenadzerSesije.getToken(this)
        if (token.isNullOrBlank()) {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
            finish()
        }

    }


    object Companion{

        private lateinit var application: Application

        fun setContext(app: Application) {
            application=app
        }
        fun getContext(): Context {
            return application.applicationContext
        }
    }

}

