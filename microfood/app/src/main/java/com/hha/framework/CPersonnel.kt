package com.hha.framework

import com.hha.grpc.GrpcServiceFactory
import com.hha.personnel.Person
import com.hha.resources.CTimestamp
import com.hha.types.CMoney
import com.hha.types.EAccess

class CPerson
{
   var access: EAccess = EAccess.ACCESS_NO_KEY
   var personId: Short = 0
   var dongle = -1
   var personName = ""
   var startTime = ""
   var endTime = ""
   var restaurantId: String = ""
   var totalCash = CMoney(0)
   var totalCard = CMoney(0)
   var totalKitchen = CMoney(0)
   var totalKitchen2 = CMoney(0)
   var totalSushi = CMoney(0)
   var totalDrinks = CMoney(0)
   var totalBar = CMoney(0)
   var totalNonFood = CMoney(0)
   var totalOthers = CMoney(0)
   var totalExchange = CMoney(0)
   var totalBillsEver = 0
   var totalAmountEver = CMoney(0)
   var workingHours = 0
   var totalAccount = CMoney(0)

   fun add(
      kitchen: CMoney, kitchen2: CMoney, drinks: CMoney, bar: CMoney, sushi: CMoney,
      nonfood: CMoney, others: CMoney, cash: CMoney, card: CMoney, exchange: CMoney)
   {
      totalKitchen = totalKitchen + kitchen
      totalKitchen2 = totalKitchen2 + kitchen2
      totalSushi = totalSushi + sushi
      totalDrinks = totalDrinks + drinks
      totalBar = totalBar + bar
      totalNonFood = totalNonFood + nonfood
      totalOthers = totalOthers + others

      val t = CTimestamp(endTime)
      val u = CTimestamp()
      if ( t.hour != u.hour)
      {
         workingHours++
         endTime = u.getDateTime()
      }
      totalCash = totalCash + cash
      totalCard = totalCard + card
      totalExchange = totalExchange + exchange
   }

   fun subtract(
      kitchen: CMoney, kitchen2: CMoney, drinks: CMoney, bar: CMoney, sushi: CMoney,
      nonfood: CMoney, others: CMoney, cash: CMoney, card: CMoney, exchange: CMoney)
   {
      totalKitchen = totalKitchen - kitchen
      totalKitchen2 = totalKitchen2 - kitchen2
      totalSushi = totalSushi - sushi
      totalDrinks = totalDrinks - drinks
      totalBar = totalBar - bar
      totalNonFood = totalNonFood - nonfood
      totalOthers = totalOthers - others

      totalCash = totalCash - cash
      totalCard = totalCard - card
      totalExchange = totalExchange - exchange
   }

   fun toPerson(): Person
   {
      val person = Person.newBuilder()
         .setPersonId(personId.toInt())
         .setDongle(dongle)
         .setRestaurantId(restaurantId)
         .setPersonName(personName)
         .setAccess(access.toAccess())
         .setEndTime(endTime)
         .setStartTime(startTime)
         .setTotalAccount(totalAccount.cents())
         .setTotalBar(totalBar.cents())
         .setTotalCard(totalCard.cents())
         .setTotalCash(totalCash.cents())
         .setTotalAmountEver(totalAmountEver.cents())
         .setTotalBillsEver(totalBillsEver)
         .setTotalDrinks(totalDrinks.cents())
         .setTotalExchange(totalExchange.cents())
         .setTotalKitchen(totalKitchen.cents())
         .setTotalKitchen2(totalKitchen2.cents())
         .setTotalNonFood(totalNonFood.cents())
         .setTotalOthers(totalOthers.cents())
         .setTotalSushi(totalSushi.cents())
         .setWorkingHours(workingHours)
         .setValid(true)
      return person.build()
   }

   fun update(complete: Boolean)
   {
      val service = GrpcServiceFactory.createPersonnelService()
      val person = toPerson()
      service.updatePerson(person, complete)
   }
}

class CPersonnel
{
   var persons = mutableMapOf<Short, CPerson>()

   fun getPerson(rfidKeyId: Short): CPerson
   {
      if (!persons.containsKey(rfidKeyId))
      {
         updatePersons()
      }
      return persons[rfidKeyId] ?: CPerson()
   }

   fun updatePersons()
   {
      val actual = GrpcServiceFactory.createPersonnelService().getAllPersons()
      if (actual == null)
      {
         return
      }
      persons.clear()
      for (person in actual.personList)
      {
         var newPerson = CPerson()
         newPerson.dongle = person.dongle
         newPerson.restaurantId = person.restaurantId
         newPerson.personName = person.personName
         newPerson.access = EAccess.fromAccess(person.access)
         newPerson.startTime = person.startTime
         newPerson.endTime = person.endTime
         newPerson.totalAccount = CMoney(person.totalAccount)
         newPerson.totalBar = CMoney(person.totalBar)
         newPerson.totalCard = CMoney(person.totalCard)
         newPerson.totalCash = CMoney(person.totalCash)
         newPerson.totalAmountEver = CMoney(person.totalAmountEver)
         newPerson.totalBillsEver = person.totalBillsEver
         newPerson.totalDrinks = CMoney(person.totalDrinks)
         newPerson.totalExchange = CMoney(person.totalExchange)
         newPerson.totalKitchen = CMoney(person.totalKitchen)
         newPerson.totalKitchen2 = CMoney(person.totalKitchen2)
         newPerson.totalNonFood = CMoney(person.totalNonFood)
         newPerson.totalOthers = CMoney(person.totalOthers)
         newPerson.totalSushi = CMoney(person.totalSushi)
         newPerson.workingHours = person.workingHours
         persons[person.personId.toShort()] = newPerson
      }
   }

   fun getEmployeeName(rfidKeyId: Short): String
   {
      return getPerson(rfidKeyId).personName
   }
}
