package imi.projekat.hotspot.UI.HomePage.SinglePost

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import imi.projekat.hotspot.R
import imi.projekat.hotspot.UI.HomePage.PostClickHandler

class ImageAdapterHomePage(private val imageList:ArrayList<String>, private val viewPager2: ViewPager2,private val clickHandler: PostClickHandler)
    :RecyclerView.Adapter<ImageAdapterHomePage.ImageViewHolder>()
{
    class ImageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val imageView:ImageView=itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.image_container_single_post,parent,false)


        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        clickHandler.getPicture(holder.imageView,"PostsFolder/"+imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }



}