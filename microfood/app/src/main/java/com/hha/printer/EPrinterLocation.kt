package com.hha.printer

// Import the gRPC-generated enum
import com.hha.printer.PrinterLocation
import com.hha.types.EItemLocation

/**
 * Represents the physical or logical location of a printer.
 * This corresponds to the PrinterLocation enum defined in the .proto file.
 */
enum class EPrinterLocation(val value: Int)
{
   /**
    * `PRINTER_LOCATION_BILL_PRN = 0;`
    */
   PRINTER_LOCATION_BILL_PRN(0),

   /**
    * `PRINTER_LOCATION_KITCHEN_PRN1 = 1;`
    */
   PRINTER_LOCATION_KITCHEN_PRN1(1),

   /**
    * `PRINTER_LOCATION_DRINKS_PRN = 2;`
    */
   PRINTER_LOCATION_DRINKS_PRN(2),

   /**
    * `PRINTER_LOCATION_BAR_PRN = 3;`
    */
   PRINTER_LOCATION_BAR_PRN(3),

   /**
    * `PRINTER_LOCATION_MANAGER_PRN = 4;`
    */
   PRINTER_LOCATION_MANAGER_PRN(4),

   /**
    * `PRINTER_LOCATION_SLIP_PRN = 5;`
    */
   PRINTER_LOCATION_SLIP_PRN(5),

   /**
    * `PRINTER_LOCATION_BARCODE_PRN = 6;`
    */
   PRINTER_LOCATION_BARCODE_PRN(6),

   /**
    * `PRINTER_LOCATION_LABEL_PRN = 7;`
    */
   PRINTER_LOCATION_LABEL_PRN(7),

   /**
    * `PRINTER_LOCATION_KITCHEN_PRN2 = 8;`
    */
   PRINTER_LOCATION_KITCHEN_PRN2(8),

   /**
    * `PRINTER_LOCATION_KITCHEN_PRN3 = 9;`
    */
   PRINTER_LOCATION_KITCHEN_PRN3(9),

   /**
    * `PRINTER_LOCATION_KITCHEN_PRN4 = 10;`
    */
   PRINTER_LOCATION_KITCHEN_PRN4(10),

   /**
    * `PRINTER_LOCATION_KITCHEN_COLLECT_PRN = 11;`
    */
   PRINTER_LOCATION_KITCHEN_COLLECT_PRN(11),

   /**
    * `PRINTER_LOCATION_INVALID_PRN = 12;`
    */
   PRINTER_LOCATION_INVALID_PRN(12),

   /**
    * `PRINTER_LOCATION_AUTOMATIC_PRN = 13;`
    */
   PRINTER_LOCATION_AUTOMATIC_PRN(13); // Use a semicolon here before adding more content

   /**
    * Converts this custom EPrinterLocation enum to the gRPC-generated PrinterLocation enum.
    */
   fun toPrinterLocation(): PrinterLocation
   {
      return PrinterLocation.forNumber(this.value)
         ?: PrinterLocation.UNRECOGNIZED // Fallback for safety
   }

   companion object
   {
      /**
       * Creates a custom EPrinterLocation from a gRPC-generated PrinterLocation enum.
       * @param printerLocation The gRPC enum instance to convert from.
       * @return The corresponding EPrinterLocation, or null if no match is found.
       */
      fun fromPrinterLocation(printerLocation: PrinterLocation): EPrinterLocation?
      {
         // Find the entry in this enum that has the same integer value.
         return entries.find { it.value == printerLocation.number }
      }

      /**
       * Converts an EItemLocation to its corresponding EPrinterLocation.
       * This is the Kotlin version of the C++ item2printerLocation function.
       */
      fun fromItemLocation(location: EItemLocation): EPrinterLocation
      {
         return when (location)
         {
            EItemLocation.ITEM_DRINKS -> PRINTER_LOCATION_DRINKS_PRN
            EItemLocation.ITEM_KITCHEN -> PRINTER_LOCATION_KITCHEN_PRN1
            EItemLocation.ITEM_KITCHEN2 -> PRINTER_LOCATION_KITCHEN_PRN2
            EItemLocation.ITEM_KITCHEN3 -> PRINTER_LOCATION_KITCHEN_PRN3
            EItemLocation.ITEM_KITCHEN4 -> PRINTER_LOCATION_KITCHEN_PRN4
            EItemLocation.ITEM_KITCHEN5 -> PRINTER_LOCATION_KITCHEN_COLLECT_PRN
            EItemLocation.ITEM_BAR,
            EItemLocation.ITEM_SUSHI,
            EItemLocation.ITEM_NONFOOD
               -> PRINTER_LOCATION_BAR_PRN

            else -> PRINTER_LOCATION_KITCHEN_PRN1 // Default case
         }
      }
   }
}
