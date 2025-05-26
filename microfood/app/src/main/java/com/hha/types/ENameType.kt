package com.hha.types

/**
 * Represents different levels/types of payed items.
 */
enum class ENameType(val value: Int) {
    NAME_NONE(0),
    NAME_SCREEN(1),
    NAME_PRINTER(2),
    NAME_ORIGINAL(3);

    companion object {
        /**
         * Get ENameType from an integer value
         * @param value The integer value to look up
         * @return Corresponding ENameType or null if not found
         */
        fun fromInt(value: Int): ENameType? {
            return values().firstOrNull { it.value == value }
        }
    }
}
