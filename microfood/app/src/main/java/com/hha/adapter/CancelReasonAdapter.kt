package com.hha.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hha.dialog.Translation

class CancelReasonAdapter(
   private val reasons: List<String>, // Assuming reasons are simple strings for now
   private val onReasonClicked: (String) -> Unit
) : RecyclerView.Adapter<CancelReasonAdapter.ReasonViewHolder>()
{

   class ReasonViewHolder(view: View) : RecyclerView.ViewHolder(view)
   {
      val reasonText: TextView = view.findViewById(android.R.id.text1)
   }

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonViewHolder
   {
      // Using a simple built-in Android layout for the list item
      val view = LayoutInflater.from(parent.context)
         .inflate(android.R.layout.simple_list_item_1, parent, false)
      return ReasonViewHolder(view)
   }

   override fun onBindViewHolder(holder: ReasonViewHolder, position: Int)
   {
      val reason = reasons[position]
      holder.reasonText.text = reason
      holder.itemView.setOnClickListener {
         onReasonClicked(reason)
      }
   }

   override fun getItemCount() = reasons.size
}
