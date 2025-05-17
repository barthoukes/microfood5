package com.hha.service

import com.hha.common.Empty
import com.hha.common.MenuCardId
import com.hha.common.TextDump
import com.hha.menu.cluster.Cluster
import com.hha.menu.cluster.ClusterId
import com.hha.menu.cluster.ClusterIdRequest
import com.hha.menu.cluster.ClusterList
import com.hha.menu.cluster.CountMenuCards
import com.hha.menu.cluster.DuplicateClusterRequest
import com.hha.menu.cluster.DuplicateMenuCardRequest
import com.hha.menu.cluster.ExchangeClusterRequest
import com.hha.menu.cluster.MenuClusterServiceGrpcKt
import com.hha.menu.cluster.SaveClusterRequest

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class MenuClusterService(channel: ManagedChannel) : BaseGrpcService<MenuClusterServiceGrpcKt.MenuClusterServiceCoroutineStub>(channel) {
    override val stub: MenuClusterServiceGrpcKt.MenuClusterServiceCoroutineStub by lazy {
        MenuClusterServiceGrpcKt.MenuClusterServiceCoroutineStub(channel)
    }

    fun backupClusters(): Boolean = runBlocking {
        try {
            stub.backupClusters(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun duplicateCluster(menuCardId: Int, clusterIndex: Int, newClusterIndex: Int): Int? = runBlocking {
        try {
            val request = DuplicateClusterRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setClusterIndex(clusterIndex)
                .setNewClusterIndex(newClusterIndex)
                .build()
            stub.duplicateCluster(request).clusterId
        } catch (e: Exception) {
            null
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

    fun eraseCluster(menuCardId: Int, clusterId: Int): Boolean = runBlocking {
        try {
            val request = ClusterIdRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setClusterId(clusterId)
                .build()
            stub.eraseCluster(request)
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

    fun findAllClusters(menuCardId: Int): ClusterList? = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.findAllClusters(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getCluster(clusterId: Int): Cluster? = runBlocking {
        try {
            val request = ClusterId.newBuilder()
                .setClusterId(clusterId)
                .build()
            stub.getCluster(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getNrClusters(menuCardId: Int): Int? = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.getNrClusters(request).menuCards
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

    fun removeAllClustersFromMenuCard(menuCardId: Int): Boolean = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.removeAllClustersFromMenuCard(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restoreClusters(): Boolean = runBlocking {
        try {
            stub.restoreClusters(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveAllClusters(menuCardId: Int, clusters: List<Cluster>): Boolean = runBlocking {
        try {
            val request = SaveClusterRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .addAllClusters(clusters)
                .build()
            stub.saveAllClusters(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateCluster(cluster: Cluster): Boolean = runBlocking {
        try {
            stub.updateCluster(cluster)
            true
        } catch (e: Exception) {
            false
        }
    }
}