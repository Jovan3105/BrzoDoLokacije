package imi.projekat.hotspot.UI.Profile

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.auth0.android.jwt.JWT
import com.google.android.material.snackbar.Snackbar
import imi.projekat.hotspot.Ostalo.*
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.LoginActivityViewModel
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentEditProfileBinding
import imi.projekat.hotspot.databinding.FragmentMyProfileBinding
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.applyConnectionSpec
import okhttp3.internal.cacheGet
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfileFragment : Fragment() {
    private lateinit var binding:FragmentEditProfileBinding
    private lateinit var imageView: ImageView
    private lateinit var file: File
    private lateinit var uri : Uri
    private lateinit var camIntent: Intent
    private lateinit var galIntent:Intent
    private lateinit var cropIntent:Intent
    private lateinit var btnImg: Button
    private lateinit var jwt: JWT
    private lateinit var profileImage:ImageView
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var selectedImageUri: Uri
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentEditProfileBinding.inflate(inflater)
        val view=inflater.inflate(R.layout.fragment_edit_profile,container,false)
        val token= MenadzerSesije.getToken(requireContext())
        if(token != null)
        {
            jwt= JWT(token)
            val usernameToken=jwt.getClaim("username").asString()
            val emailToken=jwt.getClaim("email").asString()
            profileImage=view.findViewById(binding.profileImage1.id)
            username=view.findViewById(binding.EditUsername.id)
            email=view.findViewById(binding.EditEmail.id)
            username.text=usernameToken
            email.text=emailToken
            //imageView=view.findViewById(binding.profileImage1.id)

        }


        return view

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentEditProfileBinding.bind(view)

        binding.EditProfileImage.setOnClickListener{
            contract.launch("image/*")
        }

        viewModel.liveEditProfileResponse.observe(viewLifecycleOwner){
            if(it is BaseResponse.Error){
                Toast.makeText(context, it.poruka, Toast.LENGTH_SHORT).show()
            }
            if(it is BaseResponse.Success){
                Toast.makeText(context, it.data.toString(), Toast.LENGTH_SHORT).show()
            }

        }

        binding.changeDugme.setOnClickListener {
            upload()
        }


    }

    //SLIKAAAAAAA

//    private fun openImageChooser() {
//        Intent(Intent.ACTION_PICK).also {
//            it.type = "image/*"
//            val mimeTypes = arrayOf("image/jpeg", "image/png")
//            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//            startActivityForResult(it, REQUEST_CODE_PICK_IMAGE)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                REQUEST_CODE_PICK_IMAGE -> {
//                    selectedImageUri = data?.data
//                    profileImage1.setImageURI(selectedImageUri)
//                }
//            }
//        }
//    }
    private val contract=registerForActivityResult(ActivityResultContracts.GetContent()){
        selectedImageUri=it!!
        profileImage.setImageURI(it)
    }

    private fun upload(){
//        val filesDir= getActivity()?.getApplicationContext()?.filesDir
//        val file=File(filesDir,"image.png")
//        val inputStream=getActivity()?.contentResolver?.openInputStream(selectedImageUri!!)
//        val outputStream=FileOutputStream(file)
//        inputStream!!.copyTo(outputStream)
//        val requestBody=file.asRequestBody("image/*".toMediaTypeOrNull())
//        val part=MultipartBody.Part.createFormData("profile",file.name,requestBody)
//        Log.d("Briz",MenadzerSesije.getToken(requireContext()).toString())

        val parcelFileDescriptor=getActivity()?.contentResolver?.openFileDescriptor(selectedImageUri!!,"r",null)?:return
        val inputStream=FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file=File(getActivity()?.cacheDir,getActivity()?.contentResolver?.getFileName(selectedImageUri!!))
        val outputStream=FileOutputStream(file)
        inputStream.copyTo(outputStream)
        val requestBody=file.asRequestBody("slika".toMediaTypeOrNull())
        val part=MultipartBody.Part.createFormData("slika",file.name,requestBody)

        viewModel.ChangeProfilePhoto(part)

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

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
    }

//    private fun uploadImage(){
//        if (selectedImageUri == null) {
//            binding.EditProfileImage.snackbar("Select an Image First")
//            return
//        }
//
//        val ParcelFileDescriptor=.openFileDescriptor(selectedImageUri!!, "r", null) ?: return
//
//        val file = File(cacheDir, contentResolver.getFileName(selectedImageUri!!))
//
//    }







    }
