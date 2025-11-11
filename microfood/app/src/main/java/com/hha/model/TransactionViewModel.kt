package com.hha.model

import android.util.Log
import android.view.View
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
import com.hha.dialog.Translation
import com.hha.dialog.Translation.TextId
import com.hha.resources.Global
import com.hha.types.CMoney
import com.hha.types.EBillBackgroundType
import com.hha.types.EBillLineType
import com.hha.types.EPaymentMethod
import com.hha.types.ETimeFrameIndex
import tech.hha.microfood.R

/**
 * A shared ViewModel that manages the state of a single CTransaction.
 * It acts as the single source of truth for the entire order and payment process.
 * It implements TransactionListener to react to any changes in the underlying CTransaction object.
 */
class TransactionViewModel : ViewModel(), PaymentsListener, TransactionListener,
   TransactionItemListener
{
   // Define different initialization modes
   enum class InitMode
   {
      VIEW_PAGE_ORDER,   // For PageOrderActivity: creates transaction and starts a timeframe
      VIEW_BILLING  // For BillOrderActivity: only loads the existing transaction
   }

   // 1. SINGLE SOURCE OF TRUTH
   private val _transaction = MutableLiveData<CTransaction>()
   val transaction: LiveData<CTransaction> = _transaction

   // 2. LIVE DATA FOR THE UI
   // The UI layers will observe these LiveData objects to display data.

   // For the detailed BillOrderView
   private val _displayLines = MutableLiveData<List<BillDisplayLine>>()
   val displayLines: LiveData<List<BillDisplayLine>> = _displayLines
   private var m_isChanged: Boolean = false
   private var m_isInitialized = false

   private lateinit var m_mode: InitMode

   // For the simple PageOrderView
   private val _orderTotal = MutableLiveData<CMoney>()
   val orderTotal: LiveData<CMoney> = _orderTotal

   /**
    * Initializes the transaction based on the specified mode.
    * This function is safe to call multiple times but will only execute the core logic once.
    *
    * @param mode The initialization mode (FULL_ORDER or BILLING_ONLY).
    */
   fun initializeTransaction(mode: InitMode)
   {
      if (mode == InitMode.VIEW_BILLING && _transaction.value == null)
      {
         m_isInitialized = false
         return
      }
      m_mode = mode
      val global = Global.getInstance()

      // Create transaction if it doesn't exist in the global scope
      if (_transaction.value == null)
      {
         if (global.transactionId < 1E6)
         {
            // Handle this error case gracefully
            // You might want to post an error state via another LiveData
            return
         }
         _transaction.value = CTransaction(global.transactionId)
      }

      val transaction = _transaction.value!!
      // Register 'this' ViewModel as a listener to the CTransaction object.
      // Because CTransaction now handles its own internal listeners (for items and payments),
      // this is the only listener the ViewModel needs to set up.
      transaction.addListener(this)
      transaction.addPaymentListener(this)
      transaction.addItemListener(this)

      // Trigger an initial update to populate the UI when the ViewModel is first created.
      onTransactionChanged(transaction)

      // --- Conditional Logic based on Mode ---
      if (mode == InitMode.VIEW_PAGE_ORDER)
      {
         // Only PageOrderActivity should start a new timeframe
         transaction.startNextTimeFrame()
      }
      m_isInitialized = true
   }

   // 3. INITIALIZATION
   init
   {
      // Register 'this' ViewModel as a listener to the CTransaction object.
      // Because CTransaction now handles its own internal listeners (for items and payments),
      // this is the only listener the ViewModel needs to set up.
     // transaction.addListener(this)
     // transaction.addPaymentListener(this)
     // transaction.addItemListener(this)

      // Trigger an initial update to populate the UI when the ViewModel is first created.
     // onTransactionChanged(transaction)
   }

   // 4. LISTENER IMPLEMENTATION
   /**
    * This is the single entry point for all updates from the model.
    * It is called by CTransaction whenever any of its data (items, payments, etc.) changes.
    */
   override fun onTransactionChanged(transaction: CTransaction)
   {
      _transaction.value = transaction
      // When any part of the transaction changes, update all relevant LiveData.
      _orderTotal.postValue(transaction.getTotalTransaction())
      prepareBillDisplayLines()
   }

   fun resetIsChanged()
   {
      m_isChanged = false
   }

   fun isChanged(): Boolean = m_isChanged

   // 5. PUBLIC ACTIONS
   // These are the functions that the UI (Activities/Fragments) will call to request state changes.

   /**
    * Adds an item to the current transaction.
    */
   fun addItem(item: CMenuItem, clusterId: Short): Boolean
   {
      val currentTransaction = _transaction.value
      // The ViewModel tells the model to change. The listener will handle the UI update automatically.
      return currentTransaction?.addTransactionItem(item, clusterId) ?: false
   }

   fun refreshAllData()
   {
      // When changing the language
      prepareBillDisplayLines()
   }

   /**
    * Adds a payment to the current transaction.
    */
   fun addPayment(method: EPaymentMethod, amount: CMoney)
   {
      val currentTransaction = _transaction.value
      currentTransaction?.addPayment(method, amount)
   }

   // 6. UI PREPARATION LOGIC
   /**
    * Prepares the detailed list of display lines for the BillOrderView.
    * This logic is now correctly located within the ViewModel.
    */
   fun prepareBillDisplayLines()
   {
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return
      }
      val lines = mutableListOf<BillDisplayLine>()

      // Get total from the 'transaction' object
      val kitchenTotal = currentTransaction.getKitchenTotal()
      val drinksTotal = currentTransaction.getDrinksTotal()
      val itemTotal = currentTransaction.getItemsTotal()
      val discount = currentTransaction.getDiscount()
      val totalPaid = currentTransaction.getTotalPaid()
      var subTotal = itemTotal - discount
      var myId = 0
      //val kitchenAndDrinks = !kitchenTotal.empty() and !drinksTotal.empty()
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = Translation.get(TextId.TEXT_ITEM_KITCHEN),
            amount = kitchenTotal,
            lineType = EBillLineType.BILL_LINE_NONE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
         )
      )
      lines.add(
         BillDisplayLine(
            id = ++myId,
            sign = "+",
            iconResId = null,
            text = Translation.get(TextId.TEXT_ITEM_DRINKS), // Recommended: Use string resources
            amount = drinksTotal,
            lineType = EBillLineType.BILL_LINE_NONE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
         )
      )
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = Translation.get(TextId.TEXT_SUBTOTAL), // Recommended: Use string resources
            amount = itemTotal,
            lineType = EBillLineType.BILL_LINE_ABOVE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
         )
      )
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = Translation.get(TextId.TEXT_PAID),
            sign = "-",
            amount = totalPaid,
            lineType = EBillLineType.BILL_LINE_NONE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
         )
      )
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = Translation.get(TextId.TEXT_DISCOUNT),
            amount = discount,
            sign = "-",
            lineType = EBillLineType.BILL_LINE_NONE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
         )
      )
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = Translation.get(TextId.TEXT_CLIENT_AMOUNT),
            amount = subTotal,
            lineType = EBillLineType.BILL_LINE_ABOVE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
         )
      )

      val payments: List<CPayment> = currentTransaction.getPayments()

      // Calculate remaining balance and return money
      var sign = ""
      var clientPayments = CMoney(0)
      for (payment in payments)
      {
         lines.add(
            BillDisplayLine(
               id = ++myId,
               iconResId = EPaymentMethod.getIconResource(payment.paymentMethod),
               text = EPaymentMethod.toString(payment.paymentMethod),
               sign = sign,
               amount = payment.total,
               lineType = EBillLineType.BILL_LINE_NONE,
               backgroundType = EBillBackgroundType.BILL_BACKGROUND_PAYMENTS
            )
         )
         clientPayments = clientPayments + payment.total
         sign = "+"
      }
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = Translation.get(TextId.TEXT_CLIENT_PAYS_WITH),
            sign = sign,
            amount = clientPayments,
            lineType = EBillLineType.BILL_LINE_ABOVE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_PAYMENTS
         )
      )
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = Translation.get(TextId.TEXT_CLIENT_AMOUNT),
            amount = subTotal,
            sign = "-",
            lineType = EBillLineType.BILL_LINE_NONE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_PAYMENTS
         )
      )
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = Translation.get(TextId.TEXT_EXCHANGE_MONEY),
            amount = subTotal,
            lineType = EBillLineType.BILL_LINE_ABOVE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_PAYMENTS
         )
      )
      // Post the final list to the UI. postValue is safe to call from any thread.
      _displayLines.postValue(lines)
   }

   fun setMessage(text: String)
   {
      val currentTransaction = _transaction.value
      currentTransaction?.setMessage(text)
   }

   fun hasAnyChanges(): Boolean
   {
      val currentTransaction = _transaction.value
      return currentTransaction?.hasAnyChanges() ?: false
   }


   fun getCursor(item: CItem): Int
   {
      val currentTransaction = _transaction.value
      return currentTransaction?.getCursor(item) ?: 0
   }

   /**
    * Handles the logic for when a user presses a cash payment button (e.g., €5, €10).
    * It contains the business rule to clear previous payments if needed.
    */
   fun payEuros(amount: CMoney)
   {
      val currentTransaction = _transaction.value
      currentTransaction?.addPayment(EPaymentMethod.PAYMENT_CASH, amount)
   }

   /**
    * The ViewModel is automatically cleared by the framework.
    * We should remove our listener to prevent memory leaks.
    */
   override fun onCleared()
   {
      super.onCleared()
      val currentTransaction = _transaction.value
      currentTransaction?.removeListener(this)
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

   fun needToAskCancelReason(): Boolean
   {
      val currentTransaction = _transaction.value
      if (currentTransaction != null)
      {
         return currentTransaction.isTakeaway()
               || currentTransaction.transactionEmptyAtStartAndAtEnd()
               || currentTransaction.getTimeFrameIndex().index ==
               ETimeFrameIndex.TIME_FRAME1
      }
      return false
   }

   fun closeTimeFrame()
   {
      val currentTransaction = _transaction.value
      if (currentTransaction != null)
      {
         currentTransaction.closeTimeFrame()
         if (currentTransaction.transactionEmptyAtStartAndAtEnd())
         {
            currentTransaction.emptyTransaction("");
         }
      }
   }

   fun emptyTransaction(reason: String)
   {
      val currentTransaction = _transaction.value
      currentTransaction?.emptyTransaction(reason)
   }

   fun onButtonPlus1()
   {
      val currentTransaction = _transaction.value
      if (m_mode == InitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.addOneToCursorPosition()
         m_isChanged = true
      }
   }

   fun onButtonMin1()
   {
      val currentTransaction = _transaction.value
      if (m_mode == InitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.minus1()
         m_isChanged = true
      }
   }

   fun onButtonPortion()
   {
      val currentTransaction = _transaction.value
      if (m_mode == InitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.nextPortion()
         m_isChanged = true
      }
   }

   fun onButtonRemove()
   {
      val currentTransaction = _transaction.value
      if (m_mode == InitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.remove()
         m_isChanged = true
      }
   }

   fun setMode(newMode: InitMode)
   {
      m_mode = newMode
   }

   fun handleMenuItem(selectedMenuItem: CMenuItem): Boolean
   {
      val currentTransaction = _transaction.value
      if (m_mode == InitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         val clusterId: Short = -1
         //Log.d(tag, "MenuItem clicked: ${selectedMenuItem.localName}")
         if (currentTransaction.addTransactionItem(selectedMenuItem, clusterId))
         {
            m_isChanged = true
            return true
         }
      }
      return false
   }

}