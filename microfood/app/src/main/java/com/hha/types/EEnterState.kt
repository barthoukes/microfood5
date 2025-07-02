package com.hha.types

/// @brief State where the dialog is in.
enum class EEnterState(val value: Short) {
    ENTER_ITEM_STATE(0),
    ENTER_SUBITEM_STATE(1),
    ENTER_CLUSTER_STATE(2);

    companion object {
        /**
         * Get EnterState from a short value
         * @param value The integer value to look up
         * @return Corresponding EnterState or null if not found
         */
        fun fromInt(value: Short): EEnterState? {
            return EEnterState.values().firstOrNull { it.value == value }
        }
    }
}
