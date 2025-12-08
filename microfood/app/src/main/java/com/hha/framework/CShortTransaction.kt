package com.hha.framework

import com.hha.types.CMoney
import com.hha.types.EClientOrdersType
import com.hha.types.ETransType
import com.hha.daily.transaction.TransactionListItem

class CShortTransaction {
   var transactionId: Int = 0
   var name: String = ""
   var total: CMoney = CMoney(0)
   var minutes: Int = 0
   var status: EClientOrdersType = EClientOrdersType.OPEN
   var transType: ETransType = ETransType.TRANS_TYPE_TAKEAWAY
   var timeStart: String = ""
   var timeCustomer: String = ""
   var customerId: Int = 0
   var archived: Boolean = false
   var message: String = ""

   constructor(
      transactionId: Int, name: String, timeCustomer: String,
      status: EClientOrdersType, customerId: Int,
      total: CMoney, minutes: Int, transType: ETransType,
      timeStart: String, message: String, archived: Boolean)
   {
      this.name = name
      this.total = total
      this.minutes = minutes
      this.status = status
      this.transType = transType
      this.timeStart = timeStart
      this.timeCustomer = timeCustomer
      this.customerId = customerId
      this.message = message
      this.archived = archived
      this.transactionId = transactionId
   }

   constructor(item: TransactionListItem)
   {
      name = item.name
      total = CMoney(item.total)
      minutes = item.minutes
      status = EClientOrdersType.fromClientOrdersType(item.status)
      transType = ETransType.fromTransType(item.transType)
      timeStart = item.timeStart
      timeCustomer = item.timeCustomer
      customerId = item.customerId
      message = item.message
      archived = item.archived
      transactionId = item.transactionId
   }
}
