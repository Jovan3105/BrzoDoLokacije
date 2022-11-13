package imi.projekat.hotspot.ModeliZaZahteve

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import okio.ByteString

data class loginDTS(
    val password: String,
    val username: String
)

data class LoginResponse(val message:String?,val token:String?,val refreshToken:String?,val profilePhoto:String?)

data class changeAccDataResponse(val message:String?,val token:String?,val id:Any?)
