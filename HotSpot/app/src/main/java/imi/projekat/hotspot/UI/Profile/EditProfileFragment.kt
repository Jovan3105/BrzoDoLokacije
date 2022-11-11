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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.auth0.android.jwt.JWT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import imi.projekat.hotspot.Ostalo.*
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.LoginActivityViewModel
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentEditProfileBinding
import imi.projekat.hotspot.databinding.FragmentMyProfileBinding
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

class MyLifecycleObserver(private val registry : ActivityResultRegistry)
    : DefaultLifecycleObserver {
    lateinit var getContent : ActivityResultLauncher<String>

    override fun onCreate(owner: LifecycleOwner) {
        getContent = registry.register("key", owner, ActivityResultContracts.GetContent()) { uri ->
            // Handle the returned Uri
        }
    }

    fun selectImage() {
        getContent.launch("image/*")
    }
}


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
    private var selectedImageUri: Uri?=null
    private lateinit var observer : MyLifecycleObserver

    private val contract= registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        selectedImageUri=uri!!
        profileImage.setImageURI(uri)

    }

//    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val intent = result.data
//            // Handle the Intent
//            selectedImageUri=intent?.data!!
//            profileImage.setImageURI(selectedImageUri)
//        }
//    }

    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observer = MyLifecycleObserver(requireActivity().activityResultRegistry)
        lifecycle.addObserver(observer)
    }

    private fun setup(){
        val token= MenadzerSesije.getToken(requireContext())


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
            imageView=view.findViewById(binding.profileImage1.id)

        }


        return view

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentEditProfileBinding.bind(view)

        binding.EditProfileImage.setOnClickListener{
            contract.launch("image/*")
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.liveEditProfileResponse.collectLatest{
                if(it is BaseResponse.Error){
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    Log.d("SES","SESESSE")
                    val id = UpravljanjeResursima.getResourceString(it.data?.message.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                    if(!it.data?.token.isNullOrEmpty())
                    {
                        MenadzerSesije.saveAuthToken(requireContext(),it.data?.token.toString())
                    }
                    findNavController().navigate(R.id.action_editProfileFragment_to_myProfileFragment)
                }
            }
        }

//        viewModel.liveEditProfileResponse.observe(viewLifecycleOwner){
//            if(it is BaseResponse.Error){
//                Toast.makeText(context, it.poruka, Toast.LENGTH_SHORT).show()
//            }
//            if(it is BaseResponse.Success){
//                val id = UpravljanjeResursima.getResourceString(it.data?.message.toString(),requireContext())
//                Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
//                if(!it.data?.token.isNullOrEmpty())
//                {
//                    MenadzerSesije.saveAuthToken(requireContext(),it.data?.token.toString())
//                }
//
//                findNavController().navigate(R.id.action_editProfileFragment_to_myProfileFragment)
//
//            }
//
//        }

        binding.changeDugme.setOnClickListener {
            upload()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("unistavanje","brzi")


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


    private fun upload(){
        val pomUsername=binding.EditUsername.text.toString().trim()
        val pomEmail=binding.EditEmail.text.toString().trim()
        val pomOldPassword=binding.OldPassword.text.toString().trim()
        val pomNewPasswrod=binding.NewPassword.text.toString().trim()
        Log.d("zicla",pomUsername)
        if(pomUsername.isBlank())
        {
            binding.EditUsername.setError(getString(R.string.InsertYourUsername))
            return
        }
        if(pomUsername.length<4)
        {
            binding.EditUsername.setError(getString(R.string.UserNameShortLength))
            return
        }

        if(pomUsername.length>20)
        {
            binding.EditUsername.setError(getString(R.string.UserNameLongLength))
            return
        }

        if(pomEmail.isBlank())
        {
            binding.EditEmail.setError(getString(R.string.InsertYourEmail))
            return
        }

        val regexforemail="^[a-zA-Z0-9_\\.-]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$".toRegex()
        if(!regexforemail.matches(pomEmail))
        {
            binding.EditEmail.setError(getString(R.string.ErrorEmailForm))
            return
        }


        if(pomOldPassword.isBlank())
        {
            binding.passwordWrapper.endIconMode= TextInputLayout.END_ICON_NONE
            binding.OldPassword.setError(getString(R.string.OldPasswordEmpty))
            return
        }

        if(pomNewPasswrod.isNotBlank())
        {
            if(pomNewPasswrod.length<5)
            {
                binding.passwordWrapper1.endIconMode= TextInputLayout.END_ICON_NONE
                binding.NewPassword.setError(getString(R.string.ShortPasswordLength))
                return
            }

            if(pomNewPasswrod.length>20)
            {
                binding.passwordWrapper1.endIconMode= TextInputLayout.END_ICON_NONE
                binding.NewPassword.setError(getString(R.string.PasswordLongLegth))
                return
            }

            if(!pomNewPasswrod.equals(binding.ConfirmnewPassword1.text.toString()))
            {
                binding.passwordWrapper2.endIconMode= TextInputLayout.END_ICON_NONE
                binding.ConfirmnewPassword1.setError(getString(R.string.PasswordsAreNotTheSame))
                return
            }
        }
        val usernameToken1=jwt.getClaim("username").asString()
        val emailToken1=jwt.getClaim("email").asString()
        if(usernameToken1.equals(pomUsername) && emailToken1.equals(pomEmail) && selectedImageUri==null && pomNewPasswrod.isBlank()){
            Log.d("Ne valja","Unesite izmenu")
            return
        }

        val username=MultipartBody.Part.createFormData("Username",pomUsername)
        val email=MultipartBody.Part.createFormData("Email",pomEmail)
        val oldpassword=MultipartBody.Part.createFormData("OldPassword",pomOldPassword)
        val newpassword=MultipartBody.Part.createFormData("NewPassword",pomNewPasswrod)//MOGUC BAG
//        val parcelFileDescriptor=getActivity()?.contentResolver?.openFileDescriptor(selectedImageUri!!,"r",null)?:return
//        val inputStream=FileInputStream(parcelFileDescriptor.fileDescriptor)
//        val file=File(getActivity()?.cacheDir,getActivity()?.contentResolver?.getFileName(selectedImageUri!!))
//        val outputStream=FileOutputStream(file)
//        inputStream.copyTo(outputStream)
//        val requestBody=file.asRequestBody("slika".toMediaTypeOrNull())
//        val part=MultipartBody.Part.createFormData("slika",file.name,requestBody)
//        viewModel.ChangeProfilePhoto(part,username,email,oldpassword,newpassword)

        if(selectedImageUri!=null)
        {
            val parcelFileDescriptor=getActivity()?.contentResolver?.openFileDescriptor(selectedImageUri!!,"r",null)?:return
            val inputStream=FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file=File(getActivity()?.cacheDir,getActivity()?.contentResolver?.getFileName(selectedImageUri!!))
            val outputStream=FileOutputStream(file)
            inputStream.copyTo(outputStream)
            val requestBody=file.asRequestBody("slika".toMediaTypeOrNull())
            val part=MultipartBody.Part.createFormData("slika",file.name,requestBody)
            viewModel.ChangeProfilePhoto(part,username,email,oldpassword,newpassword)
        }
        else
        {
            val part=MultipartBody.Part.createFormData("slika","")
            viewModel.ChangeProfilePhoto(part,username,email,oldpassword,newpassword)
        }



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
