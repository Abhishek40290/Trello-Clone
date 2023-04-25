package com.example.trelloclone.activities.activities

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R

class LabelColorListItemsAdapter(private val context: Context,
                         private val list: ArrayList<String>,
                        private val mSelectedColor: String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var onItemClickListener : OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_label_color, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        if (holder is MyViewHolder){
            holder.itemView.findViewById<View>(R.id.view_main).setBackgroundColor(Color.parseColor(item))
            if (item==mSelectedColor){
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_color).visibility = View.VISIBLE
            }else{
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_color).visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onItemClickListener!= null){
                    onItemClickListener!!.onClick(position, item)
                }
            }
        }
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnItemClickListener{
        fun onClick(position:Int, color:String)
    }
}