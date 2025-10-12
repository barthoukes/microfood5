package com.hha.framework

import com.hha.common.SkipInvisible
import com.hha.framework.CMenuItems
import com.hha.grpc.GrpcServiceFactory
import com.hha.menu.item.MenuItemList

data class CMenuPage(
    var menuCardId: Int,
    var menuPageId: Int,
    var pageWidth: Int,
    var chineseName: String,
    var localName: String,
    var isSelected: Boolean,
    var menuItem: CMenuItems
) {
    // Secondary constructor should properly delegate to primary constructor
    constructor(
        cardId: Int,
        pageId: Int,
        width: Int,
        chinese: String,
        local: String
    ) : this (
        menuCardId = cardId,
        menuPageId = pageId,
        chineseName = chinese,
        localName = local,
        pageWidth = width,
        isSelected = false,
        menuItem = CMenuItems()
    )

    fun loadItems(skip : SkipInvisible) : CMenuItems {
        val menuItemService = GrpcServiceFactory.createMenuItemService()
        val items: MenuItemList = menuItemService.findMenuItemsByPage(
            menuCardId, menuPageId, skip)
        val menuItems : MutableMap<Int, CMenuItem> = mutableMapOf()

        for (item in items.itemsList) {
            val newItem = CMenuItem(item)
            menuItems[newItem.menuItemId] = newItem
        }
        val mapItems = CMenuItems()
        mapItems.setItems(menuItems)
        return mapItems
    }

    fun getDisplayName(isChinese: Boolean): String {
        return if (isChinese) chineseName else localName
    }

    fun shouldLoadItems(): Boolean {
        return menuItem.menuItems.isEmpty()
    }
}
