package org.xlbean.xlhttp;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.xlbean.XlBean;
import org.xlbean.xlscript.XlScriptReader;
import org.xlbean.xlscript.XlScriptReader.XlScriptReaderBuilder;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class XlHttpTest {
    @Rule
    public WireMockRule wireMockRule1 = new WireMockRule(20080);
    @Rule
    public WireMockRule wireMockRule2 = new WireMockRule(
        options()
            .port(20081)
            .enableBrowserProxying(true)
            .usingFilesUnderDirectory("mappingsProxy"));

    @Test
    public void getAbsoluteUrl() {
        RequestConfig config = new RequestConfig();
        config.setUrl("http://test/aaa");

        assertThat(config.getAbsoluteUrl(), is("http://test/aaa"));

        config = new RequestConfig();
        config.setUrl("/aaa");
        config.setBaseURL("https://test");

        assertThat(config.getAbsoluteUrl(), is("https://test/aaa"));

        config = new RequestConfig();
        config.setUrl("/aaa");

        try {
            config.getAbsoluteUrl();
        } catch (IllegalArgumentException e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }

        config = new RequestConfig();
        config.setUrl("/aaa");
        config.setBaseURL("https://test/");

        assertThat(config.getAbsoluteUrl(), is("https://test/aaa"));

    }

    @Test
    public void testHttp() {
        XlScriptReader reader = new XlScriptReaderBuilder().addBaseBinding("$http", new XlHttp()).build();
        XlBean bean = reader.read(XlHttpTest.class.getResourceAsStream("Test_http.xlsx"));

        XlHttp http = new XlHttp();
        System.out.println(http.JSON.stringify(bean));

        RequestConfig config = new RequestConfig();
        config.setBaseURL("http://localhost:20081");
        config.setUrl("/__admin/requests");
        XlResponse response = http.request(config);
        response.getData();
        System.out.println(response.getData());
    }
}
