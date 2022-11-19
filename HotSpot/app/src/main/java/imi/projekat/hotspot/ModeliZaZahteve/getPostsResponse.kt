package imi.projekat.hotspot.ModeliZaZahteve


data class singlePost(
    val brojslika: Int,
    val ownerID: Int,
    val dateTime: String,
    val description: String,
    val location: String,
    val photos: List<String>,
    val profilephoto:String,
    val username:String,
    val shortDescription: String
)
