package org.xlbean.xlscript;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Test;
import org.xlbean.XlBean;
import org.xlbean.util.Accessors;
import org.xlbean.util.FileUtil;
import org.xlbean.xlscript.XlScriptReader.XlScriptReaderBuilder;

public class XlScriptReaderTest {

    @Before
    public void before() {
        Accessors.setInstance(new Accessors(false));
    }

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
}
