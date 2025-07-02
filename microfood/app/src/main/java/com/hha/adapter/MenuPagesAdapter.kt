package com.hha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.common.textDump
import com.hha.framework.CMenuPage
import com.hha.resources.Global
import tech.hha.microfood.databinding.AdapterMenuPageBinding

class MenuPagesAdapter(
    private val pages: MutableMap<Int, CMenuPage>,
    private val columns: Int,
    private val rows: Int,
    private val onPageSelected: (Int) -> Unit
) : RecyclerView.Adapter<MenuPagesAdapter.MenuPageViewHolder>() {
    val global = Global.getInstance()

    inner class MenuPageViewHolder(val binding: AdapterMenuPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuPageViewHolder {
        val binding = AdapterMenuPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuPageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuPageViewHolder, position: Int) {

        // Set click listener on the BUTTON (not just root view)
//        holder.binding.menuPageButton.setOnClickListener {
//            onPageSelected(position)
//        }
        holder.binding.itemName.setOnClickListener {
            Log.d("CLICK_TEST", "Name clicked at position $position") // Add this for testing
            val page = getPage(position)
            if (page != null) {
                onPageSelected(page.menuPageId)
            }
        }
        holder.binding.menuPageButton.setOnClickListener {
            Log.d("CLICK_TEST", "Button clicked at position $position") // Add this for testing
            val page = getPage(position)
            if (page !=null) {
                onPageSelected(page.menuPageId)
            }
        }

        var value = holder.itemView.context.resources.displayMetrics.widthPixels/7
        holder.itemView.layoutParams.width = value
        val page = getPage(position)
        var text = "-"
        if (page != null) {
            if (global.isChinese()) {
                text = page.chineseName
            }
            else {
                text = page.localName
            }
            if (position == global.menuPageId)
            {
                text += "**"
            }
        }
        // Update selected state
        holder.binding.itemName.text = text
        holder.binding.menuPageButton.visibility = View.VISIBLE
        holder.binding.menuPageButton.isSelected = false // page.isSelected
    }

    fun getPage(position: Int): CMenuPage? {
        val col = position / rows
        val row = position % rows
        val page = 1 + row*columns + col
        return pages[page]
    }

    override fun getItemCount() = rows*columns
}
