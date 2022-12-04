package imi.projekat.hotspot.UI.HomePage

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import imi.projekat.hotspot.MapsActivity
import imi.projekat.hotspot.ModeliZaZahteve.likeDTS
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.SnapToBlock
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.R
import imi.projekat.hotspot.UI.HomePage.SinglePost.ImageAdapterHomePage
import imi.projekat.hotspot.UI.Profile.FollowingProfilesFragmentDirections
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentHomePageBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs


class HomePageFragment : Fragment(),PostClickHandler {

    private lateinit var binding: FragmentHomePageBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var listaPostova:ArrayList<singlePost>
    private lateinit var listaPostovaAdapter: ListaPostovaAdapter
    private lateinit var recyclerView: ViewPager2
    private lateinit var handler: Handler
    private lateinit var bttAnimacija: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        bttAnimacija= AnimationUtils.loadAnimation(requireContext(),R.anim.bot_to_top)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentHomePageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listaPostovaInit()





        binding.mapaDugme.setOnClickListener{
            val intent = Intent(requireContext(), MapsActivity::class.java)
            startActivity(intent)
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.GetPostsWithUserId.collectLatest{
                if(it is BaseResponse.Error){
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    if(it.data!=null){
                        listaPostova.clear()
                        listaPostova= it.data as ArrayList<singlePost>

                        initPostsCarousel(0)
                        setupTransformer()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.LikePostResponse.collectLatest{
                if(it is BaseResponse.Error){
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    if(it.data!=null){
                        val content = it.data!!.charStream().readText()
                        for (i in 0 until  listaPostova.size){
                            if(listaPostova[i].postID==content.toInt()){
                                listaPostova[i].likedByMe=true
                                break
                            }
                        }
                        listaPostovaAdapter.update(listaPostova)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.DislikePostResponse.collectLatest{
                if(it is BaseResponse.Error){
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    if(it.data!=null){
                        val content = it.data!!.charStream().readText()
                        for (i in 0 until  listaPostova.size){
                            if(listaPostova[i].postID==content.toInt()){
                                listaPostova[i].likedByMe=false
                                break
                            }
                        }
                        listaPostovaAdapter.update(listaPostova)
                    }
                }
            }
        }
        viewModel.getPostsByUserId(1)
        binding.mapaDugme.startAnimation(bttAnimacija)




//        binding.button6.setOnClickListener {
//            viewModel.getPostsByUserId(1)
//            //findNavController().navigate(R.id.action_homePageFragment_to_singlePostFragment)
//        }



    }

    private fun initPostsCarousel(position:Int){
        recyclerView=requireView().findViewById(binding.ListaPostovaRecyclerView.id)
        handler= Handler(Looper.myLooper()!!)
        listaPostovaAdapter= ListaPostovaAdapter(listaPostova,this)
        recyclerView.adapter = listaPostovaAdapter
        recyclerView.orientation=ViewPager2.ORIENTATION_VERTICAL
    }

    private fun setupTransformer(){
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(10))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
        }
        recyclerView.setPageTransformer(transformer)
    }

    private fun listaPostovaInit(){
        listaPostova= arrayListOf<singlePost>()
    }

    override fun clickedPostItem(post: singlePost) {
        val action:NavDirections=HomePageFragmentDirections.actionHomePageFragmentToSinglePostFragment(post.postID)
        findNavController().navigate(action)
    }

    override fun likePost(like: likeDTS) {
        viewModel.likePost(like)
    }

    override fun dislikePost(like: likeDTS) {
        viewModel.dislikePost(like)
    }

    override fun getPicture(imageView: ImageView, slikaPath:String) {
        viewModel.dajSliku(imageView,slikaPath,requireContext())
    }

    override fun clickOnUser(idKorisnika: Int) {
        val action: NavDirections = HomePageFragmentDirections.actionHomePageFragmentToDrugiKorisnik(idKorisnika)
        findNavController().navigate(action)
    }


}
