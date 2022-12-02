package imi.projekat.hotspot.ModeliZaZahteve

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class singlePost(
    val brojslika: Int,
    val ownerID: Int,
    val dateTime: String,
    val description: String,
    val longitude: Double,
    val latitude: Double,
    val photos: List<String>,
    val profilephoto:String,
    val username:String,
    val shortDescription: String,
    val postID:Int,
    val brojlajkova:Int,
    var likedByMe:Boolean
):Parcelable

