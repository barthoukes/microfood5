package com.hha.framework

class CFloorPlans: Iterable<CFloorPlan>
{
   var mPlans: MutableList<CFloorPlan> = mutableListOf()

   fun add(floorPlan: CFloorPlan) = mPlans.add(floorPlan)

   override fun iterator(): Iterator<CFloorPlan> = mPlans.iterator()

   fun getFloorPlan(index: Int): CFloorPlan? = mPlans.getOrNull(index)

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
      // Find the index of the current floor plan.
      val currentIndex = mPlans.indexOfFirst { it.floorPlanId == floorPlanId }

      // Next index
      var nextIndex = (currentIndex + 1) % mPlans.size

      return mPlans[nextIndex].floorPlanId
   }
}