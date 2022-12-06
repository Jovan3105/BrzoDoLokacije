package imi.projekat.hotspot.ModeliZaZahteve

import android.graphics.Bitmap
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

data class ProfileDTR(
    val id: Int,
    val username: String
)

data class MyProfileResponse(val photo:String?,val followers:List<ProfileDTR>)

data class UserInfoResponse(val username: String,val email:String,val photo: String,val following:Boolean)

data class getuser (
    val followers:ArrayList<FollowingUserPom>
)

data class FollowingUserPom(
    val id:Int,
    val username:String,
    val userPhoto:String
)

data class FollowingUserAdapter(
    val id:Int,
    val username: String,
    val photo:String,
    var buttonName:String
)