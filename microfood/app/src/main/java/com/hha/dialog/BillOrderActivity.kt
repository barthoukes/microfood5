package com.hha.dialog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast

import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hha.adapter.BillItemsAdapter
import com.hha.adapter.PaymentsAdapter
import com.hha.callback.PaymentEnteredListener
import com.hha.callback.TransactionListener
import com.hha.framework.CItem
import com.hha.framework.CPayment
import com.hha.framework.CTransaction
import com.hha.messagebox.MessageBoxPayment
import com.hha.messagebox.MessageBoxTextInput
import com.hha.model.BillDisplayLine
import com.hha.model.TransactionViewModel
import com.hha.model.TransactionViewModelFactory
import com.hha.resources.Configuration
import com.hha.resources.Global
import com.hha.types.ETaal
import com.hha.types.CMoney
import com.hha.types.EPaymentMethod

import tech.hha.microfood.databinding.BillOrderActivityBinding

class BillOrderActivity : AppCompatActivity(),
    MessageBoxTextInput.OnTextEnteredListener,
    PaymentEnteredListener, TransactionListener
{
    val global = Global.getInstance()

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
    private val CFG: Configuration = global.CFG
    private val colourCFG = global.colourCFG
    private val m_colourOdd = colourCFG.getBackgroundColour("COLOUR_BILL_AMOUNT_BACKGROUND1")
    private val m_colourEven = colourCFG.getBackgroundColour("COLOUR_BILL_AMOUNT_BACKGROUND2")
    private val m_colourPayOdd = colourCFG.getBackgroundColour("COLOUR_BILL_TOTALS_BACKGROUND1")
    private val m_colourPayEven = colourCFG.getBackgroundColour("COLOUR_BILL_TOTALS_BACKGROUND2")
    private val m_frontText = colourCFG.getBackgroundColour("COLOUR_BILL_TOTALS_TEXT1")
    private val m_payText = colourCFG.getBackgroundColour("COLOUR_BILL_AMOUNT_TEXT1")

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = BillOrderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        m_viewModel = ViewModelProvider(this, TransactionViewModelFactory)
            .get(TransactionViewModel::class.java)

        setupRecyclerView()
        initializeViews()


        m_viewModel.transaction.observe(this) { transaction ->
            // This block will run automatically when the activity starts
            // and any time the transaction data changes.

            if (transaction != null)
            {
                // --- THIS IS WHERE YOU CALL updateData ---
                m_billItemsAdapter.updateData(transaction)

                transaction.calculateTotalTransaction()
                binding.totalBillPrice.text = transaction.getTotalTransaction().toString()
            }
        }
        // OBSERVER 2: For the payments list (bottom RecyclerView)
        m_viewModel.billDisplayLines.observe(this) { paymentLines ->
            // This block runs when the list of payment lines is ready.
            m_paymentsAdapter.submitList(paymentLines)
        }

        m_viewModel.initializeTransaction(TransactionViewModel.InitMode.VIEW_BILLING)
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
        m_paymentsAdapter = PaymentsAdapter(
            { selectedPayment -> handlePayment(selectedPayment) },
            m_colourOdd,
            m_colourEven,
            m_colourPayOdd,
            m_colourPayEven,
            m_frontText,
            m_payText
        ).apply {
            binding.layoutBillingPayments.setItemViewCacheSize(18)
        }
        binding.layoutBillingPayments.adapter = m_paymentsAdapter
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
        m_viewModel.refreshAllData()
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

    @Suppress("UNUSED_PARAMETER")
    fun onButtonMessage(view: View)
    {
        val dialog = MessageBoxTextInput()
        dialog.listener = this // Set the activity to listen for the result
        dialog.show(supportFragmentManager, "MessageBoxTextInput")
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonMore(view: View)
    {
        // Tell the ViewModel to switch its mode back to ordering.
        // This is important for when PageOrderActivity resumes.
        m_viewModel.setMode(TransactionViewModel.InitMode.VIEW_PAGE_ORDER)

        // Finish the current activity (BillOrderActivity).
        // This will automatically return the user to the previous activity (PageOrderActivity).
        finish()
    }

    // This is the new method you must implement
    override fun onPaymentEntered(
        paymentMethod: EPaymentMethod,
        amount: CMoney,
    )
    {
        m_viewModel.addPayment(paymentMethod, amount)
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

// ... inside the BillOrderActivity class

    // Add this entire function. The system will now find this method when the button is clicked.
    @Suppress("UNUSED_PARAMETER")
    fun onButtonConfirmBill(view: View)
    {
        // 1. Get the remaining amount that needs to be paid from the ViewModel.
        val requiredAmount = m_viewModel.getRequiredAdditionalPayment()

        // 2. Check if payment is actually needed.
        if (requiredAmount > CMoney(0))
        {
            // 3. If so, create and show your new MessageBoxPayment dialog.
            val dialog = MessageBoxPayment.newInstance(requiredAmount)
            dialog.listener = this // 'this' is BillOrderActivity, which implements the listener
            dialog.show(supportFragmentManager, "MessageBoxPayment")
        } else
        {
            // 4. If the bill is already paid (or has change), you can proceed to print.
            Toast.makeText(this, "Bill is already fully paid. Printing...", Toast.LENGTH_SHORT).show()
            m_viewModel.onPrintBill()
        }
    }

    override fun onTransactionChanged(transaction: CTransaction)
    {
       // binding.totalBillPrice.text = transaction.getTotalTransaction().toString()
    }

}