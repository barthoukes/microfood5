package com.hha.types

import com.hha.common.TaxType

/**
 * Enum representing different tax types
 */
enum class ETaxType(val value: Int) {
    BTW_TYPE_HIGH(2),          ///< 21%
    BTW_TYPE_LOW(1),           ///< 9%
    BTW_NONE(3),               ///< No tax
    BTW_MAX(4);                ///< Unused

    companion object {
        /**
         * Converts an integer value to the corresponding [ETaxType].
         * @param value The integer value to convert
         * @return The matching [ETaxType] or LOW if invalid
         */
        fun fromInt(value: Int): ETaxType
        {
            return when (value)
            {
                2 -> BTW_TYPE_HIGH;
                3 -> BTW_NONE;
                4 -> BTW_MAX;
                else -> BTW_TYPE_LOW
            }
        }

        fun fromTaxType(value: TaxType) : ETaxType
        {
            return when (value)
            {
                TaxType.BTW_HIGH -> BTW_TYPE_HIGH;
                TaxType.BTW_NONE -> BTW_NONE;
                TaxType.BTW_MAX -> BTW_MAX;
                else -> BTW_TYPE_LOW
            }
        }

    } // Companion
}
