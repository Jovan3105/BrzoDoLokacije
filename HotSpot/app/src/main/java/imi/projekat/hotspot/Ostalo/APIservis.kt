package imi.projekat.hotspot.Ostalo

import imi.projekat.hotspot.Interfaces.ApiInterface
import imi.projekat.hotspot.KonfigAplikacije
import imi.projekat.hotspot.MainActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object APIservis {

    private val BASE_URL:String by lazy {
        KonfigAplikacije.getInstanca(MainActivity.companion.getContext())!!.AppSettings.baseURL
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



