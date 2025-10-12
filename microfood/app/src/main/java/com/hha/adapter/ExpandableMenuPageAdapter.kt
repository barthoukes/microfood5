package com.hha.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CMenuItem
import com.hha.framework.CMenuPage
import tech.hha.microfood.databinding.ItemExpandableMenuPageBinding
import com.hha.common.SkipInvisible

class ExpandableMenuPageAdapter(
   private val pages: List<CMenuPage>,
   private val onMenuItemSelected: (CMenuItem) -> Unit
) : RecyclerView.Adapter<ExpandableMenuPageAdapter.ExpandablePageViewHolder>() {

   private val expandedStates = BooleanArray(pages.size) { false }
   private var currentlyExpandedPosition = -1
   private val maxOpenPages = 2 // Maximum number of pages that can be open simultaneously

   inner class ExpandablePageViewHolder(val binding: ItemExpandableMenuPageBinding) :
      RecyclerView.ViewHolder(binding.root)

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandablePageViewHolder {
      val binding = ItemExpandableMenuPageBinding.inflate(
         LayoutInflater.from(parent.context),
         parent,
         false
      )
      return ExpandablePageViewHolder(binding)
   }

   override fun onBindViewHolder(holder: ExpandablePageViewHolder, position: Int) {
      val page = pages[position]
      val isExpanded = expandedStates[position]

      // Set up page name button
      setupPageName(holder, page, position)

      // Set up menu items RecyclerView
      setupMenuItemsRecyclerView(holder, page, isExpanded)

      // Set initial visibility
      holder.binding.menuItemsRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
   }

   private fun setupPageName(holder: ExpandablePageViewHolder, page: CMenuPage, position: Int) {
      val global = com.hha.resources.Global.getInstance()
      val pageName = if (global.isChinese()) page.chineseName else page.localName

      holder.binding.pageNameButton.text = pageName

      holder.binding.pageNameButton.setOnClickListener {
         togglePageExpansion(position)
      }
   }

   private fun setupMenuItemsRecyclerView(
      holder: ExpandablePageViewHolder,
      page: CMenuPage,
      isExpanded: Boolean
   ) {
      // Calculate item width based on page settings
      val displayMetrics = holder.itemView.context.resources.displayMetrics
      val screenWidth = displayMetrics.widthPixels
      val itemWidth = screenWidth * page.pageWidth / 120

      // Set up horizontal GridLayoutManager with 8 rows
      val layoutManager = GridLayoutManager(holder.itemView.context, 8, GridLayoutManager.HORIZONTAL, false)
      holder.binding.menuItemsRecyclerView.layoutManager = layoutManager

      if (isExpanded) {
         // Load items only when expanded (lazy loading)
         val menuItems = page.loadItems(SkipInvisible.SKIP_INVISIBLE_TRUE)
         val adapter = MenuItemsAdapter(menuItems, page.pageWidth, onMenuItemSelected)
         holder.binding.menuItemsRecyclerView.adapter = adapter
      }
   }

   private fun togglePageExpansion(position: Int) {
      val wasExpanded = expandedStates[position]

      if (!wasExpanded) {
         // Close other pages if we have too many open
         manageOpenPages(position)
      }

      // Toggle current page
      expandedStates[position] = !wasExpanded
      notifyItemChanged(position)

      // Animate the expansion/collapse
      animatePageExpansion(position, !wasExpanded)
   }

   private fun manageOpenPages(newlyExpandedPosition: Int) {
      val openPages = expandedStates.indices.filter { expandedStates[it] && it != newlyExpandedPosition }

      if (openPages.size >= maxOpenPages) {
         // Close the oldest expanded page
         val pageToClose = openPages.first()
         expandedStates[pageToClose] = false
         notifyItemChanged(pageToClose)
         animatePageExpansion(pageToClose, false)
      }

      currentlyExpandedPosition = newlyExpandedPosition
   }

   private fun animatePageExpansion(position: Int, expand: Boolean) {
      // You can add smooth animation here
      // For now, we rely on notifyItemChanged which will trigger rebind
      // Consider using TransitionManager for smooth animations
   }

   override fun getItemCount() = pages.size
}