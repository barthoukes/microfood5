package com.hha.dialog

import android.R
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.content.res.Resources
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
    val m_menuPages = menuCard.getOrderedPages()
    var m_menuPage: CMenuPage = menuCard.getMenuPage(global.menuPageId)
    var m_menuItems : CMenuItems =
        m_menuPage.loadItems(SKIP_INVISIBLE_TRUE)
    private val CFG : Configuration = global.CFG
    private val colourCFG = global.colourCFG
    private val m_colourOrderText = colourCFG.getTextColour("COLOUR_ORDER_TEXT")
    private val m_colourOrderBackgroundSelected =
        colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_SELECTED")
    private val m_colourOrderBackgroundOdd = colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_ODD")
    private val m_colourOrderBackgroundEven = colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_EVEN")

    private val m_colourOrderSelectedText = colourCFG.getBackgroundColour("COLOUR_ORDER_SELECTED_TEXT")

    private val m_colourPage = colourCFG.getBackgroundColour("COLOUR_GROUP_BACKGROUND")
    private val m_colourSelectedPage = colourCFG.getBackgroundColour("SELECTED_GROUP_BACKGROUND")
    var itemWidth = 24
    val groups = CFG.getValue("display_groups")
    val columns = CFG.getValue("display_groups_horizontal")
    val rows = (groups + columns-1) / columns
    private lateinit var m_menuPagesAdapter: MenuPagesAdapter
    private lateinit var m_menuItemsAdapter: MenuItemsAdapter
    private lateinit var m_transactionItemsAdapter: TransactionItemsAdapter
    private lateinit var m_transaction: CTransaction
    val clusterId : Short = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PageOrderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean
    {
        // We only care about when the user lifts their finger (ACTION_UP)
        if (ev?.action == MotionEvent.ACTION_UP)
        {
            // Check if the tap was on a transaction item in the left column
            findClickedItem(ev.rawX, ev.rawY)
        }

        // IMPORTANT: Let the system continue handling the event as usual for other views
        return super.dispatchTouchEvent(ev)
    }

    private fun findClickedItem(x: Float, y: Float) {
        val recyclerView = binding.layoutTransactionItems
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        // Find which item position was clicked
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val rect = Rect()
            child.getGlobalVisibleRect(rect)

            if (rect.contains(x.toInt(), y.toInt())) {
                val position = layoutManager.getPosition(child)
                Log.d("TOUCH_DEBUG", "Clicked item at position: $position")

                // Manually trigger the click
                val item = global.transaction?.get(position)
                if (item != null) {
                    onClickTransactionItem(item)
                }
                break
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when activity resumes
        if (global.transaction == null) {
            if (global.transactionId <1E6) assert(false)
            m_transaction = CTransaction(global.transactionId)
        }
        m_transaction = global.transaction!!
        m_transactionItemsAdapter.updateData(m_transaction)
        m_transaction.startNextTimeFrame()
        //menuItemsAdapter.notifyDataSetChanged()
        //transactionItemsAdapter.notifyDataSetChanged()
    }

    // Add this new function to handle language changes
    @Suppress("UNUSED_PARAMETER")


    fun on_button_language(view: View)
    {
        Translation.nextLanguage()
        refreshAllData()
    }

    // Add this new function to handle language changes
    @Suppress("UNUSED_PARAMETER")
    fun onButtonEnter(view: View) {
        startActivity(Intent(this, BillOrderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonPlus1(view: View)
    {
        m_transaction.plus1();
    }

    private fun refreshAllData() {
        //m_transactionItemsAdapter.notifyDataSetChanged()
        //m_menuPagesAdapter.notifyDataSetChanged()
        //m_menuItemsAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        createLinearLayoutTransactionItems()
        createTransactionItemsAdapter()
        createGridLayoutMenuPages()
        createMenuPagesAdapter()
        createGridLayoutMenuItems()
        createMenuItemsAdapter()

        // 3. Add spacing between items
        binding.layoutMenuPages.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View,
                    parent: RecyclerView, state: RecyclerView.State,
                ) {
                    outRect.set(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx()) // 8dp spacing
                }
            }
        )
    }

    private fun createMenuPagesAdapter()
    {
        // 2. Initialize adapter
        m_menuPagesAdapter = MenuPagesAdapter(
            m_menuPages, columns, rows, m_colourPage, m_colourSelectedPage)
        {
            selectedPage ->
            handlePageSelection(selectedPage)
        }.apply {
            // Set dynamic height based on screen size
            binding.layoutMenuPages.setItemViewCacheSize(18)
        }
        binding.layoutMenuPages.adapter = m_menuPagesAdapter
    }

    private fun createLinearLayoutTransactionItems()
    {
        // Use LinearLayoutManager for the transaction items (left column)
        val linearLayoutManager = LinearLayoutManager(
            this@PageOrderActivity,
            LinearLayoutManager.VERTICAL, // Vertical scrolling
            false // Don't reverse layout
        )
        binding.layoutTransactionItems.layoutManager = linearLayoutManager

        // Optional: Add the touch delay fix we discussed
        binding.layoutTransactionItems.postDelayed({
            binding.layoutTransactionItems.isClickable = true
            binding.layoutTransactionItems.isFocusable = true
            binding.layoutTransactionItems.isFocusableInTouchMode = true
            Log.d("TOUCH_FIX", "Transaction items RecyclerView touch enabled")
        }, 1000)
    }

    private fun createGridLayoutMenuPages()
    {
        val gridLayoutManagerMenuPages = GridLayoutManager(
            this@PageOrderActivity,
            rows,
            GridLayoutManager.HORIZONTAL, // Horizontal scrolling
            false
        )
        binding.layoutMenuPages.layoutManager = gridLayoutManagerMenuPages
    }

    private fun createMenuItemsAdapter()
    {
        // 2. Initialize adapter
        m_menuItemsAdapter =
            MenuItemsAdapter(m_menuItems, itemWidth) { selectedMenuItem ->
                handleMenuItem(selectedMenuItem)
            }.apply {
                // Set dynamic height based on screen size
                binding.layoutMenuItems.setItemViewCacheSize(28)
            }
        binding.layoutMenuItems.adapter = m_menuItemsAdapter
    }

    private fun createGridLayoutMenuItems()
    {
        // 1b. GridLayoutManager for menu items with 8 rows (vertical span) and horizontal scrolling
        val gridLayoutManagerMenuItems = GridLayoutManager(
            this@PageOrderActivity,
            m_menuItems.verticalSize, // Span count = number of rows (vertical)
            GridLayoutManager.HORIZONTAL, // Horizontal scrolling
            false
        )
        binding.layoutMenuItems.layoutManager = gridLayoutManagerMenuItems
    }

    private fun createTransactionItemsAdapter()
    {
        // 2. Initialize adapter
        m_transactionItemsAdapter = TransactionItemsAdapter(
        {  selectedTransactionItem -> onClickTransactionItem(selectedTransactionItem) },
           m_colourOrderText,
            m_colourOrderSelectedText,
            m_colourOrderBackgroundSelected,
            m_colourOrderBackgroundOdd,
            m_colourOrderBackgroundEven
            )
            .apply {
            // Set dynamic height based on screen size
            binding.layoutMenuPages.setItemViewCacheSize(25)
        }
        binding.layoutTransactionItems.adapter = m_transactionItemsAdapter
    }

    // DP-to-pixel conversion extension
    fun Int.dpToPx(): Int = (this * Resources.getSystem()
        .displayMetrics.density).toInt()

    private fun handlePageSelection(selectedPage: Int)
    {
        if (selectedPage != global.menuPageId)
        {
            m_menuPagesAdapter.selectPage(selectedPage)
            loadPageItems(selectedPage)
        }
    }

    private fun onClickTransactionItem(selectedTransactionItem: CItem)
    {
        val oldPosition = global.cursor.position

        // Update the state
        val newCursor = m_transaction.getCursor(selectedTransactionItem)
        global.cursor.set(newCursor)
        m_transactionItemsAdapter.setCursor(global.cursor)

        // Also, scroll to the newly added item so the user can see it
        binding.layoutTransactionItems.scrollToPosition(newCursor)
        // Update UI minimum
        //m_transactionItemsAdapter.notifyItemChanged(oldPosition) // Redraw the old selected item
        //m_transactionItemsAdapter.notifyItemChanged(newCursor) // Redraw the new one
    }

    private fun handleMenuItem(selectedMenuItem: CMenuItem)
    {
        Log.d("CLICK", "MenuItem clicked: ${selectedMenuItem.localName}")
        if (m_transaction.addTransactionItem(selectedMenuItem, clusterId))
        {
            //m_menuItemsAdapter.notifyDataSetChanged()
            m_transactionItemsAdapter.notifyDataSetChanged()
            /// binding.totalPrice.text = m_transaction.getTotalAmount().str()
            //m_transactionItemsAdapter.setCursor(global.cursor)
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
        val previousRows = m_menuItems.verticalSize
        m_menuPage = menuCard.getMenuPage(global.menuPageId)
        itemWidth = m_menuPage.pageWidth
        m_menuItems = m_menuPage.loadItems(SKIP_INVISIBLE_TRUE)
        m_menuItemsAdapter.setNewItems(m_menuItems)
        if (m_menuItems.verticalSize != previousRows) {
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
