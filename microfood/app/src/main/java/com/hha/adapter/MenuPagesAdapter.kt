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
    private val colourPage: Int,
    private val colourSelectedPage: Int,
    private val onPageSelected: (Int) -> Unit
) : RecyclerView.Adapter<MenuPagesAdapter.MenuPageViewHolder>() {
    val m_global = Global.getInstance()
    var m_widthPages = 1000
    var m_page = 0

    inner class MenuPageViewHolder(val binding: AdapterMenuPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuPageViewHolder {
        val binding = AdapterMenuPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        m_widthPages = parent.width
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

        var value = m_widthPages/columns
        holder.itemView.layoutParams.width = value
        val page = getPage(position)
        var text = "-"
        var colour = colourPage
        if (page != null) {
            if (m_global.isChinese()) {
                text = page.chineseName
            }
            else {
                text = page.localName
            }
            if (position == m_global.menuPageId)
            {
                text += "**"
            }
            colour = getPageColour(page.menuPageId)
        }
        // Update selected state
        holder.binding.itemName.text = text
        holder.binding.menuPageButton.setBackgroundColor(colour)
        holder.binding.menuPageButton.visibility = View.VISIBLE
        holder.binding.menuPageButton.isSelected = false // page.isSelected
    }

    fun getPageColour(pageId: Int): Int
    {
        return when
        {
            pageId == m_page -> colourSelectedPage
            else -> colourPage
        }
    }

    fun selectPage(newPage: Int)
    {
        val previousPage = m_page
        m_page = newPage
        notifyItemChanged(getPageIndex(previousPage))
        notifyItemChanged(getPageIndex(newPage))
    }

    fun getPage(position: Int): CMenuPage? {
        val col = position / rows
        val row = position % rows
        val page = 1 + row*columns + col
        return pages[page]
    }

    fun getPageIndex(position: Int): Int {
        val row = (position-1) / columns
        val col = (position-1) % columns
        val page = row + col*rows
        return page
    }

    override fun getItemCount() = rows*columns
}
