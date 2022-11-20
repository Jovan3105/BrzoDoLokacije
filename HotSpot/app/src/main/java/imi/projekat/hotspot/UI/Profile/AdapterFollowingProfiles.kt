package imi.projekat.hotspot.UI.Profile

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import imi.projekat.hotspot.ModeliZaZahteve.FollowingUserAdapter
import imi.projekat.hotspot.R

class AdapterFollowingProfiles(private var users:ArrayList<FollowingUserAdapter>):RecyclerView.Adapter<AdapterFollowingProfiles.ViewHolder>()
{
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
       val username:TextView=itemView.findViewById(R.id.username)
        val photoview:ImageView=itemView.findViewById(R.id.profilePic)
        val dugme:Button=itemView.findViewById(R.id.unfollowButton)


    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.cardviewforfollowing,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.username.text=users[position].username
        holder.photoview.setImageBitmap(users[position].photo)
//        holder.dugme.setOnClickListener{
//
//        }OPCIONO
    }

    override fun getItemCount(): Int {
        return users.size
    }


}