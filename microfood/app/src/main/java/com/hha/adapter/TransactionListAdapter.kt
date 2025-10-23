package com.hha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import tech.hha.microfood.databinding.AdapterTransactionBinding

import com.hha.framework.CTransaction
import com.hha.resources.Global

class TransactionListAdapter(
    private val onTransactionSelected: (CTransaction) -> Unit
) : RecyclerView.Adapter<TransactionListAdapter.TransactionViewHolder>()
{
    val global = Global.getInstance()
    private lateinit var transactions: MutableMap<Int, CTransaction>

    inner class TransactionViewHolder(val binding: AdapterTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder
    {
        val binding = AdapterTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int)
    {

        val transaction = getTransaction(position)
        // Set click listener on the BUTTON (not just root view)
        holder.binding.tableId.setOnClickListener {
            if (transaction != null)
            {
                Log.d("CLICK_TEST", "Name clicked at position $position") // Add this for testing
                onTransactionSelected(transaction)
            }
        }

        holder.binding.priceId.setOnClickListener {
            if (transaction != null)
            {
                Log.d("CLICK_TEST", "Button clicked at position $position") // Add this for testing
                onTransactionSelected(transaction)
            }
        }
    }

    fun getTransaction(position: Int): CTransaction?
    {
        return transactions[position]
    }

    override fun getItemCount() = transactions.size
}
