package com.hha.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.hha.adapter.FloorTablesAdapter
import com.hha.adapter.ShortTransactionListAdapter
import com.hha.framework.CFloorTable
import com.hha.framework.CMenuCards
import com.hha.framework.COpenClientsHandler.createNewTakeawayTransaction
import com.hha.framework.CShortTransaction
import com.hha.framework.CTransaction
import com.hha.model.TransactionSelectionModel
import com.hha.resources.Global
import tech.hha.microfood.databinding.AskTransactionActivityBinding
import com.hha.dialog.Translation
import com.hha.dialog.Translation.TextId

class AskTransactionActivity : AppCompatActivity()
{

    private val global = Global.getInstance()
    private val CFG = global.CFG
    private lateinit var mBinding: AskTransactionActivityBinding

    private lateinit var mShortTransactionListAdapter: ShortTransactionListAdapter
    private lateinit var mFloorTablesAdapter: FloorTablesAdapter
    private lateinit var mTransaction: CTransaction
    private lateinit var mTransactionSelectionModel: TransactionSelectionModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mBinding = AskTransactionActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mTransactionSelectionModel = ViewModelProvider(this).get(TransactionSelectionModel::class.java)
        setupRecyclerView()

        // 3. --- THIS IS THE MISSING PIECE ---
        //    Observe the LiveData from the ViewModel. This block of code will
        //    automatically run whenever the transaction list changes.
        mTransactionSelectionModel.shortTransactionList.observe(this) { transactions ->
            // The 'transactions' parameter is the new List<CShortTransaction>
            // We need a way to give this new list to the adapter.
            // Let's assume your adapter has a method called `submitList`.
            if (transactions != null)
            {
                mShortTransactionListAdapter.submitList(transactions)
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

    fun createGridLayoutFloorTables()
    {
        val gridLayoutFloorTables = GridLayoutManager(
            this@AskTransactionActivity,
            4,
            GridLayoutManager.VERTICAL, // Horizontal scrolling
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

    fun onFloorTableSelected(selectedFloorTable: CFloorTable)
    {
        // todo
    }

    override fun onResume()
    {
        super.onResume()
        refreshAllData()
    }

    private fun refreshAllData()
    {
        mFloorTablesAdapter.notifyDataSetChanged()
        mShortTransactionListAdapter.notifyDataSetChanged()
        mTransactionSelectionModel.refreshAllData()
        mBinding.headerButton.text = Translation.get(TextId.TEXT_CHOOSE_TO_ORDER)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonLanguage(view: View)
    {
        Translation.nextLanguage()
        refreshAllData()
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
        val user = Global.getInstance().currentKeyIndex
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
        // finish()
    }

    fun onShortTransactionSelected(selectedTransaction: CShortTransaction)
    {
        // todo
        // 2x click = start to edit for transaction...
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
