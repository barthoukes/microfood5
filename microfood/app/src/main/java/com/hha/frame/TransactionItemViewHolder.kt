package com.hha.frame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import tech.hha.microfood.R

class TransactionItemViewHolder(transactionItemView: View) : RecyclerView.ViewHolder(transactionItemView) {
    val transactionItemButton: Button = itemView.findViewById(R.id.transactionItemButton) // Assuming you have this ID in your item layout

    companion object {
        fun create(parent: ViewGroup): TransactionItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adaptor_transaction_item, parent, false) // Create item_menu_page.xml layout
            return TransactionItemViewHolder(view)
        }
    }
}