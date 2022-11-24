package imi.projekat.hotspot.Ostalo

import imi.projekat.hotspot.ModeliZaZahteve.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Part
import retrofit2.http.Path

class Repository(){
    suspend fun login(loginDATA:loginDTS): Response<LoginResponse> {
        return APIservis.Servis.loginCall(loginDATA)
    }
    suspend fun signUp(registerDATA:signUpDTS): Response<ResponseBody> {
        return APIservis.Servis.signUpCall(registerDATA)
    }
    suspend fun VerifyEmail(EmailToken:String):Response<ResponseBody>{
        return APIservis.Servis.VerifyEmail(EmailToken)
    }
    suspend fun changeProfilePhoto(@Part slika: MultipartBody.Part, @Part username:MultipartBody.Part, @Part email:MultipartBody.Part,
                                   @Part oldPassword:MultipartBody.Part,@Part newPassword:MultipartBody.Part):Response<changeAccDataResponse>{
        return APIservis.Servis.changeProfilePhoto(slika,username,email,oldPassword,newPassword)
    }
    suspend fun KreirajPost(post:String):Response<ResponseBody>{
        return APIservis.Servis.KreirajPost()
    }
    suspend fun resetujToken(refreshTokenDATA:refreshTokenDTS):Response<refreshTokenResponse>{
        return RefreshTokenAPICALL.Servis.resetujToken(refreshTokenDATA)
    }

    suspend fun getProfilePhoto():Response<ResponseBody>{
        return APIservis.Servis.getPhoto()
    }

    suspend fun addPost(@Part photos:List<MultipartBody.Part>,@Part description:MultipartBody.Part,@Part location:MultipartBody.Part,@Part shortDescription:MultipartBody.Part):Response<ResponseBody>{
        return APIservis.Servis.addPost(photos,description,location,shortDescription)
    }
    suspend fun getAllFollowingByUSer():Response<getuser>{
        return APIservis.Servis.getAllFollowingByUSer()
    }

    suspend fun getPostWithId(postid:Int):Response<singlePost>{
        return APIservis.Servis.getPostWithId(postid)
    }

    suspend fun comment(comment:commentDTS): Response<ResponseBody> {
        return APIservis.Servis.comment(comment)
    }

    suspend fun getPostsByUserId(userid: Int): Response<List<singlePost>> {
        return APIservis.Servis.getPostsByUserId(userid)
    }
    suspend fun getCommentsWithID(postid:Int): Response<List<singleComment>> {
        return APIservis.Servis.getCommentsWithID(postid)
    }
    suspend fun likePost(like: likeDTS):Response<ResponseBody>{
        return APIservis.Servis.likePost(like);
    }

    suspend fun dislikePost(@Body dislike: likeDTS):Response<ResponseBody>{
        return APIservis.Servis.dislikePost(dislike);
    }

    suspend fun UnfollowUser(userid: Int):Response<ResponseBody>{
        return APIservis.Servis.UnfollowUser(userid)
    }

    suspend fun FollowUser(userid: Int):Response<ResponseBody>{
        return APIservis.Servis.FollowUser(userid)
    }
}