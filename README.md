# xlscript

[![Build Status](https://travis-ci.org/aji3/xlscript.svg?branch=master)](https://travis-ci.org/aji3/xlscript)
[![Coverage Status](https://coveralls.io/repos/github/aji3/xlscript/badge.svg?branch=master)](https://coveralls.io/github/aji3/xlscript?branch=master)

Java utility to evaluate values defined in Excel sheet as Groovy script and set the result to Map.

This library is a thin wrapper of [xlbean](https://github.com/aji3/xlbean), so that basic usage follows the same as xlbean.

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

## 2. Define Mapping Definition on Excel sheet

Since this library uses xlbean internally to load values from Excel sheet, the way for data definition is the same as xlbean as explained [here](https://github.com/aji3/xlbean/wiki).

Only difference is that a value in this format ``` `any_script` ``` will be evaluated as Groovy script.

1. Define name for each column of table in Excel sheet.

    Define **"YOUR_TABLE_NAME#YOUR_COLUMN_NAME"** on the row 1 of the corresponding column.

2. Mark the row which the data of the table starts from.

    Input **"YOUR_TABLE_NAME#~(tilda)"** on the column 1 of the corresponding row.

3. Mark the sheet as the target to be loaded by this utility.

    Input **"####"** on R1C1.

    ![Example of excel sheet](https://user-images.githubusercontent.com/23234132/29244923-4f5cba56-8002-11e7-929d-617a9ea38d83.png "Excelシートの例")

As shown in the 

## 3. Java Program
```java
InputStream in = new FileInputStream("example/presidents.xlsx");
XlBeanReader reader = new XlBeanReader();
XlBean bean = reader.read(in);

List<XlBean> list = bean.beans("presidents");
// List filled with Map of String
System.out.println(list.get(0).get("name")); // John F. Kennedy
System.out.println(list.get(0).get("dateOfBirth")); // 1917-05-29T00:00:00.000

// The map can be mapped to Class
List<President> presidents = bean.listOf("presidents", President.class);
```




