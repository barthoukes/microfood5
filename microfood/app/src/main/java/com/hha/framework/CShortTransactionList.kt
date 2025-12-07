package com.hha.framework

import com.hha.grpc.GrpcServiceFactory
import com.hha.types.CMoney
import com.hha.types.EClientOrdersType
import com.hha.framework.CShortTransaction

class CShortTransactionList() : Iterable<CShortTransaction>
{
    private var shortTransactions = mutableListOf<CShortTransaction>()

    /**
     * Returns the list of transactions that were fetched by a load method.
     * @return A list of CShortTransaction objects.
     */
    fun getList(): List<CShortTransaction>
    {
        // Simply return the internal list.
        return shortTransactions
    }

    /**
     * Loads all open transactions from the server
     */
    fun loadOpenTransactions(): Boolean
    {
        val transactionService =
            GrpcServiceFactory.createDailyTransactionService()
        shortTransactions.clear()
        try
        {
            val response = transactionService.listOpen()
            if (response != null)
            {
                val list = response.transactionsList
                for (transactionListItem in list)
                {
                    val shortTransaction = CShortTransaction(transactionListItem)
                    shortTransactions.add(shortTransaction)
                }
            }
        } catch (e: Exception)
        {
            return false
        }
        return true
    }

    /**
     * Loads all transactions (open and closed) from the server
     * @param sortOnTime Whether to sort by time (true) or by ID (false)
     */
    fun loadAllTransactions(sortOnTime: Boolean = true): Boolean
    {
        try
        {
            shortTransactions.clear()
            val service = GrpcServiceFactory.createDailyTransactionService()
            val response = service.listAll(sortOnTime)
            if (response != null)
            {
                val list = response.transactionsList
                for (transactionListItem in list)
                {
                    val shortTransaction = CShortTransaction(transactionListItem)
                    shortTransactions.add(shortTransaction)
                }
            }
        } catch (e: Exception)
        {
            return false
        }
        return true
    }

    /**
     * Gets a transaction by ID
     */
    fun getTransactionById(id: Int): CShortTransaction?
    {
        val transactionService =
            GrpcServiceFactory.createDailyTransactionService()

        return shortTransactions.firstOrNull { it.transactionId == id }
    }

    /**
     * Gets all transactions for a specific customer
     */
    fun getTransactionsByCustomer(customerId: Int): List<CShortTransaction>
    {
        return shortTransactions.filter { it.customerId == customerId }
    }

    /**
     * Gets all transactions with a specific status
     */
    fun getTransactionsByStatus(status: EClientOrdersType): List<CShortTransaction>
    {
        return shortTransactions.filter { it.status == status }
    }

    /**
     * Adds a new transaction to the list (without server persistence)
     */
    fun addShortTransaction(shortTransaction: CShortTransaction)
    {
        shortTransactions.add(shortTransaction)
    }

    /**
     * Removes a transaction from the list (without server persistence)
     */
    fun removeTransaction(shortTransaction: CShortTransaction): Boolean
    {
        return shortTransactions.remove(shortTransaction)
    }

    /**
     * Clears all transactions from the list
     */
    fun clear()
    {
        shortTransactions.clear()
    }

    /**
     * Number of transactions in the list
     */
    val size: Int
        get() = shortTransactions.size

    /**
     * Checks if the list is empty
     */
    val isEmpty: Boolean
        get() = shortTransactions.isEmpty()

    /**
     * Checks if the list is not empty
     */
    fun isNotEmpty(): Boolean = shortTransactions.isNotEmpty()

    /**
     * Gets a transaction by position
     */
    operator fun get(position: Int): CShortTransaction
    {
        return shortTransactions[position]
    }

    /**
     * Iterator implementation
     */
    override fun iterator(): Iterator<CShortTransaction>
    {
        return shortTransactions.iterator()
    }

    // Extension functions for gRPC type conversion
    private fun com.hha.common.ClientOrdersType.toClientOrdersType(): EClientOrdersType
    {
        return when (this)
        {
            com.hha.common.ClientOrdersType.CLIENT_ORDER_OPEN -> EClientOrdersType.OPEN
            com.hha.common.ClientOrdersType.CLIENT_ORDER_CLOSED -> EClientOrdersType.CLOSED
            com.hha.common.ClientOrdersType.CLIENT_ORDER_OPEN_PAID -> EClientOrdersType.OPEN_PAID
            else -> EClientOrdersType.OPEN
        }
    }
}