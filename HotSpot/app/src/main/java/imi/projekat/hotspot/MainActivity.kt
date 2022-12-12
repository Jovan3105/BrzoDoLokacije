package imi.projekat.hotspot

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import imi.projekat.hotspot.Dialogs.LoadingDialog
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.UI.HomePage.HomePageFragment
import imi.projekat.hotspot.UI.HomePage.HomePageFragmentDirections
import imi.projekat.hotspot.UI.Post.CreatePostFragment
import imi.projekat.hotspot.UI.Post.CreatePostFragmentDirections
import imi.projekat.hotspot.UI.Profile.MyProfileFragment
import imi.projekat.hotspot.UI.Profile.MyProfileFragmentDirections
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.fragment_home_page.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainActivityViewModel>()
    val dijalog= LoadingDialog(this)
    private lateinit var navKontroler: NavController
    private lateinit var homeDugme:Button
    public lateinit var bottomMenu:Menu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root


        setContentView(view)

        val navHostFragment=supportFragmentManager.findFragmentById(R.id.fragmentContainerViewMainActivity) as NavHostFragment
        navKontroler=navHostFragment.navController
        binding.bottomNavigationView2.setOnItemSelectedListener {
            val fragmentInstance = (supportFragmentManager.findFragmentById(R.id.fragmentContainerViewMainActivity) as NavHostFragment).childFragmentManager.primaryNavigationFragment

            when (it.itemId) {
                R.id.home -> {
                    when (fragmentInstance) {
                        is CreatePostFragment ->{
                            navKontroler.navigate(CreatePostFragmentDirections.actionCreatePostFragmentToHomePageFragment())
                        }
                        is MyProfileFragment->{
                            navKontroler.navigate(MyProfileFragmentDirections.actionMyProfileFragmentToHomePageFragment())
                        }
                        !is HomePageFragment->{
                            navKontroler.navigate(R.id.homePageFragment)
                        }
                    }
                }
                R.id.add -> {
                    when (fragmentInstance) {
                        is HomePageFragment ->{
                            navKontroler.navigate(HomePageFragmentDirections.actionHomePageFragmentToCreatePostFragment())
                        }
                        is MyProfileFragment->{
                            navKontroler.navigate(MyProfileFragmentDirections.actionMyProfileFragmentToCreatePostFragment())
                        }
                        !is CreatePostFragment->{
                            navKontroler.navigate(R.id.createPostFragment)
                        }
                    }
                }
                R.id.profile -> {
                    when (fragmentInstance) {
                        is HomePageFragment ->{
                            navKontroler.navigate(HomePageFragmentDirections.actionHomePageFragmentToMyProfileFragment())
                        }
                        is CreatePostFragment->{
                            navKontroler.navigate(CreatePostFragmentDirections.actionCreatePostFragmentToMyProfileFragment())
                        }
                        !is MyProfileFragment->{
                            navKontroler.navigate(R.id.myProfileFragment)
                        }
                    }

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

        this.lifecycleScope.launch{
            viewModel.GetPostsWithUserId.collectLatest{
                dijalog.isDismiss()
                if(it is BaseResponse.Loading){
                    dijalog.startLoading()
                }
            }
        }

        this.lifecycleScope.launch{
            viewModel.AllPostsSortedResponse.collectLatest{
                dijalog.isDismiss()
                if(it is BaseResponse.Loading){
                    dijalog.startLoading()
                }
            }
        }

        val bottomNavigationView: BottomNavigationView
        bottomNavigationView =
            binding.root.findViewById(binding.bottomNavigationView2.id) as BottomNavigationView
        bottomMenu =bottomNavigationView.menu

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


    fun startLoadingDialog(){
        dijalog.startLoading()
    }
    fun endLoadingDialog(){
        dijalog.isDismiss()
    }


}