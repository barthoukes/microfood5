package com.hha.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hha.floor.FloorPlanList
import com.hha.floor.Table
import com.hha.floor.TableList
import com.hha.framework.CFloorPlan
import com.hha.framework.CFloorPlans
import com.hha.framework.CFloorTable
import com.hha.framework.CFloorTables
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import com.hha.floor.floorPlanId
import com.hha.framework.CTransaction
import com.hha.grpc.GrpcStateViewModel

class FloorTableModel : ViewModel()
{
   private val global = Global.getInstance()
   private val tag = "FloorTableModel"

   // Initialize with empty list to ensure observers get called
   //private val _isLoading = MutableLiveData<Boolean>(false)
   private val _floorTables = MutableLiveData<CFloorTables>(CFloorTables())
   private val _floorPlans = MutableLiveData<CFloorPlans>(CFloorPlans())

   // Add error state
   private val _errorMessage = MutableLiveData<String?>()

   // Expose LiveData
   val floorTables: LiveData<CFloorTables> = _floorTables

   // val isLoading: LiveData<Boolean> = _isLoading
   val errorMessage: LiveData<String?> = _errorMessage
   private var grpcStateViewModel: GrpcStateViewModel? = null

   // Cache the last loaded data to restore quickly
   private var cachedFloorTables: CFloorTables? = null
   private var cachedFloorPlans: CFloorPlans? = null

   // An enum to clearly define the possible actions.
   enum class FloorTableAction
   {
      SELECT,      // First click, just select the table visually.
      NAVIGATE_TO_ORDER,  // Second click, navigate to an existing or new order.
      NAVIGATE_TO_BILL    // Second click in billing mode.
   }

   // The data class that will be our return type.
   data class TableClickResult(
      val action: FloorTableAction = FloorTableAction.NAVIGATE_TO_ORDER,
      val transactionId: Int = -1 // Default to -1, only relevant for navigation actions.
   )

   init
   {
      Log.i(tag, "FloorTableModel initialized")
      // Load initial data
      loadData()
   }

   // This is the PUBLIC function that Activities/Fragments will call.// It will manage the coroutine and update LiveData.
   private fun loadData()
   {
      Log.i(tag, "loadData called, floorPlanId: ${global.floorPlanId}")

      viewModelScope.launch {
         grpcStateViewModel?.messageSent()
         try
         {
            // Run all blocking I/O inside withContext
            val (loadedPlans, loadedTables) = withContext(Dispatchers.IO) {
               Log.i(tag, "Loading fresh data in background")

               // 1. Fetch floor plans and WAIT for the result
               val plans = fetchFloorPlansFromServer()

               // 2. Fetch floor tables using the current floorPlanId and WAIT
               val tables = fetchFloorTablesFromServer()
               // 3. Return both results together
               Pair(plans, tables)
            }

            // --- Back on the Main thread ---
            Log.i(tag, "Fresh data loaded. Plans: ${loadedPlans.size}, Tables: ${loadedTables.size}")

            // Cache the results
            cachedFloorPlans = loadedPlans
            cachedFloorTables = loadedTables

            // Compare and update LiveData if necessary
            val currentTables = _floorTables.value
            if (!areTablesEqual(currentTables, loadedTables))
            {
               Log.i(tag, "Table data has changed. Updating LiveData.")
               _floorPlans.value = loadedPlans
               _floorTables.value = loadedTables
            } else
            {
               Log.i(tag, "Table data is identical, skipping update.")
            }

         } catch (e: Exception)
         {
            Log.e(tag, "Error loading data: ${e.message}", e)
            _errorMessage.value = "Failed to load data: ${e.message}"
         } finally
         {
            // Signal that the message has been confirmed (or failed).
            // This runs after the try block or after the catch block.
            grpcStateViewModel?.messageConfirmed()
         }
      }
   }

   fun setFloorPlanId(floorPlanId: Int)
   {
      Log.i(tag, "setFloorPlanId: $floorPlanId")
      global.floorPlanId = floorPlanId
      _floorTables.value = floorTables.value
      loadTables()
   }

   fun loadTables()
   {
      Log.i(tag, "loadData called, floorPlanId: ${global.floorPlanId}")

      viewModelScope.launch {
         grpcStateViewModel?.messageSent()
         try
         {
            // Run all blocking I/O inside withContext
            val loadedTables = withContext(Dispatchers.IO) {
               Log.i(tag, "Loading fresh data in background")

               // Fetch floor tables using the current floorPlanId and WAIT
               fetchFloorTablesFromServer()
            }

            // --- Back on the Main thread ---
            Log.i(tag, "Fresh data loaded. Tables: ${loadedTables.size}")

            // Cache the results
            cachedFloorTables = loadedTables

            // Compare and update LiveData if necessary
            val currentTables = _floorTables.value
            if (!areTablesEqual(currentTables, loadedTables))
            {
               Log.i(tag, "Table data has changed. Updating LiveData.")
               _floorTables.value = loadedTables
            } else
            {
               Log.i(tag, "Table data is identical, skipping update.")
            }

         } catch (e: Exception)
         {
            Log.e(tag, "Error loading data: ${e.message}", e)
            _errorMessage.value = "Failed to load data: ${e.message}"
         } finally
         {
            // Signal that the message has been confirmed (or failed).
            // This runs after the try block or after the catch block.
            grpcStateViewModel?.messageConfirmed()
         }
      }
   }

   // NEW: A suspend function for fetching plans. It does NOT use viewModelScope.
   private suspend fun fetchFloorPlansFromServer(): CFloorPlans
   {
      try
      {
         val floorPlanService = GrpcServiceFactory.createFloorPlanService()
         val output = CFloorPlans()
         val list: FloorPlanList? = floorPlanService.getFloorPlans(-1, false)

         if (list != null)
         {
            for (plan in list.floorPlansList)
            {
               val thisFloor = CFloorPlan(plan.floorPlanId, plan.floorName, plan.enabled)
               if (thisFloor.enabled)
               {
                  output.add(thisFloor)
               }
            }
            if (global.floorPlanId < 0)
            {
               global.floorPlanId = 0
            }
            Log.i(tag, "Fetched ${output.size} floor plans from server")
         }
         return output
      } catch (e: Exception)
      {
         Log.e(tag, "Error in fetchFloorPlansFromServer: ${e.message}")
         return CFloorPlans() // Return empty on error
      }
   }

   // RENAMED: This is now the suspend function for fetching tables.
   suspend fun fetchFloorTablesFromServer(): CFloorTables
   {
      return try
      {
         val output = CFloorTables()
         val floorTableService = GrpcServiceFactory.createFloorTableService()
         val list: TableList? = floorTableService.getFloorTables(
            -1, 0, false
         )

         if (list != null)
         {
            for (table: Table in list.tablesList)
            {
               val tab = CFloorTable(table)
               output.add(tab)
            }
         }
         Log.i(tag, "Fetched ${output.size} tables")
         output
      } catch (e: Exception)
      {
         Log.e(tag, "Error in fetchFloorTablesFromServer: ${e.message}")
         CFloorTables() // Return empty on error
      }
   }

   private fun areTablesEqual(tables1: CFloorTables?, tables2: CFloorTables?): Boolean
   {
      if (tables1 == null || tables2 == null)
      {
         return false;
      }
      if (tables1.size != tables2.size) return false

      for (i in 0 until tables1.size)
      {
         val table1 = tables1.getFloorTable(i)
         val table2 = tables2.getFloorTable(i)

         if (table1 == null || table2 == null)
         {
            return false
         }
         if (table1.tableId != table2.tableId ||
            table1.name != table2.name ||
            table1.transactionId != table2.transactionId ||
            table1.tableStatus != table2.tableStatus ||
            table1.drinksStatus != table2.drinksStatus
         )
         {
            return false
         }
      }
      return true
   }

   fun floorPlanName(): String
   {
      return try
      {
         val floorPlans = _floorPlans.value
         if (floorPlans != null && floorPlans.size > 0)
         {
            floorPlans.getFloorName(global.floorPlanId.coerceAtLeast(0))
         } else
         {
            cachedFloorPlans?.getFloorName(global.floorPlanId.coerceAtLeast(0)) ?: "--"
         }
      } catch (e: Exception)
      {
         Log.e(tag, "Error getting floor plan name: ${e.message}")
         "--"
      }
   }

   fun nrFloorPlans(): Int
   {
      return try
      {
         val plans = _floorPlans.value
         if (plans != null && plans.size > 0)
         {
            plans.size
         } else
         {
            cachedFloorPlans?.size ?: 1
         }
      } catch (e: Exception)
      {
         Log.e(tag, "Error getting number of floor plans: ${e.message}")
         1
      }
   }

   /**
    * Filters the cached list of all tables to show only the ones
    * for the selected floor plan. Updates the LiveData to refresh the UI.
    */
   fun filterTablesForFloorplan(floorPlanId: Int)
   {
      Log.d(tag, "filterTablesForFloorplan floorPlanId: $floorPlanId")

      // Use the cached data; no need for a network call.
      val allTables = cachedFloorTables
      if (allTables == null)
      {
         Log.w(tag, "Cannot filter, cached tables are null. Loading data instead.")
         loadData() // Fallback in case the cache is empty
         return
      }

      // Create a new CFloorTables instance to hold the filtered results.
      val filteredTables = CFloorTables()

      // --- CORRECTED CODE ---
      // Iterate directly over the 'allTables' object.
      // It behaves like a list of CFloorTable.
      allTables.forEach { table ->
         if (table.floorPlanId == floorPlanId)
         {
            filteredTables.add(table)
         }
      }

      Log.d(tag, "Filtered list contains ${filteredTables.size} tables.")

      // Post the new, filtered list to LiveData.
      // This will trigger the observer in AskTransactionActivity.
      _floorTables.value = filteredTables
   }

   fun getNextFloorPlanId(floorPlanId: Int): Int
   {
      var nextFloorPlan = -1
      val currentFloorPlans = _floorPlans.value
      if (currentFloorPlans != null)
      {
         nextFloorPlan = currentFloorPlans.getNextFloorPlanId(floorPlanId)
      }
      Log.d(tag, "getNextFloorPlanId floorPlanId: $floorPlanId -> $nextFloorPlan")
      return nextFloorPlan
   }

   // Clear cached data (call this when activity is destroyed)
   fun clearCache()
   {
      Log.i(tag, "Clearing cache")
      cachedFloorTables = null
      cachedFloorPlans = null
      _floorTables.value = CFloorTables()
      _floorPlans.value = CFloorPlans()
   }

   /**
    * Called when a floor table circle is clicked.
    * If it's the second click, this will create a NEW transaction.
    */
   suspend fun onFloorTableSelected(
      selectedFloorTable: CFloorTable, billMode: Boolean
   ): TableClickResult
   {
      Log.i(tag, "onFloorTableSelected for table: ${selectedFloorTable.name}")
      // Floortable is already clicked twice, so just do it.
      if (billMode && (selectedFloorTable.transactionId > 0))
      {
         // billing
         Log.d(tag, "Billing mode for existing transaction: ${selectedFloorTable.transactionId}")
         return TableClickResult(
            FloorTableAction.NAVIGATE_TO_BILL,
            selectedFloorTable.transactionId
         )
      } else
      {
         // Order mode: Check for an open transaction on a background thread.
         Log.d(tag, "Order mode. Checking for open transaction...")

         // 2. USE 'withContext' TO RUN ON A BACKGROUND THREAD AND GET THE RESULT
         val transactionId = withContext(Dispatchers.IO) {
            // This code block now runs on a background I/O thread.
            CTransaction.findOpenTable(selectedFloorTable.name)
         }
         // The function resumes here on the original thread (likely Main)
         // with the 'transactionId' value returned from the background task.
         Log.d(tag, "Found transaction ID: $transactionId")

         // 3. RETURN THE RESULT
         return TableClickResult(
            FloorTableAction.NAVIGATE_TO_ORDER,
            transactionId
         )
      }
   }

}
