package com.hha.callback

import com.hha.framework.CTimeFrame
import com.hha.types.ETimeFrameIndex

interface ItemOperations
{
   var transactionId: kotlin.Int

   fun getTimeFrame(): CTimeFrame

   fun getTimeFrameIndex(): ETimeFrameIndex
}
