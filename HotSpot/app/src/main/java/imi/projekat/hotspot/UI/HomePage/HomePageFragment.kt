package imi.projekat.hotspot.UI.HomePage

import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.TextureView.SurfaceTextureListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import imi.projekat.hotspot.MapsActivity
import imi.projekat.hotspot.ModeliZaZahteve.likeDTS
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.R
import imi.projekat.hotspot.UI.MapaZaPrikazPostovaDirections
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentHomePageBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs


class HomePageFragment : Fragment() {

    private lateinit var binding: FragmentHomePageBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var bttAnimacija: Animation
    private lateinit var videoView: VideoView
    private var currentPosition = 0

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




        videoView=requireView().findViewById(binding.videoView.id)
        var uri: Uri =Uri.parse("android.resource://"+ requireContext().getPackageName()+"/"+R.raw.velika_srbija)

        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
//        videoView.setOnPreparedListener(object :OnPreparedListener{
//            override fun onPrepared(mp: MediaPlayer?) {
//                mp!!.isLooping=true
//            }
//        })
        videoView.setOnCompletionListener {
            videoView.start()
        }


        binding.mapaDugme.setOnClickListener{
            viewModel.getAllPostsSorted(3)
            val action: NavDirections = HomePageFragmentDirections.actionHomePageFragmentToMapaZaPrikazPostova(-1)
            findNavController().navigate(action)
        }


        //viewModel.getPostsByUserId(1)
        bttAnimacija.duration=1000;
        binding.mapaDugme.startAnimation(bttAnimacija)




//        binding.button6.setOnClickListener {
//            viewModel.getPostsByUserId(1)
//            //findNavController().navigate(R.id.action_homePageFragment_to_singlePostFragment)
//        }



    }



    override fun onResume() {
        super.onResume()
        if (!videoView!!.isPlaying) {
            if (currentPosition != 0)
                videoView!!.seekTo(currentPosition)

            videoView!!.start()
        }
    }

    override fun onPause() {
        super.onPause()
        videoView!!.pause()
        currentPosition = videoView!!.currentPosition
    }

    override fun onStop() {
        videoView!!.pause()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("position", currentPosition)
        super.onSaveInstanceState(outState)
    }

}
