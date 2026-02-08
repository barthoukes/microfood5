package com.hha.floor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CFloorTables
import com.hha.resources.Global
import com.hha.types.ETableStatus
import com.hha.util.ColourUtils
import com.hha.util.TimeArcDrawable
import tech.hha.microfood.databinding.AdapterFloorTableBinding
import tech.hha.microfood.R
import androidx.core.graphics.toColorInt
import com.hha.framework.CFloorTable
import com.hha.types.EDrinksStatus

class FloorTablesAdapter(
   private val onFloorTableSelected: (com.hha.framework.CFloorTable) -> Unit,
) : RecyclerView.Adapter<FloorTablesAdapter.FloorTableViewHolder>()
{
    private var mTransactionName = "123"
    private var mFloorTables: CFloorTables = CFloorTables()
    private val global = Global.getInstance()
    private val CFG = global.CFG
    private val colourCFG = global.colourCFG
    private val  mRedTime = CFG.getValue("red_time")
    private val colTableText = colourCFG.getTextColour("COLOUR_TABLE_TEXT")
    private val colTableFloorPlanBorderTimeout = colourCFG.getTextColour("COLOUR_FLOORPLAN_BORDER_TIMEOUT")
    private val colTableReserved = colourCFG.getTextColour("COLOUR_TABLE_RESERVED")
    private val colTableUnused = colourCFG.getTextColour("COLOUR_TABLE_UNUSED")
    private val colOpenTableFloorPlan = colourCFG.getTextColour("COLOUR_OPEN_TABLE_FLOORPLAN")
    private val colOpenTableFloorPlanSelected = colourCFG.getTextColour("COLOUR_OPEN_TABLE_FLOORPLAN_SELECTED")
    private val colTableFloorPlan = colourCFG.getTextColour("COLOUR_TABLE_FLOORPLAN")
    private val colTableFloorPlanSelected = colourCFG.getTextColour("COLOUR_TABLE_FLOORPLAN_SELECTED")
    private val colTableFloorPlanBorder = colourCFG.getTextColour("COLOUR_TABLE_FLOORPLAN_BORDER")
    val mMaximumTimeTimer = CFG.getValue("maximum_time")
    val mFloorPlanPersonsEnable = CFG.getBoolean("floorplan_persons_enable")

    // 2. A map to hold drawables for each view holder.
    // This avoids creating new drawables on every scroll, which is more efficient.
    private val drawableMap = mutableMapOf<FloorTableViewHolder, TimeArcDrawable>()

    /**
     * Clears all tables from the adapter and updates the UI.
     * Call this before starting a new data fetch.
     */
    fun clear()
    {
        // Clear the internal list
        if (mFloorTables.size > 0)
        {
            mFloorTables = CFloorTables() // Assuming CFloorTables() creates an empty list
            // Notify the recycler view that all items have been removed
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FloorTableViewHolder
    {
        val binding = AdapterFloorTableBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FloorTableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FloorTableViewHolder, position: Int)
    {
        // 3. Get or create the custom drawable for this specific item view.
        val timeArcDrawable = drawableMap.getOrPut(holder) {
            TimeArcDrawable().also {
                // Set the drawable as the background for the 'table_background' View.
                holder.binding.tableBackground.background = it
            }
        }

        val floorTable: CFloorTable? = mFloorTables.getFloorTable(position)
        if (floorTable != null)
        {
            // --- This is the new logic for updating the background ---

            // 4. Define your colors (example colors)
            //var colour = Color.parseColor("#FF6347") // A reddish color

            // 5. Calculate the progress angle
//            val progressPercentage = if (mMaximumTimeTimer > 0) {
//                val totalMinutes = floorTable.minutes + (floorTable.days * 24 * 60)
//                totalMinutes.toFloat() / mMaximumTimeTimer.toFloat()
//            } else {
//                0f // No progress if max time is zero
//            }

            // Convert percentage to degrees (0% = 0 degrees, 100% = 360 degrees)
            timeArcDrawable.progressAngle = 60f // progressPercentage * 360f

            var maximumTimeTimer = mMaximumTimeTimer
            val colText = colTableText
            // Create a button for the table, add it to the list and register the button.
            var name = floorTable.name
            if (mFloorPlanPersonsEnable && floorTable.maxPersonCount > 0)
            {
                name = name + "\n(" + floorTable.maxPersonCount + ")"
            }

            if (mMaximumTimeTimer > 0)
            {
                /** Todo base on endtime */
                val minutes: Int = floorTable.minutes + floorTable.days * 24 * 60
                val corner: Float = minutes * 360.0f / maximumTimeTimer
                timeArcDrawable.progressAngle = corner
            }
            // 6. Set the calculated colors and angle on the drawable
            val colour = calculateColour(floorTable)
            val col2 = ColourUtils.brighten(colour, 0.15f) or 0xff000000.toInt()
            timeArcDrawable.setColors(colour, col2)

            var icon = when
            {
                floorTable.transactionId >= 1E6 &&
                   floorTable.tableStatus == ETableStatus.TABLE_OK ->
                {
                    when (floorTable.drinksStatus)
                    {
                        EDrinksStatus.DRINK_STATUS_FULL -> R.drawable.milk100
                        EDrinksStatus.DRINK_STATUS_EMPTY -> R.drawable.milk0
                        EDrinksStatus.DRINK_STATUS_HALF_FULL -> R.drawable.milk50
                        EDrinksStatus.DRINK_STATUS_QUART_FULL -> R.drawable.milk25
                        EDrinksStatus.DRINK_STATUS_3QUART_FULL -> R.drawable.milk75
                        else -> null
                    }
                }
                else -> null
            }
            if (icon == null)
            {
                holder.binding.milk.visibility = View.GONE
            }
            else
            {
                holder.binding.milk.visibility = View.VISIBLE
                holder.binding.milk.setImageResource(icon)
            }

            // --- Your existing logic to set text and click listeners ---
            holder.binding.floorTableName.text = name
            if (floorTable.amount.empty()) holder.binding.floorTableAmount.text = ""
            else holder.binding.floorTableAmount.text = floorTable.amount.toString()

            holder.binding.root.setOnClickListener {
                onFloorTableSelected(floorTable)
            }
        }
    }

    fun calculateColour(floorTable: CFloorTable): Int
    {
        val colour = when
        {
            floorTable.maxPersonCount == 0 -> colTableUnused
            (floorTable.minutesLeft >= mRedTime &&
               floorTable.transactionId > 0) -> colTableFloorPlanBorderTimeout
            floorTable.tableStatus == ETableStatus.TABLE_OK ||
               floorTable.tableStatus == ETableStatus.TABLE_OPEN_NOT_PAID ||
               floorTable.tableStatus == ETableStatus.TABLE_OPEN_PAID-> {
                if (floorTable.name != mTransactionName) colOpenTableFloorPlan
                else colOpenTableFloorPlanSelected
            }
            floorTable.tableStatus == ETableStatus.TABLE_BUSY ||
               floorTable.tableStatus == ETableStatus.TABLE_EMPTY ||
               floorTable.tableStatus == ETableStatus.TABLE_EXIST -> {
                if (floorTable.name != mTransactionName) colTableFloorPlan
                else colTableFloorPlanSelected
            }
            floorTable.tableStatus == ETableStatus.TABLE_RESERVED -> colTableReserved
            else -> "#FF6347".toColorInt() // A reddish color
        }
        return colour
    }

    override fun getItemCount(): Int = mFloorTables.size

    fun submitList(tables: CFloorTables)
    {
        mFloorTables = tables
        notifyDataSetChanged()
    }

    fun getSelectedTransactionName(): String = mTransactionName

    fun redrawViewsAfterChangeLanguage()
    {
        notifyDataSetChanged()
    }

// ... inside FloorTablesAdapter ...

    fun clearSelection()
    {
        val previousIndex = mFloorTables.indexOfFirst { it.name == mTransactionName }
        mTransactionName = ""
        if (previousIndex != -1)
        {
            notifyItemChanged(previousIndex)
        }
    }

    fun selectTransactionName(transactionName: String)
    {
        // Store the ID of the previously selected item
        val previousTransactionName = mTransactionName

        // Find the adapter position of the previously selected item
        val previousIndex = mFloorTables.indexOfFirst { it.name == previousTransactionName }

        // Update the transaction ID to the new one
        mTransactionName = transactionName

        // Find the adapter position of the newly selected item
        val newIndex = mFloorTables.indexOfFirst { it.name == mTransactionName }

        // If the old item was found in the list, notify the adapter to redraw it.
        // This will cause onBindViewHolder to run for this specific item, removing its "selected" state.
        if (previousIndex != -1)
        {
            notifyItemChanged(previousIndex)
        }

        // If the new item was found in the list, notify the adapter to redraw it.
        // This will cause onBindViewHolder to run, applying the "selected" state.
        if (newIndex != -1)
        {
            notifyItemChanged(newIndex)
        }
    }

    inner class FloorTableViewHolder(val binding: AdapterFloorTableBinding) :
        RecyclerView.ViewHolder(binding.root)
}
