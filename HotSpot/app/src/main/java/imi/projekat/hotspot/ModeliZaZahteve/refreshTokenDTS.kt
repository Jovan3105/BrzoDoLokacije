package imi.projekat.hotspot.ModeliZaZahteve

data class refreshTokenDTS(
    val token: String,
    val refreshToken:String
)

data class refreshTokenResponse(val token:String?,val refreshToken:String?,val message:String?)