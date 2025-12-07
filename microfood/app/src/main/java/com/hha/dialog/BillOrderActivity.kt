package com.hha.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast

import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hha.adapter.BillItemsAdapter
import com.hha.adapter.PaymentsAdapter
import com.hha.callback.PaymentEnteredListener
import com.hha.callback.TransactionListener
import com.hha.framework.CItem
import com.hha.framework.CPaymentTransaction
import com.hha.framework.CTransaction
import com.hha.modalDialog.ModalDialogPayment
import com.hha.modalDialog.ModalDialogTextInput
import com.hha.model.BillDisplayLine
import com.hha.model.TransactionViewModel
import com.hha.model.TransactionViewModelFactory
import com.hha.resources.Configuration
import com.hha.resources.Global
import com.hha.types.ETaal
import com.hha.types.CMoney
import com.hha.types.EPaymentMethod
import com.hha.modalDialog.ModalDialogYesNo
import com.hha.printer.TakeawayNumber
import com.hha.resources.CTimestamp
import com.hha.types.EClientOrdersType
import com.hha.types.EInitAction
import com.hha.types.EPayingMode
import com.hha.types.EPaymentStatus
import com.hha.types.EPrintBillAction
import com.hha.types.ETransType
import tech.hha.microfood.databinding.BillOrderActivityBinding

class BillOrderActivity : AppCompatActivity(),
    ModalDialogTextInput.OnTextEnteredListener,
    PaymentEnteredListener, TransactionListener,
    ModalDialogYesNo.MessageBoxYesNoListener
{
    val global = Global.getInstance()
    val userCFG = global.userCFG

    // Views
    private lateinit var mBinding: BillOrderActivityBinding
    private lateinit var mBillItemsAdapter: BillItemsAdapter
    private lateinit var mPaymentsAdapter: PaymentsAdapter
    private lateinit var mViewModel: TransactionViewModel
    private var mSlipPrints = 1

    private var mOffer = false
    private var mAllowNewItem = true
    private val CFG: Configuration = global.CFG
    private val colourCFG = global.colourCFG
    private val mColourOdd = colourCFG.getBackgroundColour("COLOUR_BILL_AMOUNT_BACKGROUND1")
    private val mColourEven = colourCFG.getBackgroundColour("COLOUR_BILL_AMOUNT_BACKGROUND2")
    private val mColourPayOdd = colourCFG.getBackgroundColour("COLOUR_BILL_TOTALS_BACKGROUND1")
    private val mColourPayEven = colourCFG.getBackgroundColour("COLOUR_BILL_TOTALS_BACKGROUND2")
    private val mFrontText = colourCFG.getBackgroundColour("COLOUR_BILL_TOTALS_TEXT1")
    private val mPayText = colourCFG.getBackgroundColour("COLOUR_BILL_AMOUNT_TEXT1")
    private val mAskBillConfirmation = CFG.getBoolean("bill_ask_confirmation")
    private val mContinueWhenAmountEnough = CFG.getBoolean("bill_continue_when_enough")
    private var mPaidTotal = CMoney(0)

    private var mCustomerTotal = CMoney(0)
    private val mUserPrintCollect = userCFG.getBoolean("user_print_collect")

    private val CLEAN_TABLE = 123
    private val CONFIRM_BILL = 456

    fun onInit()
    {
        val action = mViewModel.onInit()
        when (action)
        {
            EInitAction.INIT_ACTION_BILL_PAYMENTS -> finish() // @todo
            EInitAction.INIT_ACTION_FINISH -> finish()
            EInitAction.INIT_ACTION_ASK_MONEY -> { askMoneyType()
                finish() }
            else -> {}
        }

        if (mViewModel.discountVisible())
        {
            mBinding.btnDiscount.visibility = View.VISIBLE
        } else
        {
            mBinding.btnDiscount.visibility = View.GONE
        }
    }

    fun askTransactionActivity()
    {
        Log.i("BillOrderActivity", "Bill processed. Navigating to AskTransactionActivity.")

        // Create an Intent to start the AskTransactionActivity
        val intent = Intent(this, AskTransactionActivity::class.java)

        // Add flags to clear the task stack and start a new one.
        // This ensures the user cannot press "Back" to return to BillOrderActivity.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Start the new activity
        startActivity(intent)

        // Close the current BillOrderActivity so it's removed from the back stack
        finish()
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
        mBinding.layoutBillingItems.layoutManager =
            gridLayoutManagerTransactionItems
    }

    // Optional but clean: Create a helper function for initialization
    private fun initializeViews()
    {
    }

    override fun onResume()
    {
        super.onResume()
        onInit()
    }

    /*----------------------------------------------------------------------------*/
    /** @brief Ask what type of payment, cash,pin,credit card */
    fun askMoneyType()
    {
        //TODO("Not yet implemented")

//        m_mainGraph->darken(0,0,0,0);
//        CmessageBoxPaymentType cmb;
//        if ( cmb.getPaymentType() !=PAYMENT_INVALID)
//        {
//            IclientOrdersHandlerPtr clientOrdersHandler(m_transactionItemModel->getClientOrdersHandler());
//
//            // valid bill.
//            m_rfidKey = personnel.waitForKey( _NEW_KEY, true);
//            if ( !m_rfidKey.get() || !m_rfidKey->valid)
//            {
//                CmessageBox cm( "mbx::print_bill_not_valid", BTN9+2, BTN1-2, BTN3+2, BTN1+2, getTranslation(_KEY_BAD), MB_BEEP
//                | MB_TIME3 | MB_TEXT_CENTER);
//                stop( MODE_ASK_TABLE_BILL_DIALOG);
//                return;
//            }
//            if ( CFG("waiter_per_table")!=0
//                && clientOrdersHandler->getEmployeeId() !=m_rfidKey->person_id)
//            {
//                // Table has different waiter !
//                char t[256];
//                sprintf(t, "%s %d", getTranslation(_EMPLOYEE).c_str(), clientOrdersHandler->getEmployeeId());
//                CmessageBox cm( "mbx::view_billing", BTN1-2, BTN1/2, BTN2+1, BTN1, t, MB_TIME1 | MB_BEEP | MB_TEXT_CENTER);
//                stop( MODE_ASK_TABLE_BILL_DIALOG);
//                return;
//            }
//            if ( m_automatic_payments )
//            {
//                // Replace by manual payment.
//                m_automatic_payments =false;
//                clientOrdersHandler->deletePayments(-1, true);
//            }
//            clientOrdersHandler->addPayment( cmb.getPaymentType(),
//            clientOrdersHandler->getItemTotal()
//            -clientOrdersHandler->getDiscount()
//            +clientOrdersHandler->getTipsTotal());
//            clientOrdersHandler->setEmployeeId( m_rfidKey->person_id);
//            clientOrdersHandler->addReturnMoney();
//            clientOrdersHandler->addToEmployee( m_rfidKey, PAY_STATUS_UNPAID);
//            clientOrdersHandler->closeTransaction( CLIENT_ORDER_CLOSED);
//        }
//        if ( CFG("bill_finish_show_floorplan"))
//        {
//            stop( MODE_ASK_TABLE_MAP_DIALOG );
//        }
//        else
//        {
//            stop( MODE_ASK_TABLE_BILL_DIALOG);
//        }
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
        mBinding.layoutBillingPayments.layoutManager =
            gridLayoutManagerTransactionItems
    }

    fun createPaymentsAdapter()
    {
        // --- FIX 3b: Assign to the class property 'm_paymentsAdapter' ---
        mPaymentsAdapter = PaymentsAdapter(
            { selectedPayment -> handlePayment(selectedPayment) },
            mColourOdd,
            mColourEven,
            mColourPayOdd,
            mColourPayEven,
            mFrontText,
            mPayText
        ).apply {
            mBinding.layoutBillingPayments.setItemViewCacheSize(18)
        }
        mBinding.layoutBillingPayments.adapter = mPaymentsAdapter
    }

    fun handlePayment(selectedPayment: BillDisplayLine)
    {
        //TODO("Not yet implemented")
    }

    fun createBillingItemsAdapter()
    {
        // --- FIX 3a: Assign to the class property 'm_billItemsAdapter' ---
        mBillItemsAdapter = BillItemsAdapter() { selectedBillItem ->
            handleBillItemSelection(selectedBillItem)
        }.apply {
            mBinding.layoutBillingItems.setItemViewCacheSize(18)
        }
        mBinding.layoutBillingItems.adapter = mBillItemsAdapter
    }

    private fun handleBillItemSelection(selectedBillItem: CItem)
    {
    }

//    // Empty button functions to be implemented
//    fun onButtonLanguage()
//    {
//        Translation.nextLanguage()
//        refreshAllData()
//        mBillItemsAdapter.notifyDataSetChanged()
//    }

    private fun refreshPrints()
    {
        mBinding.btnBillPrints.text = "${global.mKitchenPrints2bill}x"
        mBinding.btnKitchenPrints.text = "${global.mKitchenPrints}x"
    }

    private fun refreshAllData()
    {
        // Implement language switching logic
        mBinding.billingHeaderText.text = Translation.get(Translation.TextId.TEXT_BILL_HEADER)
        mBinding.btnQuickPayments.text = Translation.get(Translation.TextId.TEXT_BILL_PAYMENTS)
        mBinding.btnConfirmOrder.text = Translation.get(Translation.TextId.TEXT_PAY)
        mBinding.btnOrderMore.text = Translation.get(Translation.TextId.TEXT_MORE)
        mBinding.txtKitchenPrints.text = Translation.get(Translation.TextId.TEXT_PRINT_ROLL)
        mBinding.txtBillPrints.text = Translation.get(Translation.TextId.TEXT_PRINT_SLIP)
        refreshPrints()
        mViewModel.refreshAllData()
        mBillItemsAdapter.notifyDataSetChanged()
        mBinding.tableName.text = mViewModel.getTableName()
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
        mViewModel.payEuroButton(CMoney(500))
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButton10Euro(view: View)
    {
        // Implement €10 payment logic
        mViewModel.payEuroButton(CMoney(1000))
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButton20Euro(view: View)
    {
        mViewModel.payEuroButton(CMoney(2000))
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButton50Euro(view: View)
    {
        mViewModel.payEuroButton(CMoney(5000))
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButton100Euro(view: View)
    {
        mViewModel.payEuroButton(CMoney(10000))
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonCash(view: View)
    {
        payAllUsingCash()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonPin(view: View)
    {
        payAllUsingPin()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonMessage(view: View)
    {
        val dialog = ModalDialogTextInput()
        dialog.listener = this // Set the activity to listen for the result
        dialog.show(supportFragmentManager, "MessageBoxTextInput")
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonMore(view: View)
    {
        // Tell the ViewModel to switch its mode back to ordering.
        // This is important for when PageOrderActivity resumes.
        mViewModel.setMode(TransactionViewModel.InitMode.VIEW_PAGE_ORDER)

        // Finish the current activity (BillOrderActivity).
        // This will automatically return the user to the previous activity (PageOrderActivity).
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mBinding = BillOrderActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mViewModel = ViewModelProvider(this, TransactionViewModelFactory)
            .get(TransactionViewModel::class.java)

        setupRecyclerView()
        initializeViews()


        mViewModel.transaction.observe(this) { transaction ->
            // This block will run automatically when the activity starts
            // and any time the transaction data changes.

            if (transaction != null)
            {
                // --- THIS IS WHERE YOU CALL updateData ---
                mBillItemsAdapter.updateData(transaction)

                transaction.calculateTotalTransaction()
                mBinding.totalBillPrice.text = transaction.getTotalTransaction().toString()
            }
        }
        // OBSERVER 2: For the payments list (bottom RecyclerView)
        mViewModel.billDisplayLines.observe(this) { paymentLines ->
            // This block runs when the list of payment lines is ready.
            mPaymentsAdapter.submitList(paymentLines)
        }

        mViewModel.initializeTransaction(TransactionViewModel.InitMode.VIEW_BILLING)
    }

    // This is the new method you must implement
    override fun onPaymentEntered(
        paymentMethod: EPaymentMethod,
        amount: CMoney,
    )
    {
        mViewModel.addPayment(paymentMethod, amount)
    }

    public fun payAllUsingPin()
    {
        mViewModel.payAllUsingPin()
        if (mContinueWhenAmountEnough)
        {
            onPrintBill(false);
        }
    }

    public fun payAllUsingCash()
    {
        mViewModel.payAllUsingCash()
        if (mContinueWhenAmountEnough)
        {
            onPrintBill(false);
        }
    }

    // Public methods to update data
    fun setTableName(name: String)
    {
        mBinding.tableName.text = when (global.language)
        {
            ETaal.LANG_ENGLISH -> "Table $name"
            ETaal.LANG_SIMPLIFIED, ETaal.LANG_TRADITIONAL -> "桌子 $name"
            else -> "Tafel $name"
        }
    }

    fun setTotalPrice(price: String)
    {
        mBinding.totalBillPrice.text = when (global.language)
        {
            ETaal.LANG_ENGLISH -> "Total: €$price"
            ETaal.LANG_TRADITIONAL, ETaal.LANG_SIMPLIFIED -> "总计: €$price"
            else -> "Totaal: €$price"
        }
    }

    fun setOrderItemsAdapter(adapter: RecyclerView.Adapter<*>)
    {
        mBinding.layoutBillingItems.adapter = adapter
    }

    fun setPaymentsAdapter(adapter: RecyclerView.Adapter<*>)
    {
        mBinding.layoutBillingPayments.adapter = adapter
    }

// ... inside the BillOrderActivity class

    // Add this entire function. The system will now find this method when the button is clicked.
    @Suppress("UNUSED_PARAMETER")
    fun onButtonConfirmBill(view: View)
    {
        // 1. Get the remaining amount that needs to be paid from the ViewModel.
        val requiredAmount = mViewModel.getRequiredAdditionalPayment()

        // 2. Check if payment is actually needed.
        if (requiredAmount > CMoney(0))
        {
            // 3. If so, create and show your new MessageBoxPayment dialog.
            val dialog = ModalDialogPayment.newInstance(requiredAmount)
            dialog.listener = this // 'this' is BillOrderActivity, which implements the listener
            dialog.show(supportFragmentManager, "MessageBoxPayment")
        } else
        {
            // 4. If the bill is already paid (or has change), you can proceed to print.
            //Toast.makeText(this, "Bill is already fully paid. Printing...", Toast.LENGTH_SHORT).show()
            onPrintBill(false)
        }
    }

    // YesNo dialog, press NO/CANCEL
    override fun onDialogNegativeClick(dialog: DialogFragment, requestCode: Int)
    {
        when (requestCode)
        {
            CLEAN_TABLE,
            CONFIRM_BILL -> cancelPaymentsAskOtherTable()
            else -> {}
        }
    }

    // YesNo dialog, press OK/CONFIRM
    override fun onDialogPositiveClick(dialog: DialogFragment, requestCode: Int)
    {
        when (requestCode)
        {
            CLEAN_TABLE, CONFIRM_BILL -> confirmPrintBill()
            else -> {}
        }
    }

    override fun onTextEntered(text: String)
    {
        mViewModel.setMessage(text)
    }

    override fun onTransactionChanged(transaction: CTransaction)
    {
        // binding.totalBillPrice.text = transaction.getTotalTransaction().toString()
    }

    /**
     * Creates and displays the Yes/No confirmation dialog.
     * @param title The title to show on the dialog.
     * @param message The message to show in the dialog body.
     */
    private fun showConfirmationDialog(title: String, message: String)
    {
        // Use the factory method from your DialogFragment
        val dialog = ModalDialogYesNo.newInstance(title, message, 0)

        // Show the dialog using the FragmentManager.
        // The "tag" is a unique string name for the dialog instance.
        dialog.show(supportFragmentManager, "ConfirmationDialog")
    }

    /*----------------------------------------------------------------------------*/
    /** Check if we can print a bill, also ask whether it is allowed
     *  @return true when
     */
    fun isPrintBillAllowedAndConfirmed(
        currentTransaction: CTransaction, offer: Boolean
    )
    {
        val action = mViewModel.isPrintBillAllowedAndConfirmed(
            currentTransaction, offer, mCustomerTotal
        )
        runAction(currentTransaction, offer, action)
    }

    fun runAction(
        currentTransaction: CTransaction, offer: Boolean, action: EPrintBillAction)
    {
        when(action)
        {
            EPrintBillAction.NAVIGATE_TO_ASK_TRANSACTION -> askTransactionActivity()
            EPrintBillAction.ASK_WOK_CONFIRM_CLEAN_TABLE -> {
                // 627 wok
                val title = Translation.get(Translation.TextId.TEXT_CLEAN_TABLE)
                // Ask to confirm the bill printing
                val dlg = ModalDialogYesNo.newInstance(title, "", CLEAN_TABLE)
                dlg.show(supportFragmentManager, "ConfirmationDialog") }
            EPrintBillAction.ASK_CONFIRM_PRINT -> askConfirmationBill(offer)
            EPrintBillAction.CANCEL_BILL_PRINT -> cancelPaymentsAskOtherTable()
            EPrintBillAction.PRINT_BILL_YES -> confirmPrintBill()
            else -> {}
        }
    }

    fun onPrintBill(offer: Boolean)
    {
        val currentTransaction = mViewModel.transaction.value
        if (currentTransaction == null)
        {
            return
        }
        Log.i("model", "onPrintBill  transaction=${currentTransaction.transactionId}")
        isPrintBillAllowedAndConfirmed(currentTransaction, offer) // 675
    }

    fun askConfirmationBill(offer: Boolean)
    {
        mOffer = offer
        val title = when (offer)
        {
            false -> Translation.get(Translation.TextId.TEXT_FINISH_BILL)
            true -> Translation.get(Translation.TextId.TEXT_PRINT_OFFER)
        }
        val dlg = ModalDialogYesNo.newInstance(title, "", CONFIRM_BILL)
        dlg.show(supportFragmentManager, "ConfirmationDialog")
        // Continue in onDialogPositiveClick and onDialogNegativeClick
    }

    fun cancelPaymentsAskOtherTable()
    {
        val currentTransaction = mViewModel.transaction.value // 678
        if (currentTransaction == null)
        {
            return
        }

        val transactionId = currentTransaction.transactionId
        Log.i(
            "MODEL",
            "CbillingDialog::onPrintBill not valid = $transactionId!!"
        )
        currentTransaction.cancelPayments(EPaymentStatus.PAY_STATUS_UNPAID)
        askTransactionActivity()
    }

    fun confirmPrintBill()
    {
        mViewModel.confirmPrintBill(mOffer)

//        if (!CFG("bill_print_kitchen_first"))
//        {
//            CprinterSpoolerProxy::Instance()->checkDatabase();
//        }
        askTransactionActivity()
    }

    /*----------------------------------------------------------------------------*/
    /** Check for valid billing key, delete payments and notify when wrong.
     *  Also when the wrong key is used, notify to use the proper key
     *  @return true if ok to continue billing
     */
    fun checkBillingKey(): Boolean
    {
        val currentTransaction = mViewModel.transaction.value // 678
        if (currentTransaction == null)
        {
            return false
        }
        var retVal = true
        val employeeId = currentTransaction.getEmployeeId()
        if (!currentTransaction.isTakeaway()
            && CFG.getValue("waiter_per_table") != 0
            && employeeId != global.rfidKeyId
        )
        {
            // Table has different waiter !
            val txt = Translation.get(Translation.TextId.TEXT_EMPLOYEE)
            Toast.makeText(this@BillOrderActivity, txt, Toast.LENGTH_LONG).show()
            retVal = false;
        }
//            else if (CFG("restaurant_map"))
//        {
//            long transactionId = m_transactionItemModel->getTransactionId();
//            CsqlFloorTableIterator(-1).setTransactionTableAvailable(transactionId);
//        }

        if (!retVal)
        {
            currentTransaction.cancelNewPayments()
            askTransactionActivity()
        }
        return retVal;
    }
}