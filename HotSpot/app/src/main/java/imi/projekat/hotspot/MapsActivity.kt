package imi.projekat.hotspot

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiActivity

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import imi.projekat.hotspot.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,EasyPermissions.PermissionCallbacks {

    private lateinit var mMap: GoogleMap
private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityMapsBinding.inflate(layoutInflater)
     setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestLocPermission()
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
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            SettingsDialog.Builder(this).build().show()
            return
        }
        requestLocPermission()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        checkGooglePlayServices();
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


}