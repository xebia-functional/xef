package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class Employee(firstName: String, lastName: String, age: Int, position: String, company: Company)
    derives SerialDescriptor,
      Decoder

private final case class Address(street: String, city: String, country: String) derives SerialDescriptor, Decoder

private final case class Company(name: String, address: Address) derives SerialDescriptor, Decoder

@main def runEmployee: Unit =
  ai {
    val complexPrompt =
      """
        |Provide made up information for an Employee that includes their first name, last name, age, position,
        |and their company's name and address (street, city, and country).
        |Use the information provided.
      """.stripMargin
    val employeeData = prompt[Employee](complexPrompt)
    println(
      s"""
         |Employee Information:
         |Name: ${employeeData.firstName} ${employeeData.lastName}
         |Age: ${employeeData.age}
         |Position: ${employeeData.position}
         |Company: ${employeeData.company.name}
         |Address: ${employeeData.company.address.street},${employeeData.company.address.city}, ${employeeData.company.address.country}
      """.stripMargin
    )
  }.getOrElse(ex => println(ex.getMessage))
