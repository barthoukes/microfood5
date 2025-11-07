package com.hha.callback

import com.hha.framework.CTransaction

// In a new file, or near CTransaction.kt
interface TransactionListener
{
   fun onTransactionChanged(transaction: CTransaction)
}