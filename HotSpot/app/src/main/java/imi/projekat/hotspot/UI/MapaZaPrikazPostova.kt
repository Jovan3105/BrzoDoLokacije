package imi.projekat.hotspot.UI

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import imi.projekat.hotspot.KonfigAplikacije
import imi.projekat.hotspot.MapsActivity
import imi.projekat.hotspot.R
import imi.projekat.hotspot.databinding.ActivityMapsBinding
import imi.projekat.hotspot.databinding.FragmentHomePageBinding
import imi.projekat.hotspot.databinding.FragmentMapaZaPrikazPostovaBinding
import java.util.*

class MapaZaPrikazPostova : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentMapaZaPrikazPostovaBinding
    private var grantedPermission=false
    private lateinit var googleMap:GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private var cameraPosition: CameraPosition? = null
    private lateinit var placesClient: PlacesClient
    private var lastKnownLocation: Location? = null
    private var defaultLocation = LatLng(44.01772088671875, 20.90731628415956)
    private lateinit var selectedLocation: LatLng
    private lateinit var confirmLocationButton: Button
    private lateinit var bttAnimacija: Animation
    private lateinit var top_exitAnimacija: Animation
    private lateinit var top_enterAnimacija: Animation
    private lateinit var searchTextDugme: ImageButton
    private lateinit var searchTextTekst: EditText
    private lateinit var izborTerenaDugme: ImageButton
    private lateinit var searchDugmeActionBar: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentMapaZaPrikazPostovaBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bttAnimacija= AnimationUtils.loadAnimation(requireContext(),R.anim.bot_to_top)
        top_exitAnimacija= AnimationUtils.loadAnimation(requireContext(),R.anim.top_exit)
        top_enterAnimacija= AnimationUtils.loadAnimation(requireContext(),R.anim.top_enter)
        searchTextDugme=binding.root.findViewById(binding.searchTextDugme.id)
        searchTextTekst=binding.root.findViewById(binding.searchTextView.id)
        izborTerenaDugme=binding.root.findViewById(binding.izborTerenaDugme.id)
        searchDugmeActionBar=binding.root.findViewById(binding.searchDugmeActionBar.id)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(requireContext())

        // Construct a PlacesClient
        Places.initialize(requireContext(), KonfigAplikacije.instanca.AppSettings.MAPS_API_KEY)
        placesClient = Places.createClient(requireContext())

        requestLocPermission()

        searchTextDugme.setOnClickListener{
            var unetaLokacija:String=searchTextTekst.text.toString()
            if(!unetaLokacija.isNullOrEmpty()){
                var geoCoder: Geocoder = Geocoder(requireContext(), Locale.getDefault())


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geoCoder.getFromLocationName(unetaLokacija,1,object : Geocoder.GeocodeListener{
                        override fun onGeocode(addresses: MutableList<Address>) {
                            var listaAdresa=addresses
                            if(listaAdresa!=null && listaAdresa.size>0){
                                val latLng=LatLng(listaAdresa.get(0).latitude,listaAdresa.get(0).longitude)
                                setLocationMarker(latLng,unetaLokacija,null)
                            }
                        }
                        override fun onError(errorMessage: String?) {
                            super.onError(errorMessage)

                        }

                    })
                } else {
                    var listaAdresa: MutableList<Address>?
                    @Suppress("DEPRECATION")
                    listaAdresa= geoCoder.getFromLocationName(unetaLokacija,1)
                    if(listaAdresa!=null && listaAdresa.size>0){
                        val latLng=LatLng(listaAdresa.get(0).latitude,listaAdresa.get(0).longitude)
                        setLocationMarker(latLng,unetaLokacija,null)
                    }
                }
            }
        }

        izborTerenaDugme.setOnClickListener{
            val popupMenu = PopupMenu(requireContext(),izborTerenaDugme)
            popupMenu.menuInflater.inflate(R.menu.meni_za_izbor_terena_mape,popupMenu.menu)

            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener()  { item ->
                when (item.itemId) {
                    R.id.noneMap -> googleMap.mapType=GoogleMap.MAP_TYPE_NONE
                    R.id.NormalMap -> googleMap.mapType=GoogleMap.MAP_TYPE_NORMAL
                    R.id.SatelliteMap -> googleMap.mapType=GoogleMap.MAP_TYPE_SATELLITE
                    R.id.MapHybrid -> googleMap.mapType=GoogleMap.MAP_TYPE_HYBRID
                    else -> {
                        googleMap.mapType=GoogleMap.MAP_TYPE_TERRAIN
                    }
                }
                true
            })

            popupMenu.show()
        }

        searchDugmeActionBar.setOnClickListener{
            showSearch()
        }

    }

    private fun requestLocPermission(){
        EasyPermissions.requestPermissions(
            this,
            "This app cannot work without Location Permission",
            1,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun setLocationMarker(location:LatLng,title:String,zoom:Float?){
//        googleMap.clear();
        var markerOptions: MarkerOptions = MarkerOptions()
        selectedLocation = location
        markerOptions.title(title)
        markerOptions.position(location)
//        this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(location))
//        if(zoom!=null){
//            this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,zoom))
//        }

        this.googleMap.addMarker(markerOptions)
    }

    private fun moveCameraToLocation(location:LatLng,zoom:Float?){
//        googleMap.clear();
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(location))
        if(zoom!=null){
            this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,zoom))
        }
    }

    private fun showSearch(){
        if(binding.linearLayoutMaps.visibility== View.VISIBLE){
            binding.linearLayoutMaps.startAnimation(top_exitAnimacija)
            binding.linearLayoutMaps.visibility=View.GONE
            return
        }
        binding.linearLayoutMaps.startAnimation(top_enterAnimacija)
        binding.linearLayoutMaps.visibility=View.VISIBLE
    }

    private fun fetchLocation() {


        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }



        val task = fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location ->
            if(location!=null){
                currentLocation = location
//                Toast.makeText(applicationContext, currentLocation.latitude.toString() + "" +
//                        currentLocation.longitude, Toast.LENGTH_SHORT).show()

                moveCameraToLocation(LatLng(currentLocation.latitude,currentLocation.longitude),10f)

            }
        }
        task.addOnFailureListener {
            Log.d("Greska","Nije nadjena lokacija")
            moveCameraToLocation(LatLng(defaultLocation.latitude,defaultLocation.longitude),10f)

        }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap=googleMap

        if(lastKnownLocation!=null)
            moveCameraToLocation(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude),10f)

        this.googleMap.uiSettings.isZoomControlsEnabled = true
        this.googleMap.uiSettings.isCompassEnabled = true
        this.googleMap.uiSettings.isZoomGesturesEnabled=true
        this.googleMap.uiSettings.isMapToolbarEnabled=true
        //this.googleMap.uiSettings.isScrollGesturesEnabled=true

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        this.googleMap.isMyLocationEnabled=true
        fetchLocation()


        googleMap.setOnMapClickListener(object :GoogleMap.OnMapClickListener {
            override fun onMapClick(latlng :LatLng) {
//                setMyLocationMarker(latlng,"Selected location",null)

            }
        })
//        binding.confirmLocationButton.setOnClickListener {
//            var data: Intent = Intent()
//            data.putExtra("longitude",selectedLocation.longitude.toString())
//            data.putExtra("latitude",selectedLocation.latitude.toString())
//            setResult(AppCompatActivity.RESULT_OK, data);
//            finish();
//        }
        binding.confirmLocationButton.startAnimation(bttAnimacija)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            SettingsDialog.Builder(requireContext()).build().show()
            return
        }
        requestLocPermission()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if(checkGooglePlayServices()==false)
            return
        grantedPermission=true
        if(!isLocationEnabled()){
            Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        dozvoljenPristupMapi()
    }

    private fun checkGooglePlayServices(): Boolean {
        var result= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext())
        if (result== ConnectionResult.SUCCESS){
            return true
        }

        var dialog: Dialog? = GoogleApiAvailability.getInstance().getErrorDialog(this,result,1000)
        if (dialog != null) {
            Log.d("NEMA","GOOGLE SERVISA")
            dialog.show()
        }
        return false
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE)  as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun dozvoljenPristupMapi(){
        var supMapFragment: SupportMapFragment = SupportMapFragment()
        parentFragmentManager.beginTransaction().add(binding.mapView.id,supMapFragment).commit()
        supMapFragment.getMapAsync(this)
    }

}