package imi.projekat.hotspot


import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
            return
        }
        val intent = Intent(this@MainActivity, HomePageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(FLAG_ACTIVITY_NO_HISTORY)
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

