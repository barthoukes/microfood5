package com.hha.framework

import com.hha.floor.FloorPlanList
import com.hha.grpc.GrpcServiceFactory
import com.hha.floor.Table
import com.hha.floor.TableList

class CFloorTables {
    private var floorTables: MutableList<CFloorTable> = mutableListOf<CFloorTable>()
    private var floorPlans: MutableList<Int> = mutableListOf()

    fun load() {
        loadFloorPlans()
        val floorTableService = GrpcServiceFactory.createFloorTableService()

        for (plan in floorPlans) {
            val list :TableList? = floorTableService.getFloorTables(
                plan, 0,false)
            if (list != null) {
                for (table: Table in list.tablesList) {
                    val tab = CFloorTable(table)
                    floorTables[table.tableId] = tab
                }
            }
        }
    }

    fun getFloorTable(position: Int): CFloorTable? {
        try {
            return floorTables[position]
        }
        catch (e: Exception) {
            return null
        }
    }

    val size: Int
        get() {
            return floorTables.size
        }

    fun loadFloorPlans() {
        val floorPlanService = GrpcServiceFactory.createFloorPlanService()
        floorPlans.clear()
        val list : FloorPlanList? = floorPlanService.getFloorPlans(-1, false)

        if (list != null) {
            for (plan in list.floorPlansList) {
                if (plan.enabled) {
                    floorPlans.add(plan.floorPlanId)
                }
            }
        }
    }


}