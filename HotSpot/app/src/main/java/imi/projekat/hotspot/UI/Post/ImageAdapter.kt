package imi.projekat.hotspot.UI.Post

import android.graphics.Bitmap
import android.graphics.drawable.VectorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import imi.projekat.hotspot.R
import kotlinx.android.synthetic.main.image_container_post_create.view.*

class ImageAdapter(private val imageList:ArrayList<Bitmap>, private val viewPager2: ViewPager2,private var praznaLista:Boolean,private val addImageInterface: addImageInterface)
    :RecyclerView.Adapter<ImageAdapter.ImageViewHolder>()
{
    class ImageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val imageView:ImageView=itemView.findViewById(R.id.imageView)
        val dugme:ImageButton=itemView.findViewById(R.id.button5)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.image_container_post_create,parent,false)


        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.dugme.setOnClickListener{
            addImageInterface.removeImage(position)
        }
        if(praznaLista) {
            holder.imageView.setOnClickListener {

                addImageInterface.addImage()

            }
        }

        holder.imageView.setImageBitmap(imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }



}