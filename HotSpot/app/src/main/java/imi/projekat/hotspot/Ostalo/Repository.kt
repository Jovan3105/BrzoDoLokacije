package imi.projekat.hotspot.Ostalo

import imi.projekat.hotspot.Interfaces.ApiInterface
import imi.projekat.hotspot.ModeliZaZahteve.LoginResponse
import imi.projekat.hotspot.ModeliZaZahteve.loginDTS
import retrofit2.Call
import retrofit2.Response

class Repository(){
    suspend fun login(loginDATA:loginDTS): Response<LoginResponse> {
        return APIservis.Servis.loginCall(loginDATA)
    }
}