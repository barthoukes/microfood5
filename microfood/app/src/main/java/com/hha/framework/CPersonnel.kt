package com.hha.framework

import com.hha.grpc.GrpcServiceFactory
import com.hha.personnel.Person
import com.hha.personnel.personId
import com.hha.types.EAccess

class CPerson
{
    var access: EAccess = EAccess.ACCESS_NO_KEY
    var personName = ""
    var startTime = ""
    var endTime = ""
    var restaurantId: Short = 0
}

class CPersonnel
{
   var persons = mutableMapOf<Int, CPerson>()

   fun getPerson(personId: Int): CPerson
   {
      if (!persons.containsKey(personId))
      {
         updatePersons()
      }
      return persons[personId] ?: CPerson()
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
         val cp = getPerson(person.personId)
         cp.access = EAccess.fromInt(person.access.number)
         cp.personName = person.personName
         cp.startTime = person.startTime
         cp.endTime = person.endTime
         persons[person.personId] = cp
      }
   }

   fun getEmployeeName(key: Int): String
   {
      return getPerson(key).personName
   }
}
