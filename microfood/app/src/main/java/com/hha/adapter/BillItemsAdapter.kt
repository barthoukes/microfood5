package com.hha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CItem
import com.hha.framework.CMenuItem
import com.hha.framework.CTransaction
import com.hha.resources.Global
import tech.hha.microfood.databinding.AdapterTransactionItemBinding

class BillItemsAdapter(
    private val onBillItemSelected: (CItem) -> Unit
) : RecyclerView.Adapter<BillItemsAdapter.BillItemViewHolder>() {
    val global = Global.getInstance()
    val CFG = global.CFG
    val colourCFG = global.colourCFG

    inner class BillItemViewHolder(val binding: AdapterTransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillItemViewHolder {
        val binding = AdapterTransactionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BillItemViewHolder(binding)
    }

    override fun getItemCount() : Int {
        if (global.transaction != null) {
            return global.transaction!!.size + 1
        }
        return 1
    }

    override fun onBindViewHolder(holder: BillItemViewHolder, position: Int) {
        //holder.binding.transactionOrder.isSelected = true
        val transaction: CTransaction = global.transaction!!
        val item: CItem? = transaction[position]

        // Draw background,
        val background = getBackgroundColour(position)
        holder.binding.transactionOrder.setBackgroundColor(background)
        if (item == null) {
            return
        }
        val tc = colourCFG.getTextColour("COLOUR_ORDER_TEXT")
        holder.binding.transactionItem.setTextColor(tc)
        var str = "\n${item.getQuantity()}x "
        holder.binding.transactionItem.text = when {
            global.isChinese() -> str + item.chineseName
            else -> str + item.localName
        }
        holder.binding.transactionPrice.text = item.getTotal().str()
        holder.binding.transactionPrice.setTextColor(tc)

        val context = holder.itemView.context
        holder.binding.root.setOnClickListener {
            Log.i("TIA", "onBindViewHolder $position")
        }
    }

    private fun getBackgroundColour(position: Int): Int {
        return when {
            // cursor colour when cursor, or odd/even colour when even/odd
            global.cursor == position -> colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_SELECTED")
            (position and 1) == 0 -> colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_ODD")
            else -> colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_EVEN")
        }
    }

}