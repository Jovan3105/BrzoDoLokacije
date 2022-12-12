package imi.projekat.hotspot.UI.Mapa

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import imi.projekat.hotspot.MainActivity
import imi.projekat.hotspot.ModeliZaZahteve.likeDTS
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.R
import imi.projekat.hotspot.UI.HomePage.HomePageFragmentDirections
import imi.projekat.hotspot.UI.HomePage.ListaPostovaAdapter
import imi.projekat.hotspot.UI.HomePage.PostClickHandler
import imi.projekat.hotspot.UI.MyItem
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentHomePageBinding
import imi.projekat.hotspot.databinding.FragmentPrikazListePostovaBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs


class PrikazListePostova : Fragment(), PostClickHandler {

    private lateinit var binding: FragmentPrikazListePostovaBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var listaPostova:ArrayList<singlePost>
    private lateinit var listaPostovaAdapter: ListaPostovaAdapter
    private lateinit var recyclerView: ViewPager2
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentPrikazListePostovaBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listaPostovaInit()



        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.ClusterPosts.collectLatest {
                    listaPostova= arrayListOf<singlePost>()
                    listaPostova.clear()
                    listaPostova= it as java.util.ArrayList<singlePost>
                    if(listaPostova.size==0){
                        parentFragmentManager.popBackStack()
                        return@collectLatest
                    }

                    initPostsCarousel(0)
                    setupTransformer()
                }
            }

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
                                listaPostova[i].brojlajkova++
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
                                listaPostova[i].brojlajkova--
                                break
                            }
                        }
                        listaPostovaAdapter.update(listaPostova)
                    }
                }
            }
        }
    }

    private fun listaPostovaInit(){
        listaPostova= arrayListOf<singlePost>()
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

    override fun clickedPostItem(post: singlePost) {
        val action: NavDirections = PrikazListePostovaDirections.actionPrikazListePostovaToSinglePostFragment(post.postID)
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
        val action: NavDirections = PrikazListePostovaDirections.actionPrikazListePostovaToDrugiKorisnik2(idKorisnika)
        findNavController().navigate(action)
    }

}