package imi.projekat.hotspot.Interfaces

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
    @POST("/api/User/edituser")
    suspend fun changeProfilePhoto(@Part slika: MultipartBody.Part,@Part username:MultipartBody.Part,@Part email:MultipartBody.Part,
    @Part oldPassword:MultipartBody.Part,@Part newPassword:MultipartBody.Part
    ):Response<changeAccDataResponse>

    @POST("api/User/KreirajPost")
    suspend fun KreirajPost():Response<ResponseBody>

    @POST("KontrolerAutentikacije/ResetujToken")
    suspend fun resetujToken(@Body dts: refreshTokenDTS):Response<refreshTokenResponse>

    @GET("/api/User/GetPhoto")
    suspend fun getPhoto():Response<ResponseBody>

    @Multipart
    @POST("/api/Post/addpost")
    suspend fun addPost(@Part photos:List<MultipartBody.Part>,@Part description:MultipartBody.Part,@Part location:MultipartBody.Part):Response<ResponseBody>

    @GET("/api/User/GetAllFollowingByUser")
    suspend fun getAllFollowingByUSer():Response<getuser>



}