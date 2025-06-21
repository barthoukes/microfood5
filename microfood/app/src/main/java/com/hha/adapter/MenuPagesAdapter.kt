package com.hha.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CMenuPage
import tech.hha.microfood.databinding.AdapterMenuPageBinding

class MenuPagesAdapter(
    private val pages: List<CMenuPage>,
    private val onPageSelected: (CMenuPage) -> Unit
) : RecyclerView.Adapter<MenuPagesAdapter.PageViewHolder>() {

    inner class PageViewHolder(val binding: AdapterMenuPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = AdapterMenuPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pages[position]
        holder.binding.itemName.text = page.localName

        // Update selected state
        holder.binding.menuPageButton.visibility = View.VISIBLE
        holder.binding.menuPageButton.isSelected = false // page.isSelected
        holder.binding.root.setOnClickListener {
            onPageSelected(page)
        }
    }

    override fun getItemCount() = pages.size
}
