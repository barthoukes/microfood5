package com.hha.printer

import com.hha.framework.CTransaction
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.types.EClientOrdersType

class TakeawayNumber
{
   val global = Global.getInstance()
   val CFG = global.CFG
   val userCFG = global.userCFG

   fun printTakeawayNumber(transaction: CTransaction)
   {
      if ( userCFG.getBoolean("user_print_ta_number")
            && transaction.isTakeaway()
            && transaction.getStatus() != EClientOrdersType.CLOSED)
      {
         val name = transaction.getName()
         if (name != global.lastPrintedBillNumber)
         {
            global.lastPrintedBillNumber = name
            print(name);
         }
      }
   }

   fun print(name: String)
   {
      var F = PrinterFile("")
      val picture = CFG.getString("order_number_picture");
      val text = CFG.getString("order_number_text");
      if (!picture.isEmpty())
      {
         F.write("[graphics]\n");
         F.write("[picture:$picture]\n");
      }
      F.write("[font:order_number]");
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

   fun print(F: PrinterFile)
   {
      val service = GrpcServiceFactory.createTicketPrinterService()
      service.addRawText(PrinterLocation.PRINTER_LOCATION_BILL_PRN,
         F.toString(), Priority.PRIORITY_5, 1)
   }

}