////////////////////////////////////////////////////////////////////////////////
// @file PaymentsAdapter.kt
// @author Bart
// @brief Adapter for payments during billing. Uses ListAdapter for performance
//        and to be a stateless UI component, fitting a modern MVVM architecture.
////////////////////////////////////////////////////////////////////////////////

package com.hha.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CPayment
import com.hha.model.BillDisplayLine
import com.hha.types.CMoney
import com.hha.types.EBillBackgroundType
import com.hha.types.EBillLineType
import tech.hha.microfood.databinding.AdapterPaymentBinding
import tech.hha.microfood.R

/**
 * A modern, stateless adapter for displaying a list of CPayment objects.
 * It receives a list from the ViewModel and knows nothing about business logic.
 *
 * @param onPaymentSelected A lambda function to be called when a payment item is clicked.
 */
class PaymentsAdapter(
    private val onPaymentSelected: (BillDisplayLine) -> Unit,
    colourOrderBackgroundPayOdd: Int,
    colourOrderBackgroundPayEven: Int,
    colourOrderBackgroundOdd: Int,
    colourOrderBackgroundEven: Int,
    colourOrderText: Int,
    colourPayText: Int
    ) : ListAdapter<BillDisplayLine, PaymentsAdapter.PaymentsViewHolder>(PaymentDiffCallback())
{

    val m_colourOrderText = colourOrderText
    val m_colourPayText = colourPayText
    val m_colourPayOdd = colourOrderBackgroundPayOdd
    val m_colourPayEven = colourOrderBackgroundPayEven
    val m_colourOdd = colourOrderBackgroundOdd
    val m_colourEven = colourOrderBackgroundEven

    /**
     * The ViewHolder that holds the views for a single payment item.
     */
    inner class PaymentsViewHolder(val binding: AdapterPaymentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentsViewHolder
    {
        val binding = AdapterPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentsViewHolder, position: Int)
    {
        // getItem() is a built-in method from ListAdapter
        val payment = getItem(position)

        // Set the listener on the entire item view for a better user experience
        holder.binding.root.setOnClickListener {
            onPaymentSelected(payment)
        }
        val colour = getBackgroundColour(position, payment.backgroundType)
        holder.binding.paymentContainer.setBackgroundColor(colour)
        val textColour = getTextColour(payment.backgroundType)
        holder.binding.paymentName.setTextColor(textColour)
        holder.binding.paymentAmount.setTextColor(textColour)
        holder.binding.paymentSign.setTextColor(textColour)
        holder.binding.paymentSign.text = payment.sign
        holder.binding.paymentName.text = payment.text
        holder.binding.paymentAmount.text = payment.amount.str()
        when (payment.lineType)
        {
            EBillLineType.BILL_LINE_NONE, EBillLineType.BILL_LINE_STRIKETHROUGH -> {
                holder.binding.dividerAbove.visibility = View.GONE
                holder.binding.dividerBelow.visibility = View.GONE
            }
            EBillLineType.BILL_LINE_BELOW -> {
                holder.binding.dividerAbove.visibility = View.GONE
                holder.binding.dividerBelow.visibility = View.VISIBLE
                holder.binding.dividerBelow.setBackgroundColor(textColour)
            }
            EBillLineType.BILL_LINE_ABOVE -> {
                holder.binding.dividerBelow.visibility = View.GONE
                holder.binding.dividerAbove.visibility = View.VISIBLE
                holder.binding.dividerAbove.setBackgroundColor(textColour)
            }
            EBillLineType.BILL_LINE_ABOVE_BELOW -> {
                holder.binding.dividerBelow.visibility = View.VISIBLE
                holder.binding.dividerAbove.visibility = View.VISIBLE
                holder.binding.dividerAbove.setBackgroundColor(textColour)
                holder.binding.dividerBelow.setBackgroundColor(textColour)
            }
        }
        if (payment.iconResId == null)
        {
            holder.binding.paymentIcon.visibility = View.GONE
        }
        else
        {
            holder.binding.paymentIcon.visibility = View.VISIBLE
            holder.binding.paymentIcon.setImageResource(payment.iconResId)
        }
    }

    private fun getTextColour(backgroundType: EBillBackgroundType): Int
    {
        if (backgroundType == EBillBackgroundType.BILL_BACKGROUND_TOTAL)
        {
            return m_colourPayText
        }
        return m_colourOrderText
    }

    private fun getBackgroundColour(position: Int, backgroundType: EBillBackgroundType): Int
    {
        val odd: Boolean = (position and 1) == 1
        return when
        {
            backgroundType == EBillBackgroundType.BILL_BACKGROUND_TOTAL
                -> if (odd) m_colourPayOdd else m_colourPayEven

            else -> if (odd) m_colourOdd else m_colourEven
        }
    }
}


/**
 * DiffUtil.ItemCallback implementation for CPayment.
 * This is a requirement for ListAdapter and allows it to perform efficient
 * updates by calculating the difference between old and new lists,
 * avoiding unnecessary redraws.
 */
private class PaymentDiffCallback : DiffUtil.ItemCallback<BillDisplayLine>()
{
    override fun areItemsTheSame(oldItem: BillDisplayLine, newItem: BillDisplayLine): Boolean
    {
        // This should return true if the items represent the same entity.
        // If CPayment has a unique ID, use that: oldItem.id == newItem.id
        // As a fallback, we check for object reference equality.
        return oldItem.amount === newItem.amount && oldItem.text === newItem.text
    }

    override fun areContentsTheSame(oldItem: BillDisplayLine, newItem: BillDisplayLine): Boolean
    {
        // This should return true if the contents of the items are identical.
        // For this to work correctly, CPayment should be a 'data class'.
        return oldItem.amount == newItem.amount && oldItem.text == newItem.text
    }
}
