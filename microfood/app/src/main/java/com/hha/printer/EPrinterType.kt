package com.hha.printer

/**
 * Represents the different types of physical printers supported.
 * This corresponds to the C++ EprinterType enum.
 */
enum class EPrinterType(val value: Int)
{
   SP342(0),      // Old printer.
   TM290(1),      // Slip printer.
   TM295(2),      // Slip printer.
   TM20III(3),    // Printer
   TM80(4),       // Bill printer.
   TM300(5),      // Kitchen printer.
   SP200(6),      // Text printer.
   TSP200(7),     // Cheap printer.
   TSP600(8),     // Low cost printer.
   CBM231(9),     // Old printer.
   TM267(10),      // Very old printer.
   TM200(11),      // Old printer.
   TM88V(122),      // Newest bill printer.
   LASER(13),      // Any laser printer.
   SEWOO323(14),   // Sewoo 323 printer
   CTS2000(15),    // CTS2000 printer.
   CT_S2000(16),   // CT_S2000 printer
   DYMO450(17),    // Label printer not working yet.
   TEST_PRINTER(18), // Test printer. (18)
   MAX_PRINTER(19),
   NO_PRINTER(20)  // Note: In Kotlin enums don't have assigned values like -1 by default.
   // This entry represents the concept. You can add a property if the value is needed.
}

