package com.hha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import tech.hha.microfood.databinding.AdapterFloorTableBinding

import com.hha.resources.Global
import com.hha.framework.CFloorTables
import com.hha.framework.CFloorTable

class FloorTablesAdapter(
    private val onFloorTableSelected: (CFloorTable) -> Unit
) : RecyclerView.Adapter<FloorTablesAdapter.FloorTableViewHolder>() {
    val global = Global.getInstance()
    val floorTables = CFloorTables()

    inner class FloorTableViewHolder(val binding: AdapterFloorTableBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FloorTableViewHolder {
        val binding = AdapterFloorTableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FloorTableViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return floorTables.size
    }

    override fun onBindViewHolder(holder: FloorTableViewHolder, position: Int) {
        holder.binding.floorTableName.setOnClickListener {
            val floorTable = floorTables.getFloorTable(position)
            if (floorTable != null) {
                Log.d(
                    "CLICK_TEST",
                    "FloorTableName clicked at position $position"
                ) // Add this for testing
                onFloorTableSelected(floorTable)
            }
        }
        holder.binding.floorTableAmount.setOnClickListener {
            Log.d(
                "CLICK_TEST",
                "FloorTableAmount clicked at position $position"
            ) // Add this for testing
        }

        val floorTable = floorTables.getFloorTable(position)
        if (floorTable != null) {
            // Update selected state
            holder.binding.floorTableName.text = "123"
            holder.binding.floorTableAmount.text = "123€"
        }

    }
}