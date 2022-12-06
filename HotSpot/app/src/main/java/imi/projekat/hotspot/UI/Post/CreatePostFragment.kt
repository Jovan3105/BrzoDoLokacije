package imi.projekat.hotspot.UI.Post

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import imi.projekat.hotspot.BuildConfig
import imi.projekat.hotspot.MapsActivity
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentCreatePostBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator3
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs

private const val kraciOpis="kraciOpis"
class CreatePostFragment : Fragment(),addImageInterface {
    private lateinit var binding:FragmentCreatePostBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList:ArrayList<Bitmap>
    private lateinit var imageListUri:ArrayList<Uri?>
    private lateinit var partovi:ArrayList<MultipartBody.Part>
    private lateinit var adapter:ImageAdapter
    private lateinit var circleIndicator:CircleIndicator3
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var longitude=""
    private var latitude=""
    private var nazivLokacije=""
    private var locationSet=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentCreatePostBinding.bind(view)

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                longitude= data!!.getStringExtra("longitude").toString()
                latitude= data.getStringExtra("latitude").toString()
                nazivLokacije= data.getStringExtra("nazivLokacije").toString()
                locationSet=true
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.DodajPostResposne.collectLatest{
                if(it is BaseResponse.Error){
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    val content = it.data!!.charStream().readText()
                    Log.d(content,content)
                    val id = UpravljanjeResursima.getResourceString(content,requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_createPostFragment_to_homePageFragment)
                }
            }
        }

        binding.button2.setOnClickListener{
            addImage()
        }
        binding.button4.setOnClickListener {
            val intent = Intent(requireContext(), MapsActivity::class.java)
            resultLauncher.launch(intent)
        }

        binding.button3.setOnClickListener{
            sendPost()
            //sendPost2()
            //ConvertCameraBitmapToUri(imageBitmapListFromCamera,requireContext())
        }

        circleIndicator=view.findViewById<CircleIndicator3>(R.id.circleIndikator)

        imageList= ArrayList()
        imageListUri= ArrayList()
        partovi= ArrayList()
        val myimage = (ResourcesCompat.getDrawable(this.resources, R.drawable.addimagevector, null) as VectorDrawable).toBitmap()
        imageList.add(myimage)
        initImageCarousel(0)
        setupTransformer()

        viewPager2.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable,5000)
            }
        })
        circleIndicator.setViewPager(viewPager2)



    }
    private val runnable= Runnable {
        viewPager2.currentItem=viewPager2.currentItem+1
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }


    //KAMERA
    fun cameraCheckPermission(){
        Dexter.withContext(requireContext())
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(
                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            if(report.areAllPermissionsGranted()){
                                camera()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showRotationalDialogForPermission()
                    }
                }
            ).onSameThread().check()
    }

    val getActionCamera=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val bitmap=it?.data?.extras?.get("data") as Bitmap
        imageList.add(bitmap)
        convertCameraBitmapToUri(bitmap,requireContext())
        //Log.d("Uri sa kamere",uri.toString())
        praznaLista=false
        initImageCarousel(0)
        setupTransformer()

    }

    private fun camera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            getActionCamera.launch(intent)
        }
        catch (e:Exception) {
            Log.d("GRESKA",e.toString())
        }
    }
    private fun showRotationalDialogForPermission(){
        AlertDialog.Builder(requireContext())
            .setMessage("It looks like you have turned off permissions"
                    +"required for this feature. It can be enabled under App Settings.")

            .setPositiveButton("Go TO SETTINGS"){_,_->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package","packageName",null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("CANCEL"){dialog, _->
                dialog.dismiss()
            }.show()
    }




    //Galerija
    val getActionGallery=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        //ako je vraceno vise slika
        val resolver = requireActivity().contentResolver
        var bitmapaSlike:Bitmap

        if(it.data==null)
            return@registerForActivityResult
        if(it?.data?.clipData!=null){
            if (Build.VERSION.SDK_INT >= 28) {
                for (i in 0 until  it.data?.clipData!!.itemCount){
                    val source= ImageDecoder.createSource(resolver, it.data!!.clipData?.getItemAt(i)?.uri!!)
                    bitmapaSlike=ImageDecoder.decodeBitmap(source)
                   imageListUri.add(it.data!!.clipData?.getItemAt(i)?.uri!!)
                    imageList.add(bitmapaSlike)
                }
            } else {
                for (i in 0..it.data?.clipData!!.itemCount){
                    bitmapaSlike=MediaStore.Images.Media.getBitmap(resolver,it.data!!.clipData?.getItemAt(i)?.uri!!)
                   imageListUri.add(it.data!!.clipData?.getItemAt(i)?.uri!!)
                    imageList.add(bitmapaSlike)
                }

            }
            praznaLista=false
            initImageCarousel(0)
            setupTransformer()

            return@registerForActivityResult
        }


        val slika= it.data!!.data
        if (slika!=null)
        if(resolver!=null){
            if (Build.VERSION.SDK_INT >= 28) {
                val source= ImageDecoder.createSource(resolver, slika)
                bitmapaSlike=ImageDecoder.decodeBitmap(source)
                imageListUri.add(it.data!!.data)
                imageList.add(bitmapaSlike)
            } else {
                bitmapaSlike=MediaStore.Images.Media.getBitmap(resolver,slika)
               imageListUri.add(it.data!!.data)
                imageList.add(bitmapaSlike)
            }
            praznaLista=false
            initImageCarousel(0)
            setupTransformer()

        }
    }


    fun otvoriGaleriju(){
        var stanje=false
        Dexter.withContext(requireContext()).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                galerija()
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                Toast.makeText(
                    requireContext(),
                    "You have denied the storage permission to select image",
                    Toast.LENGTH_SHORT
                ).show()
                showRotationalDialogForPermission()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?) {
                showRotationalDialogForPermission()
            }
        }).onSameThread().check()
    }

    private fun galerija() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action=Intent.ACTION_GET_CONTENT
        try {
            getActionGallery.launch(intent)
        }
        catch (e:Exception) {
            Log.d("GRESKA",e.toString())
        }
    }



    private fun initImageCarousel(position:Int){

        viewPager2= requireView().findViewById(binding.viewPager2.id)
        handler= Handler(Looper.myLooper()!!)
        adapter= ImageAdapter(imageList,viewPager2,praznaLista,this)
        viewPager2.adapter = adapter
        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                if (position == 0) {

                }
                else if (position == 1) {

                }
                else if (position == 2){

                }

                super.onPageSelected(position)
            }
        })
    }

    private fun setupTransformer(){
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }

        viewPager2.setPageTransformer(transformer)
        circleIndicator.setViewPager(viewPager2)
    }


    override fun addImage() {
        val dialog=Dialog(requireContext(),R.style.MyDialog)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_camera_or_gallery)

        dialog.findViewById<ImageView>(R.id.btnCloseDialog).setOnClickListener{
            Log.d("Gasenje dialoga","Gasenje dialoga")
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.galleryButton).setOnClickListener{
            dialog.dismiss()
            otvoriGaleriju()
        }
        dialog.findViewById<Button>(R.id.cameraButton).setOnClickListener{
            dialog.dismiss()
            cameraCheckPermission()
        }
        dialog.show()

    }

    override fun removeImage(id: Int) {
        var pom=id
        imageList.removeAt(pom)
       imageListUri.removeAt(pom)
        if(imageList.size==0){
            praznaLista=true
            val myimage = (ResourcesCompat.getDrawable(viewPager2.resources, R.drawable.addimagevector, null) as VectorDrawable).toBitmap()
            imageList.add(myimage)
        }
        if(pom>imageList.size-1){
            pom -= 1
        }
        initImageCarousel(pom)
        setupTransformer()

    }

    override var praznaLista: Boolean = true
        set(value) {
            if(field==true && value==false){
                imageList.removeAt(0)
                //imageListUri.removeAt(0)
            }
            field=value
        }
    private var uri:Uri?=null

    private fun convertCameraBitmapToUri(image:Bitmap,context:Context){
        val imagesFolder:File=File(context.cacheDir,"images")

        try {
                imagesFolder.mkdirs()
                val file:File=File(imagesFolder,"captured_image.jpg")
                val stream:FileOutputStream=FileOutputStream(file)
                image.compress(Bitmap.CompressFormat.JPEG,100,stream)
                stream.flush()
                stream.close()
                imageListUri.add(FileProvider.getUriForFile(context.applicationContext,
                    BuildConfig.APPLICATION_ID+".provider",file))

        }catch (e:FileNotFoundException){
            e.printStackTrace()
        }catch(e:IOException){
            e.printStackTrace()
        }

    }

    private fun sendPost(){
        val location=MultipartBody.Part.createFormData("location","Proba")
        val description=MultipartBody.Part.createFormData("description",binding.opisPosta.text.toString())
        val shortDescription=MultipartBody.Part.createFormData("shortDescription",binding.kratakOpisPosta.text.toString())
        if(binding.opisPosta.text.toString().isNullOrEmpty()){
            Toast.makeText(requireContext(), "Insert your description", Toast.LENGTH_SHORT).show()
            return
        }
        if(binding.kratakOpisPosta.text.toString().isNullOrEmpty()){
            Toast.makeText(requireContext(), "Insert your description", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("duzina niza sa uriima:",imageListUri.size.toString())
        if(partovi.size!=0)
        {
            partovi.clear()
        }
        for (i in 0 until  imageListUri.size){
            Log.d("URI",imageListUri[i].toString())
            if(imageListUri[i]!=null)
            {

                val parcelFileDescriptor=getActivity()?.contentResolver?.openFileDescriptor(imageListUri[i]!!,"r",null)?:return
                val inputStream=FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file=File(getActivity()?.cacheDir,getActivity()?.contentResolver?.getFileName(imageListUri[i]!!))
                val outputStream=FileOutputStream(file)
                inputStream.copyTo(outputStream)
                val requestBody=file.asRequestBody("photos".toMediaTypeOrNull())
                partovi.add(MultipartBody.Part.createFormData("photos",file.name,requestBody))



            }

        }
        if(partovi.size==0)
        {
            Toast.makeText(requireContext(), "Insert your image", Toast.LENGTH_SHORT).show()
            return
        }
        if(!locationSet){
            Toast.makeText(requireContext(), "Insert your location", Toast.LENGTH_SHORT).show()
            return
        }
        val longitude1=MultipartBody.Part.createFormData("longitude",longitude)
        val latitude1=MultipartBody.Part.createFormData("latitude",latitude)
        val nazivLokacije=MultipartBody.Part.createFormData("nazivLokacije",nazivLokacije)
        viewModel.addPost(partovi,description,longitude1,latitude1,shortDescription,nazivLokacije)

    }

    fun ContentResolver.getFileName(fileUri: Uri): String {
        var name = ""
        val returnCursor = this.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
        return name
    }

}