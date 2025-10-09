package com.hha.adapter

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CMenuItem
import com.hha.framework.CMenuItems
import com.hha.resources.Global
import tech.hha.microfood.databinding.AdapterMenuItemBinding

class MenuItemsAdapter(
    private var menuItems: CMenuItems,
    private var itemWidth: Int,
    private val onItemSelected: (CMenuItem) -> Unit
) : RecyclerView.Adapter<MenuItemsAdapter.MenuItemViewHolder>() {
    val global = Global.getInstance()

    inner class MenuItemViewHolder(val binding: AdapterMenuItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder
    {
        val binding = AdapterMenuItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuItemViewHolder(binding)
    }

    fun setNewItems(newItems: CMenuItems) {
        menuItems = newItems
        //if (newItems.verticalSize !=

        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        // Decide the width of an item for a page.
        var value = holder.itemView.context.resources.displayMetrics.widthPixels
        value = value * itemWidth /120
        holder.itemView.layoutParams.width = value

        val item = menuItems.findItemByPosition(position)

        // Update selected state
        holder.binding.menuItemButton.visibility = View.VISIBLE
        holder.binding.menuItemButton.isSelected = false // item.isSelected
        if (item != null) {
            holder.binding.itemName.setTextColor(item.colourText)

            if (global.isChinese()) {
                holder.binding.itemName.text = item.chineseName
            }
            else {
                holder.binding.itemName.text = item.localName
            }
            // Gradient for item buttons.
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(item.colourBack, item.colourBack2)
            )
            holder.binding.itemBackground.background = gradientDrawable

            holder.binding.itemBackground.setOnClickListener {
                Log.d("CLICK_TEST", "Item touched ${item.localName}")
                onItemSelected(item)
            }
            holder.binding.itemName.setOnClickListener {
                Log.d("CLICK_TEST", "Item touched ${item.localName}")
                onItemSelected(item)
            }
        }
        else
        {
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(0xFFcccccc.toInt(), 0xFF999999.toInt()))
            holder.binding.itemBackground.background = gradientDrawable
            holder.binding.itemName.text = ""
       }
        holder.binding.root.setOnClickListener {
        }
    }
    override fun getItemCount() = menuItems.getItemCount()
}
