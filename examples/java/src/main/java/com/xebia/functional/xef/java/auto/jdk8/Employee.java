package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.ExecutionException;

public class Employee {

    public String firstName;
    public String lastName;
    public Integer age;
    public String position;
    public Company company;

    private static class Address {
        public String street;
        public String city;
        public String country;
    }

    private static class Company {
        public String name;
        public Address address;
    }

    public static Prompt complexPrompt = new Prompt(
        "Provide made up information for an Employee that includes their first name, last name, age, position, and their company's name and address (street, city, and country).\n" +
        "Use the information provided.");

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, complexPrompt, Employee.class)
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
