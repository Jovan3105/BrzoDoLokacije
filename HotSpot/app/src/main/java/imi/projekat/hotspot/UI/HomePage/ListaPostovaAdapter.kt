package imi.projekat.hotspot.UI.HomePage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import imi.projekat.hotspot.ModeliZaZahteve.likeDTS
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.R
import imi.projekat.hotspot.UI.HomePage.SinglePost.ImageAdapterHomePage
import imi.projekat.hotspot.databinding.PostZaRecyclerViewBinding
import kotlinx.android.synthetic.main.post_za_recycler_view.view.*
import me.relex.circleindicator.CircleIndicator3
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.abs

class ListaPostovaAdapter(
    private var ListaPostova:List<singlePost>,
    private val clickHandler: PostClickHandler
):RecyclerView.Adapter<ListaPostovaAdapter.ListaPostovaViewHolder>() {
    inner class ListaPostovaViewHolder(val binding:PostZaRecyclerViewBinding):RecyclerView.ViewHolder(binding.root),
    View.OnClickListener{
        val SlikaKorisnika:ImageView=itemView.findViewById(R.id.SlikaKorisnika)
        val ImeKorisnika:TextView=itemView.findViewById(R.id.ImeKorisnika)
        val circleIndicator3:CircleIndicator3=itemView.findViewById(R.id.circleIndikator)
        val viewPager:ViewPager2=itemView.findViewById(binding.viewPager2.id)
        val VremeView:TextView=itemView.findViewById(binding.VremeView.id)
        val BrojLajkovaView:TextView=itemView.findViewById(binding.BrojLajkovaView.id)
        val KratakOpis:TextView=itemView.findViewById(binding.KratakOpis.id)
        val likeDugme: ImageButton=itemView.findViewById(binding.likeButton.id)
        val VlasnikPostaLayout:LinearLayout=itemView.findViewById(binding.VlasnikPostaLayout.id)
        var brojLajkova=0
        val lajkDugme:ImageButton=itemView.findViewById(binding.likeButton.id)
        init{
            binding.root.setOnClickListener(this)

            binding.VlasnikPostaLayout.setOnClickListener{
                clickHandler.clickOnUser(ListaPostova[bindingAdapterPosition].ownerID)
            }


        }
        override fun onClick(v: View?) {
            val trenutniPost=ListaPostova[bindingAdapterPosition]
            clickHandler.clickedPostItem(trenutniPost)
        }

        public fun initImageCarousel(listaSlika: ArrayList<String>){
            var handler= Handler(Looper.myLooper()!!)
            var adapter= ImageAdapterHomePage(listaSlika,viewPager,clickHandler)
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

        fun setupTransformer(){
            val transformer = CompositePageTransformer()
            transformer.addTransformer(MarginPageTransformer(40))
            transformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleX = 0.85f + r * 0.14f
                page.setOnClickListener{
                    this.onClick(it)
                }
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
            clickHandler.getPicture(holder.SlikaKorisnika,"ProfileImages/"+post.profilephoto)
        }
        holder.ImeKorisnika.text=post.username
        var output: String=""
        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var curentDate: LocalDateTime =LocalDateTime.now()
            val postDate = LocalDateTime.parse(post.dateTime)
            var pom=Duration.between(postDate,curentDate).toDays()
            when {

                pom >=27->{
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    val formatter = SimpleDateFormat("dd.MM.yyyy")
                    output = formatter.format(parser.parse(post.dateTime))
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
            output = formatter.format(parser.parse(post.dateTime))
        }


        holder.KratakOpis.text=post.shortDescription
        holder.VremeView.text=output
        holder.BrojLajkovaView.text=post.brojlajkova.toString()+" likes"
        if(post.likedByMe)
            holder.likeDugme.setImageResource(R.drawable.puno_srce)
        else{
            holder.likeDugme.setImageResource(R.drawable.prazno_srce)
        }

        holder.lajkDugme.setOnClickListener{
            if(post.likedByMe==false){
                holder.likeDugme.setImageResource(R.drawable.puno_srce)
                var brojLajkova=post.brojlajkova+1
                clickHandler.likePost(likeDTS(post.postID))
                return@setOnClickListener
            }
            holder.likeDugme.setImageResource(R.drawable.prazno_srce)
            clickHandler.dislikePost(likeDTS(post.postID))
            var brojLajkova=post.brojlajkova-1
        }

        holder.initImageCarousel(post.photos as ArrayList<String>)
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
