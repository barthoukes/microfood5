package com.hha.types


data class ETimeFrameIndex constructor(var index: Short) {
    companion object
    {

        public const val TIME_FRAME_LATEST: Short = -1
        public const val TIME_FRAME_ALL: Short = -4
        public const val TIME_FRAME_NONE: Short = -5
        public const val TIME_FRAME_UNDEFINED: Short = 0

        public const val TIME_FRAME1: Short = 1
        public const val TIME_FRAME2: Short = 2
        public const val TIME_FRAME3: Short = 3
        public const val TIME_FRAME4: Short = 4

        fun create(index: Short): ETimeFrameIndex
        {
            return when
            {
                index in -5..100 -> ETimeFrameIndex(index)
                else -> ETimeFrameIndex(TIME_FRAME_UNDEFINED)
            }
        }
    }

    fun previous(): ETimeFrameIndex
    {
        when (index)
        {
            TIME_FRAME_LATEST, TIME_FRAME_ALL,
            TIME_FRAME_NONE, TIME_FRAME_UNDEFINED
                -> return ETimeFrameIndex(index)

            else -> return ETimeFrameIndex((index - 1).toShort())
        }
    }

    fun set(index: Short)
    {
        this.index = index
    }

    fun isBetween(lowest: ETimeFrameIndex, highest: ETimeFrameIndex) : Boolean
    {
        return index >= lowest.index && index <= highest.index
    }

    fun next(): ETimeFrameIndex
    {
        when (index)
        {
            TIME_FRAME_LATEST, TIME_FRAME_ALL,
            TIME_FRAME_NONE, TIME_FRAME_UNDEFINED
                -> return ETimeFrameIndex(index)

            else -> return ETimeFrameIndex((index + 1).toShort())
        }
    }

    fun toInt(): Int
    {
        return index.toInt()
    }

    // Secondary constructor is not needed since we're using factory method
}