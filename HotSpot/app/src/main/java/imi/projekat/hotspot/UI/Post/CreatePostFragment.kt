package imi.projekat.hotspot.UI.Post

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentCreatePostBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class CreatePostFragment : Fragment(),addImageInterface {
    private lateinit var binding:FragmentCreatePostBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList:ArrayList<Bitmap>
    private lateinit var adapter:ImageAdapter

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

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.KreirajPostResponse.collectLatest{
                if(it is BaseResponse.Error){
                    Toast.makeText(context, it.poruka, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    Log.d("SES","SESESSE")
                    Toast.makeText(context, it.data.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }



        binding.button2.setOnClickListener{
            otvoriGaleriju()
        }

        binding.button3.setOnClickListener{
            cameraCheckPermission()
        }

        imageList= ArrayList()
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
    }
    private val runnable= Runnable {
        viewPager2.currentItem=viewPager2.currentItem+1
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()

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
                    imageList.add(bitmapaSlike)
                }
            } else {
                for (i in 0..it.data?.clipData!!.itemCount){
                    bitmapaSlike=MediaStore.Images.Media.getBitmap(resolver,it.data!!.clipData?.getItemAt(i)?.uri!!)
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
                val source= ImageDecoder.createSource(resolver, slika!!)
                bitmapaSlike=ImageDecoder.decodeBitmap(source)
                imageList.add(bitmapaSlike)
            } else {
                bitmapaSlike=MediaStore.Images.Media.getBitmap(resolver,slika)
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


    }

    private fun setupTransformer(){
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }

        viewPager2.setPageTransformer(transformer)
    }


    override fun addImage() {
        Log.d("BRZI","Je dobar programer")
    }

    override fun removeImage(id: Int) {
        var pom=id
        imageList.removeAt(pom)
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
        get() = field
        set(value) {
            if(field==true && value==false){
                imageList.removeAt(0)
            }
            field=value
        }

}