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
import com.hha.framework.CShortTransaction
import com.hha.framework.CTransaction
import com.hha.model.ShortTransactionViewModel
import com.hha.resources.Global
import com.hha.types.ETaal
import tech.hha.microfood.databinding.AskTransactionActivityBinding

class AskTransactionActivity : AppCompatActivity()
{

    private val global = Global.getInstance()
    private val CFG = global.CFG
    private lateinit var mBinding: AskTransactionActivityBinding

    private lateinit var mShortTransactionListAdapter: ShortTransactionListAdapter
    private lateinit var mFloorTablesAdapter: FloorTablesAdapter
    private lateinit var mTransaction: CTransaction
    private lateinit var mViewModel: ShortTransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mBinding = AskTransactionActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mViewModel = ViewModelProvider(this).get(ShortTransactionViewModel::class.java)
        setupRecyclerView()

        // 3. --- THIS IS THE MISSING PIECE ---
        //    Observe the LiveData from the ViewModel. This block of code will
        //    automatically run whenever the transaction list changes.
        mViewModel.shortTransactionList.observe(this) { transactions ->
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
        mViewModel.refreshAllData()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonLanguage(view: View)
    {
        Translation.nextLanguage()
        refreshAllData()
    }

    fun onShortTransactionSelected(selectedTransaction: CShortTransaction)
    {
        // todo
        // 2x click = start to edit for transaction...
    }

    private fun updateTexts()
    {
//        when (global.language) {
//            ETaal.LANG_ENGLISH -> {
//                titleLabel.text = "Transaction Options"
//                descriptionLabel.text = "Select an option to continue:"
//                btnNewTransaction.text = "NEW TRANSACTION"
//                btnExistingTransaction.text = "EXISTING TRANSACTION"
//                btnCancel.text = "CANCEL"
//            }
//            ETaal.LANG_SIMPLIFIED, ETaal.LANG_TRADITIONAL -> {
//                titleLabel.text = "交易选项"
//                descriptionLabel.text = "请选择一个选项继续:"
//                btnNewTransaction.text = "新交易"
//                btnExistingTransaction.text = "现有交易"
//                btnCancel.text = "取消"
//            }
//            else -> {
//                titleLabel.text = "Transactie Opties"
//                descriptionLabel.text = "Selecteer een optie om door te gaan:"
//                btnNewTransaction.text = "NIEUWE TRANSACTIE"
//                btnExistingTransaction.text = "BESTAANDE TRANSACTIE"
//                btnCancel.text = "ANNULEREN"
//            }
//         }

        // Update adapter data if needed
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

    private fun getTransactionOptions(): List<String>
    {
        return when (global.language)
        {
            ETaal.LANG_ENGLISH -> listOf("Split Bill", "Add Discount", "Change Table", "Print Receipt")
            ETaal.LANG_SIMPLIFIED, ETaal.LANG_TRADITIONAL -> listOf("分单", "添加折扣", "换桌", "打印收据")
            else -> listOf("Rekening splitsen", "Korting toevoegen", "Tafel veranderen", "Bon afdrukken")
        }
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
