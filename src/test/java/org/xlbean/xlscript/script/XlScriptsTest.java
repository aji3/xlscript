package org.xlbean.xlscript.script;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.xlbean.xlscript.sample.SampleBizLogic;

public class XlScriptsTest {

    @Test
    public void testScript() {
        Object obj = new XlScriptFactory().getXlScript("1 + 2").execute();
        System.out.println(obj);
        assertThat(obj, is(3));
    }

    @Test
    public void testScriptWithBindings() {
        Map<String, Object> map = new HashMap<>();
        map.put("aaa", 2);
        Object obj = new XlScriptFactory().getXlScript("1 + aaa").execute(map);
        System.out.println(obj);
        assertThat(obj, is(3));
    }

    @Test
    public void testUseBizLogicForScriptBase() {
        XlScriptFactory provider = new XlScriptFactory();
        provider.setBaseInstance(new SampleBizLogic());
        Object obj = provider
            .getXlScript("setSample('aaa'); testMethod('bbb');")
            .execute();
        System.out.println(obj);
        assertThat(obj, is("bbbaaa"));
    }

    @Test
    public void testUseScriptForScriptBase() {
        XlScriptFactory provider = new XlScriptFactory();
        provider.setBaseScript("def testMethod(String val) {val + 'aaa'}");
        Object obj = provider
            .getXlScript("testMethod('bbb')")
            .execute();
        System.out.println(obj);
        assertThat(obj, is("bbbaaa"));
    }

    @Test
    public void testUseBizLogicForScriptBaseWithBindings() {
        Map<String, Object> map = new HashMap<>();
        map.put("bbb", "bindings_bbb");
        XlScriptFactory provider = new XlScriptFactory();
        provider.setBaseInstance(new SampleBizLogic());
        Object obj = provider
            .getXlScript("setSample('aaa'); testMethod(bbb);")
            .execute(map);
        System.out.println(obj);
        assertThat(obj, is("bindings_bbbaaa"));
    }

    @Test
    public void testUseScriptForScriptBaseWithBindings() {
        Map<String, Object> map = new HashMap<>();
        map.put("bbb", "bindings_bbb");
        XlScriptFactory provider = new XlScriptFactory();
        provider.setBaseScript("def testMethod(String val) {val + 'aaa'}");
        Object obj = provider
            .getXlScript("testMethod(bbb)")
            .execute(map);
        System.out.println(obj);
        assertThat(obj, is("bindings_bbbaaa"));
    }

    @Test
    public void testContextfulScript() {
        Map<String, Object> map = new HashMap<>();
        map.put("bbb", "bindings_bbb");
        XlScriptFactory provider = new XlScriptFactory();
        provider.setBaseScript("def testMethod(String val) {val + 'aaa'}");
        String sharedContextName = "test";
        XlScript script = provider.getXlScript("aaa = 'ccc'", sharedContextName);
        Object obj1 = script.execute(map);

        assertThat(obj1, is("ccc"));

        Map<String, Object> map2 = new HashMap<>();
        map2.put("ccc", "bindings_ccc");
        XlScript script2 = provider.getXlScript("testMethod(bbb) + ccc + aaa", sharedContextName);
        Object obj2 = script2.execute(map2);

        assertThat(obj2, is("bindings_bbbaaabindings_cccccc"));

        System.out.println(obj1);
    }

    @Test
    public void testGetXlScriptWithSharedContext() {
        XlScriptFactory provider = new XlScriptFactory();
        provider.getXlScript("aaa = 1", "samecontext").execute();
        Object result = provider.getXlScript("aaa", "samecontext").execute();
        System.out.println(result);

        assertThat(result, is(1));

    }

    @Test
    public void testGetXlScriptWithCache() {
        XlScriptFactory provider = new XlScriptFactory();
        provider.getXlScript("aaa = 1", "test").execute();
        XlScript script1 = provider.getXlScript("aaa++", "test", true);
        XlScript script2 = provider.getXlScript("aaa++", "test", true);
        assertThat(script2, is(script1));
        script1.execute();
        script2.execute();
        assertThat(provider.getXlScript("aaa", "test").execute(), is(3));

    }

    // @Test
    // public void aaa() {
    //
    // String scriptText = "aaa = 'aaa'; bbb = 'bbb'; ccc = 'ccc'; ddd = 'ddd'; eee
    // = \"${aaa}${bbb}${ccc}${ddd}\";";
    // int numberOfIteration = 200;
    // long start = System.currentTimeMillis();
    // for (int i = 0; i < numberOfIteration; i++) {
    // GroovyShell shell10 = new GroovyShell();
    // shell10.evaluate(scriptText);
    // }
    // System.out.println(System.currentTimeMillis() - start);
    //
    // start = System.currentTimeMillis();
    // GroovyShell shell11 = new GroovyShell();
    // for (int i = 0; i < numberOfIteration; i++) {
    // shell11.evaluate(scriptText);
    // }
    // System.out.println(System.currentTimeMillis() - start);
    //
    // start = System.currentTimeMillis();
    // for (int i = 0; i < numberOfIteration; i++) {
    // GroovyShell shell12 = new GroovyShell(shell11.getContext());
    // shell12.evaluate(scriptText);
    // }
    // System.out.println(System.currentTimeMillis() - start);
    //
    // GroovyCodeSource gcs = AccessController.doPrivileged(new
    // PrivilegedAction<GroovyCodeSource>() {
    // public GroovyCodeSource run() {
    // return new GroovyCodeSource(scriptText, "Script999.groovy", "/groovy/shell");
    // }
    // });
    //
    // start = System.currentTimeMillis();
    // for (int i = 0; i < numberOfIteration; i++) {
    // GroovyShell shell14 = new GroovyShell();
    // shell14.evaluate(gcs);
    // }
    // System.out.println(System.currentTimeMillis() - start);
    //
    // start = System.currentTimeMillis();
    // GroovyShell shell15 = new GroovyShell();
    // for (int i = 0; i < numberOfIteration; i++) {
    // shell15.evaluate(gcs);
    // }
    // System.out.println(System.currentTimeMillis() - start);
    //
    // start = System.currentTimeMillis();
    // GroovyShell shell16 = new GroovyShell();
    // Class clazz = shell16.getClassLoader().parseClass(gcs);
    // for (int i = 0; i < numberOfIteration; i++) {
    // InvokerHelper.createScript(clazz, new Binding());
    // }
    // System.out.println(System.currentTimeMillis() - start);
    //
    // start = System.currentTimeMillis();
    // GroovyShell shell20 = new GroovyShell();
    // Script script20 = shell20.parse(scriptText);
    // for (int i = 0; i < numberOfIteration; i++) {
    // script20.run();
    // }
    // System.out.println(System.currentTimeMillis() - start);
    //
    // GroovyShell shell1 = new GroovyShell();
    // shell1.evaluate("println 'aaa'");
    // GroovyShell shell2 = new GroovyShell();
    // shell2.evaluate("println 'aaa'");
    // GroovyShell shell3 = new GroovyShell();
    // shell3.evaluate("println 'aaa'");
    // GroovyShell shell4 = new GroovyShell();
    // shell4.evaluate("println 'aaa'");
    //
    // GroovyShell shell = new GroovyShell();
    // shell.evaluate("aaa = 1; bbb = 2;");
    // Script script = shell.parse("bbb++;");
    // shell.evaluate("println aaa; println bbb;");
    // script.run();
    // shell.evaluate("println aaa; println bbb;");
    // Script script2 = shell.parse("aaa++");
    // shell.evaluate("println aaa; println bbb;");
    // script2.run();
    // shell.evaluate("println aaa; println bbb;");
    // }

}
