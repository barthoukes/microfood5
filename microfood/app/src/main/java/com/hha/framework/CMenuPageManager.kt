package com.hha.framework

import com.hha.menu.page.MenuPage

class MenuPageManager {
    // The nested map structure: Map<MenuCardId, Map<PageId, MenuPage>>
    private val menuPages: MutableMap<Int,
            MutableMap<Int, CMenuPage>> = mutableMapOf()

    // Add a menu page to the structure
    fun addMenuPage(page: CMenuPage) {
        val cardMap = menuPages.getOrPut(page.menuCardId) { mutableMapOf() }
        cardMap[page.menuPageId] = page
    }

    // Add multiple items to a specific page
    fun addItemsToPage(menuCardId: Int, menuPageId: Int, items: List<CMenuItem>):
            Boolean {
        val existingPage = getPage(menuCardId, menuPageId) ?: return false
        //val updatedPage = existingPage.withItems(existingPage.menuItems + items)
        //menuPages[menuCardId]?.set(menuPageId, updatedPage)
        return true
    }

    // Add a single item to a page
    fun addItemToPage(menuCardId: Int, menuPageId: Int, item: CMenuItem): Boolean {
        return addItemsToPage(menuCardId, menuPageId, listOf(item))
    }

    // Remove an item from a page
    fun removeItemFromPage(menuCardId: Int, menuPageId: Int, itemId: Int): Boolean {
        val existingPage = getPage(menuCardId, menuPageId) ?: return false
        //val updatedPage = existingPage.withoutItem(itemId)
        //menuPages[menuCardId]?.set(menuPageId, updatedPage)
        return true
    }

    // Get all pages for a specific menu card
    fun getPagesForCard(menuCardId: Int): Map<Int, CMenuPage> {
        return menuPages[menuCardId] ?: emptyMap()
    }

    // Get a specific page
    fun getPage(menuCardId: Int, menuPageId: Int): CMenuPage? {
        return menuPages[menuCardId]?.get(menuPageId)
    }

    // Get items for a specific page
    fun getItemsForPage(menuCardId: Int, menuPageId: Int): List<CMenuItem> {
        return getPage(menuCardId, menuPageId)?.menuItems ?: emptyList()
    }

    // Remove a specific page
    fun removePage(menuCardId: Int, menuPageId: Int): Boolean {
        return menuPages[menuCardId]?.remove(menuPageId) != null
    }

    // Remove all pages for a menu card
    fun removeCard(menuCardId: Int) {
        menuPages.remove(menuCardId)
    }

    // Builder for MenuPage with items
    class Builder {
        private var menuCardId: Int = 0
        private var menuPageId: Int = 0
        private var localName: String = ""
        private var chineseName: String = ""
        private var pageButtonSize: Int = 0
        private var picture: String = ""
        private var isVerticalOrientation: Boolean = false
        private var menuItems: MutableList<CMenuItem> = mutableListOf()

        fun setMenuCardId(id: Int) = apply { this.menuCardId = id }
        fun setMenuPageId(id: Int) = apply { this.menuPageId = id }
        fun setLocalName(name: String) = apply { this.localName = name }
        fun setChineseName(name: String) = apply { this.chineseName = name }
        fun setPageButtonSize(size: Int) = apply { this.pageButtonSize = size }
        fun setPicture(pic: String) = apply { this.picture = pic }
        fun setIsVerticalOrientation(vertical: Boolean) =
            apply { this.isVerticalOrientation = vertical }

        fun setMenuItems(items: List<CMenuItem>) = apply { this.menuItems = items.toMutableList() }
        fun addMenuItem(item: CMenuItem) = apply { this.menuItems.add(item) }

        fun build(): CMenuPage {
            return CMenuPage(
                menuCardId = menuCardId,
                menuPageId = menuPageId,
                localName = localName,
                chineseName = chineseName,
                pageButtonSize = pageButtonSize,
                picture = picture,
                isVerticalOrientation = isVerticalOrientation,
                menuItems = menuItems.toList()
            )
        }
    }
}

