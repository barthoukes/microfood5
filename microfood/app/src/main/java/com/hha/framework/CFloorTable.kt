package com.hha.framework

import com.hha.floor.Table
import com.hha.types.CMoney
import com.hha.types.EClientOrdersType
import com.hha.types.ETableStatus
import com.hha.types.ETableType
import com.hha.types.EDrinksStatus

/**
 * Represents the detailed state and properties of a single table on the floor plan.
 * This corresponds to the C++ 'Stable' class and holds all relevant data for a table.
 */
data class CFloorTable(
    var tableId: Int,                   // Id for the table
    var keybutton: Int,                 // Which key represents on display.
    var name: String,                   // Name of table
    var amount: CMoney,                 // Amount in cents
    var minutes: Int,                   // Time in minutes
    var endTime: String,                // End time
    var days: Int,                      // Days
    var tableStatus: ETableStatus,      // Status table -> TODO: Convert to a TableStatus enum
    var transactionStatus: EClientOrdersType, // Status transaction -> TODO: Convert to a ClientOrdersType enum
    var left: Int,                      // Position
    var top: Int,                       // Position
    var width: Int,                     // Position
    var height: Int,                    // Position
    var shape: Int,                     // Shape (circle or square) -> TODO: Convert to Eshape enum
    var tableType: ETableType,          // Type for this table -> TODO: Convert to EtableType enum
    var customerId: Int,                // Which customer
    var transactionId: Int,             // What transaction
    var daysLeft: Int,                  // Total days left
    var minutesLeft: Int,               // total minutes left
    var actualPersonCount: Int,         // Actual number of persons
    var maxPersonCount: Int,            // Persons for table
    var drinksMinutes: Int,             // Time from last drink
    var floorPlanId: Int,               // Room for this table
    var drinksStatus: EDrinksStatus     // Status for drinks
) {
    /**
     * Secondary constructor to create a CFloorTable from a gRPC 'Table' object.
     * Note: This assumes the gRPC 'Table' object will be updated to contain all these fields.
     * You will need to map the fields from the gRPC object to this data class.
     */
    constructor(table: Table) : this(
        tableId = table.tableId,
        keybutton = table.keybutton,
        name = table.name,
        amount = CMoney(table.amount),
        minutes = table.minutes,
        endTime = table.endTime,
        days = table.days,
        tableStatus = ETableStatus.fromTableStatus(table.tableStatus),
        transactionStatus = EClientOrdersType.fromClientOrdersType(table.transactionStatus),
        left = table.left,
        top = table.top,
        width = table.width,
        height = table.height,
        shape = table.shape,
        tableType = ETableType.fromTableType(table.tableType),
        customerId = table.customerId,
        transactionId = table.transactionId,
        daysLeft = table.daysLeft,
        minutesLeft = table.minutesLeft,
        actualPersonCount = table.actualPersonCount,
        maxPersonCount = 4, //table.maxPersonCount,
        drinksMinutes = table.drinksMinutes,
        floorPlanId = table.floorPlanId,
        drinksStatus = EDrinksStatus.fromMinutes(
            table.drinksMinutes)
        )
}
