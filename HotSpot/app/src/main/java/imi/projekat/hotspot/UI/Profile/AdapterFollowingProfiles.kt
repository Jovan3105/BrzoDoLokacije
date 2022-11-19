package imi.projekat.hotspot.UI.Profile

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import imi.projekat.hotspot.ModeliZaZahteve.FollowingUserAdapter
import imi.projekat.hotspot.R

class AdapterFollowingProfiles(private val context:Activity,private val korisnici:ArrayList<FollowingUserAdapter>):ArrayAdapter<FollowingUserAdapter>(
    context, R.layout.list_item_following,korisnici
){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater:LayoutInflater=LayoutInflater.from(context)
        val view:View=inflater.inflate(R.layout.list_item_following,null)
        val imageView:ImageView=view.findViewById(R.id.profilePic)
        val userName:TextView=view.findViewById(R.id.username)
        val button:Button=view.findViewById(R.id.unfollowButton)
        imageView.setImageBitmap(korisnici[position].photo)
        userName.text=korisnici[position].username
        button.setText("Unfollow")
        return view
    }
}