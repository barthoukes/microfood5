package com.hha.printer

import android.util.Log
import androidx.activity.result.launch
import androidx.lifecycle.viewModelScope
import com.hha.framework.CTransaction
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.types.EClientOrdersType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TakeawayNumber
{
   val global = Global.getInstance()
   val CFG = global.CFG
   val userCFG = global.userCFG

   suspend fun printTakeawayNumber(transaction: CTransaction)
   {
      if ( userCFG.getBoolean("user_print_ta_number")
            && transaction.isTakeaway()
            && transaction.getStatus() != EClientOrdersType.CLOSED)
      {
         val name = transaction.getName()
         if (name != global.lastPrintedBillNumber)
         {
            global.lastPrintedBillNumber = name
            print(name)
         }
      }
   }

   suspend fun print(name: String)
   {
      var F = PrinterFile("")
      val picture = CFG.getString("order_number_picture")
      val text = CFG.getString("order_number_text")
      if (!picture.isEmpty())
      {
         F.write("[graphics]\n")
         F.write("[picture:$picture]\n")
      }
      F.write("[font:order_number]")
      var output = name
      if (!output.isEmpty())
      {
         if (!output[0].isDigit())
         {
            output = output.drop(0)
         }
      }
      val dots = CFG.getValue("order_number_dots")
      F.write("[tab:$dots]$output $text\n[text]\n")
      F.write("[cut]")
      print(F)
   }

   suspend fun print(F: PrinterFile)
   {
      // --- REPAIRED CODE ---
      // 1. Switch to a background I/O thread for the network call.
      withContext(Dispatchers.IO) {
         try
         {
            val service = GrpcServiceFactory.createTicketPrinterService()
            // 2. This gRPC call now runs safely in the background.
            service.addRawText(
               PrinterLocation.PRINTER_LOCATION_BILL_PRN,
               F.toString(),
               Priority.PRIORITY_5,
               1
            )
         } catch (e: Exception)
         {
            // 3. Gracefully handle errors (e.g., network failure) to prevent crashes.
            Log.e("TakeawayNumber", "Failed to print takeaway number", e)
         }
      }
   }
}