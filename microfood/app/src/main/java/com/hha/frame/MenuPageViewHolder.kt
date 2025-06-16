package com.hha.frame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import tech.hha.microfood.R

class MenuPageViewHolder(menuPageView: View) : RecyclerView.ViewHolder(menuPageView) {
    val menuPageButton: Button = itemView.findViewById(R.id.menuPageButton) // Assuming you have this ID in your item layout

    companion object {
        fun create(parent: ViewGroup): MenuPageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adaptor_menu_page, parent, false) // Create item_menu_page.xml layout
            return MenuPageViewHolder(view)
        }
    }
}