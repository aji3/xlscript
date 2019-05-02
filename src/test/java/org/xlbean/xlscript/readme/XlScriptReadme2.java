package org.xlbean.xlscript.readme;

import java.io.File;

import org.xlbean.XlBean;
import org.xlbean.xlscript.XlScriptReader;

public class XlScriptReadme2 {
    public static void main(String[] args) {
        XlScriptReader reader = new XlScriptReader();
        XlBean bean = reader.read(new File("readme/example_02.xlsx"));
        System.out.println(bean);
    }
}
