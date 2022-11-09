package imi.projekat.hotspot.ModeliZaZahteve

data class loginDTS(
    val password: String,
    val username: String
)

data class LoginResponse(val message:String?,val token:String?,val refreshToken:String?)