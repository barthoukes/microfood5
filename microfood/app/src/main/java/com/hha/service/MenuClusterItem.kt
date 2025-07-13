package com.hha.service

import com.hha.common.Empty
import com.hha.common.Money
import com.hha.menu.cluster.item.Cluster
import com.hha.menu.cluster.item.ClusterIdRequest
import com.hha.menu.cluster.item.ClusterItem
import com.hha.menu.cluster.item.ClusterItemList
import com.hha.menu.cluster.item.ClusterItemSize
import com.hha.menu.cluster.item.DeleteClusterItemRequest
import com.hha.menu.cluster.item.DuplicateClusterItemRequest
import com.hha.menu.cluster.item.DuplicateMenuCardRequest
import com.hha.menu.cluster.item.ExchangeClusterRequest
import com.hha.menu.cluster.item.InsertClusterItemRequest
import com.hha.menu.cluster.item.MenuCardId
import com.hha.menu.cluster.item.MenuClusterItemServiceGrpcKt
import com.hha.menu.cluster.item.SaveClusterItemsRequest
import com.hha.menu.cluster.item.TextDump

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class MenuClusterItemService(channel: ManagedChannel) : BaseGrpcService<MenuClusterItemServiceGrpcKt.MenuClusterItemServiceCoroutineStub>(channel) {
    override val stub: MenuClusterItemServiceGrpcKt.MenuClusterItemServiceCoroutineStub by lazy {
        MenuClusterItemServiceGrpcKt.MenuClusterItemServiceCoroutineStub(channel)
    }

    fun backupClusterItems(): Boolean = runBlocking {
        try {
            stub.backupClusterItems(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteClusterItem(menuCardId: Int, clusterId: Int, sequence: Int): Boolean = runBlocking {
        try {
            val request = DeleteClusterItemRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setClusterId(clusterId)
                .setSequence(sequence)
                .build()
            stub.deleteClusterItem(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun duplicateClusterItem(menuCardId: Int, clusterItemIndex: Int, newClusterItemIndex: Int): Boolean = runBlocking {
        try {
            val request = DuplicateClusterItemRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setClusterItemIndex(clusterItemIndex)
                .setNewClusterItemIndex(newClusterItemIndex)
                .build()
            stub.duplicateClusterItem(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun duplicateMenuCard(menuCardId: Int, destinationId: Int): Boolean = runBlocking {
        try {
            val request = DuplicateMenuCardRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setDestinationId(destinationId)
                .build()
            stub.duplicateMenuCard(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exchangeCluster(menuCardId: Int, srcClusterId: Int, dstClusterId: Int): Boolean = runBlocking {
        try {
            val request = ExchangeClusterRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setSrcClusterId(srcClusterId)
                .setDstClusterId(dstClusterId)
                .build()
            stub.exchangeCluster(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findAllExcelItems(menuCardId: Int): ClusterItemList? = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.findAllExcelItems(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getSize(menuCardId: Int): Int? = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.getSize(request).nrItems
        } catch (e: Exception) {
            null
        }
    }

    fun insertClusterItem(menuCardId: Int, clusterId: Int, sequence: Int, menuItemId: Int): Boolean = runBlocking {
        try {
            val request = InsertClusterItemRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setClusterId(clusterId)
                .setSequence(sequence)
                .setMenuItemId(menuItemId)
                .build()
            stub.insertClusterItem(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun mysqlDump(): String? = runBlocking {
        try {
            stub.mysqlDump(Empty.getDefaultInstance()).dump
        } catch (e: Exception) {
            null
        }
    }

    fun saveAllClusterItems(menuCardId: Int, clusterItems: List<ClusterItem>): Boolean = runBlocking {
        try {
            val request = SaveClusterItemsRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .addAllClusterItems(clusterItems)
                .build()
            stub.saveAllClusterItems(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun selectClusterItems(menuCardId: Int, clusterId: Int): ClusterItemList? = runBlocking {
        try {
            val request = ClusterIdRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setClusterId(clusterId)
                .build()
            stub.selectClusterItems(request)
        } catch (e: Exception) {
            null
        }
    }

    fun updateClusterItem(clusterItem: ClusterItem): Boolean = runBlocking {
        try {
            stub.updateClusterItem(clusterItem)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun renumberClusterItems(menuCardId: Int): Boolean = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.renumberClusterItems(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeAllClusterItemsFromMenuCard(menuCardId: Int): Boolean = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.removeAllClusterItemsFromMenuCard(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restoreClusterItems(): Boolean = runBlocking {
        try {
            stub.restoreClusterItems(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }
}