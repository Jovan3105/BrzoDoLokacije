package imi.projekat.hotspot.UI.Post

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import imi.projekat.hotspot.LoginActivity
import imi.projekat.hotspot.MainActivity
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.LoginActivityViewModel
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentCreatePostBinding
import imi.projekat.hotspot.databinding.FragmentLoginAndRegisterBinding
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreatePostFragment : Fragment() {
    private lateinit var binding:FragmentCreatePostBinding
    private val CAMERA_REQUEST_CODE = 1
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var slikaView:ImageView


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
        slikaView=view.findViewById(binding.imageView5.id)

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
        slikaView.setImageBitmap(bitmap)
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
        if(it.data==null)
            return@registerForActivityResult
        if(it?.data?.clipData!=null){
            Log.d("Vise slika", it?.data?.clipData!!.itemCount.toString())
            return@registerForActivityResult
        }

        var bitmapaSlike:Bitmap
        val slika= it.data!!.data
        var resolver = requireActivity().contentResolver
        if(resolver!=null){
            if (Build.VERSION.SDK_INT >= 28) {
                val source= ImageDecoder.createSource(resolver, slika!!)
                bitmapaSlike=ImageDecoder.decodeBitmap(source)
                slikaView.setImageBitmap(bitmapaSlike)
            } else {
                bitmapaSlike=MediaStore.Images.Media.getBitmap(resolver,slika)
                slikaView.setImageBitmap(bitmapaSlike)
            }
            Log.d("Jedna slika","Jedna slika")
            //slikaView.setImageBitmap(bitmap)
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
}