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
import com.hha.framework.CTransaction

class FloorTableModel : ViewModel() {
   private val global = Global.getInstance()
   private val tag = "FloorTableModel"

   // Initialize with empty list to ensure observers get called
   private val _isLoading = MutableLiveData<Boolean>(false)
   private val _floorTables = MutableLiveData<CFloorTables>(CFloorTables())
   private val _floorPlans = MutableLiveData<CFloorPlans>(CFloorPlans())

   // Add error state
   private val _errorMessage = MutableLiveData<String?>()

   // Expose LiveData
   val floorTables: LiveData<CFloorTables> = _floorTables
   val isLoading: LiveData<Boolean> = _isLoading
   val errorMessage: LiveData<String?> = _errorMessage

   // Cache the last loaded data to restore quickly
   private var cachedFloorTables: CFloorTables? = null
   private var cachedFloorPlans: CFloorPlans? = null

   // An enum to clearly define the possible actions.
   enum class FloorTableAction{
      SELECT,      // First click, just select the table visually.
      NAVIGATE_TO_ORDER,  // Second click, navigate to an existing or new order.
      NAVIGATE_TO_BILL    // Second click in billing mode.
   }

   // The data class that will be our return type.
   data class TableClickResult(
      val action: FloorTableAction = FloorTableAction.NAVIGATE_TO_ORDER,
      val transactionId: Int = -1 // Default to -1, only relevant for navigation actions.
   )

   init {
      Log.i(tag, "FloorTableModel initialized")
      // Load initial data
      loadFloorPlans()
      loadFloorTables()
   }

   fun loadFloorTables() {
      // Don't refresh if already loading
      if (_isLoading.value == true) {
         Log.i(tag, "Already loading, skipping refresh")
         return
      }

      Log.i(tag, "refreshAllData called, floorPlanId: ${global.floorPlanId}")

      viewModelScope.launch {
         try {
            _isLoading.value = true
            _errorMessage.value = null

            // Try to use cached data first for immediate UI update
            cachedFloorTables?.let {
               Log.i(tag, "Using cached floor tables, size: ${it.size}")
               _floorTables.value = it
            }

            cachedFloorPlans?.let {
               _floorPlans.value = it
            }

            // Then load fresh data in background
            withContext(Dispatchers.IO) {
               Log.i(tag, "Loading fresh data in background")
               val floors = loadFloorPlans()
               val tables = loadFloorTables(global.floorPlanId)

               // Cache the results
               cachedFloorTables = tables
               cachedFloorPlans = floors

               return@withContext Pair(floors, tables)
            }.let { (floors, tables) ->
               Log.i(tag, "Fresh data loaded, tables size: ${tables.size}")

               // Only update if data has actually changed
               val currentTables = _floorTables.value
               if (currentTables == null || currentTables.size != tables.size ||
                  !areTablesEqual(currentTables, tables)) {
                  _floorTables.value = tables
                  _floorPlans.value = floors
               } else {
                  Log.i(tag, "Data unchanged, not updating LiveData")
               }
            }
         } catch (e: Exception) {
            Log.e(tag, "Error loading data: ${e.message}", e)
            _errorMessage.value = "Failed to load floor tables: ${e.message}"

            // If we have cached data, keep showing it
            if (_floorTables.value?.size == 0 && cachedFloorTables?.size ?: 0 > 0) {
               Log.i(tag, "Using cached data after error")
               _floorTables.value = cachedFloorTables
               _floorPlans.value = cachedFloorPlans
            }
         } finally {
            _isLoading.value = false
         }
      }
   }

   private fun areTablesEqual(tables1: CFloorTables, tables2: CFloorTables): Boolean {
      if (tables1.size != tables2.size) return false

      for (i in 0 until tables1.size) {
         val table1 = tables1.getFloorTable(i)
         val table2 = tables2.getFloorTable(i)

         if (table1?.tableId != table2?.tableId ||
            table1?.name != table2?.name ||
            table1?.transactionId != table2?.transactionId) {
            return false
         }
      }
      return true
   }

   fun floorPlanName(): String {
      return try {
         val floorPlans = _floorPlans.value
         if (floorPlans != null && floorPlans.size > 0)
         {
            floorPlans.getFloorName(global.floorPlanId.coerceAtLeast(0))
         } else {
            cachedFloorPlans?.getFloorName(global.floorPlanId.coerceAtLeast(0)) ?: "--"
         }
      } catch (e: Exception) {
         Log.e(tag, "Error getting floor plan name: ${e.message}")
         "--"
      }
   }

   private fun loadFloorPlans(): CFloorPlans
   {
      var plans = CFloorPlans()
      viewModelScope.launch {
         plans = withContext(Dispatchers.IO)
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
                     val thisFloor = CFloorPlan(
                        plan.floorPlanId, plan.floorName,
                        plan.enabled
                     )
                     if (thisFloor.enabled)
                     {
                        output.add(thisFloor)
                     }
                  }
                  if (global.floorPlanId < 0)
                  {
                     global.floorPlanId = 0
                  }
                  Log.i(tag, "Loaded ${output.size} floor plans")
               }
               output
            } catch (e: Exception)
            {
               Log.e(tag, "Error loading floor plans: ${e.message}")
               CFloorPlans()
            }
         }
      }
      return plans
   }

   fun nrFloorPlans(): Int {
      return try {
         val plans = _floorPlans.value
         if (plans != null && plans.size > 0) {
            plans.size
         } else {
            cachedFloorPlans?.size ?: 1
         }
      } catch (e: Exception) {
         Log.e(tag, "Error getting number of floor plans: ${e.message}")
         1
      }
   }

   private fun loadFloorTables(floorPlanId: Int): CFloorTables {
      return try {
         val currentFloorPlanId = global.floorPlanId.coerceAtLeast(0)
         val output = CFloorTables()
         val floorTableService = GrpcServiceFactory.createFloorTableService()

         Log.i(tag, "Loading floor tables for planId: $currentFloorPlanId")
         val list: TableList? = floorTableService.getFloorTables(
            currentFloorPlanId, 0, false
         )

         if (list != null) {
            Log.i(tag, "Received ${list.tablesList.size} tables from server")
            for (table: Table in list.tablesList) {
               val tab = CFloorTable(table)
               output.add(tab)
            }
         } else {
            Log.w(tag, "Server returned null table list")
         }
         Log.i(tag, "Total tables loaded: ${output.size}")
         output
      } catch (e: Exception) {
         Log.e(tag, "Error loading floor tables: ${e.message}")
         CFloorTables()
      }
   }

   fun getNextFloorPlanId(floorPlanId: Int): Int
   {
      var nextFloorPlan = -1
      val currentFloorPlans = _floorPlans.value
      if (currentFloorPlans != null)
      {
         nextFloorPlan = currentFloorPlans.getNextFloorPlanId(floorPlanId)
      }
      return nextFloorPlan
   }

   // Clear cached data (call this when activity is destroyed)
   fun clearCache() {
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
            transactionId // This is now correctly an Int
         )
      }
   }

}