package com.hha.framework

import com.hha.floor.FloorPlan

data class CFloorPlan(
   var floorPlanId: Int,
   var floorName: String,
   var enabled: Boolean
)
{
   constructor(floorPlan: FloorPlan) : this(
      floorPlan.floorPlanId,
      floorPlan.floorName,
      floorPlan.enabled)
}