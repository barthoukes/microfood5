package com.hha.dialog

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hha.adapter.BillItemsAdapter
import com.hha.adapter.PaymentsAdapter
import com.hha.callback.TransactionListener
import com.hha.callback.TransactionPaymentListener
import com.hha.framework.CItem
import com.hha.framework.CPayment
import com.hha.framework.CTransaction
import com.hha.messagebox.MessageBoxTextInput
import com.hha.resources.Global
import com.hha.types.ETaal
import com.hha.types.CMoney
import com.hha.types.EPaymentMethod
import com.hha.types.EPaymentStatus


import tech.hha.microfood.databinding.BillOrderActivityBinding

class BillOrderActivity : AppCompatActivity(), TransactionPaymentListener,
   TransactionListener, MessageBoxTextInput.OnTextEnteredListener
{

    val global = Global.getInstance()
    private val CFG = global.CFG

    // Views
    private lateinit var binding: BillOrderActivityBinding
    private lateinit var tableName: TextView
    private lateinit var totalPrice: TextView
    private lateinit var txtKitchenPrints: TextView
    private lateinit var billItemsRecyclerView: RecyclerView
    private lateinit var paymentsRecyclerView: RecyclerView
    private lateinit var billItemsAdapter: BillItemsAdapter
    private lateinit var paymentsAdapter: PaymentsAdapter
    private var m_alreadyPayed = false
    private var m_customerTotal = CMoney(0)
    private var m_cashTotal = CMoney(0)
    private var m_cardTotal = CMoney(0)
    private var m_kitchenPrints2bill = 1
    private var m_slipPrints = 1
    lateinit private var m_transaction: CTransaction

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = BillOrderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        initializeViews()
    }

    // Optional but clean: Create a helper function for initialization
    private fun initializeViews()
    {
        tableName = binding.tableName
        totalPrice = binding.totalBillPrice
        txtKitchenPrints = binding.txtKitchenPrints
        billItemsRecyclerView = binding.layoutBillingItems
        paymentsRecyclerView = binding.layoutBillingPayments
    }

    override fun onResume()
    {
        super.onResume()
        m_transaction = global.transaction ?: CTransaction(-1) // Or handle error
        // Register the adapter as a listener for changes in the transaction's item list.
        // This will allow the adapter to automatically update when an item is changed,
        // added, or removed.
        m_transaction.addListener(this)
        refreshAllData()
        // Also a good place to ensure the UI is fully updated when returning to the screen
    }

    override fun onPause()
    {
        super.onPause()
        // --- UNREGISTER LISTENER ---
        // Unregister the adapter to prevent memory leaks and stop receiving updates
        // when the activity is not in the foreground. The transaction object will outlive
        // this activity, so it's crucial to remove the reference to the adapter.
        if (::m_transaction.isInitialized) {
            m_transaction.removeListener(this)
        }
    }

    private fun setupRecyclerView()
    {
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        createGridLayoutBillingItems()
        createBillingItemsAdapter()
        createGridLayoutPayments()
        createPaymentsAdapter()

        // Initialize m_transaction here. It's better than doing it in onResume to avoid re-assignment.
        m_transaction = global.transaction!!
    }

    fun createGridLayoutPayments()
    {
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        val gridLayoutManagerTransactionItems = GridLayoutManager(
            this@BillOrderActivity,
            1, // Span count = number of columns (horizontal)
            GridLayoutManager.VERTICAL, // Vertical scrolling
            false
        )
        binding.layoutBillingPayments.layoutManager =
            gridLayoutManagerTransactionItems
    }

    fun createPaymentsAdapter()
    {
        // 2. Initialize adapter
        paymentsAdapter = PaymentsAdapter() { selectedPayment ->
            handlePayment(selectedPayment)
        }.apply {
            // Set dynamic height based on screen size
            binding.layoutBillingPayments.setItemViewCacheSize(18)
        }
        binding.layoutBillingPayments.adapter = paymentsAdapter
    }

    fun handlePayment(selectedPayment: CPayment)
    {
        TODO("Not yet implemented")
    }

    private fun createGridLayoutBillingItems()
    {
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        val gridLayoutManagerTransactionItems = GridLayoutManager(
            this@BillOrderActivity,
            1, // Span count = number of columns (horizontal)
            GridLayoutManager.VERTICAL, // Vertical scrolling
            false
        )
        binding.layoutBillingItems.layoutManager =
            gridLayoutManagerTransactionItems
    }

    fun createBillingItemsAdapter()
    {
        // 2. Initialize adapter
        billItemsAdapter = BillItemsAdapter() { selectedBillItem ->
            handleBillItemSelection(selectedBillItem)
        }.apply {
            // Set dynamic height based on screen size
            binding.layoutBillingItems.setItemViewCacheSize(18)
        }
        binding.layoutBillingItems.adapter = billItemsAdapter
    }

    private fun handleBillItemSelection(selectedBillItem: CItem)
    {
    }

    // Empty button functions to be implemented
    fun onButtonLanguage()
    {
        Translation.nextLanguage()
        refreshAllData()
    }

    private fun refreshPrints()
    {
        binding.btnBillPrints.text = "${m_slipPrints}x"
        binding.btnKitchenPrints.text = "${m_kitchenPrints2bill}x" // You likely want to update the kitchen prints too
    }

    private fun refreshAllData()
    {
        // Implement language switching logic
        binding.billingHeaderText.text = Translation.get(Translation.TextId.TEXT_BILL_HEADER)
        binding.btnQuickPayments.text = Translation.get(Translation.TextId.TEXT_BILL_PAYMENTS)
        binding.btnConfirmOrder.text = Translation.get(Translation.TextId.TEXT_PAY)
        binding.btnOrderMore.text = Translation.get(Translation.TextId.TEXT_MORE)
        binding.txtKitchenPrints.text = Translation.get(Translation.TextId.TEXT_PRINT_ROLL)
        binding.txtBillPrints.text = Translation.get(Translation.TextId.TEXT_PRINT_SLIP)
        refreshPrints()

    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonLanguage(view: View)
    {
        Translation.nextLanguage()
        refreshAllData()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonCancel(view: View)
    {
        TODO("Not yet implemented")
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonDiscount(view: View)
    {
        TODO("Not yet implemented")
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonEnter(view: View)
    {
        TODO("Not yet implemented")
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButton5Euro(view: View)
    {
        payEuroButton(CMoney(500))
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButton10Euro(view: View)
    {
        // Implement €10 payment logic
        payEuroButton(CMoney(1000))
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButton20Euro(view: View)
    {
        payEuroButton(CMoney(2000))
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButton50Euro(view: View)
    {
        payEuroButton(CMoney(5000))
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButton100Euro(view: View)
    {
        TODO("Not yet implemented")
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonCash(view: View)
    {
        TODO("Not yet implemented")
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonPin(view: View)
    {
        payAllUsingPin()
    }

    fun onButtonMessage(view: View)
    {
        val dialog = MessageBoxTextInput()
        dialog.listener = this // Set the activity to listen for the result
        dialog.show(supportFragmentManager, "MessageBoxTextInput")
    }

    private fun payEuroButton(amount: CMoney)
    {
        if (m_alreadyPayed)
        {
            //ERROR_SOUND();
        } else
        {
            if (m_customerTotal <= m_cashTotal + m_cardTotal || m_customerTotal < amount)
            {
                m_transaction.cancelPayment(-1, EPaymentStatus.PAY_STATUS_UNPAID);
            }
            m_transaction.addPayment(EPaymentMethod.PAYMENT_CASH, amount)
        }
    }

    public fun payAllUsingPin()
    {
        m_transaction.cancelPayment(-1, EPaymentStatus.PAY_STATUS_UNPAID)
        if (!m_customerTotal.empty())
        {
            m_transaction.addPayment(EPaymentMethod.PAYMENT_CASH, m_customerTotal)
        }
    }

    // Public methods to update data
    fun setTableName(name: String)
    {
        tableName.text = when (global.language)
        {
            ETaal.LANG_ENGLISH -> "Table $name"
            ETaal.LANG_SIMPLIFIED, ETaal.LANG_TRADITIONAL -> "桌子 $name"
            else -> "Tafel $name"
        }
    }

    fun setTotalPrice(price: String)
    {
        totalPrice.text = when (global.language)
        {
            ETaal.LANG_ENGLISH -> "Total: €$price"
            ETaal.LANG_TRADITIONAL, ETaal.LANG_SIMPLIFIED -> "总计: €$price"
            else -> "Totaal: €$price"
        }
    }

    fun setOrderItemsAdapter(adapter: RecyclerView.Adapter<*>)
    {
        billItemsRecyclerView.adapter = adapter
    }

    fun setPaymentsAdapter(adapter: RecyclerView.Adapter<*>)
    {
        paymentsRecyclerView.adapter = adapter
    }

    override fun onListChanged()
    {
        TODO("Not yet implemented")
    }

    override fun onItemAdded(position: Int, item: CItem)
    {
        TODO("Not yet implemented")
    }

    override fun onItemRemoved(position: Int, newSize: Int)
    {
        TODO("Not yet implemented")
    }

    override fun onItemUpdated(position: Int, item: CItem)
    {
        TODO("Not yet implemented")
    }

    override fun onTransactionCleared()
    {
        TODO("Not yet implemented")
    }

    override fun onTextEntered(text: String)
    {
        m_transaction.setMessage(text)
    }
}