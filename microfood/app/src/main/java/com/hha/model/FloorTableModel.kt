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
import com.hha.framework.CShortTransaction
import com.hha.framework.CShortTransactionList
import com.hha.framework.CTransaction
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A ViewModel to manage the state and data for the floor tables UI.
 *
 * This class holds the list of floor tables and keeps track of the currently selected table.
 * The Activity observes LiveData objects from this ViewModel to update the UI in response to data changes.
 */
class FloorTableModel : ViewModel()
{
   private val global = Global.getInstance()

   private val _isLoading = MutableLiveData<Boolean>()

   // The internal, modifiable list of floor tables. This is kept private.
   private val _floorTables = MutableLiveData<CFloorTables>()
   // The public LiveData that is exposed to the UI for observation. It's read-only from the outside.
   val floorTables: LiveData<CFloorTables> = _floorTables

   private val _floorPlans = MutableLiveData<CFloorPlans>()
   // Add LiveData for navigation

   // The external, read-only LiveData for observing the selected table.
   // The UI can use this to highlight the selected item or show details.
   //val selectedFloorTable: LiveData<CFloorTable?> = _floorTables

   fun refreshAllData()
   {
      // Launch a coroutine in the ViewModel's lifecycle scope.
      // This ensures the task is cancelled if the ViewModel is destroyed.
      viewModelScope.launch {
         // 1. Set loading state to TRUE on the main thread before starting the background task.
         _isLoading.value = true
         var floors = CFloorPlans()
         var tables = CFloorTables()

         // 2. Switch to a background thread (Dispatchers.IO) for the blocking call.
         withContext(Dispatchers.IO)
         {
            floors = loadFloorPlans()
            tables = loadFloorTables(global.floorPlanId)
         }

         // 3. Back on the main thread, post the result and set loading state to FALSE.
         _floorTables.value = tables
         _floorPlans.value = floors
         _isLoading.value = false
      }
   }

   fun floorPlanName(): String
   {
      val floorPlans: CFloorPlans? = _floorPlans.value
      return floorPlans?.getFloorName(global.floorPlanId) ?: "--"
   }

   fun connectTableToTransaction(floorTableId: Int, transactionId: Int, minutes: Int)
   {
      val service = GrpcServiceFactory.createFloorTableService()
      service.connectTableToTransaction(floorTableId, transactionId, minutes)
   }

   fun loadFloorPlans(): CFloorPlans
   {
      val floorPlanService = GrpcServiceFactory.createFloorPlanService()
      var output = CFloorPlans()
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
      }
      return output
   }

   fun nrFloorPlans(): Int
   {
      val ptr = _floorPlans.value
      if (ptr != null)
      {
         return ptr.size
      }
      return 1
   }

   fun loadFloorTables(floorPlanId: Int): CFloorTables
   {
      val floorPlanId = global.floorPlanId
      val output = CFloorTables()
      val floorTableService = GrpcServiceFactory.createFloorTableService()

      val list: TableList? = floorTableService.getFloorTables(
         floorPlanId, 0, false
      )
      if (list != null)
      {
         for (table: Table in list.tablesList)
         {
            val tab = CFloorTable(table)
            output.add(tab)
         }
      }
      return output
   }

}
