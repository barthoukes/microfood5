package com.hha.service

import com.hha.common.Empty
import com.hha.common.TableStatus
import com.hha.common.TableType
import com.hha.common.TextDump
import com.hha.common.ClientOrdersType
import com.hha.floor.ConnectFloorTable
import com.hha.floor.CreateTableRequest
import com.hha.floor.FloorPlanOnlyOpen
import com.hha.floor.FloorTableServiceGrpcKt
import com.hha.floor.KeybuttonParam
import com.hha.floor.MoveTableAndNameParam
import com.hha.floor.NameParam
import com.hha.floor.NameValueParam
import com.hha.floor.PersonsParam
import com.hha.floor.Table
import com.hha.floor.TableExist
import com.hha.floor.TableIdDisplaySizeParam
import com.hha.floor.TableIdParam
import com.hha.floor.TableIdStatusParam
import com.hha.floor.TableList
import com.hha.floor.TableMinutesParam
import com.hha.floor.TransactionIdParam
import com.hha.floor.TransactionToTableParam

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class FloorTableService(channel: ManagedChannel) : BaseGrpcService<FloorTableServiceGrpcKt.FloorTableServiceCoroutineStub>(channel) {
    override val stub: FloorTableServiceGrpcKt.FloorTableServiceCoroutineStub by lazy {
        FloorTableServiceGrpcKt.FloorTableServiceCoroutineStub(channel)
    }

    fun updateTable(table: Table): Boolean = runBlocking {
        try {
            stub.updateTable(table)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun smaller(tableId: Int): Boolean = runBlocking {
        try {
            val request = TableIdParam.newBuilder()
                .setTableId(tableId)
                .build()
            stub.smaller(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun lower(tableId: Int): Boolean = runBlocking {
        try {
            val request = TableIdParam.newBuilder()
                .setTableId(tableId)
                .build()
            stub.lower(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun wider(tableId: Int, squaresWidth: Int, squaresHeight: Int): Boolean = runBlocking {
        try {
            val request = TableIdDisplaySizeParam.newBuilder()
                .setTableId(tableId)
                .setSquaresWidth(squaresWidth)
                .setSquaresHeight(squaresHeight)
                .build()
            stub.wider(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun higher(tableId: Int, squaresWidth: Int, squaresHeight: Int): Boolean = runBlocking {
        try {
            val request = TableIdDisplaySizeParam.newBuilder()
                .setTableId(tableId)
                .setSquaresWidth(squaresWidth)
                .setSquaresHeight(squaresHeight)
                .build()
            stub.higher(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setPersons(tableId: Int, numberPersons: Int): Boolean = runBlocking {
        try {
            val request = PersonsParam.newBuilder()
                .setTableId(tableId)
                .setNumberPersons(numberPersons)
                .build()
            stub.setPersons(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setName(tableId: Int, name: String): Boolean = runBlocking {
        try {
            val request = NameParam.newBuilder()
                .setTableId(tableId)
                .setName(name)
                .build()
            stub.setName(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getName(tableId: Int): NameParam? = runBlocking {
        try {
            val request = TableIdParam.newBuilder()
                .setTableId(tableId)
                .build()
            stub.getName(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getFloorTables(floorPlanId: Int, firstKey: Int, onlyOpenTables: Boolean): TableList? = runBlocking {
        try {
            val request = FloorPlanOnlyOpen.newBuilder()
                .setFloorPlanId(floorPlanId)
                .setFirstKey(firstKey)
                .setOnlyOpenTables(onlyOpenTables)
                .build()
            stub.getFloorTables(request)
        } catch (e: Exception) {
            null
        }
    }

    fun createTable(floorPlanId: Int): Table? = runBlocking {
        try {
            val request = CreateTableRequest.newBuilder()
                .setFloorPlanId(floorPlanId)
                .build()
            stub.createTable(request)
        } catch (e: Exception) {
            null
        }
    }

    fun findAny(name: String): Int? = runBlocking {
        try {
            val request = NameValueParam.newBuilder()
                .setName(name)
                .build()
            stub.findAny(request).tableId
        } catch (e: Exception) {
            null
        }
    }

    fun tableExists(name: String): Boolean? = runBlocking {
        try {
            val request = NameValueParam.newBuilder()
                .setName(name)
                .build()
            stub.tableExists(request).tableExists
        } catch (e: Exception) {
            null
        }
    }

    fun findNewTableNumber(): String? = runBlocking {
        try {
            stub.findNewTableNumber(Empty.getDefaultInstance()).name
        } catch (e: Exception) {
            null
        }
    }

    fun removeBeerDrinker(name: String): Boolean = runBlocking {
        try {
            val request = NameValueParam.newBuilder()
                .setName(name)
                .build()
            stub.removeBeerDrinker(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun connectTableToTransaction(floorTableId: Int, transactionId: Int, minutes: Int): Boolean = runBlocking {
        try {
            val request = ConnectFloorTable.newBuilder()
                .setFloorTableId(floorTableId)
                .setTransactionId(transactionId)
                .setMinutes(minutes)
                .build()
            stub.connectTableToTransaction(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun closeTransaction(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionIdParam.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.closeTransaction(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun moveTransactionAndName(transactionId: Int, transactionName: String, newTableName: String): Boolean = runBlocking {
        try {
            val request = MoveTableAndNameParam.newBuilder()
                .setTransactionId(transactionId)
                .setTransactionName(transactionName)
                .setNewTableName(newTableName)
                .build()
            stub.moveTransactionAndName(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun moveTransaction(transactionId: Int, tableId: Int): Boolean = runBlocking {
        try {
            val request = TransactionToTableParam.newBuilder()
                .setTransactionId(transactionId)
                .setTableId(tableId)
                .build()
            stub.moveTransaction(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addTransaction(transactionId: Int, tableId: Int): Boolean = runBlocking {
        try {
            val request = TransactionToTableParam.newBuilder()
                .setTransactionId(transactionId)
                .setTableId(tableId)
                .build()
            stub.addTransaction(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun extendLatestTable(name: String): Boolean = runBlocking {
        try {
            val request = NameValueParam.newBuilder()
                .setName(name)
                .build()
            stub.extendLatestTable(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getFloorTableById(tableId: Int): Table? = runBlocking {
        try {
            val request = TableIdParam.newBuilder()
                .setTableId(tableId)
                .build()
            stub.getFloorTableById(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getFloorTableByName(name: String): Table? = runBlocking {
        try {
            val request = NameValueParam.newBuilder()
                .setName(name)
                .build()
            stub.getFloorTableByName(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getFloorTableFromKeybutton(floorPlanId: Int, keybutton: Int): Table? = runBlocking {
        try {
            val request = KeybuttonParam.newBuilder()
                .setFloorPlanId(floorPlanId)
                .setKeybutton(keybutton)
                .build()
            stub.getFloorTableFromKeybutton(request)
        } catch (e: Exception) {
            null
        }
    }

    fun addMinutes(transactionId: Int, minutes: Int): Boolean = runBlocking {
        try {
            val request = TableMinutesParam.newBuilder()
                .setTransactionId(transactionId)
                .setMinutes(minutes)
                .build()
            stub.addMinutes(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateData(): Boolean = runBlocking {
        try {
            stub.updateData(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun rename(tableId: Int, name: String): Boolean = runBlocking {
        try {
            val request = NameParam.newBuilder()
                .setTableId(tableId)
                .setName(name)
                .build()
            stub.rename(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findTable(name: String): Table? = runBlocking {
        try {
            val request = NameValueParam.newBuilder()
                .setName(name)
                .build()
            stub.findTable(request)
        } catch (e: Exception) {
            null
        }
    }

    fun setStatus(tableId: Int, status: TableStatus): Boolean = runBlocking {
        try {
            val request = TableIdStatusParam.newBuilder()
                .setTableId(tableId)
                .setStatus(status)
                .build()
            stub.setStatus(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateDrinksMinutes(): Boolean = runBlocking {
        try {
            stub.updateDrinksMinutes(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getTableIdFromTransactionId(transactionId: Int): Int? = runBlocking {
        try {
            val request = TransactionIdParam.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.getTableIdFromTransactionId(request).tableId
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

    fun destroy(tableId: Int): Boolean = runBlocking {
        try {
            val request = TableIdParam.newBuilder()
                .setTableId(tableId)
                .build()
            stub.destroy(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setTransactionTableAvailable(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionIdParam.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.setTransactionTableAvailable(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}