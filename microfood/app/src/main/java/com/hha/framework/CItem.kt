import com.hha.framework.CMenuCards
import java.text.DecimalFormat

import com.hha.types.EItemLocation
import com.hha.types.EDeletedStatus
import com.hha.framework.CTimeFrameIndex
import com.hha.framework.CTimeFrameIndex.Companion.TIME_FRAME_UNDEFINED
import com.hha.types.CMoney
import com.hha.types.EItemLocation.Companion.location2Locations
import com.hha.types.ENameType
import com.hha.types.EOrderLevel
import com.hha.types.EPayed
import com.hha.types.ETaal
import com.hha.types.ETreeRow
import com.hha.framework.name

class CItem(
    var menuItemId: Int = -1,
    var twinItemId: Int = 0,
    var alias: String = "",
    var sequence: Int = -1,
    var subSequence: Int = 0,
    var subSubSequence: Int = 0,
    var localName: String = "",
    var chineseName: String = "",
    var originalAmount: Int = 0,
    var originalHalfAmount: Int = 0,
    var locations: Int = EItemLocation.location2Locations(EItemLocation.ITEM_OTHERS),
    var deletedStatus: EDeletedStatus = EDeletedStatus.DELETE_NOT,
    var parts: Int = 2,
    var group: Int = 0,
    var page: Int = 0,
    var timeFrameId: CTimeFrameIndex = CTimeFrameIndex(1),
    var level: EOrderLevel = EOrderLevel.LEVEL_OUTOFSTOCK,
    var tax: Double = 0.0,
    var id: Int = -1,
    var deviceId: Short = 0,
    var localPrinterName: String = "",
    var chinesePrinterName: String = "",
    var clusterId: Short = 0,
    var treeRow: ETreeRow = ETreeRow.TREE_ITEM,
    var paperCutPerItem: Boolean = false,
    var is_paid: EPayed = EPayed.PAID_NO,
    var shortTime: String = "",
    private var m_unitPrice: CMoney = CMoney(0),
    private var m_quantity: Int = 0,
    private var m_statiegeld: Short = 0
) {
    private var m_totalPrice: CMoney = CMoney(0)
        private set

    init {
        calculateTotal()
    }

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
        time_frame: CTimeFrameIndex,
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
        original_full_price.toLong().toInt(),
        original_half_price.toLong().toInt(),
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
        unit_statiegeld.toShort()
    )

    fun merge(found: CItem) {
        alias = found.alias
        chineseName = found.chineseName
        localName = found.localName
        chinesePrinterName = found.chinesePrinterName
        localPrinterName = found.localPrinterName
        parts = found.parts
        timeFrameId = found.timeFrameId
        m_unitPrice = found.m_unitPrice
        m_quantity += found.m_quantity
        m_statiegeld = found.m_statiegeld
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

    fun isValid(): Boolean = id >= 0

    fun name(lang: ETaal): String =
        if (lang == ETaal.LANG_SIMPLIFIED || lang == ETaal.LANG_TRADITIONAL) chineseName else localName

    fun updateName() {
        val product = CMenuCards.getInstance().getProductFromProductId(menuItemId)
        if (product != null) {
            chineseName = product.name(ETaal.LANG_SIMPLIFIED, parts, ENameType.NAME_SCREEN, false)
            localName = product.name(ETaal.LANG_DUTCH, parts, ENameType.NAME_SCREEN, false)
            chinesePrinterName =
                product.name(ETaal.LANG_SIMPLIFIED, parts, ENameType.NAME_PRINTER, false)
            localPrinterName = product.name(ETaal.LANG_DUTCH, parts, ENameType.NAME_PRINTER, false)

            if (twinItemId > 0) {
                val twinProduct = CMenuCards.getInstance().getProductFromProductId(twinItemId)
                if (twinProduct != null) {
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
        }
        else {
            chineseName = "-"
            localName = "-"
            chinesePrinterName = "-"
            localPrinterName = "-"
        }
    }

    fun getFirstLocation(): EItemLocation {
        return EItemLocation.getFirstLocation(locations)
    }

    fun getCustomerDisplayName(displayMaxWidth: Int, alignLCR: Short): String {
        var name = localName.replace("½", "ª")

        return when {
            name.length > displayMaxWidth -> name.substring(0, displayMaxWidth)
            name.length < displayMaxWidth -> when (alignLCR) {
                0.toShort() -> name.padEnd(displayMaxWidth, ' ')
                1.toShort() -> {
                    name = name.padStart(name.length + (displayMaxWidth - name.length) / 2, ' ')
                    name.padEnd(displayMaxWidth, ' ')
                }
                2.toShort() -> name.padStart(displayMaxWidth, ' ')
                else -> name.padEnd(displayMaxWidth, ' ')
            }
            else -> name
        }
    }

    fun setQuantity(quantity: Int) {
        m_quantity = quantity
        calculateTotal()
    }

    fun addQuantity(quantity: Int) {
        m_quantity += quantity
        calculateTotal()
    }

    fun getTotal(): CMoney = m_totalPrice

    fun getQuantity(): Int = m_quantity

    fun getTotalWithoutStatiegeld(): CMoney = CMoney(m_quantity * m_unitPrice.toLong())

    fun getTotalStatiegeld(): CMoney = CMoney(m_quantity * m_statiegeld.toInt())

    fun getStatiegeldPerPiece(): Int = m_statiegeld.toInt()

    private fun calculateTotal() {
        m_totalPrice = CMoney((m_unitPrice.toLong() + m_statiegeld) * m_quantity)
    }

    fun setQuantityPrice(quantity: Int, unitPrice: Int, statiegeld: Int = 0) {
        m_quantity = quantity
        m_unitPrice = CMoney(unitPrice)
        m_statiegeld = statiegeld.toShort()
        calculateTotal()
    }

    fun setUnitPrice(unitPrice: CMoney) {
        m_unitPrice = unitPrice
        calculateTotal()
    }

    fun setUnitPrice(unitPrice: Int) {
        m_unitPrice = CMoney(unitPrice)
        calculateTotal()
    }

    fun getUnitPrice(): CMoney = m_unitPrice

    fun getStatiegeld(): Short = m_statiegeld

    fun getTotalItemsStatiegeld(): Int = if (m_statiegeld != 0.toShort()) m_quantity else 0

    fun setStatiegeld(statiegeld: Int) {
        m_statiegeld = statiegeld.toShort()
        calculateTotal()
    }

    fun getTaxAmount(): CMoney {
        val tx = (getTotalWithoutStatiegeld().toLong() * tax) / (100.0 + tax)
        return CMoney((tx + 0.5).toLong())
    }
}

typealias CitemPtr = CItem
typealias CitemList = MutableList<CitemPtr>
typealias CitemListIterator = MutableListIterator<CitemPtr>