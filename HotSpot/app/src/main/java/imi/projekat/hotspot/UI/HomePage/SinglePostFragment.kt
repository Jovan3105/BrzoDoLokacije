package imi.projekat.hotspot.UI.HomePage

import android.graphics.Bitmap
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentSinglePostBinding
import kotlinx.android.synthetic.main.fragment_single_post.view.*
import me.relex.circleindicator.CircleIndicator3
import kotlin.math.abs


class SinglePostFragment : Fragment() {
    private lateinit var binding:FragmentSinglePostBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList:ArrayList<Bitmap>
    private lateinit var adapter:ImageAdapterHomePage
    private lateinit var circleIndicator: CircleIndicator3

    override fun onResume() {
        super.onResume()
        val CommentDropdownSort=resources.getStringArray(R.array.CommentDropdownSort)
        val arrayAdapter=ArrayAdapter(requireContext(),R.layout.dropdown_item,CommentDropdownSort)

        binding.commentsSelector.setAdapter(arrayAdapter)
        binding.commentsSelector.setText(CommentDropdownSort[0],false)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_single_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentSinglePostBinding.bind(view)




        circleIndicator=view.findViewById<CircleIndicator3>(R.id.circleIndikator)
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
        circleIndicator.setViewPager(viewPager2)

    }

    private val runnable= Runnable {
        viewPager2.currentItem=viewPager2.currentItem+1
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    private fun initImageCarousel(position:Int){

        viewPager2= requireView().findViewById(binding.viewPager2.id)
        handler= Handler(Looper.myLooper()!!)
        adapter= ImageAdapterHomePage(imageList,viewPager2)
        viewPager2.adapter = adapter
        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                if (position == 0) {

                }
                else if (position == 1) {

                }
                else if (position == 2){

                }

                super.onPageSelected(position)
            }
        })
    }

    private fun setupTransformer(){
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }

        viewPager2.setPageTransformer(transformer)
        circleIndicator.setViewPager(viewPager2)
    }
}