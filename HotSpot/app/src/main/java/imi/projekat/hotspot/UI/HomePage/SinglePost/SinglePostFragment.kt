package imi.projekat.hotspot.UI.HomePage.SinglePost

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.VectorDrawable
import android.os.Build
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import imi.projekat.hotspot.ModeliZaZahteve.commentDTS
import imi.projekat.hotspot.ModeliZaZahteve.likeDTS
import imi.projekat.hotspot.ModeliZaZahteve.singleComment
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.Ostalo.BaseResponse
import imi.projekat.hotspot.Ostalo.UpravljanjeResursima
import imi.projekat.hotspot.R
import imi.projekat.hotspot.UI.HomePage.PostClickHandler
import imi.projekat.hotspot.UI.HomePage.RecyclerAdapter
import imi.projekat.hotspot.UI.LoginRegister.ConfirmEmailArgs
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import imi.projekat.hotspot.databinding.FragmentSinglePostBinding
import kotlinx.android.synthetic.main.dialog_insert_comment.*
import kotlinx.android.synthetic.main.dialog_insert_comment.view.*
import kotlinx.android.synthetic.main.fragment_single_post.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator3
import org.w3c.dom.Comment
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.log


class SinglePostFragment : Fragment(), PostClickHandler {
    private lateinit var binding:FragmentSinglePostBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList:ArrayList<String>
    private lateinit var adapter: ImageAdapterHomePage
    private lateinit var circleIndicator: CircleIndicator3
    private lateinit var opisTekstView: TextView
    private lateinit var kratakOpisView: TextView
    private var currentSort=0
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
    private lateinit var nizKomentara: List<singleComment>
    private lateinit var comment:String
    private val args: SinglePostFragmentArgs by navArgs()
    private lateinit var likeDugme:ImageButton
    private lateinit var vremeTextView:TextView

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
        likeDugme=binding.root.findViewById(binding.likeButton.id)
        vremeTextView=binding.root.findViewById(binding.vremeTextView.id)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        circleIndicator=view.findViewById<CircleIndicator3>(R.id.circleIndikator)
        imageList= ArrayList()

        viewModel.GetPostWithIdResponse.observe(viewLifecycleOwner){
            when(it){
                is BaseResponse.Loading->{
                }
                is BaseResponse.Success->{
                    var array=it.data?.photos
                    if(array==null)
                        return@observe

                    imageList.clear()
                    imageList=array as ArrayList<String>
                    initImageCarousel(0)
                    setupTransformer()
                    opisTekstView.text=it.data?.description
                    kratakOpisView.text=it.data?.shortDescription
                    if(it.data?.likedByMe == true)
                        likeDugme.setImageResource(R.drawable.puno_srce)
                    else{
                        likeDugme.setImageResource(R.drawable.prazno_srce)
                    }

                    var output="ERROR"
                    val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        var curentDate: LocalDateTime = LocalDateTime.now()
                        val postDate = LocalDateTime.parse(it.data?.dateTime)
                        var pom= Duration.between(postDate,curentDate).toDays()
                        Log.d("SES",pom.toString())
                        when {

                            pom >=27->{
                                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                val formatter = SimpleDateFormat("dd.MM.yyyy")
                                output = formatter.format(parser.parse(it.data?.dateTime))
                            }
                            pom in 2..26 -> {
                                output=pom.toString()+" days ago"
                            }
                            pom.toInt() ==1->{
                                output=pom.toString()+" day ago"
                            }
                            pom < 1-> {
                                var sati= Duration.between(postDate,curentDate).toHours()
                                when{
                                    sati.toInt()==0->{
                                        output="Just now"
                                    }
                                    sati.toInt()==1->{
                                        output=sati.toString()+" hour ago"
                                    }
                                    else->{
                                        output=sati.toString()+" hours ago"
                                    }
                                }
                            }

                            else -> {
                                output="ERROR"
                            }
                        }



                    } else {
                        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
                        output = formatter.format(parser.parse(it.data?.dateTime))
                    }
                    vremeTextView.text=output
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
                is BaseResponse.Error->{
                }
            }
        }


//        val myimage = (ResourcesCompat.getDrawable(this.resources, R.drawable.addimagevector, null) as VectorDrawable).toBitmap()
//        imageList.add(myimage)




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
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.GetCommentsResponseHandler.collectLatest{
                if(it is BaseResponse.Error){
                    val id = UpravljanjeResursima.getResourceString(it.poruka.toString(),requireContext())
                    Toast.makeText(requireContext(), id, Toast.LENGTH_SHORT).show()
                }
                if(it is BaseResponse.Success){
                    nizKomentara= it.data!!
                    showComments(nizKomentara)
                }
            }
        }



        viewModel.getPostWithID(args.idPosta)


        binding.InsertCommentButton.setOnClickListener{
            addComment()
        }
        viewModel.getCommentsById(args.idPosta)


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
        adapter= ImageAdapterHomePage(imageList,viewPager2,this)
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
            comment=dialog.findViewById<EditText>(R.id.CommentTextBox).text.toString()
            viewModel.PostComment(commentDTS(0,args.idPosta,comment))
            dialog.dismiss()
        }

        dialog.show()

    }
    private fun showComments(nizKomentara: List<singleComment>){
        layoutManager = LinearLayoutManager(requireContext())
        recycler_view_comments.layoutManager = layoutManager
        recyclerAdapter = RecyclerAdapter(nizKomentara)
        recycler_view_comments.adapter = recyclerAdapter
    }

    override fun clickedPostItem(post: singlePost) {
        TODO("Not yet implemented")
    }

    override fun likePost(like: likeDTS) {
        TODO("Not yet implemented")
    }

    override fun dislikePost(like: likeDTS) {
        TODO("Not yet implemented")
    }

    override fun getPicture(imageView: ImageView, slika: String) {
        Glide.with(this)
            .load("http://10.0.2.2:5140/Storage/$slika")
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.image_holder)
            .into(imageView)
    }

}