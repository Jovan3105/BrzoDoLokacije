package imi.projekat.hotspot.UI

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import imi.projekat.hotspot.ModeliZaZahteve.changeAccDataResponse
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import kotlinx.coroutines.*
import java.util.ArrayList

class CustomInfoWindowAdapter(var context:Context,var viewModel: MainActivityViewModel,var listaPostova: ArrayList<singlePost>): GoogleMap.InfoWindowAdapter {
    var mWindow: View = LayoutInflater.from(context)
        .inflate(R.layout.custom_info_window_za_marker, null)

    val slika = mWindow.findViewById(R.id.imageView4) as ImageView

    private fun setInfoWindowText(marker: Marker): View {

        val clickCount = marker.tag as? Int

        val title = marker.title
        val tvTitle = mWindow.findViewById<TextView>(R.id.tvTitle)
        val brojLajkova = mWindow.findViewById<TextView>(R.id.brojLajkova)

        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
            brojLajkova.text= "Likes: "+listaPostova[clickCount!!].brojlajkova.toString()
        }

        return mWindow
    }

    override fun getInfoWindow(p0: Marker): View {
        setInfoWindowText(p0)
        return mWindow
    }

    override fun getInfoContents(p0: Marker): View {
        setInfoWindowText(p0)
        return mWindow
    }

    fun getInfoContents(id:Int) {
        viewModel.dajSliku(slika,"PostsFolder/"+ listaPostova[id!!].photos[0],context)
    }
}