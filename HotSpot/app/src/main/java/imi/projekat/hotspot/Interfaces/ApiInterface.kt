package imi.projekat.hotspot.Interfaces

import imi.projekat.hotspot.Modeli.LoginResponse
import imi.projekat.hotspot.Modeli.loginDTS
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {

    @POST("KontrolerAutentikacije/Login")
    fun loginCall(@Body dts: loginDTS):Call<LoginResponse>
}