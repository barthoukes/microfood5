package com.hha.service

import com.hha.common.Empty
import com.hha.printer.AddRawTextRequest
import com.hha.printer.HasSlipPrinterRequest
import com.hha.printer.HasSlipPrinterResponse
import com.hha.printer.PrinterLocation
import com.hha.printer.PrinterStatusRequest
import com.hha.printer.PrinterStatusResponse
import com.hha.printer.Priority
import com.hha.printer.TicketPrinterServiceGrpcKt
import com.hha.printer.WaitPrintersStatus
import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

/**
 * Provides a high-level API to interact with the TicketPrinter gRPC service.
 * This class abstracts the underlying gRPC communication, offering simple,
 * blocking methods that can be easily used from ViewModels or other business logic.
 *
 * @param channel The managed gRPC channel used for communication with the server.
 */
class TicketPrinterService(channel: ManagedChannel) : BaseGrpcService<TicketPrinterServiceGrpcKt.TicketPrinterServiceCoroutineStub>(channel) {

   override val stub: TicketPrinterServiceGrpcKt.TicketPrinterServiceCoroutineStub by lazy {
      TicketPrinterServiceGrpcKt.TicketPrinterServiceCoroutineStub(channel)
   }


   /**
    * Sends raw text to a specified printer location by building the request internally.
    * @param location The [PrinterLocation] where the text should be printed.
    * @param text The raw string of text to print.
    * @param priority The [Priority] of the print job.
    * @param repeat The number of times to repeat the print job.
    * @return A [PrinterStatusResponse] indicating the outcome, or null on failure.
    */
   fun addRawText(
      location: PrinterLocation,
      text: String,
      priority: Priority,
      repeat: Int = 1
   ): PrinterStatusResponse? = runBlocking {
      try {
         // Build the request object from the individual parameters
         val request = AddRawTextRequest.newBuilder()
            .setLocation(location)
            .setText(text)
            .setPriority(priority)
            .setRepeat(repeat)
            .build()
         stub.addRawText(request)
      } catch (e: Exception) {
         // Log error or handle as needed
         null
      }
   }

   /**
    * Checks the status of a specific printer.
    * @param location The [PrinterLocation] of the printer to check.
    * @return A [PrinterStatusResponse] with the printer's status, or null on failure.
    */
   fun printerStatus(location: PrinterLocation): PrinterStatusResponse? = runBlocking {
      try {
         val request = PrinterStatusRequest.newBuilder()
            .setLocation(location)
            .build()
         stub.printerStatus(request)
      } catch (e: Exception) {
         null
      }
   }

   /**
    * Checks if a slip printer is configured and available.
    * @return A [HasSlipPrinterResponse] object, or null on failure.
    */
   fun hasSlipPrinter(): HasSlipPrinterResponse? = runBlocking {
      try {
         val request = HasSlipPrinterRequest.getDefaultInstance()
         stub.hasSlipPrinter(request)
      } catch (e: Exception) {
         null
      }
   }

   /**
    * Sends a command to test all configured printers.
    * @return `true` if the command was sent successfully, `false` otherwise.
    */
   fun testPrinters(): Boolean = runBlocking {
      try {
         stub.testPrinters(Empty.getDefaultInstance())
         true
      } catch (e: Exception) {
         false
      }
   }

   /**
    * Checks the wait status of the printers.
    * @return A [WaitPrintersStatus] object, or null on failure.
    */
   fun waitPrinters(): WaitPrintersStatus? = runBlocking {
      try {
         stub.waitPrinters(Empty.getDefaultInstance())
      } catch (e: Exception) {
         null
      }
   }
}
