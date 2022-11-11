package imi.projekat.hotspot.Ostalo

import imi.projekat.hotspot.Interfaces.ApiInterface
import imi.projekat.hotspot.ModeliZaZahteve.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header

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
//    suspend fun changeProfilePhoto(photo: MultipartBody.Part):Response<ResponseBody>{
//        return APIservis.Servis.changeProfilePhoto(photo)
//    }
    suspend fun KreirajPost(post:String):Response<ResponseBody>{
        return APIservis.Servis.KreirajPost()
    }
    suspend fun resetujToken(refreshTokenDATA:refreshTokenDTS):Response<refreshTokenResponse>{
        return RefreshTokenAPICALL.Servis.resetujToken(refreshTokenDATA)
    }

}