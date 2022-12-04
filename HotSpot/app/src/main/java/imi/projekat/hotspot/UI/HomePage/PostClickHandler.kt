package imi.projekat.hotspot.UI.HomePage

import android.widget.ImageView
import imi.projekat.hotspot.ModeliZaZahteve.likeDTS
import imi.projekat.hotspot.ModeliZaZahteve.singlePost

interface PostClickHandler {
    fun clickedPostItem(post:singlePost)
    fun likePost(like:likeDTS)
    fun dislikePost(like:likeDTS)
    fun getPicture(imageView: ImageView,slika:String)
    fun clickOnUser(idKorisnika:Int)
}