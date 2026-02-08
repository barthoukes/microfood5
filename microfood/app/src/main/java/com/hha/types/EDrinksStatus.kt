package com.hha.types

import com.hha.resources.Global

/**
 * @brief State where the dialog is in, with associated minutes.
 * @param minutes The time threshold in minutes for this status.
 */
enum class EDrinksStatus(val minutes: Int)
{
    DRINK_STATUS_EMPTY(1),
    DRINK_STATUS_QUART_FULL(2),
    DRINK_STATUS_HALF_FULL(3),
    DRINK_STATUS_3QUART_FULL(4),
    DRINK_STATUS_FULL(5),
    DRINK_STATUS_GONE(-1);

    companion object
    {
        // These values are now initialized only ONCE when the class is loaded.
        private val global = Global.getInstance()
        private val CFG = global.CFG
        private val mFloorPlanDrinksEmpty = CFG.getValue("floorplan_minutes_empty")
        private val mFloorPlanDrinksHalf = CFG.getValue("floorplan_minutes_half")
        private val mFloorPlanDrinksFull = CFG.getValue("floorplan_minutes_full")
        private val mFloorPlanDrinksQuart = (mFloorPlanDrinksEmpty + mFloorPlanDrinksHalf) / 2
        private val mFloorPlanDrinks3Quart = (mFloorPlanDrinksFull + mFloorPlanDrinksHalf) / 2

        /**
         * Determines the EDrinksStatus based on the age of the transaction in minutes.
         * @param minutesSinceTransaction The number of minutes that have passed.
         * @return The corresponding EDrinksStatus.
         */
        fun fromMinutes(minutesSinceTransaction: Int): EDrinksStatus {
            return when {
                minutesSinceTransaction >= mFloorPlanDrinksEmpty -> DRINK_STATUS_EMPTY
                minutesSinceTransaction >= mFloorPlanDrinksQuart -> DRINK_STATUS_QUART_FULL
                minutesSinceTransaction >= mFloorPlanDrinksHalf -> DRINK_STATUS_HALF_FULL
                minutesSinceTransaction >= mFloorPlanDrinks3Quart -> DRINK_STATUS_3QUART_FULL
                minutesSinceTransaction >= mFloorPlanDrinksFull -> DRINK_STATUS_FULL
                else -> DRINK_STATUS_FULL // Default for very recent transactions
            }
        }
    }
}
