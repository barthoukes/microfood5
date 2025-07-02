package com.hha.adapter

import tech.hha.microfood.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CMenuItem
import com.hha.framework.CMenuItems
import com.hha.resources.Global
import tech.hha.microfood.databinding.AdapterTransactionItemBinding

class TransactionItemAdapter(
    //private val menuItems: CTransactionItems
) : RecyclerView.Adapter<TransactionItemAdapter.TransactionItemViewHolder>() {
    val global = Global.getInstance()

    inner class TransactionItemViewHolder(val binding: AdapterTransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionItemViewHolder {
        val binding = AdapterTransactionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionItemViewHolder(binding)
    }

    override fun getItemCount() = 100

    override fun onBindViewHolder(holder: TransactionItemViewHolder, position: Int) {
        //holder.binding.transactionOrder.isSelected = true
        holder.binding.transactionItem.text = "LOEMPIA     1241934938419"
        val context = holder.itemView.context

        if (position and 1 == 0) {
            holder.binding.transactionItem.setBackgroundColor(
                ContextCompat.getColor(context, R.color.COLOUR_ORDER_BACKGROUND_EVEN))
            holder.binding.transactionOrder.setBackgroundColor(
                ContextCompat.getColor(context, R.color.COLOUR_ORDER_BACKGROUND_EVEN))
        }
        else {
            holder.binding.transactionItem.setBackgroundColor(
                ContextCompat.getColor(context, R.color.COLOUR_ORDER_BACKGROUND_ODD))
            holder.binding.transactionOrder.setBackgroundColor(
                ContextCompat.getColor(context, R.color.COLOUR_ORDER_BACKGROUND_ODD))
        }

        holder.binding.root.setOnClickListener {
            }
        }
}

