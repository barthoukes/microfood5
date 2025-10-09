package com.hha.framework

import androidx.recyclerview.widget.SortedList
import com.hha.framework.CMenuItem
import com.hha.types.CMoney

data class CMenuItems(
    private var columns: List<Int> = emptyList(),
    private var rows: List<Int> = emptyList(),
    var menuItems: MutableMap<Int, CMenuItem> = mutableMapOf(),
    var itemPerPosition: MutableMap<Int, CMenuItem> = mutableMapOf(),
    var verticalSize: Int = 8

)
{
    fun setItems(items: MutableMap<Int, CMenuItem>)
    {
        menuItems = items
        itemPerPosition.clear()
        findColumnsAndRows()
        fillInSequenceIds()
    }

    fun fillInSequenceIds() {
        for (item in menuItems) {
            val x = columns.binarySearch(item.value.positionX)
            val y = rows.binarySearch(item.value.positionY)
            item.value.sequence = x*verticalSize +y
        }
        for (item in menuItems) {
            itemPerPosition[item.value.sequence] = item.value
        }
    }

    fun findColumnsAndRows() {
        val uniqueX = menuItems.map { it.value.positionX }.toSet()
        val uniqueY = menuItems.map { it.value.positionY }.toSet()
        columns = uniqueX.sorted()
        rows = uniqueY.sorted()
        if (rows.size>4) {
            verticalSize = 8
        }
        else if (rows.size>2) {
            verticalSize = 4
        }
        else if (rows.size>1) {
            verticalSize = 2
        }
        else verticalSize = 1
    }

    fun addMenuItem(menuItem: CMenuItem) {
        menuItems[menuItem.menuItemId] = menuItem
    }

    fun getItemCount() : Int {
        return verticalSize*columns.size
    }

    fun findItemByPosition(position: Int) : CMenuItem? {
        if (itemPerPosition.containsKey(position)) {
            return itemPerPosition[position]
        }
        else {
            return null
        }
    }

}

//package com.hha.framework
//
//import com.hha.framework.CMenuItem
//
//data class CMenuItems(
//    private val squareWidth: Int = 32,  // Grid cell width
//    private val squareHeight: Int = 8,  // Grid cell height
//    private val maxHeight: Int = 32    // Maximum page height (multiple of squareHeight)
//) {
//    // Primary storage by ID
//    private val menuItems: MutableMap<Int, CMenuItem> = mutableMapOf()
//
//    // Spatial index [column][row] → CMenuItem
//    private val gridIndex: Array<Array<CMenuItem?>> = run {
//        val columns = 100  // Adjust based on max expected columns
//        val rows = maxHeight / squareHeight
//        Array(columns) { arrayOfNulls<CMenuItem>(rows) }
//    }
//
//    fun addMenuItem(menuItem: CMenuItem) {
//        menuItems[menuItem.menuItemId] = menuItem
//
//        // Calculate grid position
//        val col = menuItem.positionX / squareWidth
//        val row = menuItem.positionY / squareHeight
//
//        // Store in spatial index
//        if (col < gridIndex.size && row < gridIndex[0].size) {
//            gridIndex[col][row] = menuItem
//        }
//    }
//
//    // Find by grid coordinates (x,y in pixels)
//    fun findItemByPosition(x: Int, y: Int): CMenuItem? {
//        val col = x / squareWidth
//        val row = y / squareHeight
//        return gridIndex.getOrNull(col)?.getOrNull(row)
//    }
//
//    // Find by linear index (0-7 first column, 8-15 second column etc.)
//    fun findItemByIndex(index: Int): CMenuItem? {
//        val itemsPerColumn = maxHeight / squareHeight
//        val col = index / itemsPerColumn
//        val row = index % itemsPerColumn
//        return gridIndex.getOrNull(col)?.getOrNull(row)
//    }
//
//    // Get all items in column-major order
//    fun getAllItems(): List<CMenuItem> = gridIndex.flatMap { column ->
//        column.filterNotNull()
//    }
//}