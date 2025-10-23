package com.hha.framework

import com.hha.types.ETimeFrameIndex


/**
 * Represents a time frame index with validation and undefined state handling
 */
data class OBSOLETE_CTimeFrameIndex(val value: Short) {
    companion object {
        const val TIME_FRAME_UNDEFINED :Short = 0
        const val TIME_FRAME1 :Short = 1

        /**
         * Creates a CTimeFrameIndex with validation
         * @param index The time frame index value
         * @return Valid CTimeFrameIndex or UNDEFINED if input is invalid
         */
        fun create(index: Short?): OBSOLETE_CTimeFrameIndex {
            return when {
                index == null || index < 0 -> OBSOLETE_CTimeFrameIndex(TIME_FRAME_UNDEFINED)
                else -> OBSOLETE_CTimeFrameIndex(index)
            }
        }

        fun create(index: ETimeFrameIndex): OBSOLETE_CTimeFrameIndex {
            return OBSOLETE_CTimeFrameIndex(index.index.toShort())
        }
    }

    fun isBetween(lowest: OBSOLETE_CTimeFrameIndex, highest: OBSOLETE_CTimeFrameIndex) : Boolean
    {
        return value >= lowest.value && value <= highest.value
    }

    fun isBetween(lowest: ETimeFrameIndex, highest: ETimeFrameIndex) : Boolean
    {
        return value >= lowest.index && value <= highest.index
    }

    fun index() : Short {
        return value
    }

    val isUndefined: Boolean
        get() = value == TIME_FRAME_UNDEFINED

    val isValid: Boolean
        get() = value > TIME_FRAME_UNDEFINED

    fun toInt(): Int
    {
        return value.toInt()
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OBSOLETE_CTimeFrameIndex
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String =
        if (isUndefined) "TimeFrame(UNDEFINED)" else "TimeFrame($value)"

//    toPrevious
}