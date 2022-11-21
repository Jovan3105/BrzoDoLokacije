package imi.projekat.hotspot.UI.Post

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*


class MapaFragment : Fragment() {
    private lateinit var map:MapView
    private lateinit var locationOverlay:MyLocationNewOverlay

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }

        var view = inflater.inflate(R.layout.fragment_mapa, container, false)
        val ctx: Context = requireContext()
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        val mapEventsReceiver = MapEventsReceiverImpl()
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        
        //init lokacija
        map = view.findViewById(R.id.map) as MapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.overlays.add(mapEventsOverlay)
        val mapController = map.controller
        mapController.setZoom(19.0)
        val startPoint = GeoPoint(44.0292765, 20.9094117)
        mapController.setCenter(startPoint)

        //lokacija
        this.locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
        this.locationOverlay.enableMyLocation()
        map.overlays.add(this.locationOverlay)

        //marker
        val startMarker = Marker(map)
        startMarker.position = startPoint
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(startMarker)
        map.invalidate()
        startMarker.icon = ResourcesCompat.getDrawable(this.resources, R.drawable.ic_baseline_location_on_24, null)// as VectorDrawable
        startMarker.title = "Start point"

        return view
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        Configuration.getInstance().save(requireContext(), prefs);
        map.onResume()
    }
    override fun onPause() {
        super.onPause()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        Configuration.getInstance().save(requireContext(), prefs);
        map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        Configuration.getInstance().save(requireContext(), prefs);
        map.onDetach()

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