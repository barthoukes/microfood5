package com.hha.service

import com.hha.common.Empty
import com.hha.database.*
import com.hha.database.DatabaseServiceGrpcKt
import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class DatabaseService(channel: ManagedChannel) : BaseGrpcService<DatabaseServiceGrpcKt.DatabaseServiceCoroutineStub>(channel) {
    override val stub: DatabaseServiceGrpcKt.DatabaseServiceCoroutineStub by lazy {
        DatabaseServiceGrpcKt.DatabaseServiceCoroutineStub(channel)
    }

    fun setDatabase(hostname: String, user: String, password: String, database: String): Boolean = runBlocking {
        try {
            val request = SetDatabaseRequest.newBuilder()
                .setHostname(hostname)
                .setUser(user)
                .setPassword(password)
                .setDatabase(database)
                .build()
            stub.setDatabase(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun runQuery(query: String): String? = runBlocking {
        try {
            val request = QueryRequest.newBuilder()
                .setQuery(query)
                .build()
            stub.runQuery(request).result
        } catch (e: Exception) {
            null
        }
    }

    fun validateDatabase(index: Int): Boolean? = runBlocking {
        try {
            val request = ValidateDatabaseRequest.newBuilder()
                .setIndex(index)
                .build()
            stub.validateDatabase(request).result
        } catch (e: Exception) {
            null
        }
    }

    fun setSqlDefaults(defaults: SqlDefaults): Boolean = runBlocking {
        try {
            stub.setSqlDefaults(defaults)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setConfiguration(request: ConfigurationRequest): Boolean = runBlocking {
        try {
            stub.setConfiguration(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setConfigurationList(items: List<ConfigurationItem>): Boolean = runBlocking {
        try {
            val request = ConfigurationItemList.newBuilder()
                .addAllItem(items)
                .build()
            stub.setConfigurationList(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getConfigurationList(): ConfigurationItemList? = runBlocking {
        try {
            stub.getConfigurationList(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }


    fun getFontConfigurationList(): ConfigurationItemList? = runBlocking {
        try {
            stub.getFontConfigurationList(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }


    fun getColourConfigurationList(): ConfigurationItemList? = runBlocking {
        try {
            stub.getColourConfigurationList(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun setUserConfigurationList(items: List<ConfigurationItem>): Boolean = runBlocking {
        try {
            val request = ConfigurationItemList.newBuilder()
                .addAllItem(items)
                .build()
            stub.setUserConfigurationList(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserConfigurationList(): ConfigurationItemList? = runBlocking {
        try {
            stub.getUserConfigurationList(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun resetProgram(): Boolean = runBlocking {
        try {
            stub.resetProgram(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }
}

