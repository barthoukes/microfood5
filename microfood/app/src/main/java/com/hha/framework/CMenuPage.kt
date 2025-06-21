package com.hha.framework

import com.hha.common.SkipInvisible
import com.hha.framework.CMenuItem
import com.hha.grpc.GrpcServiceFactory
import com.hha.menu.item.MenuItemList

data class CMenuPage(
    var menuCardId: Int,
    var menuPageId: Int,
    var chineseName: String,
    var localName: String,
    var isSelected: Boolean,
    var menuItem: MutableMap<Int, com.hha.framework.CMenuItem> = mutableMapOf()
) {
    // Secondary constructor should properly delegate to primary constructor
    constructor(
        cardId: Int,
        pageId: Int,
        chinese: String,
        local: String
    ) : this (
        menuCardId = cardId,
        menuPageId = pageId,
        chineseName = chinese,
        localName = local,
        isSelected = false
    )

    fun loadItems(skip : SkipInvisible) {
        val menuItemService = GrpcServiceFactory.createMenuItemService()
        val items: MenuItemList = menuItemService.findMenuItemsByPage(
            menuCardId, menuPageId, skip)

        menuItem.clear()
        for (item in items.itemsList) {
            val newItem = CMenuItem(item)
            addMenuItem(newItem)
        }
    }

    // Add a menu card to the structure
    fun addMenuItem(newItem: CMenuItem) {
        menuItem[newItem.menuItemId] = newItem
    }
}
