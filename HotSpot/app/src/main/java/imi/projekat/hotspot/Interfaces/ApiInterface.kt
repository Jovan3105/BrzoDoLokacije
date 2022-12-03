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



    @Multipart
    @POST("/api/Post/addpost")
    suspend fun addPost(@Part photos:List<MultipartBody.Part>, @Part description:MultipartBody.Part, @Part longitude: MultipartBody.Part, @Part latitude: MultipartBody.Part, @Part shortDescription:MultipartBody.Part):Response<ResponseBody>

    @GET("/api/Post/getpost/{postid}")
    suspend fun getPostWithId(@Path("postid") postid :Int):Response<singlePost>

    @POST("/api/Post/comment")
    suspend fun comment(@Body comment: commentDTS):Response<ResponseBody>

    @GET("/api/Post/getpostsbyid/{userid}")
    suspend fun getPostsByUserId(@Path("userid") userid: Int):Response<List<singlePost>>

    @GET("/api/Post/comments/{postid}")
    suspend fun getCommentsWithID(@Path("postid") postid :Int):Response<List<singleComment>>

    @POST("/api/Post/like")
    suspend fun likePost(@Body like: likeDTS):Response<ResponseBody>

    @POST("/api/Post/dislike")
    suspend fun dislikePost(@Body dislike: likeDTS):Response<ResponseBody>

    @GET("/api/User/GetAllFollowingByUser")
    suspend fun getAllFollowingByUSer():Response<getuser>

    @POST("/api/User/unfollow/{userid}")
    suspend fun UnfollowUser(@Path("userid") userid: Int):Response<ResponseBody>

    @POST("/api/User/follow/{userid}")
    suspend fun FollowUser(@Path("userid") userid: Int):Response<ResponseBody>

    @GET("/api/Post/getUserByID/{idusera}")
    suspend fun getUserWithID(@Path("idusera") idusera :Int):Response<UserInfoResponse>

}