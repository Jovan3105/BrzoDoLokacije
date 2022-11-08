package imi.projekat.hotspot.Ostalo

import android.content.Context
import android.util.Log
import imi.projekat.hotspot.Interfaces.ApiInterface
import imi.projekat.hotspot.KonfigAplikacije
import imi.projekat.hotspot.MainActivity
import imi.projekat.hotspot.SetupActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object APIservis {

    private val BASE_URL:String by lazy {
        KonfigAplikacije.getInstanca(SetupActivity.Companion.getContext())!!.AppSettings.baseURL
    }

    private val retrofitBuilder: Retrofit.Builder by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
    }

    private lateinit var _servis:ApiInterface

    private fun initServis(){
        _servis=retrofitBuilder
            .client(okhttpClient(SetupActivity.Companion.getContext()))
            .build()
            .create(ApiInterface::class.java)
    }


    public val Servis: ApiInterface
        get() {
            if(!this::_servis.isInitialized){
                initServis()
            }
            return _servis
        }



    //Mora da se izvrisi prilikom logina da bi se resetovala vrednost tokena
    public fun ResetServis(){
        initServis()
    }

    private fun okhttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()
    }




}

class AuthInterceptor(context: Context) : Interceptor {
    private var token = MenadzerSesije.getToken(context)
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        // If token has been saved, add it to the request
        requestBuilder.addHeader("Authorization", "Bearer $token")
        return chain.proceed(requestBuilder.build())
    }
}

