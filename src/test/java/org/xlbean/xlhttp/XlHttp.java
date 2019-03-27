package org.xlbean.xlhttp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.xlbean.XlBean;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class XlHttp {

    private static final MediaType DEFAULT_MEDIATYPE = MediaType.get("application/json; charset=utf-8");

    public static Jsons JSON = new Jsons();

    public XlResponse request(RequestConfig config) {
        Request.Builder builder = new Request.Builder();
        builder
            .url(createUrlWithQueryParams(config, builder))
            .method(config.getMethod(), getRequestBody(config));
        setHeader(config, builder);

        Request request = builder.build();

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        setProxy(config, clientBuilder);
        OkHttpClient client = clientBuilder.build();
        try {
            Response response = client.newCall(request).execute();
            return toXlResponse(config, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setProxy(RequestConfig config, OkHttpClient.Builder builder) {
        if (config.getProxy() == null || config.getProxy().getHost() == null || config.getProxy().getPort() == 0) {
            return;
        }
        SocketAddress address = new InetSocketAddress(config.getProxy().getHost(), config.getProxy().getPort());
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);

        if (config.getProxy().getAuth() != null) {
            builder.proxyAuthenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(
                        config.getProxy().getAuth().getUsername(),
                        config.getProxy().getAuth().getPassword());
                    return response
                        .request()
                        .newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
                }
            });
        }

        builder.proxy(proxy);
    }

    private void setAuthentication(RequestConfig config, Request.Builder builder) {
        if (config.getAuth() != null) {

        }
    }

    private void setHeader(RequestConfig config, Request.Builder builder) {
        if (config.getHeaders() == null) {
            return;
        }
        config
            .getHeaders()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null)
            .forEach(entry -> builder.addHeader(entry.getKey(), entry.getValue().toString()));
    }

    private String createUrlWithQueryParams(RequestConfig config, Request.Builder builder) {
        Map<String, Object> queryParams = config.getParams();
        if (queryParams == null) {
            return config.getAbsoluteUrl();
        }
        HttpUrl.Builder httpBuilder = HttpUrl.parse(config.getAbsoluteUrl()).newBuilder();
        queryParams
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null)
            .forEach(entry -> httpBuilder.addQueryParameter(entry.getKey(), entry.getValue().toString()));
        return httpBuilder.build().toString();
    }

    @SuppressWarnings("unchecked")
    private RequestBody getRequestBody(RequestConfig config) {
        Object data = config.getData();
        RequestBody body = null;
        if (data instanceof Map) {
            body = createRequestBody(config.getContentType(), (Map<String, Object>) data);
        } else if (data instanceof String) {
            body = createRequestBody(config.getContentType(), (String) data);
        } else {
            // body = null
        }
        return body;
    }

    private XlResponse toXlResponse(RequestConfig config, Response response) throws IOException {
        XlResponse res = new XlResponse();
        Object responseBody = null;
        if (config.getTransformResponse() != null) {
            responseBody = config.getTransformResponse().call(response.body().string());
        } else {
            responseBody = response.body().string();
        }
        res.setData(responseBody);
        res.setStatus(response.code());
        res.setStatusText(response.message());
        res.setHeaders(
            response
                .headers()
                .toMultimap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        return res;
    }

    private RequestBody createRequestBody(String mediaTypeStr, Map<String, Object> data) {
        return createRequestBody(mediaTypeStr, JSON.stringify(data));
    }

    private RequestBody createRequestBody(String mediaTypeStr, String data) {
        MediaType mediaType = MediaType.get(mediaTypeStr);
        if (mediaType == null) {
            mediaType = DEFAULT_MEDIATYPE;
        }
        return RequestBody.create(mediaType, data);
    }

    private RequestBody createFormBody(Map<String, Object> data) {
        final FormBody.Builder formBuilder = new FormBody.Builder();
        data
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null)
            .forEach(entry -> formBuilder.add(entry.getKey(), entry.getValue().toString()));
        return formBuilder.build();
    }

    public XlResponse request(XlBean request) {
        RequestConfig config = request.of(RequestConfig.class);
        return request(config);
    }

}
