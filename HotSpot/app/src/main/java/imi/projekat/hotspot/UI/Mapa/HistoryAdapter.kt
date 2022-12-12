package imi.projekat.hotspot.UI.Mapa

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import imi.projekat.hotspot.ModeliZaZahteve.history
import imi.projekat.hotspot.ModeliZaZahteve.singlePost
import imi.projekat.hotspot.R
import kotlinx.android.synthetic.main.history_item.view.*
import kotlinx.android.synthetic.main.list_item_following.view.*

class HistoryAdapter(private var searched_items:ArrayList<history>,
                      private val listener:OnItemClickListener) :RecyclerView.Adapter<HistoryAdapter.ViewHolder>()
{
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener
    {
        val searchedLocations: TextView =itemView.findViewById(R.id.searched)
        val pictureClok:ImageView=itemView.findViewById(R.id.clockImage)
        val xarrayimg:ImageView=itemView.findViewById(R.id.deleteHistoryItem)
        val ImageAndText: ConstraintLayout =itemView.findViewById(R.id.mojLayout)
        init {

            searchedLocations.setOnClickListener{
                listener.onItemClickForSearch(this.layoutPosition)
            }

            pictureClok.setOnClickListener {
                listener.onItemClickForSearch(this.layoutPosition)
            }

            xarrayimg.setOnClickListener {
                listener.onItemClickForDelete(this.layoutPosition)
            }


        }

        override fun onClick(v: View?) {
            when (itemView.id) {
                itemView.searched.id -> listener.onItemClickForSearch(this.layoutPosition)
                itemView.clockImage.id -> listener.onItemClickForSearch(this.layoutPosition)
                itemView.deleteHistoryItem.id -> listener.onItemClickForDelete(this.layoutPosition)
            }
        }


    }

    interface OnItemClickListener{
        fun onItemClickForSearch(position: Int)
        fun onItemClickForDelete(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=
            LayoutInflater.from(parent.context).inflate(R.layout.history_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.searchedLocations.text=searched_items[position].location


    }

    override fun getItemCount(): Int {
        return searched_items.size
    }

    fun update(modelList:ArrayList<history>){
        searched_items = modelList
        notifyDataSetChanged()
    }
}