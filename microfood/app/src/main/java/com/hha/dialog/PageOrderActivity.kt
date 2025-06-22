package com.hha.dialog

import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.hha.microfood.databinding.PageOrderActivityBinding
import androidx.recyclerview.widget.RecyclerView
import com.hha.adapter.MenuItemsAdapter

import com.hha.adapter.MenuPagesAdapter
import com.hha.common.SkipInvisible.SKIP_INVISIBLE_TRUE
import com.hha.framework.CMenuCards
import com.hha.framework.CMenuItem
import com.hha.framework.CMenuItems
import com.hha.framework.CMenuPage
import com.hha.resources.Global


class PageOrderActivity : AppCompatActivity() {
    private lateinit var binding: PageOrderActivityBinding
    val global = Global.getInstance()
    val menuCardId = global.menuCardId
    val menuCard = CMenuCards.getInstance().getMenuCard(menuCardId)
    val menuPages = menuCard.getOrderedPages()
    val menuPage: CMenuPage = menuCard.getMenuPage(global.menuPageId)
    val menuItems : CMenuItems =
        menuPage.loadItems(SKIP_INVISIBLE_TRUE)
    val totalColumns = (menuPages.size / 3) // 24 items / 3 rows = 8 columns
    private lateinit var menuPagesAdapter: MenuPagesAdapter
    private lateinit var menuItemsAdapter: MenuItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PageOrderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        val gridLayoutManagerMenuPages = GridLayoutManager(
            this@PageOrderActivity,
            3, // Span count = number of rows (vertical)
            GridLayoutManager.HORIZONTAL, // Horizontal scrolling
            false
        )
        binding.layoutMenuPages.layoutManager = gridLayoutManagerMenuPages

        // 2. Initialize adapter
        menuPagesAdapter = MenuPagesAdapter(menuPages) { selectedPage ->
            handlePageSelection(selectedPage)
        }.apply {
            // Set dynamic height based on screen size
            binding.layoutMenuPages.setItemViewCacheSize(18)
        }

        // 1b. GridLayoutManager for menu items with 8 rows (vertical span) and horizontal scrolling
        val gridLayoutManagerMenuItems = GridLayoutManager(
            this@PageOrderActivity,
            8, // Span count = number of rows (vertical)
            GridLayoutManager.HORIZONTAL, // Horizontal scrolling
            false
        )
        binding.layoutMenuItems.layoutManager = gridLayoutManagerMenuItems

        // 2. Initialize adapter
        menuItemsAdapter = MenuItemsAdapter(menuItems) { selectedMenuItem ->
            handleMenuItemSelection(selectedMenuItem)
        }.apply {
            // Set dynamic height based on screen size
            binding.layoutMenuItems.setItemViewCacheSize(28)
        }
        binding.layoutMenuPages.adapter = menuPagesAdapter
        binding.layoutMenuItems.adapter = menuItemsAdapter




        // 3. Add spacing between items
        binding.layoutMenuPages.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View,
                                            parent: RecyclerView, state: RecyclerView.State) {
                    outRect.set(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx()) // 8dp spacing
                }
                }
        )
    }

    // DP-to-pixel conversion extension
    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun handlePageSelection(selectedPage: CMenuPage) {
        // Update selection state
        menuPages.forEach { page ->
            page.isSelected = (page.menuPageId == selectedPage.menuPageId)
        }
        menuPagesAdapter.notifyDataSetChanged()

        // Load items for the selected page
        loadPageItems(selectedPage.menuPageId)
    }

    private fun handleMenuItemSelection(selectedMenuItem: CMenuItem) {
        // Update selection state
        menuItems.menuItems.values.forEach { item ->
            item.isSelected = (item.menuItemId == selectedMenuItem.menuItemId)
        }
        menuItemsAdapter.notifyDataSetChanged()

        // Load items for the selected page
        loadPageItems(selectedMenuItem.menuItemId)
    }

    private fun loadPageItems(pageId: Int) {
        // Implement your logic to load items for the selected page
        // This might involve another RecyclerView for the items in layout_items
    }

    private fun navigateBack() {
        // Intent to start MainActivity which will host your MainMenuDialog
        //val mainIntent = Intent(this@AboutActivity, MainMenuActivity::class.java)
        // Optional: Pass any fetched data to MainActivity
        // mainIntent.putExtra("MENU_DATA", "your_fetched_menu_data_string_or_parcelable")
        //startActivity(mainIntent)
        finish() // Close the SplashActivity so it's not in the back stack
    }
}
