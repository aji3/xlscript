package org.xlbean.xlscript.readme;

import java.io.File;

import org.xlbean.XlBean;
import org.xlbean.xlscript.XlScriptReader;

public class XlScriptReadme {
    public static void main(String[] args) {
        XlScriptReader reader = new XlScriptReader();
        XlBean bean = reader.read(new File("readme/example_01.xlsx"));
        System.out.println(bean);
    }
}
