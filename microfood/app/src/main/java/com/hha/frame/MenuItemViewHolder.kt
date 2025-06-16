package com.hha.frame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import tech.hha.microfood.R

class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val menuItemButton: Button = itemView.findViewById(R.id.menuItemButton) // Assuming you have this ID in your item layout

    companion object {
        fun create(parent: ViewGroup): MenuPageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_menu_item, parent, false) // Create item_menu_item.xml layout
            return MenuPageViewHolder(view)
        }
    }
}