package com.hha.dialog

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hha.adapter.FloorTablesAdapter
import com.hha.adapter.TransactionListAdapter
import com.hha.framework.CFloorTable
import com.hha.framework.CItem
import com.hha.framework.CPayment
import com.hha.framework.CTransaction
import com.hha.resources.Global
import com.hha.types.ETaal
import tech.hha.microfood.databinding.AskTransactionActivityBinding
import tech.hha.microfood.databinding.BillOrderActivityBinding

class AskTransactionActivity : AppCompatActivity()
{

    private val global = Global.getInstance()
    private val CFG = global.CFG
    private lateinit var binding: AskTransactionActivityBinding

    private lateinit var transactionListAdapter: TransactionListAdapter
    private lateinit var floorTablesAdapter: FloorTablesAdapter
    private lateinit var transaction: CTransaction

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = AskTransactionActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
    }

    private fun setupRecyclerView()
    {
        createGridLayoutFloorTables()
        createFloorTablesAdapter()
        createGridLayoutTransactionList()
        createTransactionsAdapter()
    }

    fun createGridLayoutFloorTables()
    {
        val gridLayoutFloorTables = GridLayoutManager(
            this@AskTransactionActivity,
            4,
            GridLayoutManager.VERTICAL, // Horizontal scrolling
            false
        )
        binding.layoutFloorTables.layoutManager = gridLayoutFloorTables
    }

    fun createFloorTablesAdapter()
    {
        // 2. Initialize adapter
        floorTablesAdapter = FloorTablesAdapter() { selectedFloorTable ->
            onFloorTableSelected(selectedFloorTable)
        }.apply {
            binding.layoutFloorTables.setItemViewCacheSize(28)
        }
        binding.layoutFloorTables.adapter = floorTablesAdapter
    }

    fun createGridLayoutTransactionList()
    {
        val gridLayoutTransactions = GridLayoutManager(
            this@AskTransactionActivity,
            1,
            GridLayoutManager.VERTICAL, // Horizontal scrolling
            false
        )
        binding.layoutTransactionList.layoutManager = gridLayoutTransactions
    }

    fun createTransactionsAdapter()
    {
        // 2. Initialize adapter
        transactionListAdapter = TransactionListAdapter() { selectedTransaction ->
            onTransactionSelected(selectedTransaction)
        }.apply {
            binding.layoutFloorTables.setItemViewCacheSize(28)
        }
        binding.layoutFloorTables.adapter = floorTablesAdapter
    }

    private fun refreshAllData()
    {
        floorTablesAdapter.notifyDataSetChanged()
        transactionListAdapter.notifyDataSetChanged()
    }


    fun onFloorTableSelected(selectedFloorTable: CFloorTable)
    {
        // todo
    }

    fun onTransactionSelected(selectedTransaction: CTransaction)
    {
        // todo
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
            transaction = CTransaction(global.transactionId)
        }
        finish()
    }

    private fun onCancelClicked()
    {
        finish()
    }

    private fun onSplitBillClicked()
    {
        // Implement split bill logic
    }

    private fun onAddDiscountClicked()
    {
        // Implement add discount logic
    }

    private fun onChangeTableClicked()
    {
        // Implement change table logic
    }

    private fun onPrintReceiptClicked()
    {
        // Implement print receipt logic
    }

    companion object
    {
        fun updateTransactionData(transaction: CTransaction)
        {
            // Update transaction data if needed
        }
    }
}
