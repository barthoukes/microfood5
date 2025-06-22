package com.hha.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CMenuItem
import com.hha.framework.CMenuItems
import tech.hha.microfood.databinding.AdapterMenuItemBinding

class MenuItemsAdapter(
    private val menuItems: CMenuItems,
    private val onItemSelected: (CMenuItem) -> Unit
) : RecyclerView.Adapter<MenuItemsAdapter.MenuItemViewHolder>() {

    inner class MenuItemViewHolder(val binding: AdapterMenuItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val binding = AdapterMenuItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val item = menuItems.findItemByPosition(position)

        if (item == null) {
            holder.binding.itemName.text = ""
        } else {
            holder.binding.itemName.text = item.localName
        }

        // Update selected state
        holder.binding.menuItemButton.visibility = View.VISIBLE
        holder.binding.menuItemButton.isSelected = false // item.isSelected
        holder.binding.root.setOnClickListener {
            if (item!=null) {
                onItemSelected(item)
            }
        }
    }

    override fun getItemCount() = menuItems.getItemCount()
}
