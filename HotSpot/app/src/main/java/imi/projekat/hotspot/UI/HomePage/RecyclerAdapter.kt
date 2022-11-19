package imi.projekat.hotspot.UI.HomePage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import imi.projekat.hotspot.ModeliZaZahteve.singleComment
import imi.projekat.hotspot.R
import imi.projekat.hotspot.ViewModeli.MainActivityViewModel

class RecyclerAdapter(private var nizKomentara: List<singleComment>):RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val v  = LayoutInflater.from(parent.context).inflate(R.layout.fragment_single_comment,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        holder.commentUsername.text = nizKomentara[position].username
        holder.commentText.text = nizKomentara[position].text
    }

    override fun getItemCount(): Int {
        return nizKomentara.size
    }
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        //var itemImage: ImageView
        var commentUsername: TextView
        var commentText: TextView

        init{
            commentUsername = itemView.findViewById(R.id.item_comment)
            commentText = itemView.findViewById(R.id.item_username_comment)
        }
    }

}