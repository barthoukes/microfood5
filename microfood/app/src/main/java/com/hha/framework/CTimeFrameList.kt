package com.hha.framework

import com.hha.resources.Global
import com.hha.types.CMoney
import com.hha.framework.CTimeFrame
import com.hha.types.EPaymentMethod

data class CTimeFrameList(
    var transactionId: Int = 0
) {
    val m_timeFrames: MutableList<CTimeFrame> = mutableListOf()

    // Add a time frame to the list
    fun addTimeFrame(timeFrame: CTimeFrame) {
        m_timeFrames.add(timeFrame)
    }

    // Remove a time frame by ID
    fun removeTimeFrameById(id: Int): Boolean {
        return m_timeFrames.removeIf { it.id == id }
    }

    // Find time frame by ID
    fun findPaymentById(id: Int): CTimeFrame? {
        return m_timeFrames.find { it.id == id }
    }

    // Clear all payments
    fun clearTimeFrames() {
        m_timeFrames.clear()
    }

    // Get payment count
    val count: Int
        get() = m_timeFrames.size

    // Check if list is empty
    val isEmpty: Boolean
        get() = m_timeFrames.isEmpty()

    val size : Int
        get() = m_timeFrames.size

    // Index operator
    operator fun get(index: Int): CTimeFrame {
        return m_timeFrames[index]
    }
}
