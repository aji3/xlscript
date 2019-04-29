package org.xlbean.xlscript;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.xlbean.XlBean;
import org.xlbean.data.ExcelDataLoader;
import org.xlbean.definition.ExcelCommentDefinitionLoader;
import org.xlbean.reader.XlBeanReader;
import org.xlbean.reader.XlBeanReaderContext;
import org.xlbean.util.FileUtil;
import org.xlbean.xlscript.XlScriptReader.Builder;
import org.xlbean.xlscript.processor.AbstractXlScriptProcessor.XlScriptBindingsBuilder;

public class XlScriptReaderTest {

    @Test
    public void testReadInputStream() {
        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test.xlsx");
        XlScriptReader reader = new XlScriptReader();
        XlBean excel = reader.read(in);

        validateTestFile(excel);
    }

    @Test
    public void testReadFile() {
        File in = new File(XlScriptReaderTest.class.getResource("Test.xlsx").getFile());
        XlScriptReader reader = new XlScriptReader();
        XlBean excel = reader.read(in);

        validateTestFile(excel);
    }

    @Test
    public void testReadContextFile() {
        File in = new File(XlScriptReaderTest.class.getResource("Test.xlsx").getFile());
        XlScriptReader reader = new XlScriptReader();
        XlBeanReaderContext context = reader.readContext(in);

        validateTestFile(context.getXlBean());
    }

    @Test
    public void testReadWorkbook() throws Exception {
        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test.xlsx");
        Workbook wb = WorkbookFactory.create(FileUtil.copyToInputStream(in));

        XlScriptReader reader = new XlScriptReader();
        XlBean excel = reader.read(wb, wb);

        validateTestFile(excel);
    }

    @Test
    public void testReadContextByInputStream() {

        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test.xlsx");
        XlScriptReader reader = new XlScriptReader();
        XlBean excel = reader.read(in);

        validateTestFile(excel);

    }

    @Test
    public void testReadContextByFile() {

        File in = new File(XlScriptReaderTest.class.getResource("Test.xlsx").getFile());
        XlScriptReader reader = new XlScriptReader();
        XlBean excel = reader.read(in);

        validateTestFile(excel);
    }

    @Test
    public void testReadContextByWorkbook() throws Exception {
        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test.xlsx");
        Workbook wb = WorkbookFactory.create(FileUtil.copyToInputStream(in));

        XlScriptReader reader = new XlScriptReader();
        XlBean excel = reader.read(wb, wb);

        validateTestFile(excel);
    }

    @Test
    public void testBaseScript() {
        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test_forBaseScript.xlsx");
        XlScriptReader reader = new Builder()
            .baseInstance(new SampleBaseInstance())
            .build();
        XlBean excel = reader.read(in);

        validateTestFile(excel);
    }

    @Test
    public void testBaseInstance() {
        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test_forBaseScript.xlsx");
        XlScriptReader reader = new Builder()
            .baseScript(
                "def concat(String s1, String s2) {\"${s1}${s2}\"}\r\n"
                        + "def add(int i1, int i2) {i1 + i2}")
            .build();
        XlBean excel = reader.read(in);

        validateTestFile(excel);
    }

    public static class SampleBaseInstance {
        public String concat(String s1, String s2) {
            if (s1 == null && s2 == null) {
                return null;
            }
            return s1 + s2;
        }

        public int add(int i1, int i2) {
            return i1 + i2;
        }
    }

    private void validateTestFile(XlBean excel) {

        excel.forEach((key, value) -> {
            System.out.println(key + "\t" + value);
        });

        assertThat(excel.beans("table").get(0).string("col1"), is("aaa"));
        assertThat(excel.beans("table").get(0).string("col2"), is("1.0"));
        assertThat(excel.beans("table").get(0).string("col3"), is("aaa1.0"));
        assertThat(excel.beans("table").get(1).string("col1"), is(nullValue()));
        assertThat(excel.beans("table").get(1).string("col2"), is("2.0"));
        assertThat(excel.beans("table").get(1).string("col3"), is("null2.0"));
        assertThat(excel.beans("table").get(2).string("col1"), is("ccc"));
        assertThat(excel.beans("table").get(2).string("col2"), is(nullValue()));
        assertThat(excel.beans("table").get(2).string("col3"), is("cccnull"));
        assertThat(excel.beans("table").get(3).string("col1"), is("ddd"));
        assertThat(excel.beans("table").get(3).string("col2"), is("4.0"));
        assertThat(excel.beans("table").get(3).string("col3"), is(nullValue()));
        assertThat(excel.beans("table").get(4).string("col1"), is(nullValue()));
        assertThat(excel.beans("table").get(4).string("col2"), is(nullValue()));
        assertThat(excel.beans("table").get(4).string("col3"), is("nullnull"));

        assertThat(excel.bean("nested").string("val1"), is("111"));
        assertThat(excel.bean("nested").string("val2"), is("222"));
        assertThat(excel.bean("nested").string("val3"), is("333"));
    }

    @Test
    public void testScriptOrder() throws IOException {
        PrintStream originalStdout = System.out;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newStdout = new PrintStream(baos);
        System.setOut(newStdout);

        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test_scriptOrder.xlsx");
        XlBeanReader reader = new XlScriptReader();
        XlBean excel = reader.read(in);

        System.setOut(originalStdout);

        System.out.println(excel);
        String result = new String(baos.toByteArray());

        List<String> expected = Arrays
            .asList(
                "value4?scriptOrder=1",
                "scriptOrderSingle?scriptOrder=50",
                "value5?scriptOrder=999",
                "aaa1.0",
                "null2.0",
                "cccnull",
                "null",
                "nullnull",
                "value1",
                "value3",
                "value6",
                "value2?scriptOrder=2000",
                "column4-1",
                "column2-1",
                "column3-1",
                "column1-1",
                "column2-2",
                "column3-2",
                "column1-2",
                "column4-3",
                "column2-3",
                "column3-3",
                "column1-3",
                "column2-4",
                "column1-4",
                "column4-5",
                "column2-5",
                "column1-5",
                "column3-6",
                "column1-6",
                "column4-7",
                "column3-7",
                "column1-7",
                "column1-8",
                "column4-9",
                "column1-9",
                "column2-10",
                "column3-10",
                "column4-11",
                "column2-11",
                "column3-11",
                "column2-12",
                "column4-13",
                "column2-13",
                "column3-14",
                "column4-15",
                "column3-15");

        String[] results = result.split(System.lineSeparator());
        Arrays.stream(results).forEach(System.out::println);
        List<String> resultsList = Arrays
            .stream(results)
            .filter(elem -> !elem.contains("org.xl"))
            .collect(
                Collectors.toList());
        for (int i = 0; i < resultsList.size(); i++) {
            assertThat(resultsList.get(i), is(expected.get(i)));
        }
    }

    @Test
    public void testScriptOrderError() {
        // not integer scriptOrder
        PrintStream originalStdout = System.out;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newStdout = new PrintStream(baos);
        System.setOut(newStdout);

        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test_scriptOrder_error.xlsx");
        XlBeanReader reader = new XlScriptReader();
        XlBean excel = reader.read(in);

        System.setOut(originalStdout);

        System.out.println(excel);
        String result = new String(baos.toByteArray());

        List<String> expected = Arrays
            .asList(
                "this script to be executed first",
                "this is second",
                "this is third");

        String[] results = result.split(System.lineSeparator());
        List<String> resultsList = Arrays
            .stream(results)
            .filter(elem -> elem.startsWith("this"))
            .collect(
                Collectors.toList());
        for (int i = 0; i < resultsList.size(); i++) {
            assertThat(resultsList.get(i), is(expected.get(i)));
        }
    }

    @Test
    public void testToBean() {
        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test_toBean.xlsx");
        XlBeanReader reader = new XlScriptReader();
        XlBean excel = reader.read(in);

        System.out.println(excel);

        assertThat(excel.string("aaa"), is("testaaa"));
        assertThat(excel.string("bbb"), is("testaaa and bbb"));
        assertThat(excel.get("ccc"), is("1"));
        assertThat(excel.get("ddd"), is("2"));
        assertThat(excel.get("eee"), is(3));

    }

    @Test
    public void testSkipScript() {
        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test_skipScript.xlsx");
        XlBeanReader reader = new XlScriptReader();
        XlScriptReaderContext context = (XlScriptReaderContext) reader.readContext(in);

        System.out.println(context.getXlBean());

        PrintStream originalStdout = System.out;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newStdout = new PrintStream(baos);
        System.setOut(newStdout);

        context.eval("test2");
        context.eval("test2");

        System.setOut(originalStdout);

        String result = new String(baos.toByteArray());
        String[] results = result.split(System.lineSeparator());
        List<String> resultsList = Arrays
            .stream(results)
            .filter(elem -> !elem.contains("org.xl"))
            .collect(
                Collectors.toList());
        assertThat(resultsList.get(0), is("test2-111.0"));
        assertThat(resultsList.get(1), is("test2-111.0"));
        assertThat(resultsList.size(), is(2));

        context.getXlBean().put("arg1", 123);
        context.getXlBean().put("arg2", 456);
        Map<String, Object> res1 = context.eval("list");
        System.out.println(res1);
        assertThat(res1.get("ddd").toString(), is("test579"));

        context.getXlBean().put("arg1", 222);
        context.getXlBean().put("arg2", 999);
        Map<String, Object> res2 = context.eval("list");
        System.out.println(res2);
        assertThat(res2.get("ddd").toString(), is("test1221"));

        Map<String, Object> optionalMap = new HashMap<>();
        optionalMap.put("arg2", 333);
        assertThat(context.eval("list", optionalMap).get("ddd").toString(), is("test555"));

    }

    @Test
    public void testReaderBuilder() {
        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test_commentDefinition.xlsx");
        XlScriptReader reader = new Builder()
            .definitionLoader(new ExcelCommentDefinitionLoader())
            .dataLoader(new ExcelDataLoader())
            .build();
        XlBean xlbean = reader.read(in);
        System.out.println(xlbean);

        assertThat(xlbean.get("someValue"), is("some value2"));
        assertThat(xlbean.get("anotherValue"), is("9992.0"));
        assertThat(xlbean.beans("table").get(0).string("col1"), is("aaa"));
        assertThat(xlbean.beans("table").get(0).string("col2"), is("12.0"));
        assertThat(xlbean.beans("table").get(0).string("col3"), is("aaa12.0"));
        assertThat(xlbean.beans("table").get(1).string("col1"), is(nullValue()));
        assertThat(xlbean.beans("table").get(1).string("col2"), is("22.0"));
        assertThat(xlbean.beans("table").get(1).string("col3"), is("null22.0"));
        assertThat(xlbean.beans("table").get(2).string("col1"), is("ccc"));
        assertThat(xlbean.beans("table").get(2).string("col2"), is(nullValue()));
        assertThat(xlbean.beans("table").get(2).string("col3"), is("cccnull"));
        assertThat(xlbean.beans("table").get(3).string("col1"), is("ddd"));
        assertThat(xlbean.beans("table").get(3).string("col2"), is("42.0"));
        assertThat(xlbean.beans("table").get(3).string("col3"), is(nullValue()));
        assertThat(xlbean.beans("table").get(4).string("col1"), is(nullValue()));
        assertThat(xlbean.beans("table").get(4).string("col2"), is(nullValue()));
        assertThat(xlbean.beans("table").get(4).string("col3"), is("nullnull"));
        assertThat(xlbean.beans("table").size(), is(5));

    }

    @Test
    public void testErrorScript() {
        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test_error.xlsx");
        XlScriptReader reader = new XlScriptReader();

        PrintStream ps = System.out;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newPs = new PrintStream(baos);
        System.setOut(newPs);

        XlBean bean = reader.read(in);
        System.out.println(bean);

        System.setOut(ps);

        String stdoutStr = new String(baos.toByteArray());
        String[] stdout = stdoutStr.split("\r\n");
        List<String> list = Arrays.stream(stdout).map(str -> str.replaceAll("^[^-]*-\\s*", "")).collect(Collectors.toList());
        list.forEach(System.out::println);
        assertThat(list.contains("----Error occured during evaluating script"), is (true));
        assertThat(list.contains("println aaa"), is (true));
        assertThat(list.contains("----EXCEPTION FOR SCRIPT----START-----------------------------"), is (true));
        assertThat(list.contains("groovy.lang.MissingPropertyException: No such property: aaa for class: Script1"), is (true));
    }
    
    @Test
    public void testXlScriptBindingsBuilder() {
        XlScriptBindingsBuilder builder = new XlScriptBindingsBuilder();
        builder.put("aaa", null);
        Map<String, Object> map = new HashMap<String, Object>();
        builder.put(null, map);
        builder.putAll(null);
        Map<String, Object> bindings = builder.build();
        assertThat(bindings.get("aaa"), is(nullValue()));
        assertThat(bindings.containsKey(null), is(false));
    }
}
