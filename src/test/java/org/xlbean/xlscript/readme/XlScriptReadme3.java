package org.xlbean.xlscript.readme;

import java.io.File;
import java.time.LocalDate;

import org.xlbean.XlBean;
import org.xlbean.xlscript.XlScriptReader;

public class XlScriptReadme3 {

    public static class Employee {
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private int age;
        private String division;
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }
        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }
        public int getAge() {
            return age;
        }
        public void setAge(int age) {
            this.age = age;
        }
        public String getDivision() {
            return division;
        }
        public void setDivision(String division) {
            this.division = division;
        }
        @Override
        public String toString() {
            return "Employee [firstName=" + firstName + ", lastName=" + lastName + ", dateOfBirth=" + dateOfBirth
                    + ", age=" + age + ", division=" + division + "]";
        }
    }

    public static class EmployeeLogic {
        public void saveEmployee(Employee emp) {
            // mock
            System.out.println("saveEmployee");
            System.out.println(emp);
        }
    }

    public static void main(String[] args) {
        XlScriptReader reader = new XlScriptReader.Builder()
            .addBaseBinding("$empLogic", new EmployeeLogic())
            .build();
        XlBean bean = reader.read(new File("readme/example_03.xlsx"));
        System.out.println(bean);
    }
}
