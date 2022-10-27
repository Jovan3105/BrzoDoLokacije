package imi.projekat.hotspot.Interfaces

import imi.projekat.hotspot.ModeliZaZahteve.LoginResponse
import imi.projekat.hotspot.ModeliZaZahteve.loginDTS
import imi.projekat.hotspot.ModeliZaZahteve.signUpDTS
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiInterface {

    @POST("KontrolerAutentikacije/Login")
    fun loginCall(@Body dts: loginDTS):Call<LoginResponse>

    @POST("KontrolerAutentikacije/signUp")
    fun signUpCall(@Body dts: signUpDTS):Call<ResponseBody>
}