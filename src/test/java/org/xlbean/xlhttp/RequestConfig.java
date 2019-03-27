package org.xlbean.xlhttp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import groovy.lang.Closure;

public class RequestConfig {

    public static final String HEADERKEY_CONTENT_TYPE = "Content-Type";
    public static final String DEFAULT_CONTENT_TYPE = "application/json; charset=utf-8";

    private String url;

    private String method = "get";

    private String baseURL = "https://api.example.com";

    private Map<String, Object> headers;

    private Map<String, Object> params;

    private Object data;

    private int timeout;

    private boolean withCredentials;

    private Authorization auth;

    private ProxyConfig proxy;

    private Closure<?> transformRequest;

    private Closure<?> transformResponse;

    public String getContentType() {
        if (headers == null) {
            return DEFAULT_CONTENT_TYPE;
        }
        return headers.getOrDefault(HEADERKEY_CONTENT_TYPE, DEFAULT_CONTENT_TYPE).toString();
    }

    public String getAbsoluteUrl() {
        URL absoluteUrl = null;
        try {
            // if url is valid URL, then use this value
            absoluteUrl = new URL(url);
        } catch (MalformedURLException e) {
            // if url is not a valid url, try baseURL
            if (baseURL == null) {
                throw new IllegalArgumentException(
                    "Illegal URL. Either \"url\" or \"baseURL + url\" must be valid URL.");
            }
            try {
                if (url == null || url.isEmpty()) {
                    absoluteUrl = new URL(baseURL);
                }
                if (baseURL.endsWith("/") && url.startsWith("/")) {
                    absoluteUrl = new URL(baseURL.substring(0, baseURL.length() - 1) + url);
                } else if (baseURL.endsWith("/") || url.startsWith("/")) {
                    absoluteUrl = new URL(baseURL + url);
                } else {
                    absoluteUrl = new URL(baseURL + "/" + url);
                }

            } catch (MalformedURLException e1) {
                // if this is not a valid url, throw exception
                throw new IllegalArgumentException(
                    "Illegal URL. Either \"url\" or \"baseURL + url\" must be valid URL.");
            }
        }
        return absoluteUrl.toString();
    }

    public static class ProxyConfig {
        private String host;
        private int port;
        private Authorization auth;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public Authorization getAuth() {
            return auth;
        }

        public void setAuth(Authorization auth) {
            this.auth = auth;
        }
    }

    public static class Authorization {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isWithCredentials() {
        return withCredentials;
    }

    public void setWithCredentials(boolean withCredentials) {
        this.withCredentials = withCredentials;
    }

    public Authorization getAuth() {
        return auth;
    }

    public void setAuth(Authorization auth) {
        this.auth = auth;
    }

    public ProxyConfig getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfig proxy) {
        this.proxy = proxy;
    }

    public Closure<?> getTransformRequest() {
        return transformRequest;
    }

    public void setTransformRequest(Closure<?> transformRequest) {
        this.transformRequest = transformRequest;
    }

    public Closure<?> getTransformResponse() {
        return transformResponse;
    }

    public void setTransformResponse(Closure<?> transformResponse) {
        this.transformResponse = transformResponse;
    }
}
