package com.hha.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.hha.adapter.ShortTransactionListAdapter
import com.hha.framework.CFloorTable
import com.hha.model.FloorTableModel
import com.hha.framework.CShortTransaction
import com.hha.resources.Global
import tech.hha.microfood.databinding.AskTransactionActivityBinding
import com.hha.dialog.Translation.TextId
import com.hha.floor.FloorTablesAdapter
import com.hha.framework.CFloorTables
import com.hha.model.ShortTransactionsModel
import com.hha.model.TransactionModel
import tech.hha.microfood.R


class AskTransactionActivity : BaseActivity()
{
    private val global = Global.getInstance()
    private val CFG = global.CFG
    private lateinit var mBinding: AskTransactionActivityBinding

    private val mShortTransactionListAdapter: ShortTransactionListAdapter = ShortTransactionListAdapter { selectedShortTransaction ->
        onShortTransactionSelected(selectedShortTransaction)
    }
    private val mFloorTablesAdapter: FloorTablesAdapter = FloorTablesAdapter { selectedFloorTable ->
        onFloorTableSelected(selectedFloorTable)
    }

    private val mFloorTableModel: FloorTableModel by viewModels()
    //private lateinit var mTransaction: CTransaction

    private val mTransactionModel: TransactionModel by viewModels()
    private val mShortTransactionsModel: ShortTransactionsModel by viewModels()
    private var isShowingFloorPlanOnPhone = true
    private var mBill = false
    private var isTablet: Boolean = false
    private val tag = "AskTransaction"

    fun createGridLayoutFloorTables()
    {
        // 1. Get the screen width in pixels
        Log.i(tag, "createGridLayoutFloorTables")
        val displayMetrics = resources.displayMetrics
        val screenWidthPx = displayMetrics.widthPixels

        // 2. Get the desired width of one item from your dimensions file
        val itemWidthPx = resources.getDimensionPixelSize(R.dimen.floor_table_item_width)

        // 3. Calculate how many columns can fit, ensuring at least 1
        val spanCount = (screenWidthPx / itemWidthPx).coerceAtLeast(1)

        // 4. Create the LayoutManager with the calculated spanCount
        val gridLayoutFloorTables = GridLayoutManager(
            this@AskTransactionActivity,
            spanCount, // Use the dynamic spanCount
            GridLayoutManager.VERTICAL,
            false
        )
        mBinding.layoutFloorTables.layoutManager = gridLayoutFloorTables
    }

    fun createGridLayoutShortTransactionList()
    {
        Log.i(tag, "createGridLayoutShortTransactionList")
        val gridLayoutShortTransactions = GridLayoutManager(
            this@AskTransactionActivity,
            1,
            GridLayoutManager.VERTICAL, // Horizontal scrolling
            false
        )
        mBinding.layoutShortTransactionList.layoutManager = gridLayoutShortTransactions
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        Log.i(tag, "onCreate")
        super.onCreate(savedInstanceState)
        isTablet = resources.getBoolean(R.bool.is_tablet)
        mBinding = AskTransactionActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupRecyclerView()
        setupNavigationPageOrderObserver()

        // Setup observers BEFORE refreshing data
        setupObservers()

        // Initialize with empty adapters
        mShortTransactionListAdapter.submitList(emptyList())
        mFloorTablesAdapter.submitList(CFloorTables())

        updateViewVisibility()
    }

    override fun onDestroy()
    {
        Log.i(tag, "onDestroy")
        super.onDestroy()
        // Optional: Clear cache if needed
        // mFloorTableModel.clearCache()
    }


    fun onShortTransactionSelected(selectedTransaction: CShortTransaction)
    {
        Log.i(
            tag, "onShortTransactionSelected ${selectedTransaction.name} ${
                selectedTransaction
                    .transactionId
            }"
        )

        if (mFloorTablesAdapter.getSelectedTransactionName() != selectedTransaction.name)
        {
            mFloorTablesAdapter.selectTransactionName(selectedTransaction.name)
            mShortTransactionListAdapter.selectTransactionId(selectedTransaction.transactionId)
        } else if (mBill)
        {
            // Change to billing activity for this transaction.
            mTransactionModel.selectTransaction(selectedTransaction.transactionId)

        } else
        {
            // Same name chosen,
            mShortTransactionListAdapter.selectTransactionId(selectedTransaction.transactionId)
        }
    }

    /**
     * Called when a floor table circle is clicked.
     * If it's the second click, this will create a NEW transaction.
     */
    fun onFloorTableSelected(selectedFloorTable: CFloorTable)
    {
        Log.i(
            tag, "onFloorTableSelected: ${selectedFloorTable.name}, current selected: " +
               "${mFloorTablesAdapter.getSelectedTransactionName()}"
        )

        // First click: Just select the item visually in both lists.
        if (mFloorTablesAdapter.getSelectedTransactionName() != selectedFloorTable.name)
        {
            mFloorTablesAdapter.selectTransactionName(selectedFloorTable.name)
            // Also highlight the corresponding transaction in the left-hand list if it exists
            mShortTransactionListAdapter.selectTransactionId(selectedFloorTable.transactionId)
        }
        // Second click on the s(ame table: Create a new transaction and navigate.
        else if (mBill && (selectedFloorTable.transactionId > 0))
        {
            // Change to billing activity for this transaction.
            mTransactionModel.selectTransaction(selectedFloorTable.transactionId)

        } else
        {
            Log.i(
                tag, "onFloorTableSelected: Second click: Creating NEW transaction for table: ${
                    selectedFloorTable
                        .name
                }"
            )
            // Tell the ViewModel to create a new transaction for this table.
            // The observer for 'mFloorTableModel.navigateToTransaction' will handle navigation.
            val minutes = CFG.getValue("maximum_time")
            mTransactionModel.createTransactionForTable(
                selectedFloorTable.name, selectedFloorTable.tableId,
                minutes
            )
        }
    }
//    fun onFloorTableSelected(selectedFloorTable: CFloorTable)
//    {
//        Log.i("AskTransaction", "onFloorTableSelected ${selectedFloorTable.name} ${selectedFloorTable
//            .transactionId}")
//        // Select this in the list
//        mFloorTablesAdapter.selectTransactionName(selectedFloorTable.name)
//        mShortTransactionListAdapter.selectTransactionId(selectedFloorTable.transactionId)
//    }

    override fun onResume()
    {
        Log.i(tag, "onResume")
        super.onResume()
        updateViewVisibility()
        mShortTransactionsModel.refreshAllShortTransactions()
        mFloorTableModel.refreshAllData()
        //refreshAllData()
    }

    private fun setupNavigationPageOrderObserver()
    {
        // 1. OBSERVE THE CORRECT EVENT: navigateToPageOrder
        mTransactionModel.navigateToPageOrder.observe(this) { event ->
            // 2. Use the MyEvent wrapper to handle this as a one-time event
            event.getContentIfNotHandled()?.let { transactionId ->
                // This block now runs ONLY when a NEW transaction is created
                Log.i(tag, "Navigating via navigateToPageOrder event with NEW ID: $transactionId")

                val intent = Intent(this, PageOrderActivity::class.java).apply {
                    // These flags are crucial
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

                    putExtra("TRANSACTION_ID", transactionId)
                }
                startActivity(intent)
            }
        }
    }

    private fun setupObservers()
    {
        // Observe loading state
        mFloorTableModel.isLoading.observe(this) { isLoading ->
            mBinding.mainProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

//        // Observe error messages
//        mFloorTableModel.errorMessage.observe(this) { error ->
//            error?.let {
//                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
//                // Clear error after showing
//                mFloorTableModel.errorMessage.value = null
//            }
//        }

        mFloorTableModel.floorTables.observe(this) { floorTables ->
            Log.i(tag, "Floor tables observed, size: ${floorTables?.size}")
            if (floorTables != null)
            {
                mFloorTablesAdapter.submitList(floorTables)
                // Force layout update
                mBinding.layoutFloorTables.requestLayout()
            }
        }

        mShortTransactionsModel.shortTransactionList.observe(this) { shortTransactions ->
            Log.i(tag, "Short transactions observed, size: ${shortTransactions?.size}")
            if (shortTransactions != null)
            {
                mShortTransactionListAdapter.submitList(shortTransactions)
            }
        }

        mTransactionModel.activeTransaction.observe(this) { transaction ->
            if (transaction != null)
            {
                val intent = if (mBill)
                {
                    Intent(this, BillOrderActivity::class.java)
                } else
                {
                    Intent(this, PageOrderActivity::class.java)
                }
                intent.putExtra("TRANSACTION_ID", transaction.transactionId)
                startActivity(intent)
                mTransactionModel.onNavigationComplete()
            }
        }
    }

    private fun setupRecyclerView()
    {
        // Set the adapters that were already created
        mBinding.layoutFloorTables.adapter = mFloorTablesAdapter
        mBinding.layoutShortTransactionList.adapter = mShortTransactionListAdapter

        createGridLayoutFloorTables()
        createGridLayoutShortTransactionList()

        mBinding.layoutFloorTables.setItemViewCacheSize(64)
        mBinding.layoutShortTransactionList.setItemViewCacheSize(64)
    }

// ... inside AskTransactionActivity class

    private fun refreshAllData()
    {
        Log.i(tag, "refreshAllData")
        refreshShortTransaction()
        refreshFloorPlan()
    }

    private fun refreshShortTransaction()
    {
        Log.i(tag, "refreshShortTransaction")
        mShortTransactionListAdapter.notifyDataSetChanged()
        mShortTransactionsModel.refreshAllShortTransactions()
    }

    private fun refreshFloorPlan()
    {
        Log.i(tag, "refreshFloorPlan")
        mFloorTablesAdapter.refreshAllData()
        mFloorTableModel.refreshAllData()
        val chooseToOrder = when (mBill)
        {
            false -> Translation.get(TextId.TEXT_CHOOSE_TO_ORDER)
            true -> Translation.get(TextId.TEXT_BILL_OPTION)
        }
        val floorPlanName = mFloorTableModel.floorPlanName()
        mBinding.headerButton.text = "$chooseToOrder $floorPlanName"
        mFloorTableModel.refreshAllData()
        val txt = Translation.get(TextId.TEXT_FLOOR_PLAN) + "\n" + mFloorTableModel.floorPlanName()
        mBinding.btnFloorplan.text = txt
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonLanguage(view: View)
    {
        Log.i(tag, "onButtonLanguage")
        Translation.nextLanguage()
        refreshAllData()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonFloorPlanNext(view: View)
    {
        Log.i(tag, "onButtonFloorPlanNext")

        if (mFloorTableModel.nrFloorPlans() > 0)
        {
            mFloorTablesAdapter.clear()
            global.floorPlanId = (global.floorPlanId + 1) % mFloorTableModel.nrFloorPlans()
            refreshFloorPlan()
        } else
        {
            Log.i(tag, "onButtonFloorPlanNext  No floorPlans found!!")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonFloorPlanOnOff(view: View)
    {
        Log.i(tag, "onButtonFloorPlanOnOff")
        if (!isTablet)
        {
            isShowingFloorPlanOnPhone = !isShowingFloorPlanOnPhone
            updateViewVisibility()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonTakeaway(view: View)
    {
        Log.i(tag, "onButtonTakeaway")
        mTransactionModel.createTakeawayTransaction()
    }

    fun MainMenuActivity()
    {
        Log.i(tag, "Navigating to Main.")

        // Create an Intent to start the AskTransactionActivity
        val intent = Intent(this, MainMenuActivity::class.java)

        // Add flags to clear the task stack and start a new one.
        // This ensures the user cannot press "Back" to return to BillOrderActivity.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Start the new activity
        startActivity(intent)

        // Close the current BillOrderActivity so it's removed from the back stack
        finish()
    }

    private fun onExistingTransactionClicked()
    {
        Log.i(tag, "onExistingTransactionClicked")
        // Implement existing transaction logic
        finish()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onHeaderButton(view: View)
    {
        Log.i(tag, "onHeaderButton")
        mBill = !mBill
        refreshAllData()
    }

    private fun onAddDiscountClicked()
    {
        // @todo Implement split bill logic
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonBack(view: View)
    {
        Log.i(tag, "onButtonBack")
        MainMenuActivity()
    }

    private fun onChangeTableClicked()
    {
        Log.i(tag, "onChangeTableClicked")
        // @todo Implement split bill logic
    }

    private fun onPrintReceiptClicked()
    {
        Log.i(tag, "onPrintReceiptClicked")
        // @todo Implement split bill logic
    }

    private fun onSplitBillClicked()
    {
        // @todo Implement split bill logic
    }

    private fun updateViewVisibility()
    {
        if (isTablet)
        {
            // On a tablet, ALWAYS show both.
            mBinding.layoutFloorTables.visibility = View.VISIBLE
            mBinding.layoutShortTransactionList.visibility = View.VISIBLE
        } else
        {
            // On a phone, toggle between the two views.
            if (isShowingFloorPlanOnPhone)
            {
                mBinding.layoutFloorTables.visibility = View.VISIBLE
                mBinding.layoutShortTransactionList.visibility = View.GONE
            } else
            {
                mBinding.layoutFloorTables.visibility = View.GONE
                mBinding.layoutShortTransactionList.visibility = View.VISIBLE
            }
        }
    }

}
