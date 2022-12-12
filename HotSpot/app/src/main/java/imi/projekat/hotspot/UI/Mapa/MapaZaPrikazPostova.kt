package imi.projekat.hotspot.UI

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.auth0.android.jwt.JWT
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.ClusterManager.OnClusterClickListener
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import imi.projekat.hotspot.KonfigAplikacije
import imi.projekat.hotspot.MainActivity
import imi.projekat.hotspot.ModeliZaZahteve.history
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.R
import imi.projekat.hotspot.UI.HomePage.SinglePost.SinglePostFragmentArgs
import imi.projekat.hotspot.UI.Mapa.HistoryAdapter
import imi.projekat.hotspot.UI.Profile.AdapterFollowingProfiles
import imi.projekat.hotspot.UI.Profile.MyProfileFragmentDirections
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.DialogMapsMarkerBinding
import imi.projekat.hotspot.databinding.FragmentMapaZaPrikazPostovaBinding
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_following_profiles.*
import kotlinx.android.synthetic.main.fragment_following_profiles.recycler
import kotlinx.android.synthetic.main.fragment_mapa_za_prikaz_postova.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


class MapaZaPrikazPostova : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks ,HistoryAdapter.OnItemClickListener{

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
    private lateinit var izadjiIzRecyclera:ImageButton
    private lateinit var searchDugmeActionBar: ImageButton
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var listaPostova:ArrayList<singlePost>
    private lateinit var listaPretrazenihLokacija:ArrayList<history>
    private lateinit var mClusterManager: ClusterManager<MyItem>
    private val args: MapaZaPrikazPostovaArgs by navArgs()
    private var layoutManager: RecyclerView.LayoutManager?=null
    private var adapter: HistoryAdapter?=null
    private var indikator:Int=0


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
        viewModel.getAllHistory()
        Log.d("OnViewCreated","Kocka i kocka")
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.PostHistoryResponse.collectLatest{
                if(it is BaseResponse.Error){
                    Toast.makeText(
                        requireContext(),
                        "Error saving history",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if(it is BaseResponse.Success){
                    Toast.makeText(
                        requireContext(),
                        "Successfully saved history",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.GetAllHistoryResponse.collectLatest{
                if(it is BaseResponse.Error){

                }
                if(it is BaseResponse.Success){
                    listaPretrazenihLokacija= arrayListOf<history>()
                    listaPretrazenihLokacija=it.data as ArrayList<history>
                    Log.d("lista pretrazenih lokacija",listaPretrazenihLokacija.size.toString())
//                    if(listaPretrazenihLokacija.size==0)
//                        listaPretrazenihLokacija= ArrayList<history>()
                }
            }
        }

        bttAnimacija= AnimationUtils.loadAnimation(requireContext(),R.anim.bot_to_top)
        top_exitAnimacija= AnimationUtils.loadAnimation(requireContext(),R.anim.top_exit)
        top_enterAnimacija= AnimationUtils.loadAnimation(requireContext(),R.anim.top_enter)
        searchTextDugme=binding.root.findViewById(binding.searchTextDugme.id)
        searchTextTekst=binding.root.findViewById(binding.searchTextView.id)
        izborTerenaDugme=binding.root.findViewById(binding.izborTerenaDugme.id)
        searchDugmeActionBar=binding.root.findViewById(binding.searchDugmeActionBar.id)
        izadjiIzRecyclera=binding.root.findViewById(binding.exitRecycler.id)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(requireContext())

        // Construct a PlacesClient
        Places.initialize(requireContext(), KonfigAplikacije.instanca.AppSettings.MAPS_API_KEY)
        placesClient = Places.createClient(requireContext())

        requestLocPermission()

        searchTextDugme.setOnClickListener{
           findLocation()
        }

        izadjiIzRecyclera.setOnClickListener{
            binding.recycler.visibility=View.GONE
            binding.exitRecycler.visibility=View.GONE
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
            if(binding.recycler.visibility== View.VISIBLE){
                binding.recycler.visibility=View.GONE
                binding.exitRecycler.visibility=View.GONE
            }
            showSearch()
        }

        searchTextTekst.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus)
            {
                CreateAdapter()
                if(listaPretrazenihLokacija.size!=0)
                {
                    showAdapter()
                }

            }
        }

        searchTextTekst.setOnClickListener {
            if(listaPretrazenihLokacija.size!=0){
                if(binding.recycler.visibility== View.GONE){
                    //binding.recycler.startAnimation(top_exitAnimacija)
                    binding.recycler.visibility=View.VISIBLE
                    binding.exitRecycler.visibility=View.VISIBLE
                }
            }

        }

    }

    private fun findLocation(){
        var unetaLokacija:String=searchTextTekst.text.toString()
        if(!unetaLokacija.isNullOrEmpty()){
            var geoCoder: Geocoder = Geocoder(requireContext(), Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geoCoder.getFromLocationName(unetaLokacija,1,object : Geocoder.GeocodeListener{
                    override fun onGeocode(addresses: MutableList<Address>) {
                        var listaAdresa=addresses

                        if(listaAdresa!=null && listaAdresa.size>0){
                            viewModel.postHistory(history(unetaLokacija))
                            if(binding.recycler.visibility== View.VISIBLE){
                                binding.recycler.startAnimation(top_exitAnimacija)
                                binding.recycler.visibility=View.GONE
                                binding.exitRecycler.visibility=View.GONE
                            }
                            checkLocations(unetaLokacija)
                            adapter!!.update(listaPretrazenihLokacija)
                           // recycler.layoutManager=layoutManager
                            recycler.adapter=adapter

                            val latLng=LatLng(listaAdresa.get(0).latitude,listaAdresa.get(0).longitude)
                            setLocationMarker(latLng,unetaLokacija,null,-1)
                            moveCameraToLocation(latLng,10f)
                            return
                        }
                        Toast.makeText(
                            requireContext(),
                            "Insert valid location",
                            Toast.LENGTH_SHORT
                        ).show()

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
                    viewModel.postHistory(history(unetaLokacija))
                    if(binding.recycler.visibility== View.VISIBLE){
                        binding.recycler.startAnimation(top_exitAnimacija)
                        binding.recycler.visibility=View.GONE
                        binding.exitRecycler.visibility=View.GONE
                    }
                    checkLocations(unetaLokacija)
                    // listaPretrazenihLokacija[indikator]=history(unetaLokacija)
                    //indikator+=1
                    //listaPretrazenihLokacija.add(0, history(unetaLokacija))
                    adapter!!.update(listaPretrazenihLokacija)
                    //recycler.layoutManager=layoutManager
                    recycler.adapter=adapter

                    val latLng=LatLng(listaAdresa.get(0).latitude,listaAdresa.get(0).longitude)
                    setLocationMarker(latLng,unetaLokacija,null,-1)
                    moveCameraToLocation(latLng,10f)
                }
                else
                {
                    Toast.makeText(
                        requireContext(),
                        "Insert valid location",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

    private fun checkLocations(lokacija:String){
        for (i in 0 until  listaPretrazenihLokacija.size){
            if(listaPretrazenihLokacija[i].location==lokacija)
            {

                listaPretrazenihLokacija.removeAt(i)
                listaPretrazenihLokacija.add(0,history(lokacija))
                return

            }
        }
        listaPretrazenihLokacija.add(0,history(lokacija))
        if(listaPretrazenihLokacija.size>5)
        {
            listaPretrazenihLokacija.removeAt(5)
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

    private fun CreateAdapter(){
        layoutManager= LinearLayoutManager(requireContext())
        adapter=HistoryAdapter(listaPretrazenihLokacija,this@MapaZaPrikazPostova)
        recycler.layoutManager=layoutManager
        recycler.adapter=adapter



    }

    private fun setLocationMarker(location:LatLng,title:String,zoom:Float?,brojPosta:Int){
//        googleMap.clear();
        var markerOptions: MarkerOptions = MarkerOptions()
        selectedLocation = location
        markerOptions.title(title)
        markerOptions.position(location)

        val icon = BitmapFactory.decodeResource(
            requireContext().resources,
            R.drawable.hotspotapplogo
        )
        val resizedBitmap = Bitmap.createScaledBitmap(icon, icon.width/2, icon.height/2, false)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
//        this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(location))
//        if(zoom!=null){
//            this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,zoom))
//        }

        var marker=this.googleMap.addMarker(markerOptions)
        marker?.tag=brojPosta
    }

    private fun moveCameraToLocation(location:LatLng,zoom:Float?){
//        googleMap.clear();
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(location))
        if(zoom!=null){
            this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,zoom))
        }
    }

    private fun showAdapter(){
        if(binding.recycler.visibility== View.VISIBLE){
            binding.recycler.startAnimation(top_exitAnimacija)
            binding.recycler.visibility=View.GONE
            binding.exitRecycler.visibility=View.GONE
            return
        }
        binding.recycler.startAnimation(top_enterAnimacija)
        binding.recycler.visibility=View.VISIBLE
        binding.exitRecycler.visibility=View.VISIBLE
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
            moveCameraToLocation(LatLng(defaultLocation.latitude,defaultLocation.longitude),10f)

        }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap=googleMap
        setUpClusterer()
        this.googleMap.setOnMarkerClickListener(object :OnMarkerClickListener{
            override fun onMarkerClick(p0: Marker): Boolean {
                val clickCount = p0.tag as? Int

                moveCameraToLocation(LatLng(p0.position.latitude,p0.position.longitude),null)

                val dialog=Dialog(requireContext(),R.style.MyDialog)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.dialog_maps_marker)

                var dialogMainBinding= DialogMapsMarkerBinding.inflate(getLayoutInflater())


                dialog.findViewById<ImageView>(R.id.btnCloseDialog).setOnClickListener{
                    dialog.dismiss()
                }
                dialog.findViewById<Button>(dialogMainBinding.showPostButton.id).setOnClickListener{
                    dialog.dismiss()
                    predjiNaPost(listaPostova[clickCount!!].postID)
                }


                viewModel.dajSliku(dialog.findViewById<ImageView>(dialogMainBinding.slikaPosta.id),"PostsFolder/"+listaPostova[clickCount!!].photos[0],requireContext())

                dialog.findViewById<TextView>(dialogMainBinding.Title.id).text=listaPostova[clickCount!!].shortDescription
                dialog.findViewById<TextView>(dialogMainBinding.numberOflikes.id).text="Number of likes:"+listaPostova[clickCount!!].brojlajkova.toString()
                dialog.findViewById<TextView>(dialogMainBinding.postDate.id).text=listaPostova[clickCount!!].dateTime


                dialog.show()
                p0.hideInfoWindow()
                return true
            }

        })


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
            return
        }
        this.googleMap.isMyLocationEnabled=true
        fetchLocation()


        googleMap.setOnMapClickListener(object :GoogleMap.OnMapClickListener {
            override fun onMapClick(latlng :LatLng) {
                var geoCoder: Geocoder = Geocoder(requireContext(), Locale.getDefault())
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geoCoder.getFromLocation(latlng.latitude,latlng.longitude,1,object : Geocoder.GeocodeListener{
                            override fun onGeocode(addresses: MutableList<Address>) {
                                var listaAdresa=addresses
                                if(listaAdresa!=null && listaAdresa.size>0){
                                    val latLng=LatLng(listaAdresa.get(0).latitude,listaAdresa.get(0).longitude)
                                    moveCameraToLocation(latLng,null)
                                }
                            }
                            override fun onError(errorMessage: String?) {
                                super.onError(errorMessage)

                            }

                        })
                    } else {
                        var listaAdresa: MutableList<Address>?
                        @Suppress("DEPRECATION")
                        listaAdresa= geoCoder.getFromLocation(latlng.latitude,latlng.longitude,1)
                        if(listaAdresa!=null && listaAdresa.size>0){
                            val latLng=LatLng(listaAdresa.get(0).latitude,listaAdresa.get(0).longitude)
//                            setLocationMarker(latLng,unetaLokacija,null)
                            moveCameraToLocation(latLng,10f)
                        }
                    }
                }
                catch (e:Exception){

                }

            }
        })


        val token= MenadzerSesije.getToken(requireContext())
        val jwt= JWT(token!!)
        val idKorisnika= jwt.getClaim("id").asString()!!.toInt()


        //ako je trenutni korisnik izabrani korisnika
        if(args.idKorisnika==idKorisnika) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.MyPostsResponse.collectLatest {
                        if (it is BaseResponse.Success) {
                            if (it.data == null) {
                                parentFragmentManager.popBackStack()
                                return@collectLatest
                            }
                            listaPostova = arrayListOf<singlePost>()
                            listaPostova.clear()
                            listaPostova = it.data as ArrayList<singlePost>
//                        (activity as MainActivity?)!!.endLoadingDialog()
                            for (i in 0 until listaPostova.size) {
//                            var latLng=LatLng(listaPostova[i].latitude, listaPostova[i].longitude)
//                            setLocationMarker(latLng,listaPostova[i].shortDescription,10f,i)
                                val offsetItem = MyItem(
                                    listaPostova[i].latitude,
                                    listaPostova[i].longitude,
                                    listaPostova[i].shortDescription,
                                    "Likes:" + listaPostova[i].brojlajkova,
                                    i
                                )
                                mClusterManager.addItem(offsetItem)
                            }
                            mClusterManager.cluster()
                        }
                        if (it is BaseResponse.Error) {
                            (activity as MainActivity?)!!.endLoadingDialog()
                            Toast.makeText(
                                requireContext(),
                                "Error while loading posts",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (it is BaseResponse.Loading) {

                        }
                    }
                }
            }
        }

        //Za sve korisnike
        if(args.idKorisnika==-1) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.AllPostsSortedResponse.collectLatest {
                        if (it is BaseResponse.Success) {
                            if (it.data == null) {
                                parentFragmentManager.popBackStack()
                                return@collectLatest
                            }
                            listaPostova = arrayListOf<singlePost>()
                            listaPostova.clear()
                            listaPostova = it.data as ArrayList<singlePost>
//                        (activity as MainActivity?)!!.endLoadingDialog()
                            for (i in 0 until listaPostova.size) {
//                            var latLng=LatLng(listaPostova[i].latitude, listaPostova[i].longitude)
//                            setLocationMarker(latLng,listaPostova[i].shortDescription,10f,i)
                                val offsetItem = MyItem(
                                    listaPostova[i].latitude,
                                    listaPostova[i].longitude,
                                    listaPostova[i].shortDescription,
                                    "Likes:" + listaPostova[i].brojlajkova,
                                    i
                                )
                                mClusterManager.addItem(offsetItem)
                            }
                            mClusterManager.cluster()
                        }
                        if (it is BaseResponse.Error) {
                            (activity as MainActivity?)!!.endLoadingDialog()
                            Toast.makeText(
                                requireContext(),
                                "Error while loading posts",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (it is BaseResponse.Loading) {

                        }
                    }
                }
            }
        }

    }

    private fun setUpClusterer() {
        mClusterManager = ClusterManager(requireContext(), googleMap)
        var renderer=PlaceRenderer(requireContext(),googleMap,mClusterManager)
        renderer.minClusterSize=1
        mClusterManager.renderer=renderer

        mClusterManager.onCameraIdle()



        mClusterManager.setOnClusterClickListener(object:OnClusterClickListener<MyItem>{
            override fun onClusterClick(cluster: Cluster<MyItem>?): Boolean {
                var listaKlusterPostova=ArrayList<singlePost>()
                for (i in 0 until  cluster!!.items.size){
                    listaKlusterPostova.add(listaPostova[cluster.items.elementAt(i).getTag()])
                }
                viewModel.setClusterPosts(listaKlusterPostova)

                findNavController().navigate(R.id.action_mapaZaPrikazPostova_to_prikazListePostova)

                return true
            }

        })

        mClusterManager.setOnClusterItemClickListener(object:OnClusterItemClickListener<MyItem>{
            override fun onClusterItemClick(item: MyItem?): Boolean {
                if (item==null)
                    return false
                val brojPosta=item.getTag()

                moveCameraToLocation(LatLng(listaPostova[brojPosta].latitude,listaPostova[brojPosta].longitude),null)

                val dialog=Dialog(requireContext(),R.style.MyDialog)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.dialog_maps_marker)

                val dialogMainBinding= DialogMapsMarkerBinding.inflate(getLayoutInflater())


                dialog.findViewById<ImageView>(R.id.btnCloseDialog).setOnClickListener{
                    dialog.dismiss()
                }
                dialog.findViewById<Button>(dialogMainBinding.showPostButton.id).setOnClickListener{
                    dialog.dismiss()
                    predjiNaPost(listaPostova[brojPosta].postID)
                }


                viewModel.dajSliku(dialog.findViewById<ImageView>(dialogMainBinding.slikaPosta.id),"PostsFolder/"+listaPostova[brojPosta].photos[0],requireContext())

                dialog.findViewById<TextView>(dialogMainBinding.Title.id).text=listaPostova[brojPosta].shortDescription
                dialog.findViewById<TextView>(dialogMainBinding.numberOflikes.id).text="Number of likes:"+listaPostova[brojPosta].brojlajkova.toString()


                var output: String=""
                val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    var curentDate: LocalDateTime = LocalDateTime.now()
                    val postDate = LocalDateTime.parse(listaPostova[brojPosta].dateTime)
                    var pom= Duration.between(postDate,curentDate).toDays()
                    when {

                        pom >=27->{
                            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            val formatter = SimpleDateFormat("dd.MM.yyyy")
                            output = formatter.format(parser.parse(listaPostova[brojPosta].dateTime))
                        }
                        pom in 2..26 -> {
                            output=pom.toString()+" days ago"
                        }
                        pom.toInt() ==1->{
                            output=pom.toString()+" day ago"
                        }
                        pom < 1-> {
                            var sati= Duration.between(postDate,curentDate).toHours()
                            when{
                                sati.toInt()==0->{
                                    output="Just now"
                                }
                                sati.toInt()==1->{
                                    output=sati.toString()+" hour ago"
                                }
                                else->{
                                    output=sati.toString()+" hours ago"
                                }
                            }
                        }

                        else -> {
                            output="ERROR"
                        }
                    }



                } else {
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
                    output = formatter.format(parser.parse(listaPostova[brojPosta].dateTime))
                }
                dialog.findViewById<TextView>(dialogMainBinding.postDate.id).text=output

                dialog.show()
                return true
            }

        })


        googleMap.setOnCameraIdleListener(mClusterManager)
        googleMap.setOnMarkerClickListener(mClusterManager)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(1,permissions,grantResults,this)
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

    private fun predjiNaPost(idPosta:Int){
        val action: NavDirections = MapaZaPrikazPostovaDirections.actionMapaZaPrikazPostovaToSinglePostFragment(idPosta)
        findNavController().navigate(action)
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
    private data class SaveData(var KEY_CAMERA_POSITION: CameraPosition?, var KEY_LOCATION: Location?): Parcelable

    override fun onItemClickForSearch(position: Int) {
        searchTextTekst.setText(listaPretrazenihLokacija[position].location)
        findLocation()

    }

    override fun onItemClickForDelete(position: Int) {
        //viewModel.DeleteHistory(listaPretrazenihLokacija[position].location)
        listaPretrazenihLokacija.removeAt(position)
        adapter!!.update(listaPretrazenihLokacija)
        if(listaPretrazenihLokacija.size==0)
        {
            binding.recycler.visibility=View.GONE
            binding.exitRecycler.visibility=View.GONE
        }
        //recycler.layoutManager=layoutManager
        //recycler.adapter=adapter
    }
}

class MyItem : ClusterItem {
    private val mPosition: LatLng
    private val mTitle: String
    private val mSnippet: String
    private var mTag: Int = 0

    constructor(lat: Double, lng: Double) {
        mPosition = LatLng(lat, lng)
        mTitle = ""
        mSnippet = ""
    }

    constructor(lat: Double, lng: Double, title: String, snippet: String,tag:Int) {
        mPosition = LatLng(lat, lng)
        mTitle = title
        mSnippet = snippet
        mTag=tag
    }

    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun getTitle(): String {
        return mTitle
    }

    override fun getSnippet(): String {
        return mSnippet
    }

    fun getTag(): Int {
        return mTag
    }
}

class PlaceRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<MyItem>
) : DefaultClusterRenderer<MyItem>(context, map, clusterManager) {

    /**
     * The icon to use for each cluster item
     */


    /**
     * Method called before the cluster item (the marker) is rendered.
     * This is where marker options should be set.
     */
    override fun onBeforeClusterItemRendered(
        item: MyItem,
        markerOptions: MarkerOptions
    ) {
        val icon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.hotspotapplogo
        )
        val resizedBitmap = Bitmap.createScaledBitmap(icon, icon.width/2, icon.height/2, false)


        markerOptions.title(item.title)
            .position(item.position)
            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
    }

    /**
     * Method called right after the cluster item (the marker) is rendered.
     * This is where properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(clusterItem: MyItem, marker: Marker) {
        marker.tag = clusterItem
    }
}