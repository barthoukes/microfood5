package com.hha.framework

import com.hha.common.TransactionData
import com.hha.grpc.GrpcServiceFactory
import com.hha.types.CMoney
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
   var rfidKeyId: Int = -1,
   var deposit: Int = 0,
   var timeStart: String = "",
   var timeEnd: String = "",
   var timeCustomer: String = "",
   var status: EClientOrdersType = EClientOrdersType.OPEN,
   var subTotalLow: CMoney = CMoney(0),
   var subTotalHigh: CMoney = CMoney(0),
   var discountLow: CMoney = CMoney(0),
   var discountHigh: CMoney = CMoney(0),
   var remainsLow: Float = 0.0f,
   var remainsHigh: Float = 0.0f,
   var tipsLow: CMoney = CMoney(0),
   var tipsHigh: CMoney = CMoney(0),
   var totalLow: CMoney = CMoney(0),
   var totalHigh: CMoney = CMoney(0),
   var taxTotalLow: CMoney = CMoney(0),
   var taxTotalHigh: CMoney = CMoney(0),
   var archived: Boolean = false,
   var message: String = "",
   var subtotalTaxFree: CMoney = CMoney(0),
   var totalTaxFree: CMoney = CMoney(0),
   var discountTaxFree: CMoney = CMoney(0),
   var tipsTaxFree: CMoney = CMoney(0)
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
      rfidKeyId = source.rfidKeyId
      deposit = source.deposit
      timeStart = source.timeStart
      timeEnd = source.timeEnd
      timeCustomer = source.timeCustomer
      status = EClientOrdersType.fromClientOrdersType(source.status)
      subTotalLow = CMoney(source.subtotalLow)
      subTotalHigh = CMoney(source.subtotalHigh)
      discountLow = CMoney(source.discountLow)
      discountHigh = CMoney(source.discountHigh)
      remainsLow = source.remainsLow
      remainsHigh = source.remainsHigh
      tipsLow = CMoney(source.tipsLow)
      tipsHigh = CMoney(source.tipsHigh)
      totalLow = CMoney(source.totalLow)
      totalHigh = CMoney(source.totalHigh)
      taxTotalLow = CMoney(source.taxTotalLow)
      taxTotalHigh = CMoney(source.taxTotalHigh)
      archived = source.archived
      message = source.message
      subtotalTaxFree = CMoney(source.subtotalTaxFree)
      totalTaxFree = CMoney(source.totalTaxFree)
      discountTaxFree = CMoney(source.discountTaxFree)
      tipsTaxFree = CMoney(source.tipsTaxFree)
      subtotalTaxFree = CMoney(source.subtotalTaxFree)
      totalTaxFree = CMoney(source.totalTaxFree)
      discountTaxFree = CMoney(source.discountTaxFree)
      tipsTaxFree = CMoney(source.tipsTaxFree)
   }

   fun cleanTransaction()
   {
      subTotalLow.clear()
      subTotalHigh.clear()
      discountHigh.clear()
      discountLow.clear()
      discountTaxFree.clear()
      remainsHigh = 1000.0f
      remainsLow = 1000.0f
      tipsHigh.clear()
      tipsLow.clear()
      totalLow.clear()
      totalHigh.clear()
      taxTotalHigh.clear()
      taxTotalLow.clear()
      subtotalTaxFree.clear()
      totalTaxFree.clear()
      discountTaxFree.clear()
      tipsTaxFree.clear()
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
            rfidKeyId = source.rfidKeyId,
            deposit = source.deposit,
            timeStart = source.timeStart,
            timeEnd = source.timeEnd,
            timeCustomer = source.timeCustomer,
            status = EClientOrdersType.fromClientOrdersType(source.status),
            subTotalLow = CMoney(source.subtotalLow.cents),
            subTotalHigh = CMoney(source.subtotalHigh.cents),
            discountLow = CMoney(source.discountLow.cents),
            discountHigh = CMoney(source.discountHigh.cents),
            remainsLow = source.remainsLow,
            remainsHigh = source.remainsHigh,
            tipsLow = CMoney(source.tipsLow.cents),
            tipsHigh = CMoney(source.tipsHigh.cents),
            totalLow = CMoney(source.totalLow.cents),
            totalHigh = CMoney(source.totalHigh.cents),
            taxTotalLow = CMoney(source.taxTotalLow.cents),
            taxTotalHigh = CMoney(source.taxTotalHigh.cents),
            archived = source.archived,
            message = source.message,
            subtotalTaxFree = CMoney(source.subtotalTaxFree.cents),
            totalTaxFree = CMoney(source.totalTaxFree.cents),
            discountTaxFree = CMoney(source.discountTaxFree.cents),
            tipsTaxFree = CMoney(source.tipsTaxFree.cents)
         )
      }
   }
}
