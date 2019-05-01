# xlscript

[![Build Status](https://travis-ci.org/aji3/xlscript.svg?branch=master)](https://travis-ci.org/aji3/xlscript)
[![Coverage Status](https://coveralls.io/repos/github/aji3/xlscript/badge.svg?branch=master)](https://coveralls.io/github/aji3/xlscript?branch=master)

Java utility to read values in Excel sheet based on the definition in the Excel sheet itself, and evaluate the values as Groovy script if necessary.

This library is a thin wrapper of [xlbean](https://github.com/aji3/xlbean), so that basic usage follows it.

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

For both example, cells in YELLOW are a simple value cells and cells in ORANGE are the cells with Groovy script.

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

