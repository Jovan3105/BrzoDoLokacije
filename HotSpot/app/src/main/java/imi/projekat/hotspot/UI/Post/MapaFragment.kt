package imi.projekat.hotspot.UI.Post

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import imi.projekat.hotspot.R
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker





class MapaFragment : Fragment() {
    private lateinit var map:MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_mapa, container, false)
        val ctx: Context = requireContext()
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        val mapEventsReceiver = MapEventsReceiverImpl()
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        

        map = view.findViewById(R.id.map) as MapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.overlays.add(mapEventsOverlay)
        val mapController = map.controller
        mapController.setZoom(19.0)
        val startPoint = GeoPoint(44.0292765, 20.9094117)
        mapController.setCenter(startPoint)
        return view
    }
}
class MapEventsReceiverImpl : MapEventsReceiver {

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        Log.d("singleTapConfirmedHelper", "${p?.latitude} - ${p?.longitude}")
        return true
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        Log.d("longPressHelper", "${p?.latitude} - ${p?.longitude}")
        return false
    }
}