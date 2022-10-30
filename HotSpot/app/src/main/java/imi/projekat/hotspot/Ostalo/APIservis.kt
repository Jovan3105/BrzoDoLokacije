package imi.projekat.hotspot.Ostalo

import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.Interfaces.ApiInterface
import imi.projekat.hotspot.KonfigAplikacije
import imi.projekat.hotspot.MainActivity
import imi.projekat.hotspot.ModeliZaZahteve.LoginResponse
import imi.projekat.hotspot.ModeliZaZahteve.loginDTS
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

object APIservis {

    private val BASE_URL:String by lazy {
        KonfigAplikacije.getInstanca(MainActivity.Companion.getContext())!!.AppSettings.baseURL
    }


    private val retrofitBuilder: Retrofit.Builder by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
    }
    val Servis:ApiInterface by lazy{
        retrofitBuilder
            .build()
            .create(ApiInterface::class.java)
    }


}



