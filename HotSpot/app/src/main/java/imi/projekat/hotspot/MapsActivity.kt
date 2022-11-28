package imi.projekat.hotspot

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
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
import imi.projekat.hotspot.databinding.ActivityMapsBinding
import kotlinx.android.parcel.Parcelize


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMapsBinding
    private var grantedPermission=false
    private lateinit var googleMap:GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private var cameraPosition: CameraPosition? = null
    private lateinit var placesClient: PlacesClient
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(44.01772088671875, 20.90731628415956)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var saveData: SaveData? = savedInstanceState.getParcelable("saveData",SaveData::class.java)
                if (saveData != null) {

                    lastKnownLocation=saveData.KEY_LOCATION
                    cameraPosition=saveData.KEY_CAMERA_POSITION
                }

            }

        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@MapsActivity)

        // Construct a PlacesClient
        Places.initialize(applicationContext, KonfigAplikacije.instanca.AppSettings.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        requestLocPermission()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        googleMap?.let { map ->

            var SAVEdata=SaveData(map.cameraPosition,lastKnownLocation)
            outState.putParcelable("saveData", SAVEdata)
        }
        super.onSaveInstanceState(outState)
    }

    private fun dozvoljenPristupMapi(){
        var supMapFragment:SupportMapFragment= SupportMapFragment()
        supportFragmentManager.beginTransaction().add(binding.mapView.id,supMapFragment).commit()
        supMapFragment.getMapAsync(this)
    }

    private fun hasLocationPersimision()=EasyPermissions.hasPermissions(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun requestLocPermission(){
        EasyPermissions.requestPermissions(
            this,
            "This app cannot work without Location Permission",
            1,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(1,permissions,grantResults,this)
    }



    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            SettingsDialog.Builder(this).build().show()
            return
        }
        requestLocPermission()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if(checkGooglePlayServices()==false)
            return
        grantedPermission=true
        if(!isLocationEnabled()){
            Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            finish()
        }
        dozvoljenPristupMapi()
    }

    private fun checkGooglePlayServices(): Boolean {
        var result=GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (result==ConnectionResult.SUCCESS){
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
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap=googleMap

        if(lastKnownLocation!=null)
            setMyLocationMarker(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude),"My location")

        this.googleMap.uiSettings.isZoomControlsEnabled = true
        this.googleMap.uiSettings.isCompassEnabled = true
        this.googleMap.uiSettings.isZoomGesturesEnabled=true
        this.googleMap.uiSettings.isMapToolbarEnabled=true
        //this.googleMap.uiSettings.isScrollGesturesEnabled=true

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
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

    }

    private fun setMyLocationMarker(location:LatLng,title:String){
        var markerOptions:MarkerOptions= MarkerOptions()
        markerOptions.title(title)
        markerOptions.position(location)
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(location))
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,10f))
        this.googleMap.addMarker(markerOptions)
    }


    private fun fetchLocation() {


        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }



        val task = fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location ->
            if(location!=null){
                currentLocation = location
                Toast.makeText(applicationContext, currentLocation.latitude.toString() + "" +
                        currentLocation.longitude, Toast.LENGTH_SHORT).show()

                setMyLocationMarker(LatLng(currentLocation.latitude,currentLocation.longitude),"My location")

            }
        }
        task.addOnFailureListener {
            Log.d("Greska","Nije nadjena lokacija")
            setMyLocationMarker(LatLng(defaultLocation.latitude,defaultLocation.longitude),"Default app location")

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.meni_za_izbor_terena_mape,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.noneMap -> googleMap.mapType=GoogleMap.MAP_TYPE_NONE
            R.id.NormalMap -> googleMap.mapType=GoogleMap.MAP_TYPE_NORMAL
            R.id.SatelliteMap -> googleMap.mapType=GoogleMap.MAP_TYPE_SATELLITE
            R.id.MapHybrid -> googleMap.mapType=GoogleMap.MAP_TYPE_HYBRID
            else -> {
                googleMap.mapType=GoogleMap.MAP_TYPE_TERRAIN
            }
        }

        return super.onOptionsItemSelected(item)
    }


    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private val saveData: SaveData? = null

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }

    @Parcelize
    private data class SaveData(var KEY_CAMERA_POSITION: CameraPosition?, var KEY_LOCATION: Location?):Parcelable

}