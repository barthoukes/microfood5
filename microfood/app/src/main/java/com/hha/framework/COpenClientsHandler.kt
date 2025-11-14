package com.hha.framework

import com.hha.common.TransType
import com.hha.dialog.Translation
import com.hha.resources.Global

import com.hha.dialog.Translation.TextId
import com.hha.grpc.GrpcServiceFactory
import com.hha.modalDialog.ModalDialogStringEdit


object COpenClientsHandler : ModalDialogStringEdit.StringListener {
    private val CFG = Global.getInstance().CFG
    val service = GrpcServiceFactory.createDailyTransactionService()
    var stringTyped = ""
    var stringValid = false

    fun createNewTakeawayTransaction(
        client_id: Int, use_bag: Boolean, user: Int, ask_remark: Boolean
    ): Int {
        if (CFG.getOption("sitin_only")) {
            return -1;
        }

        var remark: String? = null

        if (ask_remark) {
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