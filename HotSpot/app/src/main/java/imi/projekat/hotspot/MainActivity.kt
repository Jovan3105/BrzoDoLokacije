package imi.projekat.hotspot

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


    }
}