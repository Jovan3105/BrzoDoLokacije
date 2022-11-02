package imi.projekat.hotspot.Interfaces

import android.hardware.biometrics.BiometricManager.Strings
import imi.projekat.hotspot.ModeliZaZahteve.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    @POST("KontrolerAutentikacije/Login")
    suspend fun loginCall(@Body dts: loginDTS):Response<LoginResponse>

    @POST("KontrolerAutentikacije/signUp")
    suspend fun signUpCall(@Body dts: signUpDTS):Response<ResponseBody>

    @POST("KontrolerAutentikacije/{username}/changepass")
    fun ForgotPasswordCall(@Path("username") username:String):Call<ForgotPasswordResponse>

    @POST("KontrolerAutentikacije/code")
    fun SendVerificationCode(@Body dts:VerificationCodeDTS):Call<ForgotPasswordResponse>

    @PUT("KontrolerAutentikacije/Setpass")
    fun SendNewPassword(@Body dts:NewPasswordDTS):Call<ForgotPasswordResponse>

}