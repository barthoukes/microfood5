package com.hha.framework

import com.hha.client.Customer
import com.hha.grpc.GrpcServiceFactory
import java.util.Locale
import com.hha.common.Money
import com.hha.types.CMoney

data class CCustomer(
   var customerId: Int = -1,
   var phone: String = "",
   var zipCode: String = "",
   var houseNr: String = "",
   var name: String = "",
   var streetName: String = "",
   var city: String = "",
   var company: String = "",
   var totalEaten: CMoney = CMoney(0),
   var totalOrdered: CMoney = CMoney(0),
   var totalPayed: CMoney = CMoney(0)
)
{
   /**
    * Get the customer from SQL database by ID
    * @param customerId What client
    * @return Customer retrieved from database or null if not found
    */
   fun fromId(customerId: Int): CCustomer?
   {
      val service = GrpcServiceFactory.createClientService()
      val ptr: Customer? = service.getCustomerFromId(customerId.toLong())
      var customer = fromCustomer(ptr)
      customer.customerId = customerId
      return customer
   }

   /**
    * Constructor with all fields
    */
   constructor(
      id: Int,
      phoneNumber: String,
      zipCode: String,
      houseNumber: String,
      customer: String,
      company: String,
      streetName: String,
      city: String,
      totalEaten: Money,
      totalOrdered: Money,
      totalPayed: Money
   ) : this(
      customerId = id,
      phone = phoneNumber,
      zipCode = zipCode,
      houseNr = houseNumber,
      name = customer,
      company = company,
      streetName = streetName,
      city = city,
      totalEaten = CMoney(totalEaten.cents),
      totalOrdered = CMoney(totalOrdered.cents),
      totalPayed = CMoney(totalPayed.cents)
   )


   companion object {

   fun fromCustomer(he: Customer?): CCustomer
   {
      if (he == null)
      {
         return CCustomer()
      }
      return CCustomer(
         he.customerId.toInt(), he.phoneNumber,he.zipCode,
         he.houseNumber,he.name, he.streetName,
         he.city, he.company, CMoney(he.totalEaten),
         CMoney(he.totalOrdered),CMoney(he.totalPayed))
      }
   }

   fun toCustomer(): Customer
   {
      val builder = Customer.newBuilder()
         .setCustomerId(customerId.toLong())
         .setPhoneNumber(phone)
         .setZipCode(zipCode)
         .setHouseNumber(houseNr)
         .setName(name)
         .setStreetName(streetName)
         .setCity(city)
         .setCompany(company)
         .setTotalOrdered(totalOrdered.cents())
         .setTotalPayed(totalPayed.cents())
         .setTotalEaten(totalEaten.cents())
      return builder.build()
   }

   /**
    * After editing the customer, update sets it back to the database
    */
   fun updateToDatabase()
   {
      if (customerId <= 0)
      {
         return
      }
      // Convert zipCode to uppercase
      zipCode = zipCode.uppercase(Locale.getDefault())

      val service = GrpcServiceFactory.createClientService()
      service.updateCustomer(toCustomer())
   }

   fun addEaten(totalPayments: CMoney)
   {
      totalPayed = totalPayed + totalPayments
      totalEaten = totalEaten + totalPayments
      updateToDatabase()
   }

   /**
    * Decide what to print
    */
   fun printerName(): String
   {
      val total = StringBuilder()

      if (name.isNotEmpty())
      {
         total.append("$name\n")
      }

      total.append("$streetName $houseNr\n")

      if (zipCode.isNotEmpty() || city.isNotEmpty())
      {
         total.append("$zipCode $city\n")
      }

      if (company.isNotEmpty())
      {
         total.append("$company\n")
      }

      if (phone.isNotEmpty())
      {
         total.append("$phone\n")
      }

      // Remove trailing whitespace and newlines
      var result = total.toString().trimEnd()

      // Add newline if content exists (similar to original logic)
      if (result.length > 2)
      {
         result += "\n"
      }
      return result
   }
}

// Assuming Money class exists - here's a basic implementation
data class Money(
   val amount: Double = 0.0,
   val currency: String = "EUR" // or whatever your default currency is
)
