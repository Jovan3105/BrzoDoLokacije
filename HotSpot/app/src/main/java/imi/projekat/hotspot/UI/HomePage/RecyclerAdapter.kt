package imi.projekat.hotspot.UI.HomePage

import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import imi.projekat.hotspot.ModeliZaZahteve.FollowingUserAdapter
import imi.projekat.hotspot.ModeliZaZahteve.singleComment
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime

class RecyclerAdapter(
    private var nizKomentara: List<singleComment>,
    private val clickHandler: PostClickHandler
):RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val v  = LayoutInflater.from(parent.context).inflate(R.layout.fragment_single_comment,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        holder.commentUsername.text = nizKomentara[position].username

        holder.commentText.text = nizKomentara[position].text.trim()



        if(!nizKomentara[position].userPhoto.isNullOrEmpty()){
            var pom2=nizKomentara[position].userPhoto.split("\\")
            if(pom2.size==1)
                pom2=nizKomentara[position].userPhoto.split("/")
            clickHandler.getPicture(holder.commentPhoto,"ProfileImages/"+pom2[2])
        }



        var output: String=""
        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            var curentDate: LocalDateTime = LocalDateTime.now()
            var postDate=curentDate
            if(!nizKomentara[position].time.equals("-1"))
            {
                postDate = LocalDateTime.parse(nizKomentara[position].time)
            }


            var pom= Duration.between(postDate,curentDate).toDays()
            when {

                pom >=27->{
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    val formatter = SimpleDateFormat("dd.MM.yyyy")
                    output = formatter.format(parser.parse(nizKomentara[position].time))
                }
                pom in 2..26 -> {
                    output=pom.toString()+" days ago"
                }
                pom in 1 until 2->{
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
            output = formatter.format(parser.parse(nizKomentara[position].time))
        }
        holder.commentTime.text = output
    }

    override fun getItemCount(): Int {
        return nizKomentara.size
    }
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var commentPhoto: ImageView = itemView.findViewById(R.id.item_comment_photo)
        var commentUsername: TextView
        var commentText: TextView
        var commentTime: TextView

        init{
            commentUsername = itemView.findViewById(R.id.item_comment)
            commentText = itemView.findViewById(R.id.item_username_comment)
            commentPhoto = itemView.findViewById(R.id.item_comment_photo)
            commentTime = itemView.findViewById(R.id.item_comment_time)
        }
    }

    fun update(nizKomentara: List<singleComment>){
        this.nizKomentara=nizKomentara
        notifyDataSetChanged()
    }

}