package imi.projekat.hotspot.UI.Post

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import imi.projekat.hotspot.R
import kotlinx.android.synthetic.main.image_container_post_create.view.*

class ImageAdapter(private val imageList:ArrayList<Bitmap>, private val viewPager2: ViewPager2)
    :RecyclerView.Adapter<ImageAdapter.ImageViewHolder>()
{
    class ImageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val imageView:ImageView=itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.image_container_post_create,parent,false)

        view.button5.setOnClickListener{
            Log.d("SES","SES")
        }

        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageBitmap(imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}