package imi.projekat.hotspot

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import imi.projekat.hotspot.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        companion.setContext(this.application)

    }


    object companion{

        private lateinit var application: Application

        fun setContext(app: Application) {
            application=app
        }
        fun getContext(): Context {
            return application.applicationContext
        }
    }
}

