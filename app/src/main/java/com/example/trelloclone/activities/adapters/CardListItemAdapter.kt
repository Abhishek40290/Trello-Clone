package com.example.trelloclone.activities.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import com.example.trelloclone.activities.activities.TaskListActivity
import com.example.trelloclone.activities.model.Card
import com.example.trelloclone.activities.model.SelcetedMembers

open class CardListItemAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            if(model.labelColor.isNotEmpty()){
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.VISIBLE
                holder.itemView.findViewById<View>(R.id.view_label_color).setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.GONE
            }

            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name

            if ((context as TaskListActivity)
                    .mAssignedMemberDetailList.size>0){
                val selectedMembersList: ArrayList<SelcetedMembers> = ArrayList()

                for (i in context.mAssignedMemberDetailList.indices){
                    for(j in model.assignedTo){
                        if(context.mAssignedMemberDetailList[i].id==j){
                            val selectedMembers = SelcetedMembers(
                                context.mAssignedMemberDetailList[i].id,
                                context.mAssignedMemberDetailList[i].image
                            )
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }

                if(selectedMembersList.size>0){
                    if(selectedMembersList.size ==1 && selectedMembersList[0].id == model.createdBy){
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.GONE
                    }else{
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.VISIBLE

                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).layoutManager =
                            GridLayoutManager(context, 4)
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)

                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).adapter = adapter
                        adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if(onClickListener!=null){
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                }else{
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                    onClickListener!!.onClick(position, )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    // A function for OnClickListener where the Interface is the expected parameter..

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }


     // An interface for onclick items.

    interface OnClickListener {
        fun onClick(position: Int)
    }


     // A ViewHolder describes an item view and metadata about its place within the RecyclerView.

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}