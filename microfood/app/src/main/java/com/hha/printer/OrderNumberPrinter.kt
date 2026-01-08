package com.hha.printer

import android.util.Log
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderNumberPrinter
{
   val global = Global.getInstance()
   val CFG = global.CFG
   val userCFG = global.userCFG
   var F: PrinterFile = PrinterFile("")
   val mOrderNumberDots = CFG.getValue("order_number_dots")

   constructor(name: String)
   {
      val picture = CFG.getString("order_number_picture")
      val text = CFG.getString("order_number_text")
      if (!picture.isEmpty())
      {
         F.write("[graphics]\n")
         F.write("[picture:$picture]\n")
      }
      F.write("[font:order_number]")
      val s = if (name.isNotEmpty() && !name.first().isDigit())
      {
         // 2. If true, use the substring starting from the second character.
         name.substring(1)
      } else
      {
         // 3. Otherwise, use the original string.
         name
      }

      F.write("[tab:$mOrderNumberDots]$text $s\n[text]\n")
      F.write("[cut]");
   }

   suspend fun print()
   {
      // Switch to a background I/O thread to perform the network call.
      withContext(Dispatchers.IO) {
         try
         {
            // Create the gRPC service instance.
            val service = GrpcServiceFactory.createTicketPrinterService()

            // Call the suspend function from the service. This now runs safely
            // in the background.
            service.addRawText(
               PrinterLocation.PRINTER_LOCATION_BILL_PRN,
               F.toString(),
               Priority.PRIORITY_5,
               1
            )
         } catch (e: Exception)
         {
            // Log the exception or handle it as needed.
            // This prevents the app from crashing if the network call fails.
            Log.e("OrderNumberPrinter", "Failed to print order number", e)
         }
      }
   }

}
