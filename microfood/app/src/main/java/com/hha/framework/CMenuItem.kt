package com.hha.framework

import com.hha.common.ItemSort
import com.hha.common.ItemVisible
import com.hha.common.MenuItem
import com.hha.common.TaxType

data class CMenuItem(
    val menuItemId: Int = 0,
    val alias: String = "",
    val localName: String = "",
    val chineseName: String = "",
    val restaurantPrice: Int = 0,
    val takeawayPrice: Int = 0,
    val restaurantHalfPrice: Int = 0,
    val takeawayHalfPrice: Int = 0,
    val restaurantTax: TaxType = TaxType.BTW_INVALID,
    val takeawayTax: TaxType = TaxType.BTW_INVALID,
    val level: Int = 0,
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
    val statiegeld: Int = 0,
    var sequence: Int = 0,
    var isSelected: Boolean = false
    ) {
    // Constructor that copies from XMenuItem
    constructor(xItem: MenuItem) : this(
        menuItemId = xItem.menuItemId,
        alias = xItem.alias,
        localName = xItem.localName,
        chineseName = xItem.chineseName,
        restaurantPrice = xItem.restaurantPrice,
        takeawayPrice = xItem.takeawayPrice,
        restaurantHalfPrice = xItem.restaurantHalfPrice,
        takeawayHalfPrice = xItem.takeawayHalfPrice,
        restaurantTax = xItem.restaurantTax,
        takeawayTax = xItem.takeawayTax,
        level = xItem.level,
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
        colourText = xItem.colourText,
        colourBack = xItem.colourBack,
        colourSelectedText = xItem.colourSelectedText,
        colourSelectedBack = xItem.colourSelectedBack,
        image = xItem.image,
        colourBack2 = xItem.colourBack2,
        colourSelectedBack2 = xItem.colourSelectedBack2,
        menuCardId = xItem.menuCardId,
        statiegeld = xItem.statiegeld,
        sequence = xItem.sequence,
        isSelected = false
    )

    // Optional: Add extension function to XMenuItem for more fluent conversion
    companion object {
        fun MenuItem.toCMenuItem(): CMenuItem {
            return CMenuItem(this)
        }

    }
}
