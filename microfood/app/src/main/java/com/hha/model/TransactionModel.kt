package com.hha.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hha.callback.PaymentsListener
import com.hha.callback.TransactionItemListener
import com.hha.callback.TransactionListener
import com.hha.framework.CItem
import com.hha.framework.CMenuItem
import com.hha.framework.CPayment
import com.hha.framework.CTransaction
import com.hha.dialog.Translation
import com.hha.dialog.Translation.TextId
import com.hha.framework.CCursor
import com.hha.framework.CFloorTables
import com.hha.framework.CMenuCards
import com.hha.framework.COpenClientsHandler
import com.hha.framework.COpenClientsHandler.createNewTakeawayTransaction
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
import com.hha.types.EFinalizerAction
import com.hha.types.EInitAction
import com.hha.types.EInitMode
import com.hha.types.EItemLocation
import com.hha.types.EPayingMode
import com.hha.types.EPaymentMethod
import com.hha.types.EPaymentStatus
import com.hha.types.EPrintBillAction
import com.hha.types.ETimeFrameIndex
import com.hha.types.ETransType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A shared ViewModel that manages the state of a single CTransaction.
 * It acts as the single source of truth for the entire order and payment process.
 * It implements TransactionListener to react to any changes in the underlying CTransaction object.
 */
class TransactionModel : ViewModel(), PaymentsListener, TransactionListener,
   TransactionItemListener
{
   // --- NEW: LiveData to hold the fully-loaded CTransaction ---
   // This will be observed by the Activity to know when to navigate.
   // 1. SINGLE SOURCE OF TRUTH
   private val _transaction = MutableLiveData<CTransaction?>()
   val activeTransaction: LiveData<CTransaction?> = _transaction

   // This line will now be correct and have no errors
   private val _navigateToPageOrder = MutableLiveData<MyEvent<Int>>()
   val navigateToPageOrder: LiveData<MyEvent<Int>> = _navigateToPageOrder

   // LiveData to signal when data is being loaded, allowing the UI to show a progress indicator.
   private val _isLoading = MutableLiveData<Boolean>()
   val isLoading: LiveData<Boolean> = _isLoading
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
   private var initState: EInitAction = EInitAction.INIT_ACTION_NOTHING
   val mContinueWhenAmountEnough = CFG.getBoolean("bill_continue_when_enough")
   private val mAskBillConfirmation = CFG.getBoolean("bill_ask_confirmation")
   private val mBillDefaultAllCash = CFG.getBoolean("bill_default_all_cash")
   private var mFromBilling = false
   private var mPrintKitchen2PosQuantity = userCFG.getValue("user_print_kitchen2pos_quantity")
   private val mAskTime = CFG.getValue("ask_time_delay_takeaway")
   private val mAskQuantity = CFG.getValue("ask_quantity")
   private val mAskQuantityZero = CFG.getBoolean("ask_quantity_zero")
   private val mAskTimeDelaySitin = CFG.getValue("ask_time_delay_sitin")
   private val mAskSitinQuantities = CFG.getValue("ask_sitin_quantities")
   private val mUserPrintCollect = userCFG.getBoolean("user_print_collect")
   private val mAskTakeawayQuantity = CFG.getValue("ask_takeaway_quantity")
   private val mTakeawayContinueUntilCr = CFG.getBoolean("takeaway_continue_until_cr")
   private var mChangedTime = false
   var mAllowNewItem = false
   private val mMinutes = 0
   private var mAutomaticPayments = false // @todo Inspect
   private val mFloorplanBillFirst = CFG.getBoolean("floorplan_bill_first")
   private var mIsUpdating = false // Add this flag to your class

   val billDisplayLines: LiveData<List<BillDisplayLine>> = _displayLines
   val mBillPayingMode = EPayingMode.PAYING_MODE_MANUAL
   private var mEmptyTimeFrame = false
   private var mIsChanged: Boolean = false
   private var mPrintKitchenQuantity = 0
   private var mIsInitialized = false
   private var mCustomerTotal = CMoney(0)
   private var m_returnTotal = CMoney(0)
   private var m_alreadyPayed = false
   private lateinit var mMode: EInitMode
   var mPartialIndex = 0

   private var m_cardTotal = CMoney(0)
   private var m_cashTotal = CMoney(0)
   private var mPaidTotal = CMoney(0)

   // For the simple PageOrderView
   private val _orderTotal = MutableLiveData<CMoney>()
   val orderTotal: LiveData<CMoney> = _orderTotal
   var mCursor = CCursor(0)
   val tag = "transactionModel"

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
      Log.i(tag, "addReturnMoney")
      val currentTransaction = activeTransaction.value
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
      if (totalPayments > total)
      {
         addPayment(EPaymentMethod.PAYMENT_RETURN, total - totalPayments)
         //totalPayments =total
      }
   }

   /**
    * Adds an item to the current transaction.
    */
   fun addItem(item: CMenuItem, clusterId: Short): Boolean
   {
      Log.i(tag, "addItem")
      val currentTransaction = _transaction.value
      // The ViewModel tells the model to change. The listener will handle the UI update automatically.
      if (currentTransaction == null)
      {
         Log.e(tag, "addItem: currentTransaction is null!!")
         return false
      }
      return currentTransaction.addTransactionItem(item, clusterId)
   }

   fun addToEmployee(rfidKey: Short, paymentStatus: EPaymentStatus)
   {
      Log.i(tag, "addToEmployee")
      val currentTransaction = activeTransaction.value
      if (currentTransaction == null)
      {
         return
      }
      val cash = currentTransaction.getCashTotal(paymentStatus)
      val card = currentTransaction.getCardTotal(paymentStatus)
      val returns = currentTransaction.getReturnTotal(paymentStatus)
      val allKitchen = currentTransaction.getAllKitchenTotal(paymentStatus, true)
      val kitchen2 = currentTransaction.getItemTotal(EItemLocation.ITEM_KITCHEN2, paymentStatus, true)
      val drinks = currentTransaction.getItemTotal(EItemLocation.ITEM_DRINKS, paymentStatus, true)
      val bar = currentTransaction.getItemTotal(EItemLocation.ITEM_BAR, paymentStatus, true)
      val sushi = currentTransaction.getItemTotal(EItemLocation.ITEM_SUSHI, paymentStatus, true)
      val nonfood = currentTransaction.getItemTotal(EItemLocation.ITEM_NONFOOD, paymentStatus, true)
      val others = currentTransaction.getItemTotal(EItemLocation.ITEM_OTHERS, paymentStatus, true)


      Log.i(tag, "cash $cash, card $card returns=$returns")
      val employee = CPersonnel().getPerson(global.rfidKeyId)
      employee.add(
         allKitchen, kitchen2, drinks, bar, sushi, nonfood, others,
         cash, card, returns
      )
      employee.update(false)
   }

   fun allowNewItems(allowNewItems: Boolean)
   {
      //mAllowNewItem = allowNewItems
      mAllowNewItem = allowNewItems
//        val currentTransaction = mViewModel.transaction.value
//        if (currentTransaction == null)
//        {
//            return
//        }
//        currentTransaction.allowNewItems(allowNewItems)
   }

   fun closeTimeFrame()
   {
      val currentTransaction = _transaction.value
      if (currentTransaction != null)
      {
         currentTransaction.closeTimeFrame()
         if (currentTransaction.transactionEmptyAtStartAndAtEnd())
         {
            currentTransaction.emptyTransaction("")
         }
      }
   }

   fun confirmPrintBill(offer: Boolean)
   {
      val currentTransaction = activeTransaction.value
      if (currentTransaction == null)
      {
         return
      }
      if (offer) // 687
      {
         currentTransaction.setStatus(EClientOrdersType.OPEN)
      }
      val orderType = currentTransaction.getStatus()
      if (orderType == EClientOrdersType.OPEN
         || orderType == EClientOrdersType.OPEN_PAID
         || orderType == EClientOrdersType.PAYING
      )
      {
//            if (!checkBillingKey())
//            {
//                CmessageBox cm( "mbx::print_bill_not_valid", BTN9+2, BTN1-2, BTN3+2, BTN1+2, getTranslation(_KEY_BAD), MB_BEEP
//                | MB_TIME3 | MB_TEXT_CENTER)
//                cancelNewPayments()
//                stop( MODE_ASK_TABLE_BILL_DIALOG)
//                return
//            }
         currentTransaction.setEmployeeId(global.rfidKeyId)
         val transType = currentTransaction.transType

         // todo: phone_order_bill_direct
         if (transType == ETransType.TRANS_TYPE_TAKEAWAY
            || transType == ETransType.TRANS_TYPE_EAT_INSIDE
         )
         {
            val number = TakeawayNumber()
            number.printTakeawayNumber(currentTransaction)

            val timeNow = CTimestamp()
            // End transaction and print
            val tfi = currentTransaction.getTimeFrameIndex()

            endTimeFrameAndPrintToBuffer(
               global.mKitchenPrints, global.mKitchenPrints2bill,
               timeNow, false,
               mUserPrintCollect
            )
         }
         var alreadyPayed = currentTransaction.getPartialTotal(-1)
         val total: CMoney = currentTransaction.getItemTotal() - currentTransaction.getDiscount()

         if ((orderType != EClientOrdersType.OPEN_PAID)
            && (transType == ETransType.TRANS_TYPE_WOK)
         )
         {
            currentTransaction.addReturnMoney()
            addToEmployee(global.rfidKeyId, EPaymentStatus.PAY_STATUS_UNPAID)
            mTransactionTotals = currentTransaction.getTransactionPayments()
            var newType = EClientOrdersType.CLOSED
            if (mFloorplanBillFirst)
            {
               if (alreadyPayed < total)
               {
                  newType = EClientOrdersType.OPEN
               } else
               {
                  newType = when (orderType)
                  {
                     EClientOrdersType.OPEN_PAID -> EClientOrdersType.CLOSED
                     else -> EClientOrdersType.OPEN_PAID
                  }
               }
            }
            currentTransaction.closeTransaction(newType)
         } else if (mBillPayingMode == EPayingMode.PAYING_MODE_MANUAL
            || currentTransaction.isTakeaway()
            || alreadyPayed >= total
            || orderType == EClientOrdersType.PAYING
            || orderType == EClientOrdersType.OPEN_PAID
         )
         {
            addReturnMoney()
            currentTransaction.addToEmployee(EPaymentStatus.PAY_STATUS_UNPAID)
            mTransactionTotals = currentTransaction.getTransactionPayments()
            currentTransaction.closeTransaction(EClientOrdersType.CLOSED)
         } else
         {
            currentTransaction.setPayingState()
            mTransactionTotals = currentTransaction.getTransactionPayments()
         }
      }
      val quantity = currentTransaction.getBillPrinterQuantity()
      printBills(quantity, mTransactionTotals)

//        if (!CFG("bill_print_kitchen_first"))
//        {
//            CprinterSpoolerProxy::Instance()->checkDatabase()
//        }
   }

   /* Creates a NEW transaction associated with a specific tableId, loads the
    * full CTransaction object, and posts it to the navigateToTransaction LiveData.
    *
    * @param tableId The ID of the floor table to associate with the new transaction.
    * @param minutes The duration or other parameter needed for creation.
    */
   fun createTransactionForTable(tableName: String, tableId: Int, minutes: Int)
   {
      Log.i(tag, "createTransactionForTable $tableName, $tableId")
      viewModelScope.launch {
         _isLoading.value = true
         val newTransactionId = withContext(Dispatchers.IO)
         {
            // 1. Call your gRPC/framework function to create a new transaction record
            //    and link it to the tableId. This should return the new transaction's ID.
            COpenClientsHandler.createNewRestaurantTransaction(tableName, tableId, minutes)
         }
         _isLoading.value = false
         Log.d(tag, "createTransactionForTable: newTransactionId = $newTransactionId")

         // 2. If creation was successful, load the full CTransaction object.
         if (newTransactionId > 0)
         {
            // 3. --- THIS IS THE FIX ---
            // Creation was successful. Immediately post the ID as a navigation event.
            // We no longer create a CTransaction object here.
            Log.d(tag, "Posting navigation event for transaction ID: $newTransactionId")
            _navigateToPageOrder.value = MyEvent(newTransactionId)
         } else
         {
            // Handle the error if the transaction could not be created.
            // For example, post to another LiveData to show a Toast.
            Log.e(tag, "Failed to create new transaction for table: $tableName (ID: $tableId)")
            // _showToast.value = MyEvent("Failed to create order")
         }
      }
   }

   fun endTimeFrameAndPrintToBuffer(
      prints2kitchen: Int, prints2bill: Int, newTime: CTimestamp,
      timeChanged: Boolean, collectPrinter: Boolean
   )
   {
      Log.i(tag, "endTimeFrameAndPrintToBuffer")
      val currentTransaction = activeTransaction.value
      if (currentTransaction == null)
      {
         return
      }
      val tfi = currentTransaction.getTimeFrameIndex()

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
         newTime, timeChanged, newState
      )

      val pf = PrintFrame()
      pf.printTimeFrame(
         transactionId, tfi, CFG.getShort("pc_number"),
         global.rfidKeyId, prints2kitchen, 0, collectPrinter, false
      )
      pf.printTimeFrame(
         transactionId, tfi, CFG.getShort("pc_number"),
         global.rfidKeyId, prints2bill, 0, false, true
      )
   }

   fun finishTransaction(fromBilling: Boolean): EFinalizerAction
   {
      Log.i(tag, "finishTransaction")
      mFromBilling = fromBilling
      mPrintKitchen2PosQuantity = userCFG.getValue("user_print_kitchen2pos_quantity")
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return EFinalizerAction.FINALIZE_NO_ACTION
      }
      var action = EFinalizerAction.FINALIZE_NO_ACTION
      val transType = currentTransaction.transType
      when (transType)
      {
         ETransType.TRANS_TYPE_DELIVERY -> action = handleFinishDelivery(fromBilling)
         ETransType.TRANS_TYPE_RECHAUD -> action = handleFinishRechaud(fromBilling)
         ETransType.TRANS_TYPE_TAKEAWAY, ETransType.TRANS_TYPE_EAT_INSIDE ->
            action = handleFinishTakeaway()

         ETransType.TRANS_TYPE_TAKEAWAY_PHONE -> action = handleFinishTakeawayPhone(fromBilling)
         ETransType.TRANS_TYPE_WOK -> action = handleFinishWok(fromBilling)
         ETransType.TRANS_TYPE_SITIN, ETransType.TRANS_TYPE_SHOP -> action = handleFinishSitin(fromBilling)
         else ->
         {
         }
      }
      if (CFG.getBoolean("restaurant_map"))
      {
         CFloorTables().setTransactionAvailable(currentTransaction.transactionId)
      }
      return action
   }

   fun handleFinishDelivery(fromBilling: Boolean): EFinalizerAction
   {
      // @TODO implement
      return EFinalizerAction.FINALIZE_TO_BE_IMPLEMENTED
   }

   fun handleFinishRechaud(fromBilling: Boolean): EFinalizerAction
   {
      // @TODO implement
      return EFinalizerAction.FINALIZE_TO_BE_IMPLEMENTED
   }

   fun handleFinishTakeawayQuantity(ts: CTimestamp): EFinalizerAction
   {
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return EFinalizerAction.FINALIZE_NOT_IDENTIFIED
      }
      if (mAskTakeawayQuantity != 0)
      {
         ///////////////////////// ASK QUANTITY ////////////////////////////////
         val retVal = askTakeawayFinalQuantity(ts, mChangedTime);
         return retVal;
      }

      // No printing of kitchen, just go to billing...
      endTimeFrameAndPrintToBuffer(
         0, 0, ts,
         mChangedTime, false
      )

      if (!currentTransaction.empty)
      {
         currentTransaction.addReturnMoney()
         currentTransaction.closeTransaction(EClientOrdersType.CLOSED)
         return EFinalizerAction.FINALIZE_MODE_ASK_TABLE
      }
      return EFinalizerAction.FINALIZE_MODE_BILLING
   }

   /*----------------------------------------------------------------------------*/
   /** Ask final quantity takeaway.
    *  @return finalize action when Finished asking the quantity
    */
   fun askTakeawayFinalQuantity(ts: CTimestamp, changedTime: Boolean): EFinalizerAction
   {
      Log.i(tag, "askTakeawayFinalQuantity")
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return EFinalizerAction.FINALIZE_NOT_IDENTIFIED
      }
      if (hasAnyChanges())
      {
         return EFinalizerAction.FINALIZE_MODAL_DIALOG_QUANTITY
      } else if (currentTransaction.empty && hasAnyChanges())
      {
         return EFinalizerAction.FINALIZE_ASK_CANCEL_REASON
      }
      val cs = CTimestamp()
      return setTakeawayFinalQuantity(
         0, true, false, ts, false
      )
   }

   /*----------------------------------------------------------------------------*/
   fun setTakeawayFinalQuantity(
      quantity: Int, billingMode: Boolean, stop: Boolean,
      ts: CTimestamp, changedTime: Boolean
   ): EFinalizerAction
   {
      var retVal = EFinalizerAction.FINALIZE_NO_ACTION
      val currentTransaction: CTransaction? = _transaction.value
      if (currentTransaction == null)
      {
         return retVal
      }
      if (quantity != 0 || mAskQuantityZero)
      {
         if (billingMode)
         {
            retVal = EFinalizerAction.FINALIZE_MODE_BILLING
         } else if (stop)
         {
            retVal = EFinalizerAction.FINALIZE_NO_ACTION
         } else
         {
            if (mTakeawayContinueUntilCr)
            {
               // For takeaway, just continue and don't stop!!
               endTimeFrameAndPrintToBuffer(
                  quantity, 0, ts,
                  changedTime, false
               );
               currentTransaction.startTimeFrame()
               retVal = EFinalizerAction.FINALIZE_RESET_CHANGES
            } else
            {
               endTimeFrameAndPrintToBuffer(
                  0, 0, ts,
                  changedTime, false
               );
               retVal = EFinalizerAction.FINALIZE_MODE_BILLING
            }
         }
      }
      return retVal
   }

   fun handleFinishShop(fromBilling: Boolean): EFinalizerAction
   {
      // @TODO implement
      return EFinalizerAction.FINALIZE_TO_BE_IMPLEMENTED
   }

   /*----------------------------------------------------------------------------*/
   /** @brief Ask the delay in minutes and close the transaction.
    *  @return Status for finish function
    */
   private fun handleFinishSitin(fromBilling: Boolean): EFinalizerAction
   {
      Log.i(tag, "handleFinishSitin")

      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return EFinalizerAction.FINALIZE_NOT_IDENTIFIED
      }
      //////////////////// ASK QUANTIES SITIN ///////////////////////////
      var action: EFinalizerAction = handleFinishQuantiesTimeSitin(fromBilling)
      if (action != EFinalizerAction.FINALIZE_NOT_IDENTIFIED)
      {
         return action;
      }

      ///////////////////////// ASK TIME ////////////////////////////////
      if (mAskTimeDelaySitin > 0 && currentTransaction.hasAnyChanges())
      {
         return EFinalizerAction.FINALIZE_SI_ASK_DELAY_MINUTES
      }
      ///////////////////////// ASK QUANTITY ////////////////////////////////
      if (mAskQuantity != 0)
      {
         action = EFinalizerAction.FINALIZE_NO_ACTION
         if (currentTransaction.hasAnyChanges())
         {
            return EFinalizerAction.FINALIZE_MODAL_DIALOG_QUANTITY
         }
      }
      val ts = CTimestamp()
      ts.addMinutes(mMinutes)
      endTimeFrameAndPrintToBuffer(
         1, 0, ts,
         mMinutes != 0, false)
      return EFinalizerAction.FINALIZE_MODE_ASK_TABLE
   }

   fun handleFinishTakeawayPhone(fromBilling: Boolean): EFinalizerAction
   {
      // @TODO implement
      return EFinalizerAction.FINALIZE_TO_BE_IMPLEMENTED
   }

   fun handleFinishWok(fromBilling: Boolean): EFinalizerAction
   {
      Log.i(tag, "handleFinishWok $fromBilling")

      //////////////////// ASK QUANTIES SITIN ///////////////////////////
      var action: EFinalizerAction = handleFinishQuantiesTimeWok(fromBilling)
      if (action != EFinalizerAction.FINALIZE_NOT_IDENTIFIED)
      {
         return action
      }

      ///////////////////////// ASK QUANTITY ////////////////////////////////
      if (mAskQuantity > 0)
      {
         action = EFinalizerAction.FINALIZE_NO_ACTION
         if (hasAnyChanges())
         {
            return EFinalizerAction.FINALIZE_MODAL_DIALOG_QUANTITY
         }
      } else if (mFloorplanBillFirst)
      {
         endTimeFrameAndPrintToBuffer(
            1, 0, CTimestamp(), false, false
         )
         action = EFinalizerAction.FINALIZE_MODE_BILLING
      } else
      {
         action = EFinalizerAction.FINALIZE_SET_ENTER_PRESSED
      }
      return action
   }

   fun handleFinishQuantity(
      quantity: Int, billingMode: Boolean, stop: Boolean): EFinalizerAction
   {
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return EFinalizerAction.FINALIZE_NOT_IDENTIFIED
      }
      when (currentTransaction.transType)
      {
         ETransType.TRANS_TYPE_WOK -> return handleFinishWokQuantity(quantity, billingMode, stop)
         ETransType.TRANS_TYPE_TAKEAWAY -> return EFinalizerAction.FINALIZE_NO_ACTION
         ETransType.TRANS_TYPE_SITIN -> return handleFinishSitinQuantity(quantity, billingMode, stop)

         else -> {
            Log.w(tag, "handleFinishQuantity: unknown transaction type ${currentTransaction.transType}")
            return EFinalizerAction.FINALIZE_NOT_IDENTIFIED
         }
      }
   }

   fun handleFinishSitinQuantity(
      quantity: Int, toBilling: Boolean, stop: Boolean
   ): EFinalizerAction
   {
      Log.i(tag, "handleFinishSitinQuantity")

      var action = EFinalizerAction.FINALIZE_NO_ACTION

      if (stop)
      {
         return EFinalizerAction.FINALIZE_NO_ACTION
      }
      if (quantity==0 && !mAskQuantityZero)
      {
         return EFinalizerAction.FINALIZE_NO_ACTION
      }
      val ts = CTimestamp()
      ts.addMinutes(mMinutes)
      endTimeFrameAndPrintToBuffer(
         quantity,0, ts,
         mMinutes!=0, false)
      return when (toBilling)
      {
         true -> EFinalizerAction.FINALIZE_MODE_BILLING
         else -> EFinalizerAction.FINALIZE_MODE_ASK_TABLE
      }
   }

   fun handleFinishWokQuantity(
      quantity: Int, billingMode: Boolean, stop: Boolean
   ): EFinalizerAction
   {
      val action: EFinalizerAction = when
      {
         billingMode -> onQuantitySelectedBill()
         stop -> EFinalizerAction.FINALIZE_NO_ACTION
         else -> onQuantitySelected(quantity)
      }
      return action
   }

   fun handleFinishQuantiesTimeWok(fromBilling: Boolean): EFinalizerAction
   {
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return EFinalizerAction.FINALIZE_NOT_IDENTIFIED
      }
      if (currentTransaction.transType != ETransType.TRANS_TYPE_WOK)
      {
         return EFinalizerAction.FINALIZE_NOT_IDENTIFIED
      }
      return handleFinishQuantiesTimeSitin(fromBilling)
   }

   fun handleFinishQuantiesTimeSitin(fromBilling: Boolean): EFinalizerAction
   {
      Log.i(tag, "handleFinishQuantiesTimeSitin")
      var action = EFinalizerAction.FINALIZE_NOT_IDENTIFIED
      val EInitMode = EInitMode.VIEW_NONE
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
         return action

      if (currentTransaction.empty)
      {
         if (hasAnyChanges())
         {
            return EFinalizerAction.FINALIZE_ASK_CANCEL_REASON
         } else
         {
            currentTransaction.emptyTransaction("")
            return EFinalizerAction.FINALIZE_MODE_ASK_TABLE
         }
      }
      if (mAskSitinQuantities == 0)
         return EFinalizerAction.FINALIZE_NOT_IDENTIFIED

      if (!hasAnyChanges())
      {
         val ts = CTimestamp()
         ts.addMinutes(mMinutes)
         endTimeFrameAndPrintToBuffer(
            0, 0, ts,
            false, false
         )
         return when (fromBilling)
         {
            true -> EFinalizerAction.FINALIZE_MODE_BILLING
            false -> EFinalizerAction.FINALIZE_MODE_ASK_TABLE
         }
      }
      // There are changes.
      return EFinalizerAction.FINALIZE_MODAL_DIALOG_QUANTITIES
   }

   fun handleFinishQuantiesAfterAskingQuantities(
      fromBilling: Boolean, quantityKitchen: Int,
      quantityKitchen2Pos: Int,
      changedTime: Boolean, ts: CTimestamp,
      isBillingButton: Boolean
   ): EFinalizerAction
   {
      var retVal = EFinalizerAction.FINALIZE_NOT_IDENTIFIED
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
         return retVal
      mPrintKitchenQuantity = quantityKitchen
      mPrintKitchen2PosQuantity = quantityKitchen2Pos
      mChangedTime = changedTime
      if (mChangedTime)
      {
         currentTransaction.changeDeliverTime(ts)
         mChangedTime = true
      }

      if (!isBillingButton)
      {
         endTimeFrameAndPrintToBuffer(
            quantityKitchen, quantityKitchen2Pos,
            ts, mChangedTime, false
         )
         retVal = when (fromBilling)
         {
            true -> EFinalizerAction.FINALIZE_MODE_BILLING
            false -> EFinalizerAction.FINALIZE_MODE_ASK_TABLE
         }
      } else
      {
         retVal = EFinalizerAction.FINALIZE_MODE_BILLING
      }
      return retVal
   }

   fun findOpenTable(name: String): Int?
   {
      val service = GrpcServiceFactory.createDailyTransactionService()
      val table = service.findOpenTable(name)
      return table
   }

   /** @brief Handle takeaway finish
    *  @return true when the transaction is a takeaway
    */
   fun handleFinishTakeaway(): EFinalizerAction
   {
      val currentTransaction: CTransaction? = _transaction.value
      if (currentTransaction == null)
      {
         return EFinalizerAction.FINALIZE_NO_ACTION
      }
      if (!currentTransaction.isTakeaway())
      {
         return EFinalizerAction.FINALIZE_NOT_IDENTIFIED
      }
      if (currentTransaction.size == 0 && mIsChanged)
      {
         return EFinalizerAction.FINALIZE_ASK_CANCEL_REASON
      }
      if (currentTransaction.size == 0 && !mIsChanged)
      {
         // Stay in ordering mode, there is nothing ordered yet!
         return EFinalizerAction.FINALIZE_NO_ACTION
      }
      var action = handleFinishQuantiesTimeTakeaway()
      if (action != EFinalizerAction.FINALIZE_NOT_IDENTIFIED)
      {
         return action
      }
      // From here always ask time and delay
      var ts = CTimestamp()
      mChangedTime = false

      ///////////////////////// ASK TIME ////////////////////////////////
      if (mAskTime > 0 && !currentTransaction.empty)
      {
         if (mAskTime != 2)
         {
            return EFinalizerAction.FINALIZE_TA_ASK_DELAY_MINUTES
//               CmessageBoxDelay delay(RIGHT(BTN7-1), BTN8+5)
//               if ( delay.m_valid ==false)
//               {
//                  return FINALIZE_NO_ACTION
//               }
//               int minutes =( delay.m_valid ==false) ? 0:delay.m_minutes_delay
//               m_changedTime = (minutes !=0)
//               ts.addMinutes(minutes)
         }
//            else
//            {
//               CMessageBoxTimeEdit mb(BTN4-1, BTN3-1, FLAG_NO_NEW_TIME_ALLOWED, _TIME2)
//               if ( mb.valid() ==false)
//               {
//                  return FINALIZE_NO_ACTION
//               }
//               ts =mb.getTime()
//               m_changedTime = mb.isChanged()
//            }
//         }
//         if (CFG("ask_takeaway_quantity"))
//         {
//            ///////////////////////// ASK QUANTITY ////////////////////////////////
//            EfinalizerAction retVal = askTakeawayFinalQuantity(ts, m_changedTime)
//            return retVal
//         }
//         // No printing of kitchen, just go to billing...
//         m_clientOrdersHandler->endTimeFrameAndPrint( 0,0, ts, m_changedTime, false)
//
//         if ( m_clientOrdersHandler->size() !=0)
//         {
//               m_messageHandler->sendMsgTable( MODULE_BILLING, m_clientOrdersHandler->getDbTransactionId())
//            return FINALIZE_MODE_BILLING
//         }
//         else
//         {
//               m_clientOrdersHandler->addReturnMoney()
//            m_clientOrdersHandler->closeTransaction( CLIENT_ORDER_CLOSED)
//            return FINALIZE_MODE_ASK_TABLE
//         }
//         return FINALIZE_NO_ACTION
      }
      return EFinalizerAction.FINALIZE_NO_ACTION
   }

   fun handleFinishQuantiesTimeTakeaway(): EFinalizerAction
   {
      // Let's not implement for now, probably feature not required.
      return EFinalizerAction.FINALIZE_NOT_IDENTIFIED
   }

   fun handleMenuItem(selectedMenuItem: CMenuItem): Boolean
   {
      val currentTransaction = _transaction.value
      if (mMode == EInitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         val clusterId: Short = -1
         //Log.d(tag, "MenuItem clicked: ${selectedMenuItem.localName}")
         if (currentTransaction.addTransactionItem(selectedMenuItem, clusterId))
         {
            mIsChanged = true
            return true
         }
      }
      return false
   }

   fun initializeTransaction(initMode: EInitMode)
   {
      mMode = initMode
   }

   fun emptyTransaction(reason: String)
   {
      val currentTransaction = _transaction.value
      currentTransaction?.emptyTransaction(reason)
   }

   fun onInit(): EInitAction
   {
      mEmptyTimeFrame = false
      val currentTransaction = activeTransaction.value
      if (currentTransaction == null)
      {
         return EInitAction.INIT_ACTION_NOTHING
      }
      val type: EClientOrdersType = currentTransaction.getStatus()
      Log.i(tag, "onInit %$type")
      when (type)
      {
         EClientOrdersType.OPEN ->
         {
            onInitAmounts(EPayingMode.PAYING_MODE_ALL_CASH, true)
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
            mAlreadyPayed = false
         }

         EClientOrdersType.OPEN_PAID ->
         {
            onInitAmounts(EPayingMode.PAYING_MODE_ALL_CASH, true)
            allowNewItems(true)
            mAlreadyPayed = true
         }

         EClientOrdersType.CLOSED, EClientOrdersType.EMPTY ->
         {
//            stop(MODE_BILL_PAYMENTS_DIALOG)
            Log.i(tag, "onInit  BILL PAYMENTS DIALOG!!")
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
            allowNewItems(false)
            mAlreadyPayed = true
         }

         else ->
         {
            allowNewItems((CFG.getValue("change_after_bill") > 0))
            mPartialIndex = currentTransaction.getHighestPaymentIndex()
            mAlreadyPayed = true
         }
      }
      return EInitAction.INIT_ACTION_NOTHING
   }


   fun discountVisible(): Boolean
   {
      Log.i(tag, "discountVisible")
      val currentTransaction = activeTransaction.value
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

   fun getCursor(item: CItem): Int
   {
      Log.i(tag, "getCursor")
      val currentTransaction = _transaction.value
      return currentTransaction?.getCursor(item) ?: 0
   }


   fun getRequiredAdditionalPayment(): CMoney
   {
      Log.i(tag, "getRequiredAdditionalPayment")
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return CMoney(0)
      }
      // return true if a payment should be requested.
      if (!m_alreadyPayed && currentTransaction.transType != ETransType.TRANS_TYPE_WOK)
      {
         val returnTotal = mCustomerTotal - m_cashTotal - m_cardTotal
         if (returnTotal > CMoney(0))
         {
            return returnTotal
         }
      }
      return CMoney(0)
   }

   fun getTableName(): String
   {
      Log.i(tag, "getTableName")
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return "--"
      }
      var type = when (currentTransaction.transType)
      {
         ETransType.TRANS_TYPE_EAT_INSIDE,
         ETransType.TRANS_TYPE_WOK, ETransType.TRANS_TYPE_RECHAUD,
         ETransType.TRANS_TYPE_SITIN
            -> Translation.get(TextId.TEXT_TABLE)

         ETransType.TRANS_TYPE_DELIVERY -> Translation.get(TextId.TEXT_TELEPHONE)
         ETransType.TRANS_TYPE_TAKEAWAY,
         ETransType.TRANS_TYPE_TAKEAWAY_PHONE
            -> Translation.get(TextId.TEXT_TAKEAWAY)

         else -> "--"
      }
      type = type + " " + currentTransaction.getName()
      return type
   }

   fun hasAnyChanges(): Boolean
   {
      Log.i(tag, "hasAnyChanges")
      val currentTransaction = _transaction.value
      return currentTransaction?.hasAnyChanges() ?: false
   }

   /**
    * Initializes the transaction based on the specified mode.
    * This function is safe to call multiple times but will only execute the core logic once.
    *
    * @param mode The initialization mode (FULL_ORDER or BILLING_ONLY).
    */
   fun initializeTransaction(transactionId: Int, mode: EInitMode)
   {
      Log.i(tag, "initializeTransaction")
      // Perform initial checks on the main thread. These are fast.
      if (mIsInitialized || (mode == EInitMode.VIEW_BILLING && _transaction.value == null))
      {
         return
      }
      val THIS = this

      // Launch a coroutine to handle the potentially long-running transaction loading
      viewModelScope.launch {
         // You can set a loading state here if needed, similar to listOpen()
         _isLoading.value = true

         val global = Global.getInstance()
         var currentTransaction: CTransaction? = _transaction.value

         // If the transaction doesn't exist yet, load it in the background.
         if (currentTransaction == null)
         {
            // Switch to a background thread for I/O operations
            currentTransaction = withContext(Dispatchers.IO) {
               if (transactionId < 1E6)
               {
                  Log.e(tag, "Invalid global transaction ID, cannot load.")
                  null // Return null from the background thread
               } else
               {
                  Log.d(tag, "Loading transaction with ID: ${transactionId}")
                  val newTransaction = CTransaction(transactionId)
                  // Assuming .load() is the blocking call that loads data.
                  // This now runs safely in the background.
                  if (newTransaction.isValid())
                  {
                     newTransaction // Return the loaded transaction
                  } else
                  {
                     Log.e(tag, "Failed to load transaction data for ID: ${transactionId}")
                     null // Return null on failure
                  }
               }
            }
         }

         // Back on the main thread now. Check the result from the background work.
         if (currentTransaction == null)
         {
            Log.e(tag, "Transaction is null after attempting to load. Aborting initialization.")
            // _isLoading.value = false
            return@launch // Exit the coroutine
         }

         // Now that we have a valid transaction, update the LiveData
         _transaction.value = currentTransaction

         // It's now safe to add listeners and perform other main-thread logic
         mMode = mode
         if (currentTransaction != null)
         {
            currentTransaction.addListener(THIS)
            currentTransaction.addPaymentListener(THIS)
            currentTransaction.addItemListener(THIS)
            // Trigger an initial UI update
            onTransactionChanged(currentTransaction)
            if (mode == EInitMode.VIEW_PAGE_ORDER)
            {
               currentTransaction.startNextTimeFrame()
            }
            mIsInitialized = true
         }
         _isLoading.value = false
      }
   }

   fun isChanged(): Boolean = mIsChanged

   /*----------------------------------------------------------------------------*/
   /** Check if we can print a bill, also ask whether it is allowed
    *  @return true when allowed to print
    */
   fun isPrintBillAllowedAndConfirmed(
      currentTransaction: CTransaction, offer: Boolean,
      customerTotal: CMoney
   ): EPrintBillAction
   {
      Log.i(tag, "isPrintBillAllowedAndConfirmed")

      var printBill = true
      val transactionId = currentTransaction.transactionId
      val type = currentTransaction.getStatus()

      if (!currentTransaction.isValid())  // 594
      {
         // Invalid transaction, should not happen
         Log.e("BillOrderActivity", "not valid transaction=$transactionId!!")
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

            else ->
            {
            }
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

   fun needToAskCancelReason(): Boolean
   {
      Log.i(tag, "needToAskCancelReason")
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

   /**
    * The ViewModel is automatically cleared by the framework.
    * We should remove our listener to prevent memory leaks.
    */
   override fun onCleared()
   {
      Log.i(tag, "onCleared")
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
      // Get the current transaction object which now contains the newly added item.
      val currentTransaction = _transaction.value
      if (currentTransaction != null)
      {
         // Call the main update function to notify all observers.
         onTransactionChanged(currentTransaction)
      }
   }

   override fun onItemRemoved(position: Int, newSize: Int)
   {
   }

   override fun onItemUpdated(position: Int, item: CItem)
   {
   }

   /**
    * We need a way to reset the LiveData after navigation is complete.
    * Let's create a function for it.
    */
   fun onNavigationComplete()
   {
      _transaction.value = null
   }

   fun onQuantitySelected(quantity: Int): EFinalizerAction
   {
      Log.i(tag, "onQuantitySelected")
      val ts = CTimestamp()
      ts.addMinutes(mMinutes)
      return onQuantitySelected(mFromBilling, quantity, ts, mMinutes != 0)
   }

   /*----------------------------------------------------------------------------*/
   fun onQuantitySelected(
      fromBilling: Boolean, quantity: Int, minutes: Int
   ): EFinalizerAction
   {
      Log.i(tag, "onQuantitySelected")
      val ts = CTimestamp()
      ts.addMinutes(minutes)
      return onQuantitySelected(fromBilling, quantity, ts, minutes != 0)
   }

   fun onButtonPlus1()
   {
      Log.i(tag, "onButtonPlus1")
      val currentTransaction = _transaction.value
      if (mMode == EInitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.addOneToCursorPosition()
         mIsChanged = true
      }
   }

   fun onButtonMin1()
   {
      Log.i(tag, "onButtonMin1")
      val currentTransaction = _transaction.value
      if (mMode == EInitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.minus1()
         mIsChanged = true
      }
   }

   fun onButtonPortion()
   {
      Log.i(tag, "onButtonPortion")
      val currentTransaction = _transaction.value
      if (mMode == EInitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.nextPortion()
         mIsChanged = true
      }
   }

   fun onButtonRemove()
   {
      Log.i(tag, "onButtonRemove")
      val currentTransaction = _transaction.value
      if (mMode == EInitMode.VIEW_PAGE_ORDER && currentTransaction != null)
      {
         currentTransaction.remove()
         mIsChanged = true
      }
   }

   /** @brief Init the amount for the bill
    *  @param cleanTipsDiscount [in] Clean the tips and discount
    *  @param payingMode [in] How to pay the amounts
    */
   fun onInitAmounts(payingMode: EPayingMode, cleanTipsDiscount: Boolean)
   {
      Log.i(tag, "onInitAmounts")
      val currentTransaction = activeTransaction.value
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
         currentTransaction.addPayment(EPaymentMethod.PAYMENT_CASH, m)
         mAutomaticPayments = true
      }
      mPartialIndex = currentTransaction.getHighestPaymentIndex()
   }

   /*----------------------------------------------------------------------------*/
   fun onQuantitySelected(
      fromBilling: Boolean, quantity: Int, ts: CTimestamp,
      timeChanged: Boolean
   ): EFinalizerAction
   {
      Log.i(tag, "onQuantitySelected")
      val currentTransaction: CTransaction? = _transaction.value
      if (currentTransaction == null)
      {
         return EFinalizerAction.FINALIZE_NO_ACTION
      }
      endTimeFrameAndPrintToBuffer(
         quantity, 0, ts,
         timeChanged, false
      )
      val action = when (fromBilling || mFloorplanBillFirst)
      {
         true -> EFinalizerAction.FINALIZE_MODE_BILLING
         else -> EFinalizerAction.FINALIZE_MODE_ASK_TABLE
      }
      return action
   }

   fun onQuantitySelectedBill(): EFinalizerAction
   {
      TODO("implement")
      return EFinalizerAction.FINALIZE_TO_BE_IMPLEMENTED
   }

   override fun onTransactionCleared()
   {
   }

   // 4. LISTENER IMPLEMENTATION
   /**
    * This is the single entry point for all updates from the model.
    * It is called by CTransaction whenever any of its data (items, payments, etc.) changes.
    */
   override fun onTransactionChanged(transaction: CTransaction)
   {
      if (mIsUpdating)
      {
         return
      }
      try
      {
         mIsUpdating = true
         _transaction.postValue(transaction)
         // When any part of the transaction changes, update all relevant LiveData.
         transaction.calculateTotalTransaction()
         _orderTotal.postValue(transaction.getTotalTransaction())
         prepareBillDisplayLines()
      } finally {
         mIsUpdating = false
      }
   }


   fun payAllUsing(paymentMethod: EPaymentMethod)
   {
      Log.i(tag, "payAllUsing ${paymentMethod.toString()}")
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
      {
         return
      }
      currentTransaction.cancelPayments(
         -1, EPaymentStatus.PAY_STATUS_UNPAID
      )

      currentTransaction.addPayment(
         paymentMethod, mCustomerTotal
      )
   }

   fun payAllUsingCash()
   {
      payAllUsing(EPaymentMethod.PAYMENT_CASH)
   }

   fun payAllUsingPin()
   {
      payAllUsing(EPaymentMethod.PAYMENT_PIN)
   }

   fun payEuroButton(amount: CMoney)
   {
      payEuros(amount)
   }

   /**
    * Handles the logic for when a user presses a cash payment button (e.g., €5, €10).
    * It contains the business rule to clear previous payments if needed.
    */
   fun payEuros(amount: CMoney)
   {
      Log.i(tag, "payEuros")
      val currentTransaction = _transaction.value
      currentTransaction?.addPayment(EPaymentMethod.PAYMENT_CASH, amount)
   }

   // 6. UI PREPARATION LOGIC
   /**
    * Prepares the detailed list of display lines for the BillOrderView.
    * This logic is now correctly located within the ViewModel.
    */
   fun prepareBillDisplayLines()
   {
      Log.i(tag, "prepareBillDisplayLines")
      val currentTransaction = _transaction.value
      if (currentTransaction == null)
         return
      val lines = mutableListOf<BillDisplayLine>()

      // Get total from the 'transaction' object
      val kitchenTotal = currentTransaction.getKitchenTotal()
      val drinksTotal = currentTransaction.getDrinksTotal()
      val itemTotal = currentTransaction.getItemTotal()
      val discount = currentTransaction.getDiscount()
      val totalPaid = currentTransaction.getTotalAlreadyPaid()
      mCustomerTotal = itemTotal - discount - totalPaid
      val payments: List<CPayment> = currentTransaction.getPayments()

      m_cashTotal.clear()
      m_cardTotal.clear()
      for (payment in payments)
      {
         if (payment.paymentMethod == EPaymentMethod.PAYMENT_CASH)
            m_cashTotal = m_cashTotal + payment.total
         else
            m_cardTotal = m_cardTotal + payment.total
      }
      m_returnTotal = m_cashTotal + m_cardTotal - mCustomerTotal

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
      if (totalPaid != CMoney(0) || discount != CMoney(0))
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
            amount = mCustomerTotal,
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
      if (!mCustomerTotal.empty())
      {
         lines.add(
            BillDisplayLine(
               id = ++myId,
               iconResId = null,
               text = Translation.get(TextId.TEXT_CLIENT_AMOUNT),
               amount = mCustomerTotal,
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

   // 5. PUBLIC ACTIONS
   // These are the functions that the UI (Activities/Fragments) will call to request state changes.

   /*----------------------------------------------------------------------------*/
   /** Print the final bill after all key check and calculations
    *  @param quantity [in] How many bills
    *  @param transactionTotals [in] Totals to print
    */
   fun printBills(quantity: Int, transactionTotals: CPaymentTransaction)
   {
      val currentTransaction = activeTransaction.value
      if (currentTransaction == null)
      {
         return
      }

      // Local language bill print
      val billPrinter = BillPrinter(currentTransaction.transactionId)
      billPrinter.printBill(
         global.euroLang,
         EPrinterLocation.PRINTER_LOCATION_BILL_PRN,
         EBillExample.BILL_NORMAL, quantity, -1,
         transactionTotals, true
      )
   }

   fun refreshAllPayments()
   {
      // When changing the language
      prepareBillDisplayLines()
   }

   fun resetChanges()
   {
   }

   fun resetIsChanged()
   {
      mIsChanged = false
   }

   fun setMode(newMode: EInitMode)
   {
      mMode = newMode
   }

   /**
    * Fetches the full CTransaction object for a given transactionId.
    * Once fetched, it posts the object to the transaction LiveData.
    */
   fun selectTransaction(transactionId: Int)
   {
      Log.i(tag, "selectTransaction")
      // Don't do anything if an invalid ID is passed.
      if (transactionId <= 0)
      {
         return
      }

      viewModelScope.launch {
         _isLoading.value = true
         // Perform the blocking CTransaction creation on a background thread.
         val fullTransaction = withContext(Dispatchers.IO) {
            // This is where you create the full transaction object.
            // Assuming CTransaction's constructor handles loading from DB/network.
            CTransaction(transactionId)
         }
         _isLoading.value = false

         // Post the loaded transaction to the LiveData. The UI will react to this.
         _transaction.value = fullTransaction
      }
   }

   fun setMessage(text: String)
   {
      Log.i(tag, "setMessage $text")
      val currentTransaction = _transaction.value
      currentTransaction?.setMessage(text)
   }

   // In TransactionModel.kt

   fun getTransactionId(): Int
   {
      return activeTransaction?.value?.transactionId ?: -1
   }

   fun createTakeawayTransaction() {
      Log.i(tag, "createTakeawayTransaction")
      viewModelScope.launch {
         _isLoading.value = true
         val newTransactionId = withContext(Dispatchers.IO) {
            // --- All background logic is now here ---
            val useBag = true
            // It's better to pass the user ID as a parameter if possible,
            // but for now, we'll keep the Global call here.
            val user = Global.getInstance().rfidKeyId

            val transactionId = createNewTakeawayTransaction(0, useBag, user, false)

            if (transactionId > 0) {
               // Also perform any other related background work here
               CMenuCards.getInstance().loadTakeaway()
               transactionId // Return the valid ID
            } else {
               Log.e(tag, "Failed to create new takeaway transaction.")
               -1 // Return an invalid ID on failure
            }
         }
         _isLoading.value = false

         // --- Post navigation event to the UI ---
         if (newTransactionId > 0) {
            Log.d(tag, "Posting navigation event for takeaway transaction ID: $newTransactionId")
            _navigateToPageOrder.value = MyEvent(newTransactionId)
         } else {
            // Optionally, you can post to a different LiveData to show an error Toast
            // _showToast.value = MyEvent("Failed to create order")
         }
      }
   }


}