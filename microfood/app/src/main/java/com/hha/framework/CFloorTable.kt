package com.hha.framework

import com.hha.floor.Table

data class CFloorTable (
    var id: Int,
    var name: String
) {
    constructor(table: Table) : this(table.tableId, table.name)
}