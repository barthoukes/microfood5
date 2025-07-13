package com.hha.dialog

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.content.res.Resources
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import tech.hha.microfood.databinding.PageOrderActivityBinding
import androidx.recyclerview.widget.RecyclerView
import com.hha.adapter.MenuItemsAdapter
import com.hha.adapter.MenuPagesAdapter
import com.hha.adapter.TransactionItemsAdapter
import com.hha.common.SkipInvisible.SKIP_INVISIBLE_TRUE
import com.hha.framework.CItem
import com.hha.framework.CMenuCards
import com.hha.framework.CMenuItem
import com.hha.framework.CMenuItems
import com.hha.framework.CMenuPage
import com.hha.framework.CTransaction
import com.hha.resources.Global
import com.hha.resources.Configuration

class PageOrderActivity : AppCompatActivity() {
    private lateinit var binding: PageOrderActivityBinding
    val global = Global.getInstance()
    val menuCardId = global.menuCardId
    val menuCard = CMenuCards.getInstance().getMenuCard(menuCardId)
    val menuPages = menuCard.getOrderedPages()
    var menuPage: CMenuPage = menuCard.getMenuPage(global.menuPageId)
    var menuItems : CMenuItems =
        menuPage.loadItems(SKIP_INVISIBLE_TRUE)
    val CFG : Configuration = global.CFG
    var itemWidth = 24
    val groups = CFG.getValue("display_groups")
    val columns = CFG.getValue("display_groups_horizontal")
    val rows = (groups + columns-1) / columns
    private lateinit var menuPagesAdapter: MenuPagesAdapter
    private lateinit var menuItemsAdapter: MenuItemsAdapter
    private lateinit var transactionItemsAdapter: TransactionItemsAdapter
    private lateinit var transaction: CTransaction
    val clusterId : Short = -1
    val cursor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PageOrderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when activity resumes
        if (global.transaction == null) {
            if (global.transactionId <1E6) assert(false)
            transaction = CTransaction(global.transactionId)
        }
        transaction = global.transaction!!
        transaction.startNextTimeFrame()
        //menuItemsAdapter.notifyDataSetChanged()
        //transactionItemsAdapter.notifyDataSetChanged()
    }

    // Add this new function to handle language changes
    fun on_button_language(view: View) {
        Translation.nextLanguage()
        refreshAllData()
    }

    // Add this new function to handle language changes
    fun on_button_enter(view: View) {
        startActivity(Intent(this, BillOrderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun refreshAllData() {
        menuPagesAdapter.notifyDataSetChanged()
        menuItemsAdapter.notifyDataSetChanged()
        transactionItemsAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        createGridLayoutMenuPages()
        createMenuPagesAdapter()
        createGridLayoutMenuItems()
        createMenuItemsAdapter()
        createGridLayoutTransactionItems()
        createTransactionItemsAdapter()

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

    private fun createMenuPagesAdapter() {
        // 2. Initialize adapter
        menuPagesAdapter = MenuPagesAdapter(menuPages, columns, rows) {
                selectedPage -> handlePageSelection(selectedPage)
        }.apply {
            // Set dynamic height based on screen size
            binding.layoutMenuPages.setItemViewCacheSize(18)
        }
        binding.layoutMenuPages.adapter = menuPagesAdapter
    }

    private fun createGridLayoutTransactionItems() {
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        val gridLayoutManagerTransactionItems = GridLayoutManager(
            this@PageOrderActivity,
            1, // Span count = number of columns (horizontal)
            GridLayoutManager.VERTICAL, // Vertical scrolling
            false
        )
        binding.layoutTransactionItems.layoutManager =
            gridLayoutManagerTransactionItems
    }

    private fun createGridLayoutMenuPages() {
        val gridLayoutManagerMenuPages = GridLayoutManager(
            this@PageOrderActivity,
            rows,
            GridLayoutManager.HORIZONTAL, // Horizontal scrolling
            false
        )
        binding.layoutMenuPages.layoutManager = gridLayoutManagerMenuPages
    }

    private fun createMenuItemsAdapter() {
        // 2. Initialize adapter
        menuItemsAdapter =
            MenuItemsAdapter(menuItems, itemWidth) {
                selectedMenuItem -> handleMenuItem(selectedMenuItem)
            }.apply {
                // Set dynamic height based on screen size
                binding.layoutMenuItems.setItemViewCacheSize(28)
            }
        binding.layoutMenuItems.adapter = menuItemsAdapter
    }

    private fun createGridLayoutMenuItems() {
        // 1b. GridLayoutManager for menu items with 8 rows (vertical span) and horizontal scrolling
        val gridLayoutManagerMenuItems = GridLayoutManager(
            this@PageOrderActivity,
            menuItems.verticalSize, // Span count = number of rows (vertical)
            GridLayoutManager.HORIZONTAL, // Horizontal scrolling
            false
        )
        binding.layoutMenuItems.layoutManager = gridLayoutManagerMenuItems
    }

    private fun createTransactionItemsAdapter() {
        // 2. Initialize adapter
        transactionItemsAdapter = TransactionItemsAdapter() {
            selectedTransactionItem -> handleTransactionItem(selectedTransactionItem)
        } .apply {
            // Set dynamic height based on screen size
            binding.layoutMenuPages.setItemViewCacheSize(18)
        }
        binding.layoutTransactionItems.adapter = transactionItemsAdapter
    }

    // DP-to-pixel conversion extension
    fun Int.dpToPx(): Int = (this * Resources.getSystem()
        .displayMetrics.density).toInt()

    private fun handlePageSelection(selectedPage: Int) {
        if (selectedPage != global.menuPageId)
        {
            loadPageItems(selectedPage)
        }
    }

    private fun handleTransactionItem(selectedTransactionItem: CItem) {
        // todo
    }

    private fun handleMenuItem(selectedMenuItem: CMenuItem) {
        Log.d("CLICK", "MenuItem clicked: ${selectedMenuItem.localName}")
        if (transaction.addTransactionItem(selectedMenuItem, clusterId)) {
            menuItemsAdapter.notifyDataSetChanged()
            transactionItemsAdapter.notifyDataSetChanged()
        }
    // Update selection state
//        menuItems.menuItems.values.forEach { item ->
//            item.isSelected = (item.menuItemId == selectedMenuItem.menuItemId)
//        }
   //     menuItemsAdapter.notifyDataSetChanged()

        // Load items for the selected page
    //    loadPageItems(selectedMenuItem.menuItemId)
    }

    private fun loadPageItems(pageId: Int) {
        // Implement your logic to load items for the selected page
        // This might involve another RecyclerView for the items in layout_items
        global.menuPageId = pageId
        val previousRows = menuItems.verticalSize
        menuPage = menuCard.getMenuPage(global.menuPageId)
        itemWidth = menuPage.pageWidth
        menuItems = menuPage.loadItems(SKIP_INVISIBLE_TRUE)
        menuItemsAdapter.setNewItems(menuItems)
        if (menuItems.verticalSize != previousRows) {
            createGridLayoutMenuItems()
        }
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
