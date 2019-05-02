# xlscript

[![Build Status](https://travis-ci.org/aji3/xlscript.svg?branch=master)](https://travis-ci.org/aji3/xlscript)
[![Coverage Status](https://coveralls.io/repos/github/aji3/xlscript/badge.svg?branch=master)](https://coveralls.io/github/aji3/xlscript?branch=master)

Java utility to read values in Excel sheet based on the definition in the Excel sheet itself, and evaluate the values as Groovy script.

This library is a thin wrapper of [xlbean](https://github.com/aji3/xlbean), so that basic usage follows it.

# Getting Started

## 1. Dependency

**For Maven**
```
<repositories>
    <repository>
        <id>xlbean</id>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>org.xlbean</groupId>
        <artifactId>xlscript</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

**For Gradle**
```
repositories {
     jcenter()
}
dependencies {
    compile group: 'org.xlbean', name: 'xlscript', version:'0.1.0'
}
```

## 2. How to use

This library uses xlbean internally to load values from Excel sheet, so definition for data loading is the same as xlbean as explained [here](https://github.com/aji3/xlbean/wiki/Reading-excel-sheet).

Only difference is that values wrapped by back quote is evaluated as Groovy script. For instance, this will write the string to stdout: ``` `println "this is evaluated as groovy script"` ```. 

And important point is that the the values defined by xlbean is accessible from this script.

Let's take the most simple example to explain how it works.

### STEP1. Define Mapping Definition on Excel sheet

![Simplest example](https://user-images.githubusercontent.com/23234132/57008300-164bbf80-6c2a-11e9-84d0-a83e01bde81c.png)

In this example, you can see 2 types of definitions.

1. **Single definition** - for reading a single cell to element of Map instance: 

   Define name of field as **"FIELD_NAME"** at both ROW1 and COLUMN1 of the corresponding cell.

2. **Table definition** -  for reading table to list of Map instances: 

   Define name of columns at the ROW1 as **"TABLE_NAME#COLUMN_NAME"** and define **"TABLE_NAME#~"** at COLUMN1 of the row where the body of the table starts from.

For both example, cells in YELLOW are simple value cells and cells in ORANGE are the cells with Groovy script.

### STEP2. Write Java Program

```java
public class XlScriptReadme {
    public static void main(String[] args) {
        XlScriptReader reader = new XlScriptReader();
        XlBean bean = reader.read(new File("readme/example_01.xlsx"));
        System.out.println(bean);
    }
}
```
The following text will be written to stdout.

```
{
  name=World, 
  greeting=Hello World!, 
  greetings=[
    {name=xlbean, greeting=Hello xlbean!},
    {name=xlscript, greeting=Hello xlscript!}, 
    {name=xltemplating, greeting=Hello xltemplating!}
  ]
}
```

As you can see, the back-quoted values are replaced by evaluation result Groovy script.

# Use Cases

Let's use a example scenario that assumes a simple Division and Employee master table to introduce various different kinds of use cases.

<img src="https://user-images.githubusercontent.com/23234132/57052259-38dce780-6cc1-11e9-9fc8-9a50f27d1cbe.png" width=80%> 

## Basic Use Cases

### Basic-1. Before Groovy... How to read excel sheet

To read these two tables in a Java program, write the definition in ROW1 and COLUMN1 as follows:
The definition method is the same as **xlbean**, so [please refer to xlbean wiki for details.](https://github.com/aji3/xlbean/wiki)

![image](https://user-images.githubusercontent.com/23234132/57052435-097aaa80-6cc2-11e9-9e97-92bd346c5bb7.png)

This Excel can be read by the following program.

```java
public class XlScriptMain {
    public static void main(String[] args) {
        XlScriptReader reader = new XlScriptReader();
        XlBean excel = reader.read(new File("demo/demo2.xlsx"));
        System.out.println(excel);
    }
}
```
As a result, the following is output to the console: (Formatted by hand for ease of reading.)

```
{
employees=[
  {firstName=Emma,   lastName=Smith,    dateOfBirth=1990-04-01T00:00:00.000, division=001}, 
  {firstName=Lian,   lastName=Johnson,  dateOfBirth=1998-11-01T00:00:00.000, division=003}, 
  {firstName=Olivia, lastName=Williams, dateOfBirth=1991-06-20T00:00:00.000, division=003}, 
  {firstName=Noah,   lastName=Jones,    dateOfBirth=1998-06-23T00:00:00.000, division=002}, 
  {firstName=Ava,    lastName=Brown,    dateOfBirth=1996-08-26T00:00:00.000, division=001}
], 
divisions=[
  {divisionCode=001, name=Marketing},
  {divisionCode=002, name=Engineering},
  {divisionCode=003, name=Human Resources},
  {divisionCode=004, name=Legal}
]
}
```
So, first of all, it is an introduction to **xlbean**, which **defines the cells to be read in Excel itself -> It can be defined quickly and it is strong against the change of the format of the table itself!**.
In this way, it is possible to easily call data on Excel from a Java program, so **you can use structured data existing in Excel very quickly**.

## Basic-2. Using Groovy

Next, let's look at how to incorporate Groovy.

Consider the case where **you need the age of the employee** in the sample above.
This time, the following two points are additionally defined.

- Add the **Age** column next to the **Date of Birth** column and write a Groovy script that asks for the age.
- Define `fieldType = localdate` in the` dateOfBirth` column. This is because we want to use dateOfBirth field with type `java.time.LocalDate`. This is one of the standard features of **xlbean**.

![image](https://user-images.githubusercontent.com/23234132/57052478-61b1ac80-6cc2-11e9-8ebd-82139c9dd8ba.png)

And Java program can be this.

```java
public class XlScriptReadme2 {
    public static void main(String[] args) {
        XlScriptReader reader = new XlScriptReader();
        XlBean bean = reader.read(new File("readme/example_02.xlsx"));
        System.out.println(bean);
    }
}
```
As you may have noticed, **Java program is completely identical**. However, the output result changes as follows.

```
{
employees=[
  {firstName=Emma,   lastName=Smith,    dateOfBirth=1990-04-01, age=29, division=001},
  {firstName=Lian,   lastName=Johnson,  dateOfBirth=1998-11-01, age=20, division=003},
  {firstName=Olivia, lastName=Williams, dateOfBirth=1991-06-20, age=27, division=003},
  {firstName=Noah,   lastName=Jones,    dateOfBirth=1998-06-23, age=20, division=002},
  {firstName=Ava,    lastName=Brown,    dateOfBirth=1996-08-26, age=22, division=001}
],
divisions=[
  {divisionCode=001, name=Marketing},
  {divisionCode=002, name=Engineering},
  {divisionCode=003, name=Human Resources},
  {divisionCode=004, name=Legal}
]
}
```

As you can see, the `age` field has been calculated. Without adding any complexity to the Java program, **data calculation was performed by linking logic to data itself.**

# Advanced Use Cases

## Advanced-1. Define data structure

Besides using Groovy to calculate values, it can also be used to **define the structure of data.**

Now, let's think about **having a list of members who belong to the division information**. Here we name it `members` in the` division` object.

![image](https://user-images.githubusercontent.com/23234132/57052634-a2f68c00-6cc3-11e9-85c9-7dda638f22ef.png)

Here is the explanation of the one liner program in the table.

* `employees` points to the list of` employees` defined in this Excel. Since Groovy is evaluated after reading all Excel values, any logic can access all values. (However, you need to be aware of the order of execution of the Groovy logics, which will be discussed later.)
* For those unfamiliar with Groovy, the `findAll` method is a Groovy standard method that returns a list containing all the elements of the condition that apply to the closure passed in the argument. Also, the syntax is Groovy-specific, so transforming it closer to Java syntax results in:

```Groovy
// Explanation of Groovy syntax

// The part enclosed by "{" and "}" is Groovy's Closure and it is passing Closure to findAll method.
employees.findAll{it.division == '001'}
---
// Groovy can omit the parentheses "()" of the method call. The following is equivalent.
employees.findAll({it.division == '001'}) 
---
// Also, for the argument of Closure, an implicit variable "it" is defined with reference to each iteration element.
// To make look more like Java, argument can be explicitly defined this way.
employees.findAll({_it -> _it.division == '001'}) 
```

Please refer to Groovy's official [Differences with Java] (http://groovy-lang.org/differences.html) for the syntax of Groovy.

* Of course you can use spreadsheet functions when defining this method. In this example, we put a function that combines strings in a solid `` = "` employees.findAll {it.division == '' & C13 & "'}` "``.

If you read this Excel **again with the exact same Java code**, the divisions will be output to the console as below.

```
divisions=[
  {
    divisionCode=001, 
    name=Marketing,
    members=[
      {division=001, firstName=Emma, lastName=Smith, dateOfBirth=1990-04-01, age=29},
      {division=001, firstName=Ava, lastName=Brown, dateOfBirth=1996-08-26, age=22}
    ]
  },
  {
    divisionCode=002, 
    name=Engineering,
    members=[
      {division=002, firstName=Noah, lastName=Jones, dateOfBirth=1998-06-23, age=20}
    ]
  },
  {
    divisionCode=003, 
    name=Human Resources,
    members=[
      {division=003, firstName=Lian, lastName=Johnson, dateOfBirth=1998-11-01, age=20},
      {division=003, firstName=Olivia, lastName=Williams, dateOfBirth=1991-06-20, age=27}]
    },
  {
    divisionCode=004, 
    name=Legal
  }
]
```

Thus, data defined in two separate tables in Excel could be combined and displayed by a simple definition.

## Advanced-2. Complete program by Excel only

In the explanation so far, you looked at the result which was written out to standard output by Java program. In other words, the assumed scenario was that **the data defined in Excel was read into a Java program to get used in Java program.**
However, **the fact that there is data and you can write logic is that it can be a complete program**.

Now, let's think about writing the employee and department information to a file in JSON format.

Add a sheet and fill in the following:

![image](https://user-images.githubusercontent.com/23234132/57052816-a3dbed80-6cc4-11e9-9b96-5bc961a4314b.png)

* Description of column C: **"The object ` employees` defined in another sheet is converted to JSON format by `org.xlbean.xscript.util.JSON#stringify` method and set to the `json` field of the n-th element of the list `output`"**
* Description of column D: **"Writes the value set in the `json` field of the same element (`$it` is an implicit variable of xlscript) to a file named "employees.json". The result for the `write` field of the n-th element of the List `output` is set to `null` because `File#write` returns `void`.**
* Groovy's execution order is the order in which the definitions are read from left to right, and for horizontal tables, from top to bottom. Also, when there are multiple sheets, it is executed from the left sheet to the right sheet.
* In this example, it doesn't really matter if you don't specify the order of the script, but if there is a change, you won't run this output before the `employees` side of Groovy is executed, so `scriptOrder = 2000` option is included to ensure that it will be executed after other scripts. By default, it is treated as `scriptOrder = 1000`.

When this is executed, two files **employees.json** and **divisions.json** are output to the current directory.
The contents are as follows.

```json:employees.json
[
  {
    "division": "001",
    "firstName": "Emma",
    "lastName": "Smith",
    "dateOfBirth": "1990-04-01T00:00:00.000",
    "age": 29
  },
  {
    "division": "003",
    "firstName": "Lian",
    "lastName": "Johnson",
    "dateOfBirth": "1998-11-01T00:00:00.000",
    "age": 20
  },
  {
    "division": "003",
    "firstName": "Olivia",
    "lastName": "Williams",
    "dateOfBirth": "1991-06-20T00:00:00.000",
    "age": 27
  },
  {
    "division": "002",
    "firstName": "Noah",
    "lastName": "Jones",
    "dateOfBirth": "1998-06-23T00:00:00.000",
    "age": 20
  },
  {
    "division": "001",
    "firstName": "Ava",
    "lastName": "Brown",
    "dateOfBirth": "1996-08-26T00:00:00.000",
    "age": 22
  }
]
```

```json:divisions.json
[
  {
    "divisionCode": "001",
    "members": [
      {
        "division": "001",
        "firstName": "Emma",
        "lastName": "Smith",
        "dateOfBirth": "1990-04-01T00:00:00.000",
        "age": 29
      },
      {
        "division": "001",
        "firstName": "Ava",
        "lastName": "Brown",
        "dateOfBirth": "1996-08-26T00:00:00.000",
        "age": 22
      }
    ],
    "name": "Marketing"
  },
  {
    "divisionCode": "002",
    "members": [
      {
        "division": "002",
        "firstName": "Noah",
        "lastName": "Jones",
        "dateOfBirth": "1998-06-23T00:00:00.000",
        "age": 20
      }
    ],
    "name": "Engineering"
  },
  {
    "divisionCode": "003",
    "members": [
      {
        "division": "003",
        "firstName": "Lian",
        "lastName": "Johnson",
        "dateOfBirth": "1998-11-01T00:00:00.000",
        "age": 20
      },
      {
        "division": "003",
        "firstName": "Olivia",
        "lastName": "Williams",
        "dateOfBirth": "1991-06-20T00:00:00.000",
        "age": 27
      }
    ],
    "name": "Human Resources"
  },
  {
    "divisionCode": "004",
    "name": "Legal"
  }
]
```

Again, the point is that **the Java program that calls `xlscript` is completely identical to the first simple one.**

## Advanced-3. Calling external logic from Excel

Up to here, we have demonstrated that **any script can be executed**, thus **it can express any data structure**, and **you can do anything** if you want.

However, the examples so far have been only very simple logic. On the other hand, practical logic requires a program with a certain size or more. But writing medium-sized logic in Excel is basically not possible since there is no IDE support in the first place.

The **xlscript** approach to this problem is **"call external logic from Excel"**.

Let's look at it with the example program.
As an example of external logic, we are going to use DTO `Employee` class and` EmployeeLogic` class.

Now change the method of creating a `XlScriptReader` instance from a simple `new` to one that uses a` Builder`. At this time, use `Builder#addBaseBinding(String key, Object value)` with `$empLogic` as a key and an instance of `EmployeeLogic` as a value. It enables to call this instance from Excel logic by specifying `$empLogic` variable.

```java
public class XlScriptReadme3 {

    public static class Employee {
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private int age;
        private String division;
        // Omit getter/setter/toString
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
```

Calling `$empLogic` from Excel will look like this.

![image](https://user-images.githubusercontent.com/23234132/57056481-d514e780-6cdd-11e9-8b47-53aeaa596b35.png)

The points are as follows.

* You can call methods from Excel by `$empLogic.saveEmployee`.
* This method receives a `Employee` instance, but employees is a list of `XlBean` instances and can not be passed as it is. By calling `XlBean#of`, we map `XlBean` to an instance of `Employee`. This method is a standard method of ** xlbean **.
* `$index` is an implicit variable of **xlscript**. When reading a table, return the row number.

Thus, it was possible to **easily integrate programs implemented in a general development process into Excel. **This enables complex and robust programs and natural integration.

## Advanced-4. Calling logics defined in Excel from external program

All the samples so far used Groovy to process the data defined in Excel.
This is the last example, but let's define the logic, not the data.

Consider the case where data format conversion is required when sending the `employees` defined above to a certain system.
The requirement is to convert it into a CSV file with such fields.

![image](https://user-images.githubusercontent.com/23234132/57056808-d2b38d00-6cdf-11e9-9358-52f6176d4067.png)

Adding logic that conforms to the requirements to a new column. ("field type" and "remarks" columns are hidden for space reasons)
![image](https://user-images.githubusercontent.com/23234132/57056826-eeb72e80-6cdf-11e9-91e2-884dbd881c71.png)

The points in this logic are as follows.

* Loading **CSV column names** columns and **Logic** columns into the list `transform`.
* `transform` has an option `skipScript = true`. This is an option of **xlscript**, which is not to evaluate the string as Groovy script but to keep it as string at the time of reading by `XlScriptReader`. Later, `transform` can be executed from external program by using `XlScriptReaderContext#eval(String name, Map context)`.
* For each column of `transform`, the options `toMap=key` and `toMap=value` are defined. This is an option of **xlbean**, which converts List format data into a Map with each columns as key-value.
* Each logic refers to the variable `in`, which is not defined anywhere on Excel. This is a variable to receive the value from external program later.
 
```java
public static void main(String[] args) {
    XlScriptReader reader = new XlScriptReader();
    XlScriptReaderContext context = (XlScriptReaderContext) reader.readContext(new File("demo/demo2.xlsx"));
    String csv = context
        .getXlBean()
        .beans("employees")
        .stream()
        // Converting Map to Employee class by calling "transform" logics
        .map(emp ->
        {
            Map<String, Object> additionalContext = new HashMap<>();
            additionalContext.put("in", emp);
            return context.eval("transform", additionalContext);
        })
        // As defined in Excel, csv field already has concatenated string in it
        .map(emp -> emp.get("csv").toString())
        // Joining each rows of CSV with line separator
        .collect(Collectors.joining("\r\n"));
    System.out.println(csv);
}
```
The following is displayed on the standard output.

```
Smith　Emma,19900401,29,Marketing
Johnson　Lian,19981101,20,Human Resources
Williams　Olivia,19910620,27,Human Resources
Jones　Noah,19980623,20,Engineering
Brown　Ava,19960826,22,Marketing
```

The point of this sample is that the Groovy script defined in the Excel table can be called as logic, not necessarily from the Java program but also from the Groovy script in Excel. Since an instance of `XlScriptReaderContext` is included in implicit variable `$context`, this logic can also be called from Excel by `$context.eval("transform", [in: employees[0]])`.

# Options

## Option: skipScript - Skip evaluation of Grooy script

### Description

By default any cell with back-quoted values will be evaluated as Groovy script at the time of reading Excel. Using `skipScript=true`, you can skip this evaluation and get the back-quoted value as it is.

Basically the purpose of skipping is to invoke it later using `XlScriptReaderContext#eval(String definitionName, Map context)`.

### Available values

- true: skip evaluation
- false(default): execute evaluation 

## Option: scriptOrder - explicitly define the order of the evaluation

### Description

By default the order of the evaluation of back-quoted values are from left to right and top to bottom. You can change this order by using `scriptOrder` option. The definitions without this option has implicit scriptOrder value of 1000, so that if you specify any number larger than this, that script will be evaluated after other definitions and vise versa.

### Available values

Any integer: The default value is 1000







