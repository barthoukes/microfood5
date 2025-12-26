package com.hha.framework

class CFloorPlans: Iterable<CFloorPlan>
{
   var mPlans: MutableList<CFloorPlan> = mutableListOf()

   fun add(floorPlan: CFloorPlan) = mPlans.add(floorPlan)

   override fun iterator(): Iterator<CFloorPlan> = mPlans.iterator()

   fun getFloorPlan(index: Int): CFloorPlan? = mPlans.getOrNull(index)

   fun getFloorName(floorPlanId: Int): String
   {
      val floorPlan = mPlans[floorPlanId]
      val name = floorPlan.floorName
      return name
   }

   val size: Int
      get() = mPlans.size
}