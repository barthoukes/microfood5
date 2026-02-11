package com.hha.dialog

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.content.res.Resources
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast

import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.getValue

import com.hha.adapter.MenuPagesAdapter
import com.hha.adapter.TransactionItemsAdapter
import com.hha.common.SkipInvisible.SKIP_INVISIBLE_TRUE
import com.hha.framework.CItem
import com.hha.framework.CMenuCards
import com.hha.framework.CMenuItem
import com.hha.framework.CMenuItems
import com.hha.framework.CMenuPage
import com.hha.modalDialog.ModalDialogCancelReason
import com.hha.modalDialog.ModalDialogUndoChanges
import com.hha.modalDialog.ModalDialogQuantity
import com.hha.modalDialog.ModalDialogYesNo
import com.hha.resources.Configuration
import com.hha.resources.Global
import MenuItemsAdapter
import com.hha.dialog.Translation.str
import com.hha.modalDialog.ModalDialogDelay
import com.hha.modalDialog.ModalDialogQuantities
import com.hha.model.TransactionModel
import com.hha.resources.CTimestamp
import com.hha.types.CMoney
import com.hha.types.EFinalizerAction
import com.hha.types.EInitMode

import tech.hha.microfood.databinding.PageOrderActivityBinding

class PageOrderActivity : BaseActivity(), ModalDialogYesNo.MessageBoxYesNoListener,
    ModalDialogCancelReason.ModalDialogCancelReasonListener,
    ModalDialogUndoChanges.MessageBoxUndoChangesListener,
    ModalDialogQuantity.ModalDialogQuantityListener,
    ModalDialogDelay.ModalDialogDelayListener,
    ModalDialogQuantities.ModalDialogQuantitiesListener
{
    val REQUEST_CODE_UNDO_CHANGES = 1
    private final var tag = "POE"
    private lateinit var mBinding: PageOrderActivityBinding
    val global = Global.getInstance()
    val menuCardId = global.menuCardId
    val menuCard = CMenuCards.getInstance().getMenuCard(menuCardId)
    val mMenuPages = menuCard.getOrderedPages()
    var mMenuPage: CMenuPage = menuCard.getMenuPage(global.menuPageId)
    var mMenuItems: CMenuItems =
        mMenuPage.loadItems(SKIP_INVISIBLE_TRUE)
    private val CFG: Configuration = global.CFG
    private val colourCFG = global.colourCFG
    private val mColourOrderText = colourCFG.getTextColour("COLOUR_ORDER_TEXT")
    private val mColourOrderBackgroundSelected =
        colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_SELECTED")
    private val mColourOrderBackgroundOdd = colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_ODD")
    private val mColourOrderBackgroundEven = colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_EVEN")

    private val mColourOrderSelectedText = colourCFG.getBackgroundColour("COLOUR_ORDER_SELECTED_TEXT")
    private val mAskQuantityZero = CFG.getBoolean("ask_quantity_zero")

    private val mColourPage = colourCFG.getBackgroundColour("COLOUR_GROUP_BACKGROUND")
    private val mColourSelectedPage = colourCFG.getBackgroundColour("SELECTED_GROUP_BACKGROUND")
    private val mFloorplanBillFirst = CFG.getBoolean("floorplan_bill_first")
    lateinit private var mTimestamp: CTimestamp
    private var mOriginalBrightness: Float = -1.0f

    var itemWidth = 24
    var mChangedTime = false
    private val _orderTotal = MutableLiveData<CMoney>()
    val orderTotal: LiveData<CMoney> = _orderTotal
    var mEnterPressed = false
    var mFirstTime = false
    val groups = CFG.getValue("display_groups")
    val columns = CFG.getValue("display_groups_horizontal")
    val rows = (groups + columns - 1) / columns
    private lateinit var mMenuPagesAdapter: MenuPagesAdapter
    private lateinit var mMenuItemsAdapter: MenuItemsAdapter
    private lateinit var mTransactionItemsAdapter: TransactionItemsAdapter
    private val mTransactionModel: TransactionModel by viewModels()
    val mClusterId: Short = -1
    var mFromBilling: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        Log.i(tag, "onCreate")
        super.onCreate(savedInstanceState)
        mBinding = PageOrderActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.poeLoading.text = Translation.get(
            Translation.TextId.TEXT_LOADING)
//        mTransactionModel = ViewModelProvider(this, TransactionModelFactory)
//            .get(TransactionModel::class.java)
        setMaxScreenBrightness()

        val transactionId = intent.getIntExtra("TRANSACTION_ID", -1)
        mFromBilling = intent.getBooleanExtra("FROM_BILL", false)
        val mNewTimeFrame = intent.getBooleanExtra("NEW_TIME_FRAME", false)
        Log.i(tag, "onCreate $transactionId $mFromBilling $mNewTimeFrame")
        mFirstTime = true
        // 2. Validate the ID. If it's missing, the activity cannot function.
        if (transactionId <= 1E6)
        {
            Log.e(tag, "FATAL: No TRANSACTION_ID found in Intent. Finishing activity.")
            Toast.makeText(this, "Error: Transaction not found", Toast.LENGTH_LONG).show()
            finish() // Exit immediately
            return   // Stop executing onCreate
        }

        // 3. Set up observers to react to data changes from the ViewModel.
        //    This is where you connect the data to your UI.
        setupRecyclerViews()
        setupClickListeners()
        setupObservers()
        setupNavigationObserver()

        // 2. Tell the ViewModel to start loading the transaction data.
        //    The ViewModel will handle background threads and state updates.
        mTransactionModel.initializeTransaction(
            transactionId, mNewTimeFrame, EInitMode.VIEW_PAGE_ORDER
        )
    }

    private fun setupClickListeners()
    {
        Log.i(tag, "setupClickListeners")
        // Table overview button
        mBinding.OK.apply {
            setOnClickListener {
                onButtonEnter()
            }
        }
    }

    private fun setupNavigationObserver()
    {
        Log.i(tag, "setupNavigationObserver")

        mTransactionModel.navigateToPageOrder.observe(this) { event ->
            // Use the MyEvent wrapper to ensure navigation happens only once
            event.getContentIfNotHandled()?.let { navEvent ->
                // This block will only execute once per event, even on screen rotation.
                Log.i(tag, "Received navigation event for transaction ID: ${navEvent.transactionId}")

                // Create the Intent to start PageOrderActivity
                val intent = Intent(this, PageOrderActivity::class.java).apply {
                    // Pass the transaction ID to the next activity
                    // Make sure to convert Int to Long if the receiving end expects a Long
                    putExtra("TRANSACTION_ID", navEvent.transactionId)
                    putExtra("NEW_TIME_FRAME", navEvent.newTimeFrame)
                    putExtra("FROM_BILL", navEvent.fromBilling)
                }
                startActivity(intent)
            }
        }
    }

    private fun setupObservers()
    {
        Log.i(tag, "setupObservers")

        // Observe the transaction itself
        mTransactionModel.activeTransaction.observe(this) { transaction ->
            if (transaction != null)
            {
                if (mFirstTime)
                {
                    mFirstTime = false
                    val newCursor = mTransactionModel.size
                    global.cursor.set(newCursor)
                    mTransactionItemsAdapter.setCursor(global.cursor)
                    mTransactionModel.initPageOrder()
                    showTransactionLoading(false)
                }
                updateTransactionName()

                // When the transaction is ready, update the adapter.
                mTransactionItemsAdapter.submitList(transaction)
            } else
            {
                // Handle the case where the transaction failed to load.
                // Maybe show an error message and close the activity.
                Log.e(tag, "Failed to load transaction. Finishing activity.")
                finish()
            }
        }

        // This block will run every time _orderTotal is updated in the ViewModel.
        mTransactionModel.orderTotal.observe(this) { newTotal ->
            // The 'newTotal' is the CMoney object from the LiveData.
            // We format it and set it on the TextView.
            mBinding.pageOrderPrice.text = newTotal.toString()
        }
        // Observe other LiveData like total price, display lines, etc.
        // mTransactionModel.orderTotal.observe(this) { total -> ... }
    }

    override fun onDestroy()
    {
        Log.i(tag, "onDestroy")
        // Get the current transaction object
        restoreOriginalScreenBrightness()
        mTransactionModel.resetIsChanged()
        super.onDestroy() // Always call the superclass method first
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
        val recyclerView = mBinding.layoutTransactionItems
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
                break
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonPlus1(view: View)
    {
        Log.i(tag, "onButtonPlus1")
        mTransactionModel.onButtonPlus1()
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

    fun onButtonEnter()
    {
        Log.i(tag, "onButtonEnter")
        val action: EFinalizerAction = mTransactionModel.finishTransaction(mFromBilling)
        handleAction(action)
    }

    private fun handleAction(action: EFinalizerAction)
    {
        Log.i(tag, "handleAction $action")
        when (action)
        {
            EFinalizerAction.FINALIZE_NO_ACTION,
            EFinalizerAction.FINALIZE_NOT_IDENTIFIED,
                -> return

            EFinalizerAction.FINALIZE_MODE_ASK_TABLE -> askTransactionActivity()
            EFinalizerAction.FINALIZE_MODE_BILLING -> BillOrderActivity()
            EFinalizerAction.FINALIZE_RESET_CHANGES -> mTransactionModel.resetChanges()
            EFinalizerAction.FINALIZE_SET_ENTER_PRESSED -> mEnterPressed = true
            EFinalizerAction.FINALIZE_ASK_CANCEL_REASON -> askCancelReasonAndClearTransaction()
            EFinalizerAction.FINALIZE_TO_BE_IMPLEMENTED ->
            {
                TODO("Not implemented")
            }

            EFinalizerAction.FINALIZE_TA_ASK_DELAY_CONTINUE -> takeawayAskDelayContinue()
            EFinalizerAction.FINALIZE_TA_ASK_DELAY_MINUTES -> takeawayAskDelayMinutes()
            EFinalizerAction.FINALIZE_MODAL_DIALOG_QUANTITY ->
            {
                //askCancelReasonAndClearTransaction()
                askQuantityDialog()
            }

            EFinalizerAction.FINALIZE_MODAL_DIALOG_QUANTITIES ->
            {
                val txt = Translation.TextId.TEXT_QUANTITY.str()
                ModalDialogQuantities.newInstance(txt)
                // Fun handleFinishQuantiesAfterAskingQuantities will be called after the dialog.
            }

            EFinalizerAction.FINALIZE_SI_ASK_DELAY_MINUTES -> ModalDialogDelay.newInstance(0)
            EFinalizerAction.FINALIZE_SI_ASK_DELAY_CONTINUE -> finalizeSiAskDelayContinue()
            else -> finalizeSiAskDelayContinue()
        }
    }

    private fun finalizeSiAskDelayContinue()
    {
        Log.i(tag, "finalizeSiAskDelayContinue")
        // REMOVE THIS LINE: throw IllegalStateException("Not implemented")

        // Instead, log it or show a Toast for now
        Log.w(tag, "Action FINALIZE_SI_ASK_DELAY_CONTINUE is not yet implemented.")
        toast("Delay/Continue is not implemented yet.")
    }

    private fun askCancelReasonAndClearTransaction()
    {
        Log.i(tag, "askCancelReasonAndClearTransaction")
        val dialog = ModalDialogCancelReason()
        dialog.show(supportFragmentManager, "MessageBoxCancelReason")
    }

    private fun askQuantityDialog()
    {
        Log.i(tag, "askQuantityDialog")
        try
        {
            val txt = Translation.TextId.TEXT_QUANTITY.str()
            val dialog = ModalDialogQuantity.newInstance(txt)

            // Check fragment manager state
            if (!isFinishing && !isDestroyed)
            {
                dialog.show(supportFragmentManager, "ModalDialogQuantity")
                Log.i(tag, "Dialog shown")
            } else
            {
                Log.e(tag, "Activity is finishing or destroyed, cannot show dialog")
            }
        } catch (e: Exception)
        {
            Log.e(tag, "Error showing dialog: ${e.message}", e)
            Toast.makeText(this, "Error showing dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun takeawayAskDelayContinue()
    {
        toast("Not implemented")
        throw IllegalStateException("Not implemented")
    }

    fun takeawayAskDelayMinutes()
    {
        ModalDialogDelay.newInstance(0)
        handleAction(EFinalizerAction.FINALIZE_NO_ACTION)
        // AFter the dialog, onDelayFinalized() is called.
    }

    private fun stopInCorrectMode()
    {
        // Todo: Implement rechaud, wok, sitin
        val intent = when (mFromBilling)
        {
            true -> Intent(this, BillOrderActivity::class.java)
            false -> Intent(this, AskTransactionActivity::class.java)
        }
        intent.putExtra("TRANSACTION_ID", mTransactionModel.getTransactionId())
        startActivity(intent.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
               Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        //finish()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonMin1(view: View)
    {
        mTransactionModel.onButtonMin1()
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
        mTransactionModel.onButtonPortion()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonRemove(view: View)
    {
        mTransactionModel.onButtonRemove()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonCancel(view: View)
    {
        escapeTransaction()
    }

    private fun escapeTransaction()
    {
        Log.d(tag, "handleEscapeTransaction called.")
        val transaction = mTransactionModel.activeTransaction.value

        if (transaction == null) {
            // No transaction, just finish the activity.
            finish()
            mFromBilling = false
            stopInCorrectMode()
        }
        if (mTransactionModel.needToAskCancelReason())
        {
            showAskCancelReasonDialog()

        } else if (!mTransactionModel.isChanged())
        {
            // No changes to table, simple to remove the time frame.
            mTransactionModel.removeAndUndoTimeFrame()
            stopInCorrectMode()
        }
        else
        {
            showUndoChanges()
        }
    }

    suspend fun addOneToCursorPosition(): Boolean
    {
        val currentTransaction = mTransactionModel.activeTransaction.value
        return currentTransaction?.addOneToCursorPosition() ?: false
    }

    private fun refreshAllData()
    {
        mTransactionItemsAdapter.notifyDataSetChanged()
        mMenuPagesAdapter.notifyDataSetChanged()
        mMenuItemsAdapter.notifyDataSetChanged()
        refreshButtons()
    }

    private fun refreshButtons()
    {
        mBinding.poeHeaderText.text = Translation.get(Translation.TextId.TEXT_PAGE_ORDER)
        updateTransactionName()
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, requestCode: Int)
    {
        Log.d(tag, "MessageBox User clicked Yes, requestCode=$requestCode.")
        // User clicked "Yes".
        // Put your logic here, for example: clear the transaction.
        when (requestCode)
        {
            REQUEST_CODE_UNDO_CHANGES ->
            {
                mTransactionModel.removeAndUndoTimeFrame()
                stopInCorrectMode()
            }
            else -> {}
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment, requestCode: Int)
    {
        // User clicked "No".
        // The dialog is automatically dismissed. You can log or do nothing.
        Log.d(tag, "MessageBox User clicked No.")
        when (requestCode)
        {
            REQUEST_CODE_UNDO_CHANGES -> {}
            else -> {}
        }
    }

    private fun setupRecyclerViews()
    {
        Log.i(tag, "setupRecyclerViews")
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        createLinearLayoutTransactionItems()
        createTransactionItemsAdapter()
        createGridLayoutMenuPages()
        createMenuPagesAdapter()
        createGridLayoutMenuItems()
        createMenuItemsAdapter()

        // 3. Add spacing between items
        mBinding.layoutMenuPages.addItemDecoration(
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

    private fun showTransactionLoading(isLoading: Boolean)
    {
        if (isLoading)
        {
            mBinding.poeLoading.visibility = View.VISIBLE
            mBinding.layoutTransactionItems.visibility = View.INVISIBLE
        } else
        {
            mBinding.poeLoading.visibility = View.GONE
            mBinding.layoutTransactionItems.visibility = View.VISIBLE
        }
    }

    private fun createMenuPagesAdapter()
    {
        // 2. Initialize adapter
        mMenuPagesAdapter = MenuPagesAdapter(
            mMenuPages, columns, rows, mColourPage, mColourSelectedPage
        )
        { selectedPage ->
            handlePageSelection(selectedPage)
        }.apply {
            // Set dynamic height based on screen size
            mBinding.layoutMenuPages.setItemViewCacheSize(18)
        }
        mBinding.layoutMenuPages.adapter = mMenuPagesAdapter
    }

    private fun createLinearLayoutTransactionItems()
    {
        // Use LinearLayoutManager for the transaction items (left column)
        val linearLayoutManager = LinearLayoutManager(
            this@PageOrderActivity,
            LinearLayoutManager.VERTICAL, // Vertical scrolling
            false // Don't reverse layout
        )
        mBinding.layoutTransactionItems.layoutManager = linearLayoutManager

        // Optional: Add the touch delay fix we discussed
        mBinding.layoutTransactionItems.postDelayed({
            mBinding.layoutTransactionItems.isClickable = true
            mBinding.layoutTransactionItems.isFocusable = true
            mBinding.layoutTransactionItems.isFocusableInTouchMode = true
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
        mBinding.layoutMenuPages.layoutManager = gridLayoutManagerMenuPages
    }

    private fun createMenuItemsAdapter()
    {
        // 2. Initialize adapter
        mMenuItemsAdapter =
            MenuItemsAdapter(mMenuItems, itemWidth) { selectedMenuItem ->
                handleMenuItem(selectedMenuItem)
            }.apply {
                // Set dynamic height based on screen size
                mBinding.layoutMenuItems.setItemViewCacheSize(28)
            }
        mBinding.layoutMenuItems.adapter = mMenuItemsAdapter
    }

    private fun createGridLayoutMenuItems()
    {
        // 1b. GridLayoutManager for menu items with 8 rows (vertical span) and horizontal scrolling
        val gridLayoutManagerMenuItems = GridLayoutManager(
            this@PageOrderActivity,
            mMenuItems.verticalSize, // Span count = number of rows (vertical)
            GridLayoutManager.HORIZONTAL, // Horizontal scrolling
            false
        )
        mBinding.layoutMenuItems.layoutManager = gridLayoutManagerMenuItems
    }

    private fun createTransactionItemsAdapter()
    {
        // 2. Initialize adapter
        mTransactionItemsAdapter = TransactionItemsAdapter(
            { selectedTransactionItem -> onClickTransactionItem(selectedTransactionItem) },
            mColourOrderText,
            mColourOrderSelectedText,
            mColourOrderBackgroundSelected,
            mColourOrderBackgroundOdd,
            mColourOrderBackgroundEven
        )
            .apply {
                // Set dynamic height based on screen size
                mBinding.layoutMenuPages.setItemViewCacheSize(25)
            }
        mBinding.layoutTransactionItems.adapter = mTransactionItemsAdapter
    }

    // DP-to-pixel conversion extension
    fun Int.dpToPx(): Int = (this * Resources.getSystem()
        .displayMetrics.density).toInt()

    override fun handleFinishQuantiesAfterAskingQuantities(
        kitchenQuantity: Int,
        billQuantity: Int,
        action: ModalDialogQuantities.Action,
    )
    {
        val ts = CTimestamp()
        val isBilling = (action == ModalDialogQuantities.Action.BILL)
        val newAction = mTransactionModel.handleFinishQuantiesAfterAskingQuantities(
            mFromBilling, kitchenQuantity,
            billQuantity,
            mChangedTime, ts, isBilling
        )
        handleAction(newAction)
    }

    private fun handlePageSelection(selectedPage: Int)
    {
        if (selectedPage != global.menuPageId)
        {
            mMenuPagesAdapter.selectPage(selectedPage)
            loadPageItems(selectedPage)
        }
    }

    private fun onClickTransactionItem(selectedTransactionItem: CItem)
    {
        val oldPosition = mTransactionModel.mCursor.position

        // Update the state
        val newCursor = mTransactionModel.getCursor(selectedTransactionItem)
        if (newCursor == global.cursor.position)
        {
            // Add one item
            mTransactionModel.onButtonPlus1()
            mTransactionItemsAdapter.invalidate(newCursor)
        } else
        {
            // Set cursor
            global.cursor.set(newCursor)
            mTransactionItemsAdapter.setCursor(global.cursor)
            // Update UI minimum
            mTransactionItemsAdapter.notifyItemChanged(oldPosition) // Redraw the old selected item
            mTransactionItemsAdapter.notifyItemChanged(newCursor) // Redraw the new one
        }
        // Also, scroll to the newly added item so the user can see it
        mBinding.layoutTransactionItems.scrollToPosition(newCursor)
    }

    private fun handleMenuItem(selectedMenuItem: CMenuItem)
    {
        Log.i(tag, "handleMenuItem: ${selectedMenuItem.menuItemId} ${selectedMenuItem.localName}")
        if (mTransactionModel.addItem(selectedMenuItem, mClusterId))
        {
            mTransactionItemsAdapter.setCursor(global.cursor)
        }
    }

    private fun loadPageItems(pageId: Int)
    {
        // Implement your logic to load items for the selected page
        // This might involve another RecyclerView for the items in layout_items
        global.menuPageId = pageId
        val previousRows = mMenuItems.verticalSize
        mMenuPage = menuCard.getMenuPage(global.menuPageId)
        itemWidth = mMenuPage.pageWidth
        mMenuItems = mMenuPage.loadItems(SKIP_INVISIBLE_TRUE)
        mMenuItemsAdapter.setNewItems(mMenuItems)
        if (mMenuItems.verticalSize != previousRows)
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
            "Are you sure you want to proceed?",
            0
        )
        dialog.show(supportFragmentManager, "MessageBoxYesNo")
    }

    fun billOrderActivity(transactionId: Int)
    {
        // Change to billing with the same transaction.
       try
       {//mTransactionModel.initializeTransaction(TransactionModel.InitMode.VIEW_BILLING)
       } catch (e: Exception)
       {
          TODO("Not yet implemented")
       }
       val intent = Intent(this, BillOrderActivity::class.java)
        intent.putExtra("TRANSACTION_ID", transactionId)
        startActivity(intent)
        finish()
    }

    fun askTransactionActivity()
    {
        Log.i("PageOrder", "Bill processed. Navigating to AskTransactionActivity.")

        // Create an Intent to start the AskTransactionActivity
        val intent = Intent(this, AskTransactionActivity::class.java)

        // Add these flags
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        // Start the new activity
        startActivity(intent)

        // Close the current PageOrderActivity so it's removed from the back stack
        finish()
    }

    override fun onCancelReasonSelected(reason: String)
    {
        Log.i(tag, "Cancelled $reason")
        mTransactionModel.emptyTransaction(reason)
        handleAction(EFinalizerAction.FINALIZE_MODE_ASK_TABLE)
    }

    override fun onDialogCancelReasonCancelled()
    {
        Log.i(tag, "Cancel order cancelled")
        handleAction(EFinalizerAction.FINALIZE_NO_ACTION)
    }

    override fun onDialogUndoChangesYes(dialog: DialogFragment)
    {
        TODO("Not yet implemented")
    }

    override fun onDialogUndoChangesNo(dialog: DialogFragment)
    {
        TODO("Not yet implemented")
    }

    private fun showAskCancelReasonDialog()
    {
        val dialog = ModalDialogCancelReason()
        dialog.show(supportFragmentManager, "MessageBoxCancelReason")
    }

    override fun onQuantitySelected(
        quantity: Int, billingMode: Boolean, stop: Boolean,
    )
    {
        // Modal dialog pressed a button.
        val action = mTransactionModel.handleFinishQuantity(
            quantity, billingMode, stop
        )
        handleAction(action)
    }

    override fun onDelayFinalized(action: ModalDialogDelay.DelayAction, finalDelay: Int)
    {
        // We typed the delay in steps of 10 minutes.
        if (action == ModalDialogDelay.DelayAction.STOP)
        {
            handleAction(EFinalizerAction.FINALIZE_NO_ACTION)
        }
        val ts = CTimestamp()
        ts.addMinutes(finalDelay)
        val action = mTransactionModel.handleFinishTakeawayQuantity(ts)
        handleAction(action)
    }

    private fun showUndoChanges()
    {
        val dialog = ModalDialogYesNo.newInstance(
            "Undo Changes",
            "Undo the changes made to the order?",
            REQUEST_CODE_UNDO_CHANGES
        )
        dialog.show(supportFragmentManager, "MessageBoxYesNo")
        // If yes -> onDialogPositiveClick()
        // If no -? onDialogNegativeClick()
    }

    fun updateTransactionName()
    {
        mBinding.pageOrderName.text = mTransactionModel.getTableName()
    }
}
