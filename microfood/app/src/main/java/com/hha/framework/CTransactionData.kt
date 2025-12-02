package com.hha.framework

import com.hha.common.TransactionData
import com.hha.types.CMoney
import com.hha.types.C3Moneys
import com.hha.types.EClientOrdersType
import com.hha.types.ETransType

/**
 * A native Kotlin data class representing transaction data.
 * This class decouples the application from the gRPC-generated TransactionData class.
 */
data class CTransactionData(
   var transactionId: Int = -1,
   var customerId: Int = -1,
   var name: String = "",
   var transType: ETransType = ETransType.TRANS_TYPE_UNDEFINED,
   var rfidKeyId: Short = -1,
   var deposit: Int = 0,
   var timeStart: String = "",
   var timeEnd: String = "",
   var timeCustomer: String = "",
   var status: EClientOrdersType = EClientOrdersType.OPEN,
   var subTotal: C3Moneys = C3Moneys(0,0,0),
   var tips: C3Moneys = C3Moneys(0,0,0),
   var discount: C3Moneys = C3Moneys(0, 0, 0),
   var total: C3Moneys = C3Moneys(0,0,0),
   var taxTotal: C3Moneys = C3Moneys(0, 0, 0),
   var remainsLow: Float = 0.0f,
   var remainsHigh: Float = 0.0f,
   var archived: Boolean = false,
   var message: String = "",
)
{
   // Copy constructor
   fun setTransactionData(source: TransactionData?)
   {
      if (source == null)
      {
         return
      }
      transactionId = source.transactionId
      customerId = source.customerId
      name = source.name
      transType = ETransType.fromTransType(source.transType)
      rfidKeyId = source.rfidKeyId.toShort()
      deposit = source.deposit
      timeStart = source.timeStart
      timeEnd = source.timeEnd
      timeCustomer = source.timeCustomer
      status = EClientOrdersType.fromClientOrdersType(source.status)
      subTotal = C3Moneys(source.subtotalLow, source.subtotalHigh, source.subtotalTaxFree)
      discount = C3Moneys(source.discountLow, source.discountHigh, source.discountTaxFree)
      remainsLow = source.remainsLow
      remainsHigh = source.remainsHigh
      tips = C3Moneys(source.tipsLow, source.tipsHigh, source.tipsTaxFree)
      total = C3Moneys(source.totalLow, source.totalHigh, source.totalTaxFree)
      taxTotal = C3Moneys(CMoney(source.taxTotalLow), CMoney(source.taxTotalHigh), CMoney(0))
      archived = source.archived
      message = source.message
   }

   fun cleanTransaction()
   {
      subTotal.clear()
      discount.clear()
      remainsHigh = 1000.0f
      remainsLow = 1000.0f
      tips.clear()
      total.clear()
      taxTotal.clear()
   }

   companion object
   {
      /**
       * A factory function to create a CTransactionData object
       * from a gRPC-generated TransactionData object.
       *
       * @param source The gRPC TransactionData object.
       * @return A new CTransactionData object, or null if the source is null.
       */
      fun fromGrpc(source: TransactionData?): CTransactionData?
      {
         if (source == null)
         {
            return null
         }

         return CTransactionData(
            transactionId = source.transactionId,
            customerId = source.customerId,
            name = source.name,
            transType = ETransType.fromTransType(source.transType),
            rfidKeyId = source.rfidKeyId.toShort(),
            deposit = source.deposit,
            timeStart = source.timeStart,
            timeEnd = source.timeEnd,
            timeCustomer = source.timeCustomer,
            status = EClientOrdersType.fromClientOrdersType(source.status),
            subTotal = C3Moneys(source.subtotalLow.cents, source.subtotalHigh.cents, source.subtotalTaxFree.cents),
            discount = C3Moneys(source.discountLow.cents, source.discountHigh.cents, source.discountTaxFree.cents),
            tips = C3Moneys(source.tipsLow.cents, source.tipsHigh.cents, source.tipsTaxFree.cents),
            total = C3Moneys(source.totalLow.cents, source.totalHigh.cents, source.totalTaxFree.cents),
            taxTotal = C3Moneys(CMoney(source.taxTotalLow), CMoney(source.taxTotalHigh), CMoney(0)),
            remainsLow = source.remainsLow,
            remainsHigh = source.remainsHigh,
            archived = source.archived,
            message = source.message
         )
      }
   }
}
