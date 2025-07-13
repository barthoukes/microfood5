package com.hha.framework

import android.util.Log
import com.hha.common.ItemSort
import com.hha.common.ItemVisible
import com.hha.common.MenuItem
import com.hha.common.OrderLevel
import com.hha.common.TaxType
import com.hha.types.CMoney
import com.hha.types.ENameType
import com.hha.types.EOrderLevel
import com.hha.types.ETaal
import com.hha.types.ETaxType

data class CMenuItem(
    val menuItemId: Int = 0,
    val alias: String = "",
    val localName: String = "",
    val chineseName: String = "",
    val restaurantPrice: CMoney = CMoney(0),
    val takeawayPrice : CMoney = CMoney(0),
    val restaurantHalfPrice: CMoney = CMoney(0),
    val takeawayHalfPrice: CMoney = CMoney(0),
    val restaurantTax: TaxType = TaxType.BTW_INVALID,
    val takeawayTax: TaxType = TaxType.BTW_INVALID,
    var level: EOrderLevel = EOrderLevel.LEVEL_NOTHING,
    val taxClusterId: Int = 0,
    val page: Int = 0,
    val locations: Int = 0,
    val paperCutPerItem: Boolean = false,
    val isVisible: ItemVisible = ItemVisible.ITEM_UNDEFINED,
    val sort: ItemSort = ItemSort.SORT_NONE,
    val isTwin: Boolean = false,
    val positionX: Int = 0,
    val positionY: Int = 0,
    val positionWidth: Int = 0,
    val positionHeight: Int = 0,
    val clusters: Int = 0,
    val specialPrices: Int = 0,
    val colourText: Int = 0,
    val colourBack: Int = 0,
    val colourSelectedText: Int = 0,
    val colourSelectedBack: Int = 0,
    val image: String = "",
    val colourBack2: Int = 0,
    val colourSelectedBack2: Int = 0,
    val menuCardId: Int = 0,
    val statiegeld: CMoney = CMoney(0),
    var sequence: Int = 0,
    var isSelected: Boolean = false
    ) {
    // Constructor that copies from XMenuItem
    constructor(xItem: MenuItem) : this(
        menuItemId = xItem.menuItemId,
        alias = xItem.alias,
        localName = xItem.localName,
        chineseName = xItem.chineseName,
        restaurantPrice = CMoney(xItem.restaurantPrice),
        takeawayPrice = CMoney(xItem.takeawayPrice),
        restaurantHalfPrice = CMoney(xItem.restaurantHalfPrice),
        takeawayHalfPrice = CMoney(xItem.takeawayHalfPrice),
        restaurantTax = xItem.restaurantTax,
        takeawayTax = xItem.takeawayTax,
        level = EOrderLevel.fromOrderLevel(xItem.level),
        taxClusterId = xItem.taxClusterId,
        page = xItem.page,
        locations = xItem.locations,
        paperCutPerItem = xItem.paperCutPerItem,
        isVisible = xItem.isVisible,
        sort = xItem.sort,
        isTwin = xItem.isTwin,
        positionX = xItem.positionX,
        positionY = xItem.positionY,
        positionWidth = xItem.positionWidth,
        positionHeight = xItem.positionHeight,
        clusters = xItem.clusters,
        specialPrices = xItem.specialPrices,
        colourText = toTabletColour(xItem.colourText),
        colourBack = toTabletColour(xItem.colourBack),
        colourSelectedText = toTabletColour(xItem.colourSelectedText),
        colourSelectedBack = toTabletColour(xItem.colourSelectedBack),
        image = xItem.image,
        colourBack2 = toTabletColour(xItem.colourBack2),
        colourSelectedBack2 = toTabletColour(xItem.colourSelectedBack2),
        menuCardId = xItem.menuCardId,
        statiegeld = CMoney(xItem.statiegeld),
        sequence = xItem.sequence,
        isSelected = false
    )

    // Optional: Add extension function to XMenuItem for more fluent conversion
    companion object {
        fun MenuItem.toCMenuItem(): CMenuItem {
            return CMenuItem(this)
        }

        fun toTabletColour(colour: Int): Int {
            return colour or 0xFF000000.toInt()
        }
    }

    fun getPriceAndHalfPrice(cluster_id: Int, isTakeawayPrice: Boolean): CPriceAndHalfPrice {
        if (!isTakeawayPrice) {
            return CPriceAndHalfPrice(restaurantPrice, restaurantHalfPrice)
        }
        return CPriceAndHalfPrice(takeawayPrice, takeawayHalfPrice)
    }

    fun isOutOfStock(): Boolean = menuItemId<0 || level == EOrderLevel.LEVEL_NOTHING
                || level == EOrderLevel.LEVEL_OUTOFSTOCK

    fun getTaxPercentage(isTakeawayPrice: Boolean) : Double {
        val perc : TaxType = when (isTakeawayPrice) {
            false -> restaurantTax
            else -> takeawayTax
        }
        val percentage = CTaxProvider.getTax(perc)
        Log.i("CMenuItem", "getTaxPercentage: $percentage")
        return percentage
    }

    //----------------------------------------------------------------------------*/
    // @brief Calculate the name for display.
    // @param language [in] What language
    // @param portion [in] What portion
    // @param type [in] Type for the element, see #EnameType
    //
    fun Name(language: ETaal, portion: Int, type: ENameType, trim: Boolean): String
    {
        var resultString: String
        val portionString = duoShao(portion, language, type == ENameType.NAME_PRINTER)
        return Name(language, portionString, this.localName,
            this.chineseName, type, trim)
    }
}
