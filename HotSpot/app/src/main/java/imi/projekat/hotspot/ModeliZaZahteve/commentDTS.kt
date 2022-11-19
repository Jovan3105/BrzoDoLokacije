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