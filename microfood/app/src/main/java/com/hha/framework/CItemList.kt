package com.hha.framework

import com.hha.framework.CItem

import com.hha.common.ItemList

data class CItemList (
    var items : MutableList<CItem> = mutableListOf(),
    var fromGrpc : Boolean = false
) : Iterable<CItem> {

    constructor(initialItems: List<CItem>) : this() {
        for (i in initialItems) {
            items.add(CItem(i)) // Using copy constructor
        }
        fromGrpc = false
    }

    // Conversion from GRPC type ItemList to our CItemList
    constructor(dummy: Boolean, itemsGRPC: ItemList?) : this() {
        fromGrpc = dummy
        if (itemsGRPC == null) return
        for (i in itemsGRPC.entryList) {
            val p = CItem(i)
            items.add(p)
        }
    }

    // Override the [] operator for getting items
    operator fun get(index: Int): CItem
    {
        return items[index]
    }

    fun add(item: CItem)
    {
        items.add(item)
    }

    val size : Int
        get() = items.size

    fun clear()
    {
        items.clear()
    }

    // Iterable implementation
    override fun iterator(): Iterator<CItem> = items.iterator()

    // Additional useful operations
    fun asSequence(): Sequence<CItem> = items.asSequence()
    fun toList(): List<CItem> = items.toList()
    fun isEmpty(): Boolean = items.isEmpty()
    fun isNotEmpty(): Boolean = items.isNotEmpty()
}
