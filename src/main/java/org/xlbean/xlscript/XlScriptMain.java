package org.xlbean.xlscript;

import java.io.File;

public class XlScriptMain {

    public static void main(String[] args) {
        XlScriptReader reader = new XlScriptReader();
        reader.read(new File("demo/demo.xlsx"));
    }
}
