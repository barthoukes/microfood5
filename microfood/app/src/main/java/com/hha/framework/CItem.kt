package com.hha.framework

import com.hha.common.Item
import com.hha.types.EItemLocation
import com.hha.types.EDeletedStatus
import com.hha.types.CMoney
import com.hha.types.ENameType
import com.hha.types.EOrderLevel
import com.hha.types.EPayed
import com.hha.types.EPaymentStatus
import com.hha.types.ETaal
import com.hha.types.ETimeFrameIndex
import com.hha.types.ETreeRow

class CItem(
    var menuItemId: Int = -1,
    var twinItemId: Int = 0,
    var alias: String = "",
    var sequence: Int = -1,
    var subSequence: Int = 0,
    var subSubSequence: Int = 0,
    var localName: String = "",
    var chineseName: String = "",
    var originalAmount: CMoney,
    var originalHalfAmount: CMoney,
    var locations: Int = EItemLocation.location2Locations(EItemLocation.ITEM_OTHERS),
    var deletedStatus: EDeletedStatus = EDeletedStatus.DELETE_NOT,
    var parts: Int = 2,
    var group: Int = 0,
    var page: Int = 0,
    var timeFrameId: ETimeFrameIndex = ETimeFrameIndex(1),
    var level: EOrderLevel = EOrderLevel.LEVEL_OUTOFSTOCK,
    var tax: Double = 0.0,
    var id: Int = -1,
    var deviceId: Short = 0,
    var localPrinterName: String = "",
    var chinesePrinterName: String = "",
    var clusterId: Short = 0,
    var treeRow: ETreeRow = ETreeRow.TREE_ITEM,
    var paperCutPerItem: Boolean = false,
    var isPaid: EPayed = EPayed.PAID_NO,
    var shortTime: String = "",
    private var unitPrice: CMoney = CMoney(0),
    private var quantity: Int = 0,
    private var statiegeld : CMoney = CMoney(0)
)
{

    // Add this new parameterless constructor
    constructor() : this(
        menuItemId = -1,
        twinItemId = 0,
        alias = "",
        sequence = -1,
        subSequence = 0,
        subSubSequence = 0,
        localName = "",
        chineseName = "",
        originalAmount = CMoney(0),
        originalHalfAmount = CMoney(0),
        locations = EItemLocation.location2Locations(EItemLocation.ITEM_OTHERS),
        deletedStatus = EDeletedStatus.DELETE_NOT,
        parts = 2,
        group = 0,
        page = 0,
        timeFrameId = ETimeFrameIndex(1),
        level = EOrderLevel.LEVEL_OUTOFSTOCK,
        tax = 0.0,
        id = -1,
        deviceId = 0,
        localPrinterName = "",
        chinesePrinterName = "",
        clusterId = 0,
        treeRow = ETreeRow.TREE_ITEM,
        paperCutPerItem = false,
        isPaid = EPayed.PAID_NO,
        shortTime = "",
        unitPrice = CMoney(0),
        quantity = 0,
        statiegeld = CMoney(0)
    )

    private var totalPrice: CMoney = CMoney(0)
        private set

    init
    {
        calculateTotal()
    }

    constructor(item: CItem) : this(
        menuItemId = item.menuItemId,
        twinItemId = item.twinItemId,
        alias = item.alias,
        sequence = item.sequence,
        subSequence = item.subSequence,
        subSubSequence = item.subSubSequence,
        localName = item.localName,
        chineseName = item.chineseName,
        originalAmount = item.originalAmount,
        originalHalfAmount = item.originalHalfAmount,
        locations = item.locations,
        deletedStatus = item.deletedStatus,
        parts = item.parts,
        group = item.group,
        page = item.page,
        timeFrameId = item.timeFrameId,
        level = item.level,
        tax = item.tax,
        id = item.id,
        deviceId = item.deviceId,
        localPrinterName = item.localPrinterName,
        chinesePrinterName = item.chinesePrinterName,
        clusterId = item.clusterId,
        treeRow = item.treeRow,
        paperCutPerItem = item.paperCutPerItem,
        isPaid = item.isPaid,
        shortTime = item.shortTime,
        unitPrice = item.unitPrice,
        quantity = item.quantity,
        statiegeld = item.statiegeld
    )

    constructor(
        menu_item_id: Int,
        alias_name: String,
        sequence_nr: Int,
        sub_sequence: Short,
        sub_sub_sequence: Short,
        local_name: String,
        chinese_name: String,
        parts: Short,
        order_quantity: Int,
        unit_price: CMoney,
        original_full_price: CMoney,
        original_half_price: CMoney,
        unit_statiegeld: Int,
        print_locations: Int,
        item_group: Int,
        item_page: Int,
        time_frame: ETimeFrameIndex,
        tax_percentage: Double,
        item_level: EOrderLevel,
        sql_id: Int,
        devc: Short,
        cluster_id: Short,
        cut_paper_kitchen: Boolean,
        isPaid: EPayed,
        short_time: String,
        deleted: EDeletedStatus
    ) : this(
        menu_item_id,
        0,
        alias_name,
        sequence_nr,
        sub_sequence.toInt(),
        sub_sub_sequence.toInt(),
        local_name,
        chinese_name,
        original_full_price,
        original_half_price,
        locations = (if (print_locations < 10) (16 shl print_locations) else print_locations),
        deleted,
        parts.toInt(),
        item_group,
        item_page,
        time_frame,
        item_level,
        tax_percentage,
        sql_id,
        devc,
        local_name,
        chinese_name,
        cluster_id,
        ETreeRow.TREE_ITEM,
        cut_paper_kitchen,
        isPaid,
        short_time,
        unit_price,
        order_quantity,
        CMoney(unit_statiegeld)
    )

    constructor(item: Item) : this(
        menuItemId = item.menuItemId.toInt(),
        twinItemId = -1,
        alias = item.aliasName,
        sequence = item.sequenceNr,
        subSequence = item.subSequence,
        subSubSequence = item.subSubSequence,
        localName = item.localName,
        chineseName = item.chineseName,
        originalAmount = CMoney(item.fullPrice.cents),
        originalHalfAmount = CMoney(item.halfPrice.cents),
        locations = item.printLocations,
        deletedStatus = EDeletedStatus.fromDeletedStatus(item.deletedStatus),
        parts = item.parts,
        group = item.itemGroup,
        page = item.itemPage,
        timeFrameId = ETimeFrameIndex(item.timeFrame.toShort()),
        level = EOrderLevel.fromOrderLevel(item.orderLevel),
        tax = item.taxPercentage,
        id = -1,
        deviceId = item.deviceId.toShort(),
        localPrinterName = item.localPrinterName,
        chinesePrinterName = item.chinesePrinterName,
        clusterId = item.clusterId.toShort(),
        treeRow = ETreeRow.TREE_ITEM,
        paperCutPerItem = item.cutPaperKitchen,
        isPaid = EPayed.fromPayed(item.isPaid),
        shortTime = item.shortTime,
        unitPrice = CMoney(item.unitPrice.cents),
        quantity = item.orderQuantity,
        statiegeld = CMoney(item.unitStatiegeld)
    )

    fun addQuantity(quantity: Int)
    {
        this.quantity += quantity
        calculateTotal()
    }

    private fun calculateTotal()
    {
        totalPrice = (unitPrice + statiegeld) * quantity
    }

    fun getCustomerDisplayName(displayMaxWidth: Int, alignLCR: Short): String
    {
        var name = localName.replace("½", "ª")

        return when
        {
            name.length > displayMaxWidth -> name.substring(0, displayMaxWidth)
            name.length < displayMaxWidth -> when (alignLCR)
            {
                0.toShort() -> name.padEnd(displayMaxWidth, ' ')
                1.toShort() ->
                {
                    name = name.padStart(name.length + (displayMaxWidth - name.length) / 2, ' ')
                    name.padEnd(displayMaxWidth, ' ')
                }

                2.toShort() -> name.padStart(displayMaxWidth, ' ')
                else -> name.padEnd(displayMaxWidth, ' ')
            }

            else -> name
        }
    }

    fun getFirstLocation(): EItemLocation
    {
        return EItemLocation.getFirstLocation(locations)
    }

    fun getStatiegeld(): CMoney = statiegeld

    fun getTaxAmount(): CMoney
    {
        val tx = (getTotalWithoutStatiegeld().toLong() * tax) / (100.0 + tax)
        return CMoney((tx + 0.5).toInt())
    }

    fun getTotalItemsStatiegeld(): Int = if (!statiegeld.empty()) quantity else 0

    fun getUnitPrice(): CMoney = unitPrice

    fun isValidItem(payStatus: EPaymentStatus): Boolean
    {
        var retVal = true
        if (payStatus != EPaymentStatus.PAY_STATUS_ANY)
        {
            when (isPaid)
            {
                EPayed.PAID_NO -> if (payStatus != EPaymentStatus.PAY_STATUS_UNPAID)
                {
                    retVal = false
                }

                EPayed.PAID_ORDER -> if (payStatus == EPaymentStatus.PAY_STATUS_UNPAID)
                {
                    retVal = false
                }

                EPayed.PAID_BEFORE -> if (payStatus == EPaymentStatus.PAY_STATUS_PAID_ORDER
                    || payStatus == EPaymentStatus.PAY_STATUS_UNPAID
                )
                {
                    retVal = false
                }

                EPayed.PAID_CANCEL -> if (payStatus != EPaymentStatus.PAY_STATUS_CANCEL)
                {
                    retVal = false
                }

                EPayed.PAID_ALL ->
                {
                }
            }
        }
        return retVal
    }

    fun getQuantity(): Int = quantity

    fun getTotal(): CMoney = totalPrice

    fun getTotalWithoutStatiegeld(): CMoney = CMoney(quantity * unitPrice.cents())

    fun getTotalStatiegeld(): CMoney = CMoney(quantity * statiegeld.cents())

    fun getStatiegeldPerPiece(): Int = statiegeld.cents()

    fun isValid(): Boolean = id >= 0

    fun merge(found: CItem)
    {
        alias = found.alias
        chineseName = found.chineseName
        localName = found.localName
        chinesePrinterName = found.chinesePrinterName
        localPrinterName = found.localPrinterName
        parts = found.parts
        timeFrameId = found.timeFrameId
        unitPrice = found.unitPrice
        quantity += found.quantity
        statiegeld = found.statiegeld
        calculateTotal()
        sequence = found.sequence
        subSequence = found.subSequence
        subSubSequence = found.subSubSequence
        group = found.group
        level = found.level
        locations = found.locations
        shortTime = found.shortTime
        twinItemId = found.twinItemId
        localPrinterName = found.localPrinterName
        chinesePrinterName = found.chinesePrinterName
    }

    fun name(lang: ETaal): String =
        if (lang == ETaal.LANG_SIMPLIFIED ||
            lang == ETaal.LANG_TRADITIONAL
        ) chineseName else localName

    fun setQuantityPrice(quantity: Int, unitPrice: CMoney, statiegld: CMoney = CMoney(0))
    {
        this.quantity = quantity
        this.unitPrice = unitPrice
        statiegeld = statiegld
        calculateTotal()
    }

    fun setUnitPrice(unitPrice: CMoney)
    {
        this.unitPrice = unitPrice
        calculateTotal()
    }

    fun setUnitPrice(unitPrice: Int)
    {
        this.unitPrice = CMoney(unitPrice)
        calculateTotal()
    }

    fun setQuantity(quantity: Int)
    {
        this.quantity = quantity
        calculateTotal()
    }

    fun setStatiegeld(statiegld: Int)
    {
        statiegeld = CMoney(statiegld)
        calculateTotal()
    }

    fun updateName()
    {
        val product = CMenuCards.getInstance().getProductFromProductId(menuItemId)
        if (product != null)
        {
            chineseName = product.name(ETaal.LANG_SIMPLIFIED, parts, ENameType.NAME_SCREEN, false)
            localName = product.name(ETaal.LANG_DUTCH, parts, ENameType.NAME_SCREEN, false)
            chinesePrinterName =
                product.name(ETaal.LANG_SIMPLIFIED, parts, ENameType.NAME_PRINTER, false)
            localPrinterName = product.name(ETaal.LANG_DUTCH, parts, ENameType.NAME_PRINTER, false)

            if (twinItemId > 0)
            {
                val twinProduct = CMenuCards.getInstance().getProductFromProductId(twinItemId)
                if (twinProduct != null)
                {
                    chineseName += twinProduct.name(
                        ETaal.LANG_SIMPLIFIED,
                        2,
                        ENameType.NAME_SCREEN,
                        true
                    )
                    localName += " " + twinProduct.name(
                        ETaal.LANG_DUTCH,
                        2,
                        ENameType.NAME_SCREEN,
                        true
                    )
                    chinesePrinterName += twinProduct.name(
                        ETaal.LANG_SIMPLIFIED,
                        2,
                        ENameType.NAME_PRINTER,
                        true
                    )
                    localPrinterName += " " + twinProduct.name(
                        ETaal.LANG_DUTCH,
                        2,
                        ENameType.NAME_PRINTER,
                        true
                    )
                }
            }
        } else
        {
            chineseName = "-"
            localName = "-"
            chinesePrinterName = "-"
            localPrinterName = "-"
        }
    }

}
