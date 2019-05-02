package org.xlbean.xlscript.readme;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.xlbean.xlscript.XlScriptReader;
import org.xlbean.xlscript.XlScriptReaderContext;

public class XlScriptReadme4 {
    public static void main(String[] args) {
        XlScriptReader reader = new XlScriptReader();

        XlScriptReaderContext context = (XlScriptReaderContext) reader.readContext(new File("readme/example_04.xlsx"));
        String csv = context
            .getXlBean()
            .beans("employees")
            .stream()
            .map(emp ->
            {
                Map<String, Object> additionalContext = new HashMap<>();
                additionalContext.put("in", emp);
                return context.eval("transform", additionalContext);
            })
            .map(emp -> emp.get("csv").toString())
            .collect(Collectors.joining("\r\n"));
        System.out.println(csv);
    }
}
