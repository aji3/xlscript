package org.xlbean.xlscript.util;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.xlbean.XlBean;
import org.xlbean.xlscript.XlScriptReader;

public class JSONTest {

    @Test
    public void stringify() {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("aaa", "value");
        obj.put("bbb", 1);
        Map<String, Object> objChild = new LinkedHashMap<>();
        obj.put("child", objChild);
        objChild.put("aaa", "value");
        objChild.put("bbb", 1);
        objChild.put("_self", objChild);
        objChild.put("testClass", new TestClass());
        Map<String, Object> objGrandChild = new LinkedHashMap<>();
        objChild.put("child", objGrandChild);
        objGrandChild.put("localdate", LocalDate.of(2019, 4, 29));
        objGrandChild.put("localdatetime", LocalDateTime.of(2019, 4, 29, 12, 34, 56, 789));
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        obj.put("list", list);
        Map<String, Object> objElem = new LinkedHashMap<>();
        objElem.put("ddd", "ddd-1");
        objElem.put("eee", "eee-1");
        list.add(objElem);
        objElem = new LinkedHashMap<>();
        objElem.put("ddd", "ddd-2");
        objElem.put("eee", "eee-2");
        list.add(objElem);
        
        String json = JSON.stringify(obj);
        System.out.println(json);
    }
    
    public static class TestClass {
        
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void parse() {
        XlScriptReader reader = new XlScriptReader();
        XlBean bean = reader.read(JSONTest.class.getResourceAsStream("Test_JSON.xlsx"));
        String str = JSON.stringify(bean);
        System.out.println(str);
        Map<String, Object> obj = (Map<String, Object>) JSON.parse(str);
        System.out.println(obj);

        assertThat(obj.get("someValue"), is("some value"));
        assertThat(obj.get("anotherValue"), is("999.0"));
        List<Map<String, Object>> list = (List) obj.get("list");
        assertThat(list.size(), is(3));
        assertThat(list.get(0).get("value"), is("111"));
        assertThat(list.get(1).get("value"), is("222"));
        assertThat(list.get(2).get("value"), is(333));
        assertThat(list.get(0).get("key"), is("nested.val1"));
        assertThat(list.get(1).get("key"), is("nested.val2"));
        assertThat(list.get(2).get("key"), is("nested.val3"));
        Map<String, Object> nested = (Map<String, Object>)obj.get("nested");
        assertThat(nested.get("val1"), is("111"));
        assertThat(nested.get("val2"), is("222"));
        assertThat(nested.get("val3"), is(333));
        List<Map<String, Object>> table = (List) obj.get("table");
        assertThat(table.get(0).get("col1"), is("aaa"));
        assertThat(table.get(1).get("col1"), is(nullValue()));
        assertThat(table.get(2).get("col1"), is("ccc"));
        assertThat(table.get(3).get("col1"), is("ddd"));
        assertThat(table.get(4).get("col1"), is(nullValue()));
        assertThat(table.get(0).get("col2"), is("1.0"));
        assertThat(table.get(1).get("col2"), is("2.0"));
        assertThat(table.get(2).get("col2"), is(nullValue()));
        assertThat(table.get(3).get("col2"), is("4.0"));
        assertThat(table.get(4).get("col2"), is(nullValue()));
        assertThat(table.get(0).get("col3"), is("aaa1.0"));
        assertThat(table.get(1).get("col3"), is("null2.0"));
        assertThat(table.get(2).get("col3"), is("cccnull"));
        assertThat(table.get(3).get("col3"), is(nullValue()));
        assertThat(table.get(4).get("col3"), is("nullnull"));
    }

}
