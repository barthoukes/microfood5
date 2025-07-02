package com.hha.framework

import com.hha.grpc.GrpcServiceFactory
import com.hha.types.CMoney
import com.hha.types.ClientOrdersType

class CTransactionList() : Iterable<CTransaction> {
    private val transactions = mutableListOf<CTransaction>()
    private var transactionService = GrpcServiceFactory.createDailyTransactionService()
    /**
     * Loads all open transactions from the server
     */
    suspend fun loadOpenTransactions(): Boolean {
        return try {
            val response = transactionService.listOpen()
            response?.transactionsList?.mapNotNull { protoTransaction ->
                CTransaction(
                    id = protoTransaction.transactionId.toLong(),
                    name = protoTransaction.name,
                    time = protoTransaction.timeStart,
                    status = protoTransaction.status.toClientOrdersType(),
                    customerId = protoTransaction.customerId,
                    total = CMoney(protoTransaction.total)
                )
            }?.let {
                transactions.clear()
                transactions.addAll(it)
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Loads all transactions (open and closed) from the server
     * @param sortOnTime Whether to sort by time (true) or by ID (false)
     */
    suspend fun loadAllTransactions(sortOnTime: Boolean = true): Boolean {
        return try {
            val response = transactionService.listAll(sortOnTime)
            response?.transactionsList?.mapNotNull { protoTransaction ->
                CTransaction(
                    id = protoTransaction.transactionId.toLong(),
                    name = protoTransaction.name,
                    time = protoTransaction.timeStart,
                    status = protoTransaction.status.toClientOrdersType(),
                    customerId = protoTransaction.customerId,
                    total = CMoney(protoTransaction.total)
                )
            }?.let {
                transactions.clear()
                transactions.addAll(it)
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets a transaction by ID
     */
    fun getTransactionById(id: Long): CTransaction? {
        return transactions.firstOrNull { it.id == id }
    }

    /**
     * Gets all transactions for a specific customer
     */
    fun getTransactionsByCustomer(customerId: Int): List<CTransaction> {
        return transactions.filter { it.customer_id == customerId }
    }

    /**
     * Gets all transactions with a specific status
     */
    fun getTransactionsByStatus(status: ClientOrdersType): List<CTransaction> {
        return transactions.filter { it.status == status }
    }

    /**
     * Adds a new transaction to the list (without server persistence)
     */
    fun addTransaction(transaction: CTransaction) {
        transactions.add(transaction)
    }

    /**
     * Removes a transaction from the list (without server persistence)
     */
    fun removeTransaction(transaction: CTransaction): Boolean {
        return transactions.remove(transaction)
    }

    /**
     * Clears all transactions from the list
     */
    fun clear() {
        transactions.clear()
    }

    /**
     * Number of transactions in the list
     */
    val size: Int
        get() = transactions.size

    /**
     * Checks if the list is empty
     */
    fun isEmpty(): Boolean = transactions.isEmpty()

    /**
     * Checks if the list is not empty
     */
    fun isNotEmpty(): Boolean = transactions.isNotEmpty()

    /**
     * Gets a transaction by position
     */
    operator fun get(position: Int): CTransaction {
        return transactions[position]
    }

    /**
     * Iterator implementation
     */
    override fun iterator(): Iterator<CTransaction> {
        return transactions.iterator()
    }

    // Extension functions for gRPC type conversion
    private fun com.hha.common.ClientOrdersType.toClientOrdersType(): ClientOrdersType {
        return when (this) {
            com.hha.common.ClientOrdersType.CLIENT_ORDER_OPEN -> ClientOrdersType.OPEN
            com.hha.common.ClientOrdersType.CLIENT_ORDER_CLOSED -> ClientOrdersType.CLOSED
            com.hha.common.ClientOrdersType.CLIENT_ORDER_OPEN_PAID -> ClientOrdersType.OPEN_PAID
            else -> ClientOrdersType.OPEN
        }
    }
}