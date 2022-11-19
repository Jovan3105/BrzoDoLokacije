package imi.projekat.hotspot.UI.HomePage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.R
import imi.projekat.hotspot.UI.HomePage.SinglePost.ImageAdapterHomePage
import imi.projekat.hotspot.databinding.PostZaRecyclerViewBinding
import kotlinx.android.synthetic.main.post_za_recycler_view.view.*
import me.relex.circleindicator.CircleIndicator3
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import kotlin.math.abs

class ListaPostovaAdapter(private var ListaPostova:List<singlePost>)
    :RecyclerView.Adapter<ListaPostovaAdapter.ListaPostovaViewHolder>() {
    class ListaPostovaViewHolder(val binding:PostZaRecyclerViewBinding):RecyclerView.ViewHolder(binding.root){
        val SlikaKorisnika:ImageView=itemView.findViewById(R.id.SlikaKorisnika)
        val ImeKorisnika:TextView=itemView.findViewById(R.id.ImeKorisnika)
        val circleIndicator3:CircleIndicator3=itemView.findViewById(R.id.circleIndikator)
        val viewPager:ViewPager2=itemView.findViewById(binding.viewPager2.id)
        val VremeView:TextView=itemView.findViewById(binding.VremeView.id)

        public fun initImageCarousel(listaSlika:ArrayList<Bitmap>){

            var handler= Handler(Looper.myLooper()!!)
            var adapter= ImageAdapterHomePage(listaSlika,viewPager)
            viewPager.adapter = adapter
            viewPager.offscreenPageLimit = 3
            viewPager.clipToPadding = false
            viewPager.clipChildren = false
            viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

//            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
//                override fun onPageSelected(position: Int) {
//                    if (position == 0) {
//
//                    }
//                    else if (position == 1) {
//
//                    }
//                    else if (position == 2){
//
//                    }
//
//                    super.onPageSelected(position)
//                }
//            })

        }

        public fun setupTransformer(){
            val transformer = CompositePageTransformer()
            transformer.addTransformer(MarginPageTransformer(40))
            transformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.14f
            }
            viewPager.setPageTransformer(transformer)
            circleIndicator3.setViewPager(viewPager)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaPostovaViewHolder {
        return ListaPostovaViewHolder(PostZaRecyclerViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ListaPostovaViewHolder, position: Int) {
        val post=ListaPostova[position]

        if(!post.profilephoto.isNullOrEmpty()){
            var byte= Base64.decode(post.profilephoto, Base64.DEFAULT)
            var bitmapa= BitmapFactory.decodeByteArray(byte,0,byte.size)
            holder.SlikaKorisnika.setImageBitmap(bitmapa)
        }
        holder.ImeKorisnika.text=post.username
        var output: String=""
        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var curentDate: LocalDateTime =LocalDateTime.now()
            val postDate = LocalDateTime.parse(post.dateTime)
            var pom=Duration.between(postDate,curentDate).toDays()
            Log.d(pom.toString(),pom.toString())
            when {

                pom >=27->{
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    val formatter = SimpleDateFormat("dd.MM.yyyy")
                    output = formatter.format(parser.parse(post.dateTime))
                }
                pom > 1 -> {
                    output=pom.toString()+" days ago"
                }
                pom <= 1-> {
                    var sati=Duration.between(postDate,curentDate).toHours()
                    when{
                        sati.equals(0)->{
                            output="Just now"
                        }
                        sati.equals(1)->{
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
            output = formatter.format(parser.parse(post.dateTime))
        }



        holder.VremeView.text=output

        var imageList=ArrayList<Bitmap>()
        for (i in 0 until  post.photos.size){
            var byte=Base64.decode(post.photos.get(i),Base64.DEFAULT)
            var bitmapa=BitmapFactory.decodeByteArray(byte,0,byte.size)
            imageList.add(bitmapa)
        }

        holder.initImageCarousel(imageList)
        holder.setupTransformer()

    }

    override fun getItemCount(): Int {
        return ListaPostova.size
    }

    fun update(modelList:List<singlePost>){
        ListaPostova = modelList
        notifyDataSetChanged()
    }

}