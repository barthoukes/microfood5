package com.hha.dialog

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hha.adapter.BillItemsAdapter
import com.hha.adapter.PaymentsAdapter
import com.hha.framework.CItem
import com.hha.framework.CPayment
import com.hha.framework.CTransaction
import com.hha.resources.Global
import com.hha.types.ETaal
import com.hha.types.CMoney
import com.hha.types.EPaymentMethod
import com.hha.types.EPaymentStatus


import tech.hha.microfood.databinding.BillOrderActivityBinding

class BillOrderActivity : AppCompatActivity() {

    val global = Global.getInstance()
    private val CFG = global.CFG
    // Views
    private lateinit var binding: BillOrderActivityBinding
    private lateinit var tableName: TextView
    private lateinit var totalPrice: TextView
    private lateinit var paymentMethodsLabel: TextView
    private lateinit var paymentHistoryLabel: TextView
    private lateinit var txtKitchenPrints: TextView
    private lateinit var txtBillPrints: TextView
    private lateinit var btnDiscount: Button
    private lateinit var btnOrderMore: Button
    private lateinit var btnConfirmOrder: Button
    private lateinit var btnKitchen1: Button
    private lateinit var btnBillPrints: Button
    private lateinit var btnLanguage: ImageButton
    private lateinit var btnEuro5: ImageButton
    private lateinit var btnEuro10: ImageButton
    private lateinit var btnEuro20: ImageButton
    private lateinit var btnEuro50: ImageButton
    private lateinit var btnCash: ImageButton
    private lateinit var btnPin: ImageButton
    private lateinit var billItemsRecyclerView: RecyclerView
    private lateinit var paymentsRecyclerView: RecyclerView
    private lateinit var billItemsAdapter: BillItemsAdapter
    private lateinit var paymentsAdapter: PaymentsAdapter
    private var m_alreadyPayed = false
    private var m_customerTotal = CMoney(0)
    private var m_cashTotal = CMoney(0)
    private var m_cardTotal = CMoney(0)


    private lateinit var m_transaction: CTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BillOrderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        initializeViews()
    }



    // Optional but clean: Create a helper function for initialization
    private fun initializeViews() {
        tableName = binding.tableName
        totalPrice = binding.totalPrice
        //paymentMethodsLabel = binding.paymentMethodsLabel
        //paymentHistoryLabel = binding.paymentHistoryLabel
        txtKitchenPrints = binding.txtKitchenPrints
        txtBillPrints = binding.txtBillPrints
        btnDiscount = binding.btnDiscount
        btnOrderMore = binding.btnOrderMore
        btnConfirmOrder = binding.btnConfirmOrder
        btnKitchen1 = binding.btnKitchenPrints // Initialization happens here
        btnBillPrints = binding.btnBillPrints
        btnLanguage = binding.btnLanguage
        btnEuro5 = binding.btnEuro5
        btnEuro10 = binding.btnEuro10
        btnEuro20 = binding.btnEuro20
        btnEuro50 = binding.btnEuro50
        btnCash = binding.btnCash
        btnPin = binding.btnPin
        billItemsRecyclerView = binding.layoutBillingItems
        paymentsRecyclerView = binding.layoutBillingPayments
    }

    private fun setupRecyclerView() {
        // 1. GridLayoutManager for pages with 3 rows (vertical span) and horizontal scrolling
        createGridLayoutBillingItems()
        createBillingItemsAdapter()
        createGridLayoutPayments()
        createPaymentsAdapter()

//        override fun onResume() {
//        super.onResume()
//        // Refresh data when activity resumes
//        if (global.transaction == null) {
//            if (global.transactionId <1E6) assert(false)
//            transaction = CTransaction(global.transactionId)
//        }
        m_transaction = global.transaction!!
    }

    fun createGridLayoutPayments() {
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

    fun createPaymentsAdapter() {
        // 2. Initialize adapter
        paymentsAdapter = PaymentsAdapter() {
                selectedPayment -> handlePayment(selectedPayment)
        }.apply {
            // Set dynamic height based on screen size
            binding.layoutBillingPayments.setItemViewCacheSize(18)
        }
        binding.layoutBillingPayments.adapter = paymentsAdapter
    }

    fun handlePayment(selectedPayment: CPayment) {
        // todo
    }

    private fun createGridLayoutBillingItems() {
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

    fun createBillingItemsAdapter() {
        // 2. Initialize adapter
        billItemsAdapter = BillItemsAdapter() {
                selectedBillItem -> handleBillItemSelection(selectedBillItem)
        }.apply {
            // Set dynamic height based on screen size
            binding.layoutBillingItems.setItemViewCacheSize(18)
        }
        binding.layoutBillingItems.adapter = billItemsAdapter
    }

    private fun handleBillItemSelection(selectedBillItem: CItem) {
    }

    private fun refreshAllData() {
        billItemsAdapter.notifyDataSetChanged()
        paymentsAdapter.notifyDataSetChanged()
    }

    private fun updateTexts() {
        when (global.language) {
            ETaal.LANG_ENGLISH -> {
                tableName.text = "Table 5"
                totalPrice.text = "Total: €0.00"
                //paymentMethodsLabel.text = "Payment Methods:"
                //paymentHistoryLabel.text = "Payment History:"
                txtKitchenPrints.text = "Kitchen Prints:"
                txtBillPrints.text = "Bill Prints:"
                btnDiscount.text = "Discount"
                btnOrderMore.text = "ORDER MORE"
                btnConfirmOrder.text = "CONFIRM ORDER"
                btnKitchen1.text = "1x"
                btnBillPrints.text = "1x"
            }
            ETaal.LANG_SIMPLIFIED, ETaal.LANG_TRADITIONAL -> {
                tableName.text = "桌子 5"
                totalPrice.text = "总计: €0.00"
                //paymentMethodsLabel.text = "支付方式:"
                //paymentHistoryLabel.text = "支付记录:"
                txtKitchenPrints.text = "厨房打印:"
                txtBillPrints.text = "账单打印:"
                btnDiscount.text = "折扣"
                btnOrderMore.text = "继续点餐"
                btnConfirmOrder.text = "确认订单"
                btnKitchen1.text = "1次"
                btnBillPrints.text = "1次"
            }
            else -> {
                tableName.text = "Tafel 5"
                totalPrice.text = "Totaal: €0.00"
                //paymentMethodsLabel.text = "Betaalmethoden:"
                //paymentHistoryLabel.text = "Betalingsgeschiedenis:"
                txtKitchenPrints.text = "Keuken afdrukken:"
                txtBillPrints.text = "Rekening afdrukken:"
                btnDiscount.text = "Korting"
                btnOrderMore.text = "MEER BESTELLEN"
                btnConfirmOrder.text = "BEVESTIGEN"
                btnKitchen1.text = "1x"
                btnBillPrints.text = "1x"
            }
        }
    }

    private fun setButtonListeners() {
        btnLanguage.setOnClickListener { onLanguageButtonClicked() }
        btnDiscount.setOnClickListener { onDiscountClicked() }
        btnOrderMore.setOnClickListener { onOrderMoreClicked() }
        btnConfirmOrder.setOnClickListener { onConfirmOrderClicked() }
        btnKitchen1.setOnClickListener { onKitchenPrintClicked() }
        btnBillPrints.setOnClickListener { onBillPrintClicked() }
        btnEuro5.setOnClickListener { onEuro5Clicked() }
        btnEuro10.setOnClickListener { onEuro10Clicked() }
        btnEuro20.setOnClickListener { onEuro20Clicked() }
        btnEuro50.setOnClickListener { onEuro50Clicked() }
        btnCash.setOnClickListener { onCashClicked() }
        btnPin.setOnClickListener { onPinClicked() }
    }

    // Empty button functions to be implemented
    fun onLanguageButtonClicked() {
        // Implement language switching logic
    }

    private fun onDiscountClicked() {
        // Implement discount logic
    }

    private fun onOrderMoreClicked() {
        // Implement order more logic
    }

    private fun onConfirmOrderClicked() {
        // Implement confirm order logic
    }

    private fun onKitchenPrintClicked() {
        // Implement kitchen print logic
    }

    private fun onBillPrintClicked() {
        // Implement bill print logic
    }

    private fun payEuroButton(amount: CMoney)
    {
        if ( m_alreadyPayed)
        {
            //ERROR_SOUND();
        }
        else
        {
            if (m_customerTotal <= m_cashTotal + m_cardTotal || m_customerTotal < amount)
            {
                m_transaction.cancelPayment( -1, EPaymentStatus.PAY_STATUS_UNPAID);
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

    private fun onEuro5Clicked()
    {
        // Implement €5 payment logic
        payEuroButton(CMoney(500))
    }

    private fun onEuro10Clicked()
    {
        // Implement €10 payment logic
        payEuroButton(CMoney(1000))
    }

    private fun onEuro20Clicked()
    {
        // Implement €20 payment logic
        payEuroButton(CMoney(2000))
    }

    private fun onEuro50Clicked() {
        // Implement €50 payment logic
        payEuroButton(CMoney(5000))
    }

    private fun onCashClicked() {
        // Implement cash payment logic
    }

    private fun onPinClicked() {
        payAllUsingPin()
    }

    // Public methods to update data
    fun setTableName(name: String) {
        tableName.text = when (global.language) {
            ETaal.LANG_ENGLISH -> "Table $name"
            ETaal.LANG_SIMPLIFIED, ETaal.LANG_TRADITIONAL -> "桌子 $name"
            else -> "Tafel $name"
        }
    }

    fun setTotalPrice(price: String) {
        totalPrice.text = when (global.language) {
            ETaal.LANG_ENGLISH -> "Total: €$price"
            ETaal.LANG_TRADITIONAL, ETaal.LANG_SIMPLIFIED -> "总计: €$price"
            else -> "Totaal: €$price"
        }
    }

    fun setOrderItemsAdapter(adapter: RecyclerView.Adapter<*>) {
        billItemsRecyclerView.adapter = adapter
    }

    fun setPaymentsAdapter(adapter: RecyclerView.Adapter<*>) {
        paymentsRecyclerView.adapter = adapter
    }
}