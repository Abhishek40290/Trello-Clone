package com.example.trelloclone.activities.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import com.example.trelloclone.activities.activities.LabelColorListItemsAdapter

abstract class LabelColorListDialog (
        context: Context,
        private var list: ArrayList<String>,
        private val title: String ="",
        private var mSelectedColor: String =""
        ): Dialog(context){
                private var adapter: LabelColorListItemsAdapter? = null

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val view = LayoutInflater.from(context).inflate(
                        R.layout.dialog_list, null)

                setContentView(view)
                setCanceledOnTouchOutside(true)
                setCancelable(true)
                setUpRecyclerView(view)
        }

        @SuppressLint("CutPasteId")
        private fun setUpRecyclerView(view: View){
                view.findViewById<TextView>(R.id.tvTitle).text = title
                view.findViewById<RecyclerView>(R.id.rvList).layoutManager = LinearLayoutManager(context)
                adapter = LabelColorListItemsAdapter(context, list, mSelectedColor)
                view.findViewById<RecyclerView>(R.id.rvList).adapter = adapter

                adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener{
                        override fun onClick(position: Int, color: String) {
                                dismiss()
                                onItemSelected(color)
                        }
                }
        }

        protected abstract fun onItemSelected(color: String)
 }