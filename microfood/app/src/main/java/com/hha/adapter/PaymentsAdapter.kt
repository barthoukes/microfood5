////////////////////////////////////////////////////////////////////////////////
// @file PaymentsAdapter.kt
// @author Bart
// @brief Adapter for payments during billing
////////////////////////////////////////////////////////////////////////////////

package com.hha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.callback.PaymentsListener
import com.hha.framework.CPayment
import com.hha.resources.Global
import com.hha.types.CMoney
import tech.hha.microfood.databinding.AdapterPaymentBinding


class PaymentsAdapter(
    private val onPaymentSelected: (CPayment) -> Unit
) : RecyclerView.Adapter<PaymentsAdapter.PaymentsViewHolder>(),
    PaymentsListener
{
    val global = Global.getInstance()
    val m_payments: MutableList<CPayment> = mutableListOf()

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
        val payment = getPayment(position)

        holder.binding.paymentName.setOnClickListener {
            Log.d("CLICK_TEST", "Payment clicked at position $position") // Add this for testing
            // val payment = getPayment(position)
            if (payment != null)
            {
                onPaymentSelected(payment)
            }
        }
        holder.binding.paymentName.setOnClickListener {
            Log.d("CLICK_TEST", "PAyment clicked at position $position") // Add this for testing
            // val payment = getPayment(position)
            if (payment != null)
            {
                onPaymentSelected(payment)
            }
        }
        if (payment != null)
        {
            holder.binding.paymentName.text = payment.paymentMethod.toString()
            holder.binding.dividerView.visibility = View.VISIBLE
        }
    }

    fun getPayment(position: Int): CPayment?
    {
        if (position >= 0 && position < m_payments.size)
        {
            return m_payments[position]
        }
        return null
    }

    fun getCardTotal(): CMoney
    {
        return CMoney(0.0)
    }

    fun getCashTotal(): CMoney
    {
        return CMoney(0.0)
    }

    override fun getItemCount() = 10

    override fun onPaymentAdded(position: Int, item: CPayment)
    {
        Log.d("PA", "Listener: Payment added at $position")
        m_payments.add(item)
        notifyItemInserted(position)
        // You might need to update the "empty" item row as well
    }

    override fun onPaymentRemoved(position: Int)
    {
        Log.d("PA", "Listener: Payment removed at $position")
        m_payments.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onPaymentUpdated(position: Int, item: CPayment)
    {
        Log.d("PA", "Listener: Payment updated at $position")
        m_payments[position] = item
        notifyItemRemoved(position)
    }

    override fun onPaymentsCleared()
    {
        Log.d("PA", "Listener: Payments cleared")
        m_payments.clear()
        notifyDataSetChanged()
    }
}
