package com.mosl.requester.http;

import com.mosl.requester.constant.HTTP;
import com.mosl.requester.util.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mosl on 2017/1/19.
 */
public class Requester {

    private Method method;
    private String baseUrl;
    private final List<Entry> args = new ArrayList<Entry>();
    private CloseableHttpClient client;

    public Requester(String baseUrl) {
        this.baseUrl = baseUrl;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
        client = HttpClientBuilder.create().setDefaultRequestConfig(defaultRequestConfig).build();
    }

    public Requester method(Method method) {
        this.method = method;
        return this;
    }

    public Requester with(String key, int value) {
        return _with(key, value);
    }

    public Requester with(String key, Integer value) {
        if (value != null)
            _with(key, value);
        return this;
    }

    public Requester with(String key, String value) {
        return _with(key, value);
    }

    public Requester with(Map<String, Object> params) {
        if (params != null) {
            for (String key : params.keySet()) {
                args.add(new Entry(key, String.valueOf(params.get(key))));
            }
        }
        return this;
    }

    private Requester _with(String key, Object value) {
        if (value != null) {
            args.add(new Entry(key, value));
        }
        return this;
    }

    public <T> T to(String tailApiUrl, Class<T> type) throws IOException {
        return _to(tailApiUrl, type);
    }

    private <T> T _to(String tailApiUrl, Class<T> type) throws IOException {
        if (this.method == Method.GET) {
            String url = this.baseUrl + tailApiUrl + "?" + urlEncode(this.args);
            HttpGet httpGet = new HttpGet(url);
            String json = execute(httpGet);
            return JsonUtil.fromJson(json, type);
        } else if (this.method == Method.POST) {
            String url = this.baseUrl + tailApiUrl;
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            for (Entry entry : this.args) {
                params.add(new BasicNameValuePair(entry.key, String.valueOf(entry.value)));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            String json = execute(httpPost);
            return JsonUtil.fromJson(json, type);
        } else if (this.method == Method.PUT) {
            String url = this.baseUrl + tailApiUrl;
            HttpPut httpPut = new HttpPut(url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            for (Entry entry : this.args) {
                params.add(new BasicNameValuePair(entry.key, String.valueOf(entry.value)));
            }
            httpPut.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            String json = execute(httpPut);
            return JsonUtil.fromJson(json, type);
        } else if (this.method == Method.DELETE) {
            String url = this.baseUrl + tailApiUrl + "?" + urlEncode(this.args);
            HttpDelete httpDelete = new HttpDelete(url);
            String json = execute(httpDelete);
            return JsonUtil.fromJson(json, type);
        }
        return null;
    }

    private String execute(HttpUriRequest request) throws IOException {
        HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = response.getEntity();
            return getStrFromInputStream(entity.getContent());
        }
        return null;
    }

    private static String getStrFromInputStream(InputStream inputStream) {
        try {
            return IOUtils.toString(inputStream, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * encode url
     *
     * @param entries
     * @return
     */
    private String urlEncode(List<Entry> entries) {
        String retVal = "";
        for (Entry entry : entries) {
            try {
                String encodeValue = URLEncoder.encode(String.valueOf(entry.value), "UTF8");
                retVal += entry.key + "=" + encodeValue + "&";
            } catch (UnsupportedEncodingException ex) {
            }
        }
        return retVal;
    }

    private static class Entry {
        String key;
        Object value;

        private Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }


}
