package com.hha.types

/**
 * Represents different languages.
 */
enum class ETaal(val value: Int) {
    LANG_NONE(0),
    LANG_SIMPLIFIED(1),
    LANG_DUTCH(2),
    LANG_ENGLISH(3),
    LANG_GERMAN(4),
    LANG_INDONESIAN(5),
    LANG_TRADITIONAL(6);

    companion object {
        /**
         * Get ETaal from an integer value
         * @param value The integer value to look up
         * @return Corresponding ETaal null if not found
         */
        fun fromInt(value: Int): ETaal? {
            return values().firstOrNull { it.value == value }
        }
    }
}
