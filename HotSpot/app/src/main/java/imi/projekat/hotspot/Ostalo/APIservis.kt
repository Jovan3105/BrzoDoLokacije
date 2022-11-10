package imi.projekat.hotspot.Ostalo


import android.content.Context
import android.util.Log
import imi.projekat.hotspot.Interfaces.ApiInterface
import imi.projekat.hotspot.KonfigAplikacije
import imi.projekat.hotspot.ModeliZaZahteve.refreshTokenDTS
import imi.projekat.hotspot.ModeliZaZahteve.refreshTokenResponse
import imi.projekat.hotspot.SetupActivity
import kotlinx.coroutines.*
import okhttp3.*
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


    val Servis: ApiInterface
        get() {
            if(!this::_servis.isInitialized){
                initServis()
            }
            return _servis
        }



    //Mora da se izvrisi prilikom logina da bi se resetovala vrednost tokena
    fun ResetServis(){
        initServis()
    }

    private fun okhttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .authenticator(AccessTokenAuthenticator())
            .addInterceptor(AuthInterceptor(context))
            .build()
    }
}






object RefreshTokenAPICALL {

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


    val Servis: ApiInterface
        get() {
            if(!this::_servis.isInitialized){
                initServis()
            }
            return _servis
        }


    private fun okhttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }
    public fun ResetServis(){
        RefreshTokenAPICALL.initServis()
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

class AccessTokenAuthenticator(
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        var handleJob: Job?=null

        val exceptionHandler= CoroutineExceptionHandler{ _, throwable->onError(
            "ConnectionError"
        )
            Log.d("Exceptionauthenticate",throwable.localizedMessage.toString())
        }

        val token = MenadzerSesije.getToken(SetupActivity.Companion.getContext())
        val RefreshToken = MenadzerSesije.getRefreshToken(SetupActivity.Companion.getContext())

        if(token==null || RefreshToken==null){
            Log.d("Autentikator","token ili refreshToken su null")
            return null
        }


        synchronized(this) {
            // Check if the request made was previously made as an authenticated request.

            if (response.request.header("Authorization") != null) {
                // If the token has changed since the request was made, use the new token.

                var pom: Request? =null
                var responseObject:refreshTokenResponse?=null
                handleJob= CoroutineScope(Dispatchers.IO+exceptionHandler).launch {
                    val response2 = RefreshTokenAPICALL.Servis.resetujToken(refreshTokenDTS(token,RefreshToken.toString()))
                    Log.d("GRESKA1", response2.body().toString())
                    if(response2.isSuccessful){
                        var pom2=BaseResponse.Success(response2.body())

                        Log.d("GRESKA3", pom2.data?.message.toString())
                        MenadzerSesije.saveRefteshToken(SetupActivity.Companion.getContext(),
                            pom2.data?.refreshToken.toString())
                        MenadzerSesije.saveAuthToken(SetupActivity.Companion.getContext(),
                            pom2.data?.token.toString())

                        pom=response.request
                            .newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer ${pom2.data?.token}")
                            .build()
                        Log.d("pom2", pom!!.header("Authorization").toString())
                    }
                    else{
                        MenadzerSesije.clearData(SetupActivity.Companion.getContext())
                        MenadzerSesije.logOut=true
                    }
                }

                return runBlocking{
                    handleJob!!.join()
                    Log.d("pom", pom!!.header("Authorization").toString())
                    APIservis.ResetServis()
                    RefreshTokenAPICALL.ResetServis()
                    Log.d("APIservis","Resetovani servisi")
                    pom
                }



            }
        }
        Log.d("Velika","GRESKA")
        return null
    }

    private fun onError(greska: String){
        Log.d("GRESKAuAutentikatoru",greska)
    }


}
