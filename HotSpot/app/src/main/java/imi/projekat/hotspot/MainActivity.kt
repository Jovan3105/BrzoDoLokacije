package imi.projekat.hotspot

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import imi.projekat.hotspot.Dialogs.LoadingDialog
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainActivityViewModel>()
    val dijalog= LoadingDialog(this)
    private lateinit var navKontroler: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment=supportFragmentManager.findFragmentById(R.id.fragmentContainerViewMainActivity) as NavHostFragment
        navKontroler=navHostFragment.navController

        binding.bottomNavigationView2.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    navKontroler.navigate(R.id.homePageFragment)
                }
                R.id.add -> {
                    navKontroler.navigate(R.id.createPostFragment)
                }
                R.id.profile -> {
                    navKontroler.navigate(R.id.myProfileFragment)
                }
            }
            true
        }

        viewModel.GetPostWithIdResponse.observe(this){
            dijalog.isDismiss()
            if(it is BaseResponse.Loading){
                dijalog.startLoading()
            }
        }

        this.lifecycleScope.launch{
            viewModel.DodajPostResposne.collectLatest{
                dijalog.isDismiss()
                if(it is BaseResponse.Loading){
                    dijalog.startLoading()
                }
            }
        }

        this.lifecycleScope.launch{

            viewModel.GreskaHendler.collectLatest{
                if(it is BaseResponse.Error){
                    dijalog.isDismiss()
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),applicationContext)
                    Toast.makeText(this@MainActivity,id, Toast.LENGTH_SHORT).show()
                }

            }
        }

        this.lifecycleScope.launch{
            viewModel.GetPostsWithUserId.collectLatest{
                dijalog.isDismiss()
                if(it is BaseResponse.Loading){
                    dijalog.startLoading()
                }
            }
        }

    }

    fun clearImageCache(){
        val executor = Executors.newSingleThreadExecutor()
        executor.execute(kotlinx.coroutines.Runnable {
            Glide.get(this).clearDiskCache()
        })
        Glide.get(this).clearMemory()
    }

    fun restartActivity(){
        finish();
        startActivity(getIntent());
    }

}