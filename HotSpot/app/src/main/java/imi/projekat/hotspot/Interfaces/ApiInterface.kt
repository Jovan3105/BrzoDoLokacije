package imi.projekat.hotspot.Interfaces

import android.hardware.biometrics.BiometricManager.Strings
import imi.projekat.hotspot.ModeliZaZahteve.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

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

    @POST("KontrolerAutentikacije/VerifyEmail/{EmailToken}")
    suspend fun VerifyEmail(@Path("EmailToken") EmailToken:String):Response<ResponseBody>

    @Multipart
    @POST("/api/User/photo")
    suspend fun changeProfilePhoto(@Part slika: MultipartBody.Part):Response<ResponseBody>

    @POST("api/User/KreirajPost")
    suspend fun KreirajPost():Response<ResponseBody>

    @POST("")
    suspend fun refresujToken(@Body dts: refreshTokenDTS):Response<refreshTokenResponse>
}