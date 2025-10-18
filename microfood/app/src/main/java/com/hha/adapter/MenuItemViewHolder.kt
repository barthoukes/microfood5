package com.hha.adapter

import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CMenuItem
import tech.hha.microfood.databinding.AdapterMenuItemBinding

class MenuItemViewHolder(
   private val binding: AdapterMenuItemBinding,
   private val onItemSelected: (CMenuItem) -> Unit,
   private val isChinese: Boolean
) : RecyclerView.ViewHolder(binding.root) {

   fun bind(item: CMenuItem?)
   {
      if (item != null)
      {
         bindMenuItem(item)
      } else
      {
         bindEmptyState()
      }
   }

   private fun bindMenuItem(item: CMenuItem)
   {
      with(binding) {
         itemName.setTextColor(item.colourText)
         itemName.text = if (isChinese) item.chineseName else item.localName

         setupBackground(item.colourBack, item.colourBack2)
         setupClickListeners(item)

         menuItemButton.visibility = View.VISIBLE
         menuItemButton.isSelected = false
      }
   }

   private fun bindEmptyState()
   {
      with(binding) {
         setupBackground(0xFFcccccc.toInt(), 0xFF999999.toInt())
         itemName.text = ""
         menuItemButton.visibility = View.VISIBLE
      }
   }

   private fun setupBackground(color1: Int, color2: Int)
   {
      val gradientDrawable = GradientDrawable(
         GradientDrawable.Orientation.BOTTOM_TOP,
         intArrayOf(color1, color2)
      )
      binding.itemBackground.background = gradientDrawable
   }

   private fun setupClickListeners(item: CMenuItem)
   {
      val clickListener = { _ : View ->
         Log.d("CLICK_TEST", "Item touched ${item.localName}")
         onItemSelected(item)
      }

      binding.itemBackground.setOnClickListener(clickListener)
      binding.itemName.setOnClickListener(clickListener)
   }
}