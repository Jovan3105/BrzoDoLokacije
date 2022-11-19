package imi.projekat.hotspot.UI.HomePage.SinglePost

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import imi.projekat.hotspot.ModeliZaZahteve.commentDTS
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentSinglePostBinding
import kotlinx.android.synthetic.main.fragment_single_post.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator3
import kotlin.math.abs


class SinglePostFragment : Fragment() {
    private lateinit var binding:FragmentSinglePostBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList:ArrayList<Bitmap>
    private lateinit var adapter: ImageAdapterHomePage
    private lateinit var circleIndicator: CircleIndicator3
    private lateinit var opisTekstView: TextView
    private lateinit var kratakOpisView: TextView
    private var idPosta:Int=-1
    private var currentSort=0

    override fun onResume() {
        super.onResume()
        val CommentDropdownSort=resources.getStringArray(R.array.CommentDropdownSort)
        val arrayAdapter=ArrayAdapter(requireContext(),R.layout.dropdown_item,CommentDropdownSort)
        binding.commentsSelector.setAdapter(arrayAdapter)
        (textInputLayout.getEditText() as AutoCompleteTextView).onItemClickListener =
            OnItemClickListener { adapterView, view, position, id ->
                if(position!=currentSort){
                    //pozovi back api
                }
                currentSort=position
            }

        binding.commentsSelector.setText(CommentDropdownSort[0],false)


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentSinglePostBinding.inflate(inflater,container,false)
        opisTekstView=binding.root.findViewById(binding.DuziOpis.id)
        kratakOpisView=binding.root.findViewById(binding.KratakOpis.id)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.GetPostWithIdResponse.observe(viewLifecycleOwner){
            when(it){
                is BaseResponse.Loading->{
                }
                is BaseResponse.Success->{
                    var array=it.data?.photos
                    if(array==null)
                        return@observe

                    imageList.clear()
                    for (i in 0 until  array.size){
                        var byte=Base64.decode(array.get(i),Base64.DEFAULT)
                        var bitmapa=BitmapFactory.decodeByteArray(byte,0,byte.size)
                        imageList.add(bitmapa)
                    }
                    initImageCarousel(0)
                    setupTransformer()
                    opisTekstView.text=it.data?.description
                    kratakOpisView.text=it.data?.shortDescription
                }
                is BaseResponse.Error->{
                }
            }
        }

        circleIndicator=view.findViewById<CircleIndicator3>(R.id.circleIndikator)
        imageList= ArrayList()
        val myimage = (ResourcesCompat.getDrawable(this.resources, R.drawable.addimagevector, null) as VectorDrawable).toBitmap()
        imageList.add(myimage)
        initImageCarousel(0)
        setupTransformer()


        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.PostCommentResponse.collectLatest{
                if(it is BaseResponse.Error){
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
//                    val content = it.data!!.charStream().readText()
//                    val id = UpravljanjeResursima.getResourceString(content,requireContext())
//                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                    Log.d("USPSENO DODAT KOMENTAR","USPSENO DODAT KOMENTAR")
                }
            }
        }

        viewPager2.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable,5000)
            }
        })
        circleIndicator.setViewPager(viewPager2)

        idPosta=1
        viewModel.getPostWithID(idPosta)


        binding.InsertCommentButton.setOnClickListener{
            addComment()
        }
    }

    private val runnable= Runnable {
        viewPager2.currentItem=viewPager2.currentItem+1
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    private fun initImageCarousel(position:Int){

        viewPager2=requireView().findViewById(binding.viewPager2.id)
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

    private fun addComment() {
        val dialog= Dialog(requireContext(),R.style.WrapEverythingDialog)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_insert_comment)

        dialog.findViewById<ImageView>(R.id.btnCloseDialog).setOnClickListener{
            Log.d("Gasenje dialoga","Gasenje dialoga")
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.addCommentButton).setOnClickListener{
            var comment=dialog.findViewById<EditText>(R.id.CommentTextBox).text.toString()
            viewModel.PostComment(commentDTS(0,idPosta,comment))
            dialog.dismiss()
        }

        dialog.show()

    }
}