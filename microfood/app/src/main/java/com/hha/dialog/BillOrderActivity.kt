package com.hha.dialog

import android.os.Bundle
import android.view.View
import android.widget.TextView

import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hha.adapter.BillItemsAdapter
import com.hha.adapter.PaymentsAdapter
import com.hha.framework.CItem
import com.hha.framework.CPayment
import com.hha.messagebox.MessageBoxTextInput
import com.hha.model.BillDisplayLine
import com.hha.model.TransactionViewModel
import com.hha.resources.Global
import com.hha.types.ETaal
import com.hha.types.CMoney

import tech.hha.microfood.databinding.BillOrderActivityBinding

class BillOrderActivity : AppCompatActivity(),
    MessageBoxTextInput.OnTextEnteredListener
{

    val global = Global.getInstance()
    private val CFG = global.CFG

    // Views
    private lateinit var binding: BillOrderActivityBinding
    private lateinit var tableName: TextView
    private lateinit var totalPrice: TextView
    private lateinit var txtKitchenPrints: TextView
    private lateinit var m_billItemsAdapter: BillItemsAdapter
    private lateinit var m_paymentsAdapter: PaymentsAdapter
    private lateinit var m_viewModel: TransactionViewModel
    private lateinit var billItemsRecyclerView: RecyclerView
    private lateinit var paymentsRecyclerView: RecyclerView
    private var m_kitchenPrints2bill = 1
    private var m_slipPrints = 1

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = BillOrderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        m_viewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        setupRecyclerView()
        initializeViews()
        observeViewModel()
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
        m_viewModel.prepareBillDisplayLines()
        // Register the adapter as a listener for changes in the transaction's item list.
        // This will allow the adapter to automatically update when an item is changed,
        // added, or removed.
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
    }

    private fun setupRecyclerView()
    {
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        createGridLayoutBillingItems()
        createBillingItemsAdapter()
        createGridLayoutPayments()
        createPaymentsAdapter()
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
        // --- FIX 3b: Assign to the class property 'm_paymentsAdapter' ---
        m_paymentsAdapter = PaymentsAdapter() { selectedPayment ->
            handlePayment(selectedPayment)
        }.apply {
            binding.layoutBillingPayments.setItemViewCacheSize(18)
        }
        binding.layoutBillingPayments.adapter = m_paymentsAdapter
    }

    private fun observeViewModel() {
        m_viewModel.displayLines.observe(this) { billDisplayLines ->
            if (billDisplayLines != null)
            {
                m_paymentsAdapter.submitList(billDisplayLines)
            }
        }
       // m_viewModel.displayLines.observe(this) { billDisplayLines ->
    }

    fun handlePayment(selectedPayment: BillDisplayLine)
    {
        //TODO("Not yet implemented")
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
        // --- FIX 3a: Assign to the class property 'm_billItemsAdapter' ---
        m_billItemsAdapter = BillItemsAdapter() { selectedBillItem ->
            handleBillItemSelection(selectedBillItem)
        }.apply {
            binding.layoutBillingItems.setItemViewCacheSize(18)
        }
        binding.layoutBillingItems.adapter = m_billItemsAdapter
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
        payEuroButton(CMoney(10000))
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
        m_viewModel.payEuros(amount)
    }

    public fun payAllUsingPin()
    {
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

    override fun onTextEntered(text: String)
    {
        m_viewModel.setMessage(text)
    }
}