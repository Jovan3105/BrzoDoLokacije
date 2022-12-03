package imi.projekat.hotspot.UI.Profile

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import imi.projekat.hotspot.ModeliZaZahteve.FollowingUserAdapter
import imi.projekat.hotspot.R
import imi.projekat.hotspot.UI.HomePage.PostClickHandler
import kotlinx.android.synthetic.main.list_item_following.view.*

class AdapterFollowingProfiles(private val clickHandler: FollowersImages
                               ,private var users:ArrayList<FollowingUserAdapter>,
                               private val listener:OnItemClickListener
                               ):RecyclerView.Adapter<AdapterFollowingProfiles.ViewHolder>()

{

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView),
            OnClickListener
    {
       val username:TextView=itemView.findViewById(R.id.username)
        val photoview:ImageView=itemView.profilePic
        val dugme:Button=itemView.unfollowButton
        init {

            dugme.setOnClickListener{
                listener.onItemClickFollow(this.layoutPosition)
            }
            username.setOnClickListener{
                listener.onItemClickProfile(this.layoutPosition)
            }
            photoview.setOnClickListener {
                listener.onItemClickProfile(this.layoutPosition)
            }
        }

        override fun onClick(v: View?) {
//            val position:Int=adapterPosition
//            listener.onItemClick(position)
            when (itemView.id) {
                itemView.unfollowButton.id -> listener.onItemClickFollow(this.layoutPosition)
                itemView.profilePic.id -> listener.onItemClickProfile(this.layoutPosition)
                itemView.username.id -> listener.onItemClickProfile(this.layoutPosition)
            }
        }


    }

    interface OnItemClickListener{
        fun onItemClickFollow(position: Int)
        fun onItemClickProfile(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.cardviewforfollowing,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.username.text=users[position].username
       holder.dugme.text=users[position].buttonName
        if(!users[position].photo.isNullOrEmpty())
        {
            Log.d("slikaFOllowera",users[position].photo)
            clickHandler.getPicture(holder.photoview,users[position].photo)
        }

    }

    override fun getItemCount(): Int {
        return users.size
    }


}