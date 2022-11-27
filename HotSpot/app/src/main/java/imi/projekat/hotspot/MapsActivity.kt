package imi.projekat.hotspot

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import imi.projekat.hotspot.databinding.ActivityMapsBinding
import kotlinx.android.synthetic.main.activity_maps.view.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMapsBinding
    private var grantedPermission=false
    private lateinit var googleMap:GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@MapsActivity)


        requestLocPermission()
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



        fetchLocation()

    }

    private fun setMyLocationMarker(location:LatLng,title:String){
        var markerOptions:MarkerOptions= MarkerOptions()
        markerOptions.title(title)
        markerOptions.position(location)
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(location))
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,7f))
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
                Log.d("Nova lokacija","Nova lokacija")
                Toast.makeText(applicationContext, currentLocation.latitude.toString() + "" +
                        currentLocation.longitude, Toast.LENGTH_SHORT).show()

                setMyLocationMarker(LatLng(currentLocation.latitude,currentLocation.longitude),"My location")

            }
        }
        task.addOnFailureListener {
            Log.d("Greska","Nije nadjena lokacija")
        }

    }


}