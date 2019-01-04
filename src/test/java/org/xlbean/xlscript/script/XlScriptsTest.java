package org.xlbean.xlscript.script;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.xlbean.xlscript.sample.SampleBizLogic;
import org.xlbean.xlscript.util.XlScript;

public class XlScriptsTest {

    @Test
    public void testScript() {
        Object obj = new XlScript().evaluate("1 + 2");
        System.out.println(obj);
        assertThat(obj, is(3));
    }

    @Test
    public void testScriptWithBindings() {
        Map<String, Object> map = new HashMap<>();
        map.put("aaa", 2);
        Object obj = new XlScript().evaluate("1 + aaa", map);
        System.out.println(obj);
        assertThat(obj, is(3));
    }

    @Test
    public void testUseBizLogicForScriptBase() {
        Object obj = new XlScript(new SampleBizLogic()).evaluate("setSample('aaa'); testMethod('bbb');");
        System.out.println(obj);
        assertThat(obj, is("bbbaaa"));
    }

    @Test
    public void testUseScriptForScriptBase() {
        Object obj = new XlScript("def testMethod(String val) {val + 'aaa'}").evaluate("testMethod('bbb')");
        System.out.println(obj);
        assertThat(obj, is("bbbaaa"));
    }

    @Test
    public void testUseBizLogicForScriptBaseWithBindings() {
        Map<String, Object> map = new HashMap<>();
        map.put("bbb", "bindings_bbb");
        Object obj = new XlScript(new SampleBizLogic()).evaluate("setSample('aaa'); testMethod(bbb);", map);
        System.out.println(obj);
        assertThat(obj, is("bindings_bbbaaa"));
    }

    @Test
    public void testUseScriptForScriptBaseWithBindings() {
        Map<String, Object> map = new HashMap<>();
        map.put("bbb", "bindings_bbb");
        Object obj = new XlScript("def testMethod(String val) {val + 'aaa'}").evaluate("testMethod(bbb)", map);
        System.out.println(obj);
        assertThat(obj, is("bindings_bbbaaa"));
    }

    @Test
    public void testContextfulScript() {
        Map<String, Object> map = new HashMap<>();
        map.put("bbb", "bindings_bbb");
        XlScript script = new XlScript("def testMethod(String val) {val + 'aaa'}");
        Object obj1 = script.evaluate("aaa = 'ccc'", map);

        assertThat(obj1, is("ccc"));

        Map<String, Object> map2 = new HashMap<>();
        map2.put("ccc", "bindings_ccc");
        Object obj2 = script.evaluate("testMethod(bbb) + ccc + aaa", map2);

        assertThat(obj2, is("bindings_bbbaaabindings_cccccc"));

        System.out.println(obj1);
    }

}
