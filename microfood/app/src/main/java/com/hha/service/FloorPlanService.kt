package com.hha.service

import com.hha.common.Empty
import com.hha.common.TextDump
import com.hha.floor.FloorPlan
import com.hha.floor.FloorPlanId
import com.hha.floor.FloorPlanList
import com.hha.floor.FloorPlanName
import com.hha.floor.FloorPlanServiceGrpcKt
import com.hha.floor.GetFloorPlanRequest

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class FloorPlanService(channel: ManagedChannel) : BaseGrpcService<FloorPlanServiceGrpcKt.FloorPlanServiceCoroutineStub>(channel) {
    override val stub: FloorPlanServiceGrpcKt.FloorPlanServiceCoroutineStub by lazy {
        FloorPlanServiceGrpcKt.FloorPlanServiceCoroutineStub(channel)
    }

    fun createFloorPlan(name: String): Int? = runBlocking {
        try {
            val request = FloorPlanName.newBuilder()
                .setName(name)
                .build()
            stub.createFloorPlan(request).floorPlanId
        } catch (e: Exception) {
            null
        }
    }

    fun getFloorPlans(floorPlanId: Int, disabledFloors: Boolean): FloorPlanList? = runBlocking {
        try {
            val request = GetFloorPlanRequest.newBuilder()
                .setFloorPlanId(floorPlanId)
                .setDisabledFloors(disabledFloors)
                .build()
            stub.getFloorPlans(request)
        } catch (e: Exception) {
            null
        }
    }

    fun mysqlDump(): String? = runBlocking {
        try {
            stub.mysqlDump(Empty.getDefaultInstance()).dump
        } catch (e: Exception) {
            null
        }
    }

    fun removeFloorPlan(floorPlanId: Int): Boolean = runBlocking {
        try {
            val request = FloorPlanId.newBuilder()
                .setFloorPlanId(floorPlanId)
                .build()
            stub.removeFloorPlan(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateRoom(floorPlanId: Int, floorName: String, enabled: Boolean): Boolean = runBlocking {
        try {
            val request = FloorPlan.newBuilder()
                .setFloorPlanId(floorPlanId)
                .setFloorName(floorName)
                .setEnabled(enabled)
                .build()
            stub.updateRoom(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}