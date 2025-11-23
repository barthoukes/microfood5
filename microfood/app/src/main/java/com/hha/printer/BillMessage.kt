package com.hha.printer

import com.hha.resources.Global

class BillMessage
{
   private var buffer = StringBuilder()

   val global = Global.getInstance()
   val CFG = global.CFG

   fun print(line0: Int, size: Int): String
   {
      var flag = 0
      if (CFG.getValue("message_height") !=0)
      {
         val msgHeight = CFG.getValue("message_height")
         buffer.append( "[height:$msgHeight]")
      }
//      for (int r = 0; r < (int)line.size(); r++)
//      {
//         // print the line
//         if (!flag++)
//         {
//            F << '\n' << "[text|font:0|width:2]";
//         }
//         F << line[r] << "\n";
//      }
      buffer.append("[font:1|height:1|width:1]")
      return buffer.toString()
   }
}