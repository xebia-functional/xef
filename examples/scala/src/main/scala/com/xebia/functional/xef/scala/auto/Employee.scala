package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

private final case class Employee(firstName: String, lastName: String, age: Int, position: String, company: Company)
    derives ScalaSerialDescriptor,
      Decoder

private final case class Address(street: String, city: String, country: String) derives ScalaSerialDescriptor, Decoder

private final case class Company(name: String, address: Address) derives ScalaSerialDescriptor, Decoder

@main def runEmployee: Unit =
  val complexPrompt =
    """
       |Provide made up information for an Employee that includes their first name, last name, age, position,
       |and their company's name and address (street, city, and country).
       |Use the information provided.
    """.stripMargin
  val employeeData: Employee = ai(prompt[Employee](complexPrompt))
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
