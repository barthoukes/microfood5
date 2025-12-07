package com.hha.model

import android.util.Log
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
import com.hha.dialog.Translation
import com.hha.dialog.Translation.TextId
import com.hha.framework.CPaymentTransaction
import com.hha.framework.CPersonnel
import com.hha.grpc.GrpcServiceFactory
import com.hha.printer.BillPrinter
import com.hha.printer.EBillExample
import com.hha.printer.EPrinterLocation
import com.hha.printer.PrintFrame
import com.hha.printer.TakeawayNumber
import com.hha.resources.CTimestamp
import com.hha.resources.Global
import com.hha.types.CMoney
import com.hha.types.EBillBackgroundType
import com.hha.types.EBillLineType
import com.hha.types.EClientOrdersType
import com.hha.types.ECookingState
import com.hha.types.EInitAction
import com.hha.types.EItemLocation
import com.hha.types.EPayingMode
import com.hha.types.EPaymentMethod
import com.hha.types.EPaymentStatus
import com.hha.types.EPrintBillAction
import com.hha.types.ETimeFrameIndex
import com.hha.types.ETransType

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
   private var mShowDiscount = false // @todo Inspect

   // 2. LIVE DATA FOR THE UI
   // The UI layers will observe these LiveData objects to display data.

   // For the detailed BillOrderView
   private val _displayLines = MutableLiveData<List<BillDisplayLine>>()
   val global: Global = Global.getInstance()
   val CFG = global.CFG
   val userCFG = global.userCFG
   private var mAlreadyPayed = false
   private var mTransactionTotals = CPaymentTransaction()

   val mContinueWhenAmountEnough = CFG.getBoolean("bill_continue_when_enough")
   private val mAskBillConfirmation = CFG.getBoolean("bill_ask_confirmation")
   private val mBillDefaultAllCash = CFG.getBoolean("bill_default_all_cash")

   public var mAllowNewItem = false
   private var mAutomaticPayments = false // @todo Inspect

   val billDisplayLines: LiveData<List<BillDisplayLine>> = _displayLines
   val mBillPayingMode = EPayingMode.PAYING_MODE_MANUAL
   private var mEmptyTimeFrame = false
   private var m_isChanged: Boolean = false
   private var m_isInitialized = false
   private var m_customerTotal = CMoney(0)
   private var m_returnTotal = CMoney(0)
   private var m_alreadyPayed = false
   private lateinit var mMode: InitMode
   var mPartialIndex = 0


   private var m_cardTotal = CMoney(0)
   private var m_cashTotal = CMoney(0)
   private var mPaidTotal = CMoney(0)

   // For the simple PageOrderView
   private val _orderTotal = MutableLiveData<CMoney>()
   val orderTotal: LiveData<CMoney> = _orderTotal


   fun onInit(): EInitAction
   {
      mEmptyTimeFrame = false
      val currentTransaction = transaction.value
      if (currentTransaction == null)
      {
         return EInitAction.INIT_ACTION_NOTHING
      }
      val type: EClientOrdersType = currentTransaction.getStatus()
      Log.i("Bill", "onInit %$type")
      when (type)
      {
         EClientOrdersType.OPEN ->
         {
            onInitAmounts(EPayingMode.PAYING_MODE_ALL_CASH, true);
            allowNewItems(true)
            mAlreadyPayed = false
         }

         EClientOrdersType.PAYING ->
         {
            if (mBillPayingMode == EPayingMode.PAYING_MODE_START_PAYMENTS_DIALOG)
            {
               return EInitAction.INIT_ACTION_ASK_MONEY
            }
            onInitAmounts(EPayingMode.PAYING_MODE_ALL_CASH, true)
            allowNewItems(true)
            mAlreadyPayed = false;
         }

         EClientOrdersType.OPEN_PAID ->
         {
            onInitAmounts(EPayingMode.PAYING_MODE_ALL_CASH, true);
            allowNewItems(true)
            mAlreadyPayed = true
         }

         EClientOrdersType.CLOSED, EClientOrdersType.EMPTY ->
         {
//            stop(MODE_BILL_PAYMENTS_DIALOG)
            Log.i("bill", "BILL PAYMENTS DIALOG!!")
            return EInitAction.INIT_ACTION_BILL_PAYMENTS
         }

         EClientOrdersType.ALL ->
         {
         }

         EClientOrdersType.PERSONNEL ->
         {
         }

         EClientOrdersType.CREDIT, EClientOrdersType.CLOSED_CREDIT ->
         {
            mPartialIndex = currentTransaction.getHighestPaymentIndex()
            allowNewItems(false);
            mAlreadyPayed = true;
         }

         else ->
         {
            allowNewItems((CFG.getValue("change_after_bill") > 0));
            mPartialIndex = currentTransaction.getHighestPaymentIndex();
            mAlreadyPayed = true;
         }
      }
      return EInitAction.INIT_ACTION_NOTHING
   }

   /**
    * Adds a payment to the current transaction.
    */
   fun addPayment(method: EPaymentMethod, amount: CMoney)
   {
      val currentTransaction = _transaction.value
      currentTransaction?.addPayment(method, amount)
   }

   /**
    * Adds a payment to the current transaction.
    */
   fun addPayment(payment: CPayment)
   {
      addPayment(payment.paymentMethod, payment.total)
   }

   fun addReturnMoney()
   {
      Log.i("model", "addReturnMoney")
      val currentTransaction = transaction.value
      if (currentTransaction == null)
      {
         return
      }
      val total = currentTransaction.getTotalTransaction()
      val transactionId = currentTransaction.transactionId

      // Only close if all payments together are bigger or equal to the total.
      val payments: CPaymentTransaction =
         currentTransaction.getTransactionPaymentTotals(false)
      val totalPayments = payments.getTotal(EPaymentStatus.PAY_STATUS_ANY)
      if ( totalPayments > total)
      {
         addPayment(EPaymentMethod.PAYMENT_RETURN, total-totalPayments);
         //totalPayments =total;
      }
   }

   /**
    * Adds an item to the current transaction.
    */
   fun addItem(item: CMenuItem, clusterId: Short): Boolean
   {
      val currentTransaction = _transaction.value
      // The ViewModel tells the model to change. The listener will handle the UI update automatically.
      return currentTransaction?.addTransactionItem(item, clusterId) ?: false
   }

   fun addToEmployee(rfidKey: Short, paymentStatus: EPaymentStatus)
   {
      Log.i("model", "addToEmployee")
      val currentTransaction = transaction.value
      if (currentTransaction == null)
      {
         return
      }
      val cash = currentTransaction.getCashTotal(paymentStatus)
      val card = currentTransaction.getCardTotal(paymentStatus)
      val returns = currentTransaction.getReturnTotal(paymentStatus)
      val allKitchen = currentTransaction.getAllKitchenTotal(paymentStatus,true)
      val kitchen2 = currentTransaction.getItemTotal(EItemLocation.ITEM_KITCHEN2, paymentStatus, true)
      val drinks = currentTransaction.getItemTotal(EItemLocation.ITEM_DRINKS, paymentStatus, true)
      val bar = currentTransaction.getItemTotal(EItemLocation.ITEM_BAR, paymentStatus, true)
      val sushi = currentTransaction.getItemTotal(EItemLocation.ITEM_SUSHI, paymentStatus, true)
      val nonfood = currentTransaction.getItemTotal(EItemLocation.ITEM_NONFOOD, paymentStatus, true)
      val others = currentTransaction.getItemTotal(EItemLocation.ITEM_OTHERS, paymentStatus, true)

      Log.i("model", "cash $cash, card $card returns=$returns")
      val employee = CPersonnel().getPerson(global.rfidKeyId)
      employee.add(
         allKitchen, kitchen2, drinks, bar, sushi, nonfood, others,
         cash, card,returns)
      employee.update(false)
   }

   fun allowNewItems(allowNewItems: Boolean)
   {
      //mAllowNewItem = allowNewItems
      mAllowNewItem = allowNewItems;
//        val currentTransaction = mViewModel.transaction.value
//        if (currentTransaction == null)
//        {
//            return
//        }
//        currentTransaction.allowNewItems(allowNewItems)
   }

   fun discountVisible(): Boolean
   {
      val currentTransaction = transaction.value
      if (currentTransaction == null)
      {
         return false
      }
      if (currentTransaction.isTakeaway()
         && CFG.getValue("takeaway_discount") == 0
         && currentTransaction.getStatus() != EClientOrdersType.CLOSED
      )
      {
         return false
      } else
      {
         return true
      }
   }

   fun endTimeFrameAndPrintToBuffer(
      prints2kitchen: Int, prints2bill: Int, newTime: CTimestamp,
      timeFrame: ETimeFrameIndex, timeChanged: Boolean, collectPrinter: Boolean)
   {
      Log.i("model", "endTimeFrameAndPrintToBuffer")
      val currentTransaction = transaction.value
      if (currentTransaction == null)
      {
         return
      }
      currentTransaction.updateTotal()
      val transactionId = currentTransaction.transactionId

      var newState = ECookingState.COOKING_DONE
      if (CFG.getBoolean("prepare_display_enable"))
      {
         val kitchen: Int = currentTransaction.numberKitchenItems()
         if (kitchen > 0)
         {
            newState = ECookingState.COOKING_IN_KITCHEN
         }
      }
      currentTransaction.endTimeFrame(
         transactionId, CFG.getValue("pc_number"),
         newTime, timeChanged, newState)

      val pf = PrintFrame()
      pf.printTimeFrame( transactionId, timeFrame, CFG.getShort("pc_number"),
         global.rfidKeyId, prints2kitchen, 0, collectPrinter, false);
      pf.printTimeFrame( transactionId, timeFrame, CFG.getShort("pc_number"),
         global.rfidKeyId, prints2bill, 0, false, true);
   }

   fun getCursor(item: CItem): Int
   {
      val currentTransaction = _transaction.value
      return currentTransaction?.getCursor(item) ?: 0
   }


   fun getRequiredAdditionalPayment() : CMoney
   {
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return CMoney(0)
      }
      // return true if a payment should be requested.
      if (!m_alreadyPayed && currentTransaction.transType != ETransType.TRANS_TYPE_WOK)
      {
         val returnTotal = m_customerTotal-m_cashTotal-m_cardTotal
         if ( returnTotal > CMoney(0))
         {
            return returnTotal
         }
      }
      return CMoney(0)
   }

   fun getTableName(): String
   {
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return "--"
      }
      var type = when (currentTransaction.transType)
      {
         ETransType.TRANS_TYPE_EAT_INSIDE,
         ETransType.TRANS_TYPE_WOK, ETransType.TRANS_TYPE_RECHAUD,
         ETransType.TRANS_TYPE_SITIN -> Translation.get(TextId.TEXT_TABLE)
         ETransType.TRANS_TYPE_DELIVERY -> Translation.get(TextId.TEXT_TELEPHONE)
         ETransType.TRANS_TYPE_TAKEAWAY,
         ETransType.TRANS_TYPE_TAKEAWAY_PHONE -> Translation.get(TextId.TEXT_TAKEAWAY)
         else -> "--"
      }
      type = type + " " + currentTransaction.getName()
      return type
   }

   fun hasAnyChanges(): Boolean
   {
      val currentTransaction = _transaction.value
      return currentTransaction?.hasAnyChanges() ?: false
   }

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
      mMode = mode
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

   fun isChanged(): Boolean = m_isChanged

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

   fun isEmptyTransaction(): Boolean
   {
      val currentTransaction = _transaction.value
      return currentTransaction?.isEmpty() ?: false
   }

   /*----------------------------------------------------------------------------*/
   /** Check if we can print a bill, also ask whether it is allowed
    *  @return true when allowed to print
    */
   fun isPrintBillAllowedAndConfirmed(
      currentTransaction: CTransaction, offer: Boolean,
      customerTotal: CMoney
   ): EPrintBillAction
   {
      var printBill = true
      val transactionId = currentTransaction.transactionId
      val type = currentTransaction.getStatus()

      if (!currentTransaction.isValid())  // 594
      {
         // Invalid transaction, should not happen
         Log.e("BillOrderActivity", "not valid transaction=$transactionId!!");
         if (CFG.getBoolean("restaurant_map"))
         {
            val service = GrpcServiceFactory.createFloorTableService()
            service.setTransactionTableAvailable(transactionId)
         }
         currentTransaction.cancelPayments(EPaymentStatus.PAY_STATUS_UNPAID)
         return EPrintBillAction.NAVIGATE_TO_ASK_TRANSACTION // 606
      } else if (!offer
         && (currentTransaction.transType == ETransType.TRANS_TYPE_WOK)
         && (type != EClientOrdersType.CLOSED)
         && CFG.getBoolean("floorplan_bill_first")
      )
      {
         when (type) // 614 wok
         {
            EClientOrdersType.OPEN_PAID ->
            {
               if (customerTotal > CMoney(0))
               {
                  currentTransaction.setStatus(EClientOrdersType.OPEN)
               } else
               {
                  return EPrintBillAction.ASK_WOK_CONFIRM_CLEAN_TABLE
               }
            }
            else -> {}
         }
      } else if (!mAskBillConfirmation)
      {
         printBill = true
      } else if (mAllowNewItem)
      {
         return EPrintBillAction.ASK_CONFIRM_PRINT
      }
      if (!printBill)
      {
         return EPrintBillAction.CANCEL_BILL_PRINT
      } else
      {
         return EPrintBillAction.PRINT_BILL_YES
      }
   }

   // 4. LISTENER IMPLEMENTATION
   /**
    * This is the single entry point for all updates from the model.
    * It is called by CTransaction whenever any of its data (items, payments, etc.) changes.
    */
   override fun onTransactionChanged(transaction: CTransaction)
   {
      _transaction.postValue(transaction)
      // When any part of the transaction changes, update all relevant LiveData.
      transaction.calculateTotalTransaction()
      _orderTotal.postValue(transaction.getTotalTransaction())
      prepareBillDisplayLines()
   }

   fun payEuroButton(amount: CMoney)
   {
      payEuros(amount)
   }

   fun resetIsChanged()
   {
      m_isChanged = false
   }

   // 5. PUBLIC ACTIONS
   // These are the functions that the UI (Activities/Fragments) will call to request state changes.

   fun refreshAllData()
   {
      // When changing the language
      prepareBillDisplayLines()
   }

   fun payAllUsing(paymentMethod: EPaymentMethod)
   {
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return
      }
      currentTransaction.cancelPayments(
         -1, EPaymentStatus.PAY_STATUS_UNPAID)

      currentTransaction.addPayment(
         paymentMethod, m_customerTotal);
   }

   fun payAllUsingCash()
   {
      payAllUsing(EPaymentMethod.PAYMENT_CASH)
   }

   fun payAllUsingPin()
   {
      payAllUsing(EPaymentMethod.PAYMENT_PIN)
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
      val itemTotal = currentTransaction.getItemTotal()
      val discount = currentTransaction.getDiscount()
      val totalPaid = currentTransaction.getTotalAlreadyPaid()
      m_customerTotal = itemTotal - discount - totalPaid
      val payments: List<CPayment> = currentTransaction.getPayments()

      m_cashTotal.clear()
      m_cardTotal.clear()
      for (payment in payments)
      {
         if (payment.paymentMethod == EPaymentMethod.PAYMENT_CASH)
         {
            m_cashTotal = m_cashTotal + payment.total
         }
         else
         {
            m_cardTotal = m_cardTotal + payment.total
         }
      }
      m_returnTotal = m_cashTotal+m_cardTotal - m_customerTotal

      var myId = 0
      var sign = ""
      //val kitchenAndDrinks = !kitchenTotal.empty() and !drinksTotal.empty()
      if (kitchenTotal > CMoney(0))
      {
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
         sign = "+"
      }
      lines.add(
         BillDisplayLine(
            id = ++myId,
            sign = sign,
            iconResId = null,
            text = Translation.get(TextId.TEXT_ITEM_DRINKS), // Recommended: Use string resources
            amount = drinksTotal,
            lineType = EBillLineType.BILL_LINE_NONE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
         )
      )
      if (totalPaid != CMoney(0) || discount !=CMoney(0))
      {
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
      }
      if (!totalPaid.empty())
      {
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
      }
      if (!discount.empty())
      {
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
      }
      lines.add(
         BillDisplayLine(
            id = ++myId,
            iconResId = null,
            text = Translation.get(TextId.TEXT_CLIENT_AMOUNT),
            amount = m_customerTotal,
            lineType = EBillLineType.BILL_LINE_ABOVE,
            backgroundType = EBillBackgroundType.BILL_BACKGROUND_TOTAL
         )
      )

      // Calculate remaining balance and return money
      sign = ""
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
      if (!m_customerTotal.empty())
      {
         lines.add(
            BillDisplayLine(
               id = ++myId,
               iconResId = null,
               text = Translation.get(TextId.TEXT_CLIENT_AMOUNT),
               amount = m_customerTotal,
               sign = "-",
               lineType = EBillLineType.BILL_LINE_NONE,
               backgroundType = EBillBackgroundType.BILL_BACKGROUND_PAYMENTS
            )
         )
      }
      if (m_returnTotal >= CMoney(0))
      {
         lines.add(
            BillDisplayLine(
               id = ++myId,
               iconResId = null,
               text = Translation.get(TextId.TEXT_EXCHANGE_MONEY),
               amount = m_returnTotal,
               lineType = EBillLineType.BILL_LINE_ABOVE,
               backgroundType = EBillBackgroundType.BILL_BACKGROUND_PAYMENTS
            )
         )
      }
      // Post the final list to the UI. postValue is safe to call from any thread.
      _displayLines.postValue(lines)
   }

   fun setMessage(text: String)
   {
      val currentTransaction = _transaction.value
      currentTransaction?.setMessage(text)
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

   fun confirmPrintBill(offer: Boolean)
   {
      val currentTransaction = transaction.value
      if (currentTransaction == null)
      {
         return
      }
      if (offer) // 687
      {
         currentTransaction.setStatus(EClientOrdersType.OPEN)
      }
      val orderType = currentTransaction.getStatus()
      if (  orderType == EClientOrdersType.OPEN
         || orderType == EClientOrdersType.OPEN_PAID
         || orderType == EClientOrdersType.PAYING)
      {
//            if (!checkBillingKey())
//            {
//                CmessageBox cm( "mbx::print_bill_not_valid", BTN9+2, BTN1-2, BTN3+2, BTN1+2, getTranslation(_KEY_BAD), MB_BEEP
//                | MB_TIME3 | MB_TEXT_CENTER);
//                cancelNewPayments();
//                stop( MODE_ASK_TABLE_BILL_DIALOG);
//                return;
//            }
         currentTransaction.setEmployeeId(global.rfidKeyId)
         val transType = currentTransaction.transType

         // todo: phone_order_bill_direct
         if (  transType == ETransType.TRANS_TYPE_TAKEAWAY
            || transType == ETransType.TRANS_TYPE_EAT_INSIDE)
         {
            val number = TakeawayNumber()
            number.printTakeawayNumber(currentTransaction)

            val timeNow = CTimestamp()
            // End transaction and print
            val tfi = currentTransaction.getTimeFrameIndex()

            endTimeFrameAndPrintToBuffer(
               global.mKitchenPrints, global.mKitchenPrints2bill,
               timeNow, tfi,false,
               userCFG.getBoolean("user_print_collect"))

            // End transaction and print
//                if (CFG("bill_print_kitchen_first"))
//                {
//                    CprinterSpoolerProxy::Instance()->checkDatabase();
//                }
         }
         var alreadyPayed = currentTransaction.getPartialTotal(-1);
         val total: CMoney = currentTransaction.getItemTotal() - currentTransaction.getDiscount();

         if (  (orderType != EClientOrdersType.OPEN_PAID)
            && (transType == ETransType.TRANS_TYPE_WOK))
         {
            currentTransaction.addReturnMoney()
            addToEmployee(global.rfidKeyId, EPaymentStatus.PAY_STATUS_UNPAID)
            mTransactionTotals = currentTransaction.getTransactionPayments()
            var newType = EClientOrdersType.CLOSED
            if (CFG.getBoolean("floorplan_bill_first"))
            {
               if (alreadyPayed < total)
               {
                  newType = EClientOrdersType.OPEN
               } else
               {
                  newType = when (orderType)
                  {
                     EClientOrdersType.OPEN_PAID -> EClientOrdersType.CLOSED
                     else -> EClientOrdersType.OPEN_PAID;
                  }
               }
            }
            currentTransaction.closeTransaction(newType);
         } else if (mBillPayingMode == EPayingMode.PAYING_MODE_MANUAL
            || currentTransaction.isTakeaway()
            || alreadyPayed >= total
            || orderType == EClientOrdersType.PAYING
            || orderType == EClientOrdersType.OPEN_PAID
         )
         {
            addReturnMoney();
            currentTransaction.addToEmployee(EPaymentStatus.PAY_STATUS_UNPAID)
            mTransactionTotals = currentTransaction.getTransactionPayments()
            currentTransaction.closeTransaction(EClientOrdersType.CLOSED)
         }
         else
         {
            currentTransaction.setPayingState()
            mTransactionTotals = currentTransaction.getTransactionPayments();
         }
      }
      val quantity = currentTransaction.getBillPrinterQuantity()
      printBills(quantity, mTransactionTotals)

//        if (!CFG("bill_print_kitchen_first"))
//        {
//            CprinterSpoolerProxy::Instance()->checkDatabase();
//        }
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
   }

   override fun onItemRemoved(position: Int, newSize: Int)
   {
   }

   override fun onItemUpdated(position: Int, item: CItem)
   {
   }

   override fun onTransactionCleared()
   {
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
      if (mMode == InitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.addOneToCursorPosition()
         m_isChanged = true
      }
   }

   fun onButtonMin1()
   {
      val currentTransaction = _transaction.value
      if (mMode == InitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.minus1()
         m_isChanged = true
      }
   }

   fun onButtonPortion()
   {
      val currentTransaction = _transaction.value
      if (mMode == InitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.nextPortion()
         m_isChanged = true
      }
   }

   fun onButtonRemove()
   {
      val currentTransaction = _transaction.value
      if (mMode == InitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.remove()
         m_isChanged = true
      }
   }

   /** @brief Init the amount for the bill
    *  @param cleanTipsDiscount [in] Clean the tips and discount
    *  @param payingMode [in] How to pay the amounts
    */
   fun onInitAmounts(payingMode: EPayingMode, cleanTipsDiscount: Boolean)
   {
      val currentTransaction = transaction.value
      if (currentTransaction == null)
      {
         return
      }
      if (cleanTipsDiscount)
      {
         currentTransaction.setDiscount(CMoney(0))
         currentTransaction.setTips(CMoney(0))
      }
      val discount = currentTransaction.getDiscount()
      mShowDiscount = (!discount.empty())
      currentTransaction.updateTotal()

      mAutomaticPayments = false // @todo remove automatic_payments and check all tests.
      currentTransaction.cancelPayments(
         -1, EPaymentStatus.PAY_STATUS_UNPAID
      )
      mPaidTotal = currentTransaction.getPaidTotal(EPaymentStatus.PAY_STATUS_PAID_ORDER_BEFORE)
      val itemTotal = currentTransaction.getItemTotal()

      if ((mBillDefaultAllCash && payingMode == EPayingMode.PAYING_MODE_ALL_CASH)
         || itemTotal.cents() < 0
      )
      {
         // Add payment all cash!
         val m = itemTotal - discount - mPaidTotal
         currentTransaction.addPayment(EPaymentMethod.PAYMENT_CASH, m);
         mAutomaticPayments = true
      }
      mPartialIndex = currentTransaction.getHighestPaymentIndex()
   }


   /*----------------------------------------------------------------------------*/
   /** Print the final bill after all key check and calculations
    *  @param quantity [in] How many bills
    *  @param transactionTotals [in] Totals to print
    */
   fun printBills(quantity: Int, transactionTotals: CPaymentTransaction)
   {
      val currentTransaction = transaction.value
      if (currentTransaction == null)
      {
         return
      }

      // Local language bill print
      val billPrinter = BillPrinter(currentTransaction.transactionId);
      billPrinter.printBill(
         global.euroLang,
         EPrinterLocation.PRINTER_LOCATION_BILL_PRN,
         EBillExample.BILL_NORMAL, quantity, -1,
         transactionTotals, true);
//        if ( m_slipPrints)
//        {
//            billPrinter.printBill( EuroLang, SLIP_PRN, BILL_NORMAL, m_slipPrints, -1, transactionTotals, false);
//        }
   }

   fun setMode(newMode: InitMode)
   {
      mMode = newMode
   }

   fun handleMenuItem(selectedMenuItem: CMenuItem): Boolean
   {
      val currentTransaction = _transaction.value
      if (mMode == InitMode.VIEW_PAGE_ORDER && currentTransaction != null)
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