package imi.projekat.hotspot.Modeli

data class loginDTS(
    val password: String,
    val username: String
)

data class LoginResponse(val message:String?,val data:loginDTS?)