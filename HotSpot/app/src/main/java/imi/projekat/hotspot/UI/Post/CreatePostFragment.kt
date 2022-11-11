package imi.projekat.hotspot.UI.Post

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.MenadzerSesije
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.LoginActivityViewModel
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentCreatePostBinding
import imi.projekat.hotspot.databinding.FragmentLoginAndRegisterBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreatePostFragment : Fragment() {
    private lateinit var binding:FragmentCreatePostBinding
    private val CAMERA_REQUEST_CODE = 1
    private val viewModel: MainActivityViewModel by activityViewModels()
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
            val token:String= MenadzerSesije.getToken(requireContext()).toString()
            viewModel.KreirajPost("SES")
        }

        binding.button3.setOnClickListener{
            cameraCheckPermission()
        }
    }
    private fun cameraCheckPermission(){
        Dexter.withContext(this.requireContext())
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA).withListener(
                    object : MultiplePermissionsListener{
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

    private fun camera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent,CAMERA_REQUEST_CODE)
        //startActivity(intent)
    }
    private fun showRotationalDialogForPermission(){
        AlertDialog.Builder(this.requireContext())
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
}