package com.hha.framework

import android.util.Log

class CFloorPlans: Iterable<CFloorPlan>
{
   var mPlans: MutableList<CFloorPlan> = mutableListOf()

   fun add(floorPlan: CFloorPlan) = mPlans.add(floorPlan)

   override fun iterator(): Iterator<CFloorPlan> = mPlans.iterator()

   fun getFloorPlan(index: Int): CFloorPlan? = mPlans.getOrNull(index)
   val tag = "CFloorPlans"

   fun getFloorName(floorPlanId: Int): String
   {
      for (floorPlan in mPlans)
      {
         if (floorPlan.floorPlanId == floorPlanId)
         {
            return floorPlan.floorName
         }
      }
      return "--"
   }

   val size: Int
      get() = mPlans.size

   fun getNextFloorPlanId(floorPlanId: Int): Int
   {
      Log.i(tag, "getNextFloorPlanId floorPlanId: $floorPlanId")
      var nextIndex = 0
      if (mPlans.isEmpty())
      {
         return -1
      }
      try
      {
         // Find the index of the current floor plan.
         val currentIndex = mPlans.indexOfFirst { it.floorPlanId == floorPlanId }
         nextIndex = (currentIndex + 1) % mPlans.size
      }
      catch (e: Exception)
      {
         nextIndex = 0
      }
      return mPlans[nextIndex].floorPlanId
   }
}