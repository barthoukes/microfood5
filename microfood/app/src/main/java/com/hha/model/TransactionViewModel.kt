package com.hha.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hha.callback.PaymentsListener
import com.hha.callback.TransactionItemListener
import com.hha.callback.TransactionListener
import com.hha.framework.CItem
import com.hha.framework.CMenuItem
import com.hha.framework.CPayment
import com.hha.framework.CTransaction
import com.hha.types.EPaymentStatus
import com.hha.resources.Global
import com.hha.types.CMoney
import com.hha.types.EBillBackgroundType
import com.hha.types.EBillLineType
import com.hha.types.EPaymentMethod

/**
 * A shared ViewModel that manages the state of a single CTransaction.
 * It acts as the single source of truth for the entire order and payment process.
 * It implements TransactionListener to react to any changes in the underlying CTransaction object.
 */
class TransactionViewModel : ViewModel(), PaymentsListener, TransactionListener,
   TransactionItemListener
{

   // 1. SINGLE SOURCE OF TRUTH
   // The ViewModel owns the CTransaction object. It's initialized with the globally-scoped transaction ID.
   private val transaction = CTransaction(Global.getInstance().transactionId)

   // 2. LIVE DATA FOR THE UI
   // The UI layers will observe these LiveData objects to display data.

   // For the detailed BillOrderView
   private val _displayLines = MutableLiveData<List<BillDisplayLine>>()
   val displayLines: LiveData<List<BillDisplayLine>> = _displayLines

   // For the simple PageOrderView
   private val _orderTotal = MutableLiveData<CMoney>()
   val orderTotal: LiveData<CMoney> = _orderTotal

   // 3. INITIALIZATION
   init
   {
      // Register 'this' ViewModel as a listener to the CTransaction object.
      // Because CTransaction now handles its own internal listeners (for items and payments),
      // this is the only listener the ViewModel needs to set up.
      transaction.addListener(this)
      transaction.addPaymentListener(this)
      transaction.addItemListener(this)

      // Trigger an initial update to populate the UI when the ViewModel is first created.
      onTransactionChanged(transaction)
   }

   // 4. LISTENER IMPLEMENTATION
   /**
    * This is the single entry point for all updates from the model.
    * It is called by CTransaction whenever any of its data (items, payments, etc.) changes.
    */
   override fun onTransactionChanged(transaction: CTransaction)
   {
      // When any part of the transaction changes, update all relevant LiveData.
      _orderTotal.postValue(transaction.getTotalTransaction())
      prepareBillDisplayLines()
   }

   // 5. PUBLIC ACTIONS
   // These are the functions that the UI (Activities/Fragments) will call to request state changes.

   /**
    * Adds an item to the current transaction.
    */
   fun addItem(item: CMenuItem, clusterId: Short)
   {
      // The ViewModel tells the model to change. The listener will handle the UI update automatically.
      transaction.addTransactionItem(item, clusterId)
   }

   /**
    * Adds a payment to the current transaction.
    */
   fun addPayment(method: EPaymentMethod, amount: CMoney)
   {
      transaction.addPayment(method, amount)
   }

   // 6. UI PREPARATION LOGIC
   /**
    * Prepares the detailed list of display lines for the BillOrderView.
    * This logic is now correctly located within the ViewModel.
    */
   fun prepareBillDisplayLines()
   {
      val lines = mutableListOf<BillDisplayLine>()

      // Get total from the 'transaction' object
      val itemTotal = transaction.getItemsTotal()
      val discount = transaction.getDiscount()
      var subTotal = itemTotal - discount
      var myId = 0
      if (!discount.empty())
      {
         lines.add(
            BillDisplayLine(
               id = ++myId,
               iconResId = null,
               text = "Total Amount", // Recommended: Use string resources
               amount = itemTotal,
               lineType = EBillLineType.BILL_LINE_NONE,
               backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
            )
         )

         lines.add(
            BillDisplayLine(
               id = ++myId,
               iconResId = null,
               text = "Discount", // Recommended: Use string resources
               amount = discount,
               lineType = EBillLineType.BILL_LINE_BELOW,
               backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
            )
         )
      }
      val totalPaid = transaction.getTotalPaid()
      if (!totalPaid.empty())
      {
         lines.add(
            BillDisplayLine(
               id = ++myId,
               iconResId = null,
               text = "Subtotal", // Recommended: Use string resources
               amount = subTotal,
               lineType = EBillLineType.BILL_LINE_NONE,
               backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
            )
         )
         lines.add(
            BillDisplayLine(
               id = ++myId,
               iconResId = null,
               text = "Total Paid", // Recommended: Use string resources
               amount = totalPaid,
               "-",
               lineType = EBillLineType.BILL_LINE_NONE,
               backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
            )
         )
         subTotal = subTotal - totalPaid
      }
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = "Subtotal", // Recommended: Use string resources
            amount = subTotal,
            lineType = EBillLineType.BILL_LINE_NONE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
         )
      )
      val payments: List<CPayment> = transaction.getPayments()

      // Calculate remaining balance and return money
      val balance = subTotal - totalPaid

      for (payment in payments)
      {
         lines.add(
            BillDisplayLine(
               id = ++myId,
               iconResId = EPaymentMethod.getIcon(payment.paymentMethod),
               text = EPaymentMethod.getName(payment.paymentMethod),
               amount = payment.total,
               lineType = EBillLineType.BILL_LINE_NONE,
               backgroundType = EBillBackgroundType.BILL_BACKGROUND_PAYMENTS
            )
         )
      }
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = "Totaal", // Recommended: Use string resources
            amount = subTotal,
            lineType = EBillLineType.BILL_LINE_NONE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_PAYMENTS
         )
      )
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = "Betaald", // Recommended: Use string resources
            amount = subTotal,
            lineType = EBillLineType.BILL_LINE_NONE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_PAYMENTS
         )
      )
      // Post the final list to the UI. postValue is safe to call from any thread.
      _displayLines.postValue(lines)
   }

   fun setMessage(text: String)
   {
      transaction.setMessage(text)
   }

   /**
    * Handles the logic for when a user presses a cash payment button (e.g., €5, €10).
    * It contains the business rule to clear previous payments if needed.
    */
   fun payEuros(amount: CMoney)
   {
      // Tell the model to add the new payment. The listener will update the LiveData.
      transaction.addPayment(EPaymentMethod.PAYMENT_CASH, amount)
   }

   /**
    * The ViewModel is automatically cleared by the framework.
    * We should remove our listener to prevent memory leaks.
    */
   override fun onCleared()
   {
      super.onCleared()
      transaction.removeListener(this)
   }

   override fun onPaymentAdded(position: Int, item: CPayment)
   {
      prepareBillDisplayLines()
   }

   override fun onPaymentRemoved(position: Int)
   {
      prepareBillDisplayLines()
   }

   override fun onPaymentUpdated(position: Int, item: CPayment)
   {
      prepareBillDisplayLines()
   }

   override fun onPaymentsCleared()
   {
      prepareBillDisplayLines()
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
}
