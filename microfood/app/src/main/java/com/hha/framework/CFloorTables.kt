package com.hha.framework

import com.hha.floor.FloorPlanList
import com.hha.grpc.GrpcServiceFactory
import com.hha.floor.Table
import com.hha.floor.TableList
import com.hha.resources.Global

class CFloorTables: Iterable<CFloorTable>
{
    var mTables: MutableList<CFloorTable> = mutableListOf()

    fun add(floorTable: CFloorTable) = mTables.add(floorTable)

    override fun iterator(): Iterator<CFloorTable> = mTables.iterator()

    fun getFloorTable(index: Int): CFloorTable? = mTables.getOrNull(index)

    fun setTransactionAvailable(transactionId: Int)
    {
        val service = GrpcServiceFactory.createFloorTableService()
        service.setTransactionTableAvailable(transactionId)
    }

    val size: Int
        get() = mTables.size

}