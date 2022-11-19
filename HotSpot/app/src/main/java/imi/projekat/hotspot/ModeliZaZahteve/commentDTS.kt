package imi.projekat.hotspot.ModeliZaZahteve

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import okio.ByteString

data class commentDTS(
    val parentid: Int,
    val postid: Int,
    val text:String
)
data class singleComment(
    val numOfLikes: Int,
    val ownerID: Int,
    val text: String,
    val time: String,
    val userPhoto: String,
    val username: String
)