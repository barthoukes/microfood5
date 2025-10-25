package com.hha.framework

import com.hha.grpc.GrpcServiceFactory
import com.hha.types.CMoney
import com.hha.types.EClientOrdersType

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
                    transactionId = protoTransaction.transactionId.toInt(),
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
                    transactionId = protoTransaction.transactionId.toInt(),
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
    fun getTransactionById(id: Int): CTransaction? {
        return transactions.firstOrNull { it.data.transactionId == id }
    }

    /**
     * Gets all transactions for a specific customer
     */
    fun getTransactionsByCustomer(customerId: Int): List<CTransaction> {
        return transactions.filter { it.data.customerId == customerId }
    }

    /**
     * Gets all transactions with a specific status
     */
    fun getTransactionsByStatus(status: EClientOrdersType): List<CTransaction> {
        return transactions.filter { it.data.status == status }
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
    val isEmpty : Boolean
        get() = transactions.isEmpty()

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
    private fun com.hha.common.ClientOrdersType.toClientOrdersType(): EClientOrdersType {
        return when (this) {
            com.hha.common.ClientOrdersType.CLIENT_ORDER_OPEN -> EClientOrdersType.OPEN
            com.hha.common.ClientOrdersType.CLIENT_ORDER_CLOSED -> EClientOrdersType.CLOSED
            com.hha.common.ClientOrdersType.CLIENT_ORDER_OPEN_PAID -> EClientOrdersType.OPEN_PAID
            else -> EClientOrdersType.OPEN
        }
    }
}