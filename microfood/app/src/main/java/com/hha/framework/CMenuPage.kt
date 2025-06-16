package com.hha.framework

import com.hha.menu.page.MenuPage

data class CMenuPage(
val menuCardId: Int,
val menuPageId: Int,
val localName: String,
val chineseName: String,
val pageButtonSize: Int,
val picture: String,
val isVerticalOrientation: Boolean,
val menuItems: List<CMenuItem> = emptyList()  // Added list of MenuItems
) {
    // Helper function to get display name based on locale
    fun getDisplayName(useChinese: Boolean): String {
        return if (useChinese) chineseName else localName
    }

    // Helper function to add items (returns a new immutable instance)
    fun withItems(newItems: List<CMenuItem>): CMenuPage {
        return this.copy(menuItems = newItems)
    }

    // Helper function to add a single item
    fun withAddedItem(item: CMenuItem): CMenuPage {
        return this.copy(menuItems = menuItems + item)
    }

    // Helper function to remove an item
    fun withoutItem(itemId: Int): CMenuPage {
        return this.copy(menuItems =
            menuItems.filter { it.menuItemId != itemId })
    }
}
