package com.hha.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.hha.adapter.ShortTransactionListAdapter
import com.hha.framework.CFloorTable
import com.hha.framework.CMenuCards
import com.hha.model.FloorTableModel
import com.hha.framework.COpenClientsHandler.createNewTakeawayTransaction
import com.hha.framework.CShortTransaction
import com.hha.framework.CTransaction
import com.hha.resources.Global
import tech.hha.microfood.databinding.AskTransactionActivityBinding
import com.hha.dialog.Translation.TextId
import com.hha.floor.FloorTablesAdapter
import com.hha.model.TransactionModel
import com.hha.model.TransactionModelFactory
import tech.hha.microfood.R


class AskTransactionActivity : BaseActivity()
{
    private val global = Global.getInstance()
    private val CFG = global.CFG
    private lateinit var mBinding: AskTransactionActivityBinding

    private lateinit var mShortTransactionListAdapter: ShortTransactionListAdapter
    private lateinit var mFloorTablesAdapter: FloorTablesAdapter
    private lateinit var mFloorTableModel: FloorTableModel
    private lateinit var mTransaction: CTransaction
    private lateinit var mTransactionModel: TransactionModel
    private var mBill = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mBinding = AskTransactionActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        mTransactionModel = ViewModelProvider(this, TransactionModelFactory)
            .get(TransactionModel::class.java)
        //mTransactionModel = ViewModelProvider(this).get(TransactionModel::class.java)
        mFloorTableModel = ViewModelProvider(this).get(FloorTableModel::class.java)

        setupRecyclerView()

        mTransactionModel.activeTransaction
            .observe(this) { transaction ->
                if (transaction != null)
                {
                    global.transactionId = transaction.transactionId

                    val intent = if (mBill) {
                        Intent(this, BillOrderActivity::class.java)
                    } else {
                        Intent(this, PageOrderActivity::class.java)
                    }
                    startActivity(intent)

                    // IMPORTANT: Tell the ViewModel that navigation is complete
                    // to prevent re-navigating if the screen rotates.
                    mTransactionModel.onNavigationComplete()
                }
            }

        // 3. --- THIS IS THE MISSING PIECE ---
        //    Observe the LiveData from the ViewModel. This block of code will
        //    automatically run whenever the transaction list changes.
        mTransactionModel.shortTransactionList
            .observe(this)
            { transactions ->
                // The 'transactions' parameter is the new List<CShortTransaction>
                // We need a way to give this new list to the adapter.
                // Let's assume your adapter has a method called `submitList`.
                if (transactions != null)
                {
                    mShortTransactionListAdapter.submitList(transactions)
                }
            }

        mFloorTableModel.floorTables.observe(this) { floorTables ->
            if (floorTables != null)
            {
                mFloorTablesAdapter.submitList(floorTables)
            }
        }
    }

    private fun setupRecyclerView()
    {
        createGridLayoutFloorTables()
        createFloorTablesAdapter()

        createGridLayoutShortTransactionList()
        createShortTransactionsAdapter()
    }

// ... inside AskTransactionActivity class

    fun createGridLayoutFloorTables() {
        // 1. Get the screen width in pixels
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

    fun createFloorTablesAdapter()
    {
        // 2. Initialize adapter
        mFloorTablesAdapter = FloorTablesAdapter() { selectedFloorTable ->
            onFloorTableSelected(selectedFloorTable)
        }.apply {
            mBinding.layoutFloorTables.setItemViewCacheSize(64)
        }
        mBinding.layoutFloorTables.adapter = mFloorTablesAdapter
    }

    fun createGridLayoutShortTransactionList()
    {
        val gridLayoutShortTransactions = GridLayoutManager(
            this@AskTransactionActivity,
            1,
            GridLayoutManager.VERTICAL, // Horizontal scrolling
            false
        )
        mBinding.layoutShortTransactionList.layoutManager = gridLayoutShortTransactions
    }

    fun createShortTransactionsAdapter()
    {
        // 2. Initialize adapter
        mShortTransactionListAdapter = ShortTransactionListAdapter() { selectedShortTransaction ->
            onShortTransactionSelected(selectedShortTransaction)
        }.apply {
            mBinding.layoutShortTransactionList.setItemViewCacheSize(28)
        }
        mBinding.layoutShortTransactionList.adapter = mShortTransactionListAdapter
    }

    fun onShortTransactionSelected(selectedTransaction: CShortTransaction)
    {
        Log.i("AskTransaction", "onShortTransactionSelected ${selectedTransaction.name} ${selectedTransaction
            .transactionId}")

        if (mFloorTablesAdapter.getSelectedTransactionName() != selectedTransaction.name)
        {
            mFloorTablesAdapter.selectTransactionName(selectedTransaction.name)
            mShortTransactionListAdapter.selectTransactionId(selectedTransaction.transactionId)
        }
        else if (mBill)
        {
            // Change to billing activity for this transaction.
            mTransactionModel.selectTransaction(selectedTransaction.transactionId)

        }
        else
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
        Log.i("AskTransaction", "Floor table selected: ${selectedFloorTable.name}, current selected: ${mFloorTablesAdapter.getSelectedTransactionName()}")

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

        }
        else
        {
            Log.i("AskTransaction", "Second click detected. Creating NEW transaction for table: ${selectedFloorTable.name}")
            // Tell the ViewModel to create a new transaction for this table.
            // The observer for 'mFloorTableModel.navigateToTransaction' will handle navigation.
            val minutes = CFG.getValue("maximum_time")
            mTransactionModel.createTransactionForTable(
                selectedFloorTable.name, selectedFloorTable.tableId,
                minutes)
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
        super.onResume()
        refreshAllData()
    }

    private fun refreshAllData()
    {
        refreshShortTransaction()
        refreshFloorPlan()
    }

    private fun refreshShortTransaction()
    {
        mShortTransactionListAdapter.notifyDataSetChanged()
        mTransactionModel.refreshAllShortTransactions()
    }

    private fun refreshFloorPlan()
    {
        mFloorTablesAdapter.refreshAllData()
        mFloorTableModel.refreshAllData()
        val chooseToOrder = when(mBill) {
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
        Translation.nextLanguage()
        refreshAllData()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonFloorPlanNext(view: View)
    {
        mFloorTablesAdapter.clear()
        global.floorPlanId = (global.floorPlanId+1) % mFloorTableModel.nrFloorPlans()
        refreshFloorPlan()
     }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonTakeaway(view: View)
    {
        navigateToTakeaway()
    }

    private fun navigateToTakeaway()
    {
        // Todo new takeaway
        val useBag = true
        val user = Global.getInstance().rfidKeyId //currentKeyIndex
        val transactionId: Int =
            createNewTakeawayTransaction(
                0, useBag, user, false
            );

        if (transactionId <= 0)
        {
            return
        }
        Global.getInstance().transactionId = transactionId
        CMenuCards.getInstance().loadTakeaway()
        navigateToPageOrderActivity()
    }

    private fun navigateToPageOrderActivity()
    {
        startActivity(Intent(this, PageOrderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    fun MainMenuActivity()
    {
        Log.i("ASkTransactionActivity", "Navigating to Main.")

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
        // Implement existing transaction logic
        if (global.transactionId > 0)
        {
            mTransaction = CTransaction(global.transactionId)
        }
        finish()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onHeaderButton(view: View)
    {
        mBill = !mBill
        refreshAllData()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonBack(view: View)
    {
        MainMenuActivity()
    }

    private fun onSplitBillClicked()
    {
        // @todo Implement split bill logic
    }

    private fun onAddDiscountClicked()
    {
        // @todo Implement split bill logic
    }

    private fun onChangeTableClicked()
    {
        // @todo Implement split bill logic
    }

    private fun onPrintReceiptClicked()
    {
        // @todo Implement split bill logic
    }

    companion object
    {
        fun updateTransactionData(transaction: CTransaction)
        {
            // Update transaction data if needed
        }
    }
}
