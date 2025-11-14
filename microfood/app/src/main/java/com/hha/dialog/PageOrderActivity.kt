package com.hha.dialog

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.content.res.Resources
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hha.adapter.MenuPagesAdapter
import com.hha.adapter.TransactionItemsAdapter
import com.hha.common.SkipInvisible.SKIP_INVISIBLE_TRUE
import com.hha.framework.CItem
import com.hha.framework.CMenuCards
import com.hha.framework.CMenuItem
import com.hha.framework.CMenuItems
import com.hha.framework.CMenuPage
import com.hha.framework.CTransaction
import com.hha.modalDialog.ModalDialogCancelReason
import com.hha.modalDialog.ModalDialogUndoChanges
import com.hha.modalDialog.ModalDialogYesNo
import com.hha.resources.Configuration
import com.hha.resources.Global
import MenuItemsAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.hha.callback.TransactionListener
import com.hha.model.TransactionViewModel
import com.hha.model.TransactionViewModelFactory
import com.hha.types.CMoney

import tech.hha.microfood.databinding.PageOrderActivityBinding

class PageOrderActivity : AppCompatActivity(), ModalDialogYesNo.MessageBoxYesNoListener,
   ModalDialogCancelReason.MessageBoxCancelReasonListener,
   ModalDialogUndoChanges.MessageBoxUndoChangesListener,
    TransactionListener
{
    private final var tag = "POE"
    private lateinit var binding: PageOrderActivityBinding
    val global = Global.getInstance()
    val menuCardId = global.menuCardId
    val menuCard = CMenuCards.getInstance().getMenuCard(menuCardId)
    val m_menuPages = menuCard.getOrderedPages()
    var m_menuPage: CMenuPage = menuCard.getMenuPage(global.menuPageId)
    var m_menuItems: CMenuItems =
        m_menuPage.loadItems(SKIP_INVISIBLE_TRUE)
    private val CFG: Configuration = global.CFG
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
    private val _orderTotal = MutableLiveData<CMoney>()
    val orderTotal: LiveData<CMoney> = _orderTotal

    val groups = CFG.getValue("display_groups")
    val columns = CFG.getValue("display_groups_horizontal")
    val rows = (groups + columns - 1) / columns
    private lateinit var m_menuPagesAdapter: MenuPagesAdapter
    private lateinit var m_menuItemsAdapter: MenuItemsAdapter
    private lateinit var m_transactionItemsAdapter: TransactionItemsAdapter
    private lateinit var m_viewModel: TransactionViewModel
    val clusterId: Short = -1
    var m_fromBilling: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = PageOrderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        m_viewModel = ViewModelProvider(this, TransactionViewModelFactory)
            .get(TransactionViewModel::class.java)

        setupRecyclerView()
        // 3. OBSERVE the LiveData from the ViewModel
        m_viewModel.transaction.observe(this) { transaction ->
            // This block will run automatically when the transaction is loaded.
            if (transaction != null) {
                // --- THIS IS THE CORRECT PLACE TO CALL .updateData() ---
                m_transactionItemsAdapter.updateData(transaction)

                transaction.calculateTotalTransaction()
                binding.totalPrice.text = transaction.getTotalTransaction().toString()
                transaction.addListener(this)
                // It's also the correct place to add the listener
                transaction.addItemListener(m_transactionItemsAdapter)
            }
        }
        m_viewModel.initializeTransaction(TransactionViewModel.InitMode.VIEW_PAGE_ORDER)
    }

    override fun onDestroy()
    {
        super.onDestroy() // Always call the superclass method first

        // Get the current transaction object
        m_viewModel.resetIsChanged()
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

    private fun findClickedItem(x: Float, y: Float)
    {
        val recyclerView = binding.layoutTransactionItems
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        // Find which item position was clicked
        for (i in 0 until recyclerView.childCount)
        {
            val child = recyclerView.getChildAt(i)
            val rect = Rect()
            child.getGlobalVisibleRect(rect)

            if (rect.contains(x.toInt(), y.toInt()))
            {
                val position = layoutManager.getPosition(child)
                Log.d(tag, "Clicked item at position: $position")

                // Manually trigger the click
//                val item = global.transaction?.get(position)
//                if (item != null)
//                {
//                    onClickTransactionItem(item)
//                }
                break
            }
        }
    }

    override fun onResume()
    {
        super.onResume()
//        // Refresh data when activity resumes
//        if (global.transaction == null)
//        {
//            if (global.transactionId < 1E6) assert(false)
//            global.transaction = CTransaction(global.transactionId)
//        }
//        m_transactionItemsAdapter.updateData(global.transaction!!)
//        global.transaction!!.addItemListener(m_transactionItemsAdapter)
//        global.transaction!!.startNextTimeFrame()
//        //menuItemsAdapter.notifyDataSetChanged()
        //transactionItemsAdapter.notifyDataSetChanged()
    }

    // Add this new function to handle language changes
    @Suppress("UNUSED_PARAMETER")
    fun onButtonEnter(view: View)
    {
        startActivity(Intent(this, BillOrderActivity::class.java).apply {
        })
    }

    private fun stopInCorrectMode()
    {
        // Check the boolean condition
        if (m_fromBilling)
        {
            // If true, go to BillOrderActivity
            startActivity(Intent(this, BillOrderActivity::class.java))

//                .apply {
//                // Optional: Add flags if you need to clear the back stack
//                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            })
        } else
        {
            // If false, go to AskTransactionActivity
            // Note: Make sure AskTransactionActivity exists in your project
            startActivity(Intent(this, AskTransactionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        // Finish the current activity so the user can't navigate back to it
        //finish()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonPlus1(view: View)
    {
        m_viewModel.onButtonPlus1()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonMin1(view: View)
    {
        m_viewModel.onButtonMin1()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonLanguage(view: View)
    {
        Translation.nextLanguage()
        refreshAllData()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonPortion(view: View)
    {
        m_viewModel.onButtonPortion()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonRemove(view: View)
    {
        m_viewModel.onButtonRemove()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonCancel(view: View)
    {
        escapeTransaction()
    }

    private fun escapeTransaction()
    {
        if (!m_viewModel.isChanged())
        {
            // No changes to table, simple to remove the time frame.
            m_viewModel.closeTimeFrame()

            stopInCorrectMode()
        } else if (m_viewModel.needToAskCancelReason())
        {
            showAskCancelReasonDialog()

        } else if (m_viewModel.hasAnyChanges())
        {
            showUndoChanges()
        } else
        {
            stopInCorrectMode()
        }
    }

    fun addOneToCursorPosition(): Boolean
    {
        val currentTransaction = m_viewModel.transaction.value
        return currentTransaction?.addOneToCursorPosition() ?: false
    }

    private fun refreshAllData()
    {
        m_transactionItemsAdapter.notifyDataSetChanged()
        m_menuPagesAdapter.notifyDataSetChanged()
        m_menuItemsAdapter.notifyDataSetChanged()
    }

    override fun onDialogPositiveClick(dialog: DialogFragment)
    {
        // User clicked "Yes".
        // Put your logic here, for example: clear the transaction.
        Log.d(tag, "MessageBox User clicked Yes.")
    }

    override fun onDialogNegativeClick(dialog: DialogFragment)
    {
        // User clicked "No".
        // The dialog is automatically dismissed. You can log or do nothing.
        Log.d(tag, "MessageBox User clicked No.")
    }

    private fun setupRecyclerView()
    {
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        createLinearLayoutTransactionItems()
        createTransactionItemsAdapter()
        createGridLayoutMenuPages()
        createMenuPagesAdapter()
        createGridLayoutMenuItems()
        createMenuItemsAdapter()

        // 3. Add spacing between items
        binding.layoutMenuPages.addItemDecoration(
            object : RecyclerView.ItemDecoration()
            {
                override fun getItemOffsets(
                    outRect: Rect, view: View,
                    parent: RecyclerView, state: RecyclerView.State,
                )
                {
                    outRect.set(1.dpToPx(), 1.dpToPx(), 1.dpToPx(), 1.dpToPx()) // 8dp spacing
                }
            }
        )
    }

    private fun createMenuPagesAdapter()
    {
        // 2. Initialize adapter
        m_menuPagesAdapter = MenuPagesAdapter(
            m_menuPages, columns, rows, m_colourPage, m_colourSelectedPage
        )
        { selectedPage ->
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
            Log.d(tag, "Transaction items RecyclerView touch enabled")
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
            { selectedTransactionItem -> onClickTransactionItem(selectedTransactionItem) },
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

    // Add this new function to PageOrderActivity
//    private fun setupTransaction()
//    {
//        // 1. Remove the listener from any old transaction to prevent leaks
//        m_viewModel.transaction.removeItemListener(m_transactionItemsAdapter)
//
//        // 3. Assign the transaction to the local variable and add the listener
//        m_viewModel.transaction.addItemListener(m_transactionItemsAdapter)
//
//        // 4. Update the adapter with the fresh transaction data
//        m_transactionItemsAdapter.updateData(m_viewModel.transaction)
//        m_viewModel.transaction.startNextTimeFrame()
//    }

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
        val newCursor = m_viewModel.getCursor(selectedTransactionItem)
        if (newCursor == global.cursor.position)
        {
            // Add one item
            m_viewModel.onButtonPlus1()
            m_transactionItemsAdapter.invalidate(newCursor)
        } else
        {
            // Set cursor
            global.cursor.set(newCursor)
            m_transactionItemsAdapter.setCursor(global.cursor)
        }
        // Also, scroll to the newly added item so the user can see it
        binding.layoutTransactionItems.scrollToPosition(newCursor)
        // Update UI minimum
        //m_transactionItemsAdapter.notifyItemChanged(oldPosition) // Redraw the old selected item
        //m_transactionItemsAdapter.notifyItemChanged(newCursor) // Redraw the new one
    }

    private fun handleMenuItem(selectedMenuItem: CMenuItem)
    {
        Log.d(tag, "MenuItem clicked: ${selectedMenuItem.localName}")
        if (m_viewModel.addItem(selectedMenuItem, clusterId))
        {
            m_transactionItemsAdapter.setCursor(global.cursor)
           // m_menuItemsAdapter.notifyDataSetChanged()
            //m_transactionItemsAdapter.notifyDataSetChanged()
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

    private fun loadPageItems(pageId: Int)
    {
        // Implement your logic to load items for the selected page
        // This might involve another RecyclerView for the items in layout_items
        global.menuPageId = pageId
        val previousRows = m_menuItems.verticalSize
        m_menuPage = menuCard.getMenuPage(global.menuPageId)
        itemWidth = m_menuPage.pageWidth
        m_menuItems = m_menuPage.loadItems(SKIP_INVISIBLE_TRUE)
        m_menuItemsAdapter.setNewItems(m_menuItems)
        if (m_menuItems.verticalSize != previousRows)
        {
            createGridLayoutMenuItems()
        }
    }

    private fun navigateBack()
    {
        // Intent to start MainActivity which will host your MainMenuDialog
        //val mainIntent = Intent(this@AboutActivity, MainMenuActivity::class.java)
        // Optional: Pass any fetched data to MainActivity
        // mainIntent.putExtra("MENU_DATA", "your_fetched_menu_data_string_or_parcelable")
        //startActivity(mainIntent)
        finish() // Close the SplashActivity so it's not in the back stack
    }

    private fun showConfirmationDialog()
    {
        val dialog = ModalDialogYesNo.newInstance(
            "Confirm Action",
            "Are you sure you want to proceed?"
        )
        dialog.show(supportFragmentManager, "MessageBoxYesNo")
    }

    private fun showUndoChanges()
    {
        val dialog = ModalDialogYesNo.newInstance(
            "Undo Changes",
            "Undo the changes made to the order?"
        )
        dialog.show(supportFragmentManager, "MessageBoxYesNo")
    }

    private fun showAskCancelReasonDialog()
    {
        val dialog = ModalDialogCancelReason()
        dialog.show(supportFragmentManager, "MessageBoxCancelReason")
    }

    override fun onReasonSelected(reason: String)
    {
        Log.i(tag, "Cancelled $reason")
        m_viewModel.emptyTransaction(reason)
        finish()
    }

    override fun onDialogCancelled()
    {
        Log.i(tag, "Cancel order cancelled")
    }

    override fun onDialogUndoChanges(dialog: DialogFragment)
    {
        TODO("Not yet implemented")
    }

    override fun onDialogContinueOrder(dialog: DialogFragment)
    {
        TODO("Not yet implemented")
    }

    override fun onTransactionChanged(transaction: CTransaction)
    {
        // Update display price not allowed here.
        //binding.totalPrice.text = transaction.getTotalTransaction().toString()
    }

}
