package com.hha.framework

import com.hha.common.TransType
import com.hha.dialog.Translation
import com.hha.resources.Global

import com.hha.dialog.Translation.TextId
import com.hha.grpc.GrpcServiceFactory
import com.hha.modalDialog.ModalDialogStringEdit
import com.hha.types.ETransType


object COpenClientsHandler : ModalDialogStringEdit.StringListener {
    private val global = Global.getInstance()
    private val CFG = global.CFG
    val service = GrpcServiceFactory.createDailyTransactionService()
    var stringTyped = ""
    var stringValid = false
    val mMaximumTime = CFG.getValue("maximum_time")
    val mRestaurantMap = CFG.getBoolean("restaurant_map")
    val mFloorplanBillFirst = CFG.getBoolean("floorplan_bill_first")

    fun createNewRestaurantTransaction(name: String, floorTableId: Int, minutes: Int): Int
    {
        if (CFG.getBoolean("takeaway_only"))
            return -1
        if (!CFG.getBoolean("sitin_only") && name == "0")
        {
            return createNewTakeawayTransaction(-1, true, -1, false)
        }
        else
        {
            var type = ETransType.TRANS_TYPE_SITIN
            val n = name.length
            //CsqlFloorTableIterator floorTables(CFG("remove_time"));
            //CsqlDailyTransactionIterator clientOrders

            // If long number, then probably a phone number
            if ( n > 6 && name[0].isDigit() && name[n-1].isDigit())
            {
                if (CFG.getBoolean("telephone_order"))
                {
                    type = ETransType.TRANS_TYPE_DELIVERY
                }
                else
                {
                    type = ETransType.TRANS_TYPE_TAKEAWAY
                }
            }
            else if (mFloorplanBillFirst) // Wok table option
            {
                type = ETransType.TRANS_TYPE_WOK
            }
            val service2 = GrpcServiceFactory.createDailyTransactionService()
            val orderId: Int = service2.insertTransaction(
                name, -1,global.rfidKeyId,
                type.toTransType()) ?: -1

            if (mRestaurantMap &&
                (type == ETransType.TRANS_TYPE_WOK
                   || type == ETransType.TRANS_TYPE_SITIN))
            {
                val service = GrpcServiceFactory.createFloorTableService()
                service.connectTableToTransaction(
                    floorTableId, orderId,mMaximumTime)
                service.updateDrinksMinutes()
            }
            return orderId
        }
    }

    fun createNewTransactionForTable(tableId: Int, minutes: Int)
    {


    }

    fun createNewTakeawayTransaction(
        client_id: Int, use_bag: Boolean, user: Short, askRemark: Boolean
    ): Int {
        if (CFG.getOption("sitin_only")) {
            return -1;
        }

        var remark: String? = null

        if (askRemark) {
            remark = askRemark(
                CFG.getOption("telephone_remark"),
                TextId.TEXT_NAME
            )
            if (remark == null) {
                return -1;
            }
        }
        val name = createNewTransactionName(TransType.TRANS_TYPE_TAKEAWAY);
        val transType = when {
            use_bag -> TransType.TRANS_TYPE_TAKEAWAY
            else -> TransType.TRANS_TYPE_EAT_INSIDE
        }
        val order_id = service.insertTransaction(
            name, client_id, user, transType
        );
        if (order_id != null && remark != null) {
            service.setMessage(order_id, remark)
        }
        if (order_id == null)
            return -1
        return order_id
    }

    fun createNewTransactionName(transType: TransType): String {
        val maxId = CFG.getValue("transaction_max_number")
        val separateNumbers = CFG.getOption("transaction_separate_numbers")

        var number = service.findLatestTransaction(transType, separateNumbers, maxId)
        number = (number % maxId) + 1

        return "%03d".format(number)
    }

    fun askRemark(addTextPhone: Boolean, titleId: TextId): String? {
        val title = Translation.get(titleId)
        ModalDialogStringEdit.Builder()
            .setTitle(title)
            .setListener(this)
            .show()
        //            .setHint("Optional hint")
        //            .setInitialText("Default text")
        //            .allowChineseInput(true)
        //            .show()
        //    }
        if (stringValid) {
            return stringTyped
        } else {
            return null
        }
    }

    override fun onStringConfirmed(text: String) {
        TODO("Not yet implemented")
        stringTyped = text
        stringValid = true
    }

    override fun onStringCancelled() {
        stringTyped = ""
        stringValid = false
    }
}