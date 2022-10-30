package imi.projekat.hotspot.Interfaces

import android.hardware.biometrics.BiometricManager.Strings
import imi.projekat.hotspot.ModeliZaZahteve.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {

    @POST("KontrolerAutentikacije/Login")
    suspend fun loginCall(@Body dts: loginDTS):Response<LoginResponse>

    @POST("KontrolerAutentikacije/signUp")
    fun signUpCall(@Body dts: signUpDTS):Call<ResponseBody>

    @POST("KontrolerAutentikacije/{username}/changepass")
    fun ForgotPasswordCall(@Path("username") username:String):Call<ForgotPasswordResponse>



}