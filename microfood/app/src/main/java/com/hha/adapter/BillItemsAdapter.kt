package com.hha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CCursor
import com.hha.framework.CItem
import com.hha.framework.CTransaction
import com.hha.resources.Global
import tech.hha.microfood.databinding.AdapterTransactionItemBinding

class BillItemsAdapter(
    private val mCursor: CCursor,
    private val onBillItemSelected: (CItem) -> Unit
) : RecyclerView.Adapter<BillItemsAdapter.BillItemViewHolder>()
{
    val global = Global.getInstance()
    val CFG = global.CFG
    val colourCFG = global.colourCFG
    private var mTransaction: CTransaction? = null

    inner class BillItemViewHolder(val binding: AdapterTransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillItemViewHolder
    {
        val binding = AdapterTransactionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BillItemViewHolder(binding)
    }

    override fun getItemCount(): Int
    {
        return mTransaction?.size?.plus(1) ?: 1
    }

    override fun onBindViewHolder(holder: BillItemViewHolder, position: Int)
    {
        //holder.binding.transactionOrder.isSelected = true
        val currentTransaction = mTransaction
        if (currentTransaction == null)
        {
            return
        }
        val item: CItem? = currentTransaction.get(position)
        // Draw background,
        val background = getBackgroundColour(position, mCursor)
        holder.binding.transactionOrder.setBackgroundColor(background)
        if (item == null)
        {
            return
        }
        val tc = colourCFG.getTextColour("COLOUR_ORDER_TEXT")
        holder.binding.transactionItem.setTextColor(tc)
        var str = "\n${item.getQuantity()}x "
        holder.binding.transactionItem.text = when
        {
            global.isChinese() -> str + item.chineseName
            else -> str + item.localName
        }
        holder.binding.transactionPrice.text = item.getTotal().str()
        holder.binding.transactionPrice.setTextColor(tc)

        holder.binding.root.setOnClickListener {
            Log.i("TIA", "onBindViewHolder $position")
        }
    }

    private fun getBackgroundColour(position: Int, cursor: CCursor): Int
    {
        return when
        {
            // cursor colour when cursor, or odd/even colour when even/odd
            cursor.position == position -> colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_SELECTED")
            (position and 1) == 0 -> colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_ODD")
            else -> colourCFG.getBackgroundColour("COLOUR_ORDER_BACKGROUND_EVEN")
        }
    }

    // update the data and refresh the list
    fun submitList(newTransaction: CTransaction)
    {
        this.mTransaction = newTransaction
        notifyDataSetChanged() // Tell the RecyclerView to redraw itself
    }
}