package imi.projekat.hotspot.UI.HomePage

import imi.projekat.hotspot.ModeliZaZahteve.singlePost

interface PostClickHandler {
    fun clickedPostItem(post:singlePost)
}