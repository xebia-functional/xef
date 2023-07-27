package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Employees {

    public record Employee(String firstName, String lastName, Integer age, String position, Company company){}
    public record Address(String street, String city, String country){}
    public record Company(String name, Address address){}

    public static String complexPrompt =
        "Provide made up information for an Employee that includes their first name, last name, age, position, and their company's name and address (street, city, and country).\n" +
        "Use the information provided.";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            scope.prompt(complexPrompt, Employees.Employee.class)
                  .thenAccept(employeeData -> System.out.println(
                        "Employee Information:\n\n" +
                              "Name: " + employeeData.firstName + " " + employeeData.lastName + "\n" +
                              "Age: " + employeeData.age + "\n" +
                              "Position: " + employeeData.position + "\n" +
                              "Company: " + employeeData.company.name + "\n" +
                              "Address: " + employeeData.company.address.street + ", " + employeeData.company.address.city + ", " + employeeData.company.address.country + "."
                  ))
                  .get();
        }
    }
}
