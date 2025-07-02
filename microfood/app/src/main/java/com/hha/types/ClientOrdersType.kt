package com.hha.types

enum class ClientOrdersType(val value: Int) {
    INIT(0),               // No order yet
    OPEN(1),               // Busy to order
    PAYING(16),            // Busy paying
    CLOSED(2),             // Closed order
    OPEN_PAID(3),          // Order paid but not closed yet
    EMPTY(4),              // Empty order, all removed
    ALL(5),                // All things
    PERSONNEL(6),          // Eaten by myself or my personnel
    CREDIT(7),             // Negative amount for another transaction
    CLOSED_CREDIT(8);      // Transaction closed and credited

    companion object {
        fun fromValue(value: Int): ClientOrdersType? {
            return values().firstOrNull { it.value == value }
        }
    }
}

class Types {

}