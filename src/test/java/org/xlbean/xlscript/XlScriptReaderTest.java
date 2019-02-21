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
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.xlbean.XlBean;
import org.xlbean.reader.XlBeanReader;
import org.xlbean.util.FileUtil;
import org.xlbean.xlscript.XlScriptReader.XlScriptReaderBuilder;

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
        XlScriptReader reader = new XlScriptReaderBuilder()
            .baseInstance(new SampleBaseInstance())
            .build();
        XlBean excel = reader.read(in);

        validateTestFile(excel);
    }

    @Test
    public void testBaseInstance() {
        InputStream in = XlScriptReaderTest.class.getResourceAsStream("Test_forBaseScript.xlsx");
        XlScriptReader reader = new XlScriptReaderBuilder()
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

        List<String> expected = Arrays.asList(
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
        for (int i = 0; i < results.length; i++) {
            assertThat(results[i], is(expected.get(i)));
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

        context.process("test2");
        context.process("test2");

        System.setOut(originalStdout);

        String result = new String(baos.toByteArray());
        String[] results = result.split(System.lineSeparator());
        Arrays.stream(results).forEach(System.out::println);
        assertThat(results[0], is("test2-111.0"));
        assertThat(results[1], is("test2-111.0"));
        assertThat(results.length, is(2));

        context.getXlBean().put("arg1", 123);
        context.getXlBean().put("arg2", 456);
        Map<String, Object> res1 = context.process("list");
        System.out.println(res1);
        assertThat(res1.get("ddd").toString(), is("test579"));

        context.getXlBean().put("arg1", 222);
        context.getXlBean().put("arg2", 999);
        Map<String, Object> res2 = context.process("list");
        System.out.println(res2);
        assertThat(res2.get("ddd").toString(), is("test1221"));

    }
}
