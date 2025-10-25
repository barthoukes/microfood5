package com.hha.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
) : RecyclerView.Adapter<MenuPagesAdapter.MenuPageViewHolder>()
{

    private val m_global = Global.getInstance()
    private var m_widthPages = 1000
    private var m_page = 0

    // Cache for page lookup to avoid recalculating
    private val pageCache = mutableMapOf<Int, CMenuPage?>()

    // Cache for page indices
    private val pageIndexCache = mutableMapOf<Int, Int>()

    inner class MenuPageViewHolder(val binding: AdapterMenuPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuPageViewHolder
    {
        val binding = AdapterMenuPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        m_widthPages = parent.width

        val holder = MenuPageViewHolder(binding)

        // Set click listeners once
        binding.itemName.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION)
            {
                val page = getPage(position)
                page?.menuPageId?.let { onPageSelected(it) }
            }
        }

        binding.menuPageButton.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION)
            {
                val page = getPage(position)
                page?.menuPageId?.let { onPageSelected(it) }
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: MenuPageViewHolder, position: Int)
    {
        // Set click listeners once in onCreateViewHolder instead of every bind
        // Move this to onCreateViewHolder if the listeners don't change

        val page = getPage(position)
        val text = getPageText(page, position)
        val colour = getPageColour(page?.menuPageId ?: -1)

        // Calculate width only when needed
        val value = (m_widthPages - (columns + 1) * 2) / columns
        holder.itemView.layoutParams.width = value

        // Update views
        holder.binding.itemName.text = text
        holder.binding.menuPageButton.setBackgroundColor(colour)
        holder.binding.menuPageButton.visibility = View.VISIBLE
        holder.binding.menuPageButton.isSelected = false
    }

    override fun onViewRecycled(holder: MenuPageViewHolder)
    {
        // Clear any heavy resources if needed
        super.onViewRecycled(holder)
    }

    private fun getPageText(page: CMenuPage?, position: Int): String
    {
        if (page == null) return "-"

        return buildString {
            if (m_global.isChinese())
            {
                append(page.chineseName)
            } else
            {
                append(page.localName)
            }
            if (position == m_global.menuPageId)
            {
                append("**")
            }
        }
    }

    fun getPageColour(pageId: Int): Int
    {
        return if (pageId == m_page) colourSelectedPage else colourPage
    }

    fun selectPage(newPage: Int)
    {
        val previousPage = m_page
        m_page = newPage

        // Only update if the page actually changed
        if (previousPage != newPage)
        {
            val previousIndex = getPageIndex(previousPage)
            val newIndex = getPageIndex(newPage)

            if (previousIndex != -1) notifyItemChanged(previousIndex)
            if (newIndex != -1) notifyItemChanged(newIndex)
        }
    }

    fun getPage(position: Int): CMenuPage?
    {
        // Use cache to avoid recalculating
        return pageCache.getOrPut(position) {
            val col = position / rows
            val row = position % rows
            val page = 1 + row * columns + col
            pages[page]
        }
    }

    fun getPageIndex(pageId: Int): Int
    {
        // Use cache to avoid recalculating
        return pageIndexCache.getOrPut(pageId) {
            val row = (pageId - 1) / columns
            val col = (pageId - 1) % columns
            row + col * rows
        }
    }

    override fun getItemCount() = rows * columns

    // Call this when the data changes significantly to clear caches
    fun clearCaches()
    {
        pageCache.clear()
        pageIndexCache.clear()
    }
}