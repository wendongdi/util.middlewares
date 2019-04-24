package wdd.utils.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wdd.utils.commons.StringUtils;
import wdd.utils.http.exception.ApiTimeOutException;
import wdd.utils.http.exception.HttpException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求发送器
 */
public class HttpRequest {
    private static Logger logger = LoggerFactory.getLogger(HttpRequest.class);
    private static int defalut_MaxThreads = 8000;
    private static int defalut_MaxTimeout = 5000;
    private int MaxThreads;
    private int MaxTimeout;
    private static HttpRequest instance;
    private HttpClient client;
    private List<Header> headers = new ArrayList<>();

    public HttpRequest addHeader(String name, String value) {
        headers.add(new BasicHeader(name, value));
        return this;
    }

    private HttpRequest() {
        init(defalut_MaxThreads, defalut_MaxTimeout);
    }

    private HttpRequest(int tMaxThreads, int tMaxTimeout) {
        init(tMaxThreads, tMaxTimeout);
    }

    public static HttpRequest instance() {
        if (instance == null)
            synchronized (HttpRequest.class) {
                if (instance == null)
                    instance = new HttpRequest();
            }
        return instance;
    }

    private void init(int tMaxThreads, int tMaxTimeout) {
        MaxThreads = tMaxThreads;
        MaxTimeout = tMaxTimeout;
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");
        HttpProtocolParams.setUseExpectContinue(params, true);
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, tMaxTimeout);
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, tMaxTimeout);

        SchemeRegistry schreg = new SchemeRegistry();
        schreg.register(new Scheme("wdd/utils/http", 80, PlainSocketFactory.getSocketFactory()));
        schreg.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
        PoolingClientConnectionManager pccm = new PoolingClientConnectionManager(schreg);
        pccm.setDefaultMaxPerRoute(tMaxThreads); //每个主机的最大并行链接数
        pccm.setMaxTotal(tMaxThreads);          //客户端总并行链接最大数

        client = new DefaultHttpClient(pccm, params);
    }

    public static HttpRequest instance(int MaxThreads, int MaxTimeout) {
        instance.close();
        if (instance == null)
            synchronized (HttpRequest.class) {
                if (instance == null)
                    instance = new HttpRequest(MaxThreads, MaxTimeout);
            }
        return instance;
    }

    private static URI getUri(String href) {
        try {
            if (StringUtils.nonEmpty(href)) return new URL(href).toURI();
        } catch (Exception e) {
            logger.error("HttpRequest - getUri " + e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 发送参数格式的请求
     *
     * @param url
     * @param params
     * @return
     */
    public String post(String url, final Map<String, String> params) throws IOException, ApiTimeOutException, HttpException {
        return post(url, new ArrayList<NameValuePair>() {{
            for (Map.Entry<String, String> entry : params.entrySet())
                add(new BasicNameValuePair(entry.getKey(), entry.getValue()));

        }});
    }

    public String post(String url, List<NameValuePair> params) throws IOException, ApiTimeOutException, HttpException {
        String result = "";
        HttpPost request = postRequest(url);
        try {
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");

            request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

        } catch (Exception e) {
            logger.error("HttpRequest - send " + e.getLocalizedMessage());
        }
        result = excute(request, "utf-8");
        return result;
    }

    /**
     * 发送JSON格式的请求
     *
     * @param url
     * @param json
     * @return
     */
    public String send(String url, Map json) throws IOException, ApiTimeOutException, HttpException {
        String result = "";
        HttpPost request = postRequest(url);
        try {
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");

            request.setHeader("Content-Type", "application/json;charset=utf-8");
            request.setHeader("Accept", "application/json;charset=utf-8");
            request.setEntity(new StringEntity(JSON.toJSONString(json), "utf-8"));
        } catch (Exception e) {
            logger.error("HttpRequest - send " + e.getLocalizedMessage());
        }
        result = excute(request, "utf-8");
        return result;
    }

    private String excute(HttpUriRequest request, String encode) throws IOException, ApiTimeOutException, HttpException {
        String result = "";
        HttpResponse response = execute(request);
        if (response != null) {
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine() != null)
                if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                    result = EntityUtils.toString(entity, encode);
                    EntityUtils.consume(entity);
                } else {
                    logger.error(request.getURI() + " " + response.getStatusLine().getStatusCode());
                    EntityUtils.consume(entity);
                    throw new HttpException(response.getStatusLine());
                }
        }
        return result;
    }

    private HttpResponse execute(HttpUriRequest request) throws IOException, ApiTimeOutException {
        try {
            for (Header header : headers) {
                request.addHeader(header);
            }
            return client.execute(request);
        } catch (ConnectTimeoutException | SocketTimeoutException te) {
            throw new ApiTimeOutException(request.getURI().toString(), te.getLocalizedMessage(), MaxTimeout);
        }
    }

    private byte[] excute(HttpUriRequest request) throws IOException, ApiTimeOutException {
        byte[] result = null;
        HttpResponse response = execute(request);
        if (response != null) {
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine() != null)
                if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                    result = EntityUtils.toByteArray(entity);
                } else {
                    logger.error(request.getURI() + " " + response.getStatusLine().getStatusCode());
                }
            EntityUtils.consume(entity);
        }
        return result;
    }

    /**
     * 发送JSON格式的请求
     *
     * @param url
     * @param json
     * @return
     */
    public String send(String url, JSONObject json) throws IOException, ApiTimeOutException, HttpException {
        return send(url, json.toJSONString());
    }

    public String send(String url, String json) throws IOException, ApiTimeOutException, HttpException {
        String result = "";
        HttpPost request = postRequest(url);

        try {
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");

            request.setHeader("Content-Type", "application/json;charset=utf-8");
            request.setHeader("Accept", "application/json;charset=utf-8");
            request.setEntity(new StringEntity(json, "utf-8"));

        } catch (Exception e) {
            logger.error("HttpRequest - send " + e.getLocalizedMessage());
        }
        result = excute(request, "utf-8");

        return result;
    }

    public String sendForm(String url, Map<String, String> json) throws IOException, ApiTimeOutException, HttpException {
        String result = "";
        HttpPost request = postRequest(url);
        try {
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            request.setHeader("Accept", "application/json;charset=utf-8");
            StringBuilder data = new StringBuilder();
            for (Map.Entry<String, String> entry : json.entrySet()) {
                if (!data.toString().equals(""))
                    data.append("&");
                data.append(entry.getKey()).append('=').append(entry.getValue());
            }
            request.setEntity(new StringEntity(data.toString(), "utf-8"));
        } catch (Exception e) {
            logger.error("HttpRequest - send " + e.getLocalizedMessage());
        }
        result = excute(request, "utf-8");
        return result;
    }

    /**
     * 发送PROTOBUF格式的请求
     *
     * @param url
     * @param data
     * @return
     */
    public byte[] send(String url, byte[] data) {
        byte[] result = null;
        try {
            HttpPost request = postRequest(url);
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");

            request.setHeader("Content-Type", "application/x-protobuf");
            request.setEntity(new ByteArrayEntity(data));

            result = excute(request);

        } catch (Exception e) {
            logger.error("HttpRequest - send " + e.getLocalizedMessage());
        }
        return result;
    }

    public byte[] send(String url, byte[] data, String token) {
        byte[] result = null;
        try {
            HttpPost request = postRequest(url);
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");

            request.setHeader("token", token);
            request.setHeader("Content-Type", "application/x-protobuf");
            request.setEntity(new ByteArrayEntity(data));
            result = excute(request);
        } catch (Exception e) {
            logger.error("HttpRequest - send " + e.getLocalizedMessage());
        }
        return result;
    }

    public String get(String url) throws IOException, ApiTimeOutException, HttpException {
        return get(url, null);
    }

    public String get(String url, Map<String, String> headers) throws IOException, ApiTimeOutException, HttpException {
        String result = "";
        HttpGet request = getRequest(url, headers);
        try {
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");

        } catch (Exception e) {
            e.printStackTrace();
        }
        result = excute(request, "utf-8");

        return result;
    }

    protected HttpPost postRequest(String url) {
        HttpPost request = null;
        try {
            if (StringUtils.nonEmpty(url)) {
                URI uri = getUri(url);
                if (uri != null) {
                    request = new HttpPost(uri);
                    request.setHeader("Connection", "Keep-Alive");
                }
            }
        } catch (Exception e) {
            logger.error("HttpRequest - postRequest " + e.getLocalizedMessage());
        }
        return request;
    }

    protected HttpGet getRequest(String url, Map<String, String> headers) {
        HttpGet request = null;
        try {
            if (StringUtils.nonEmpty(url)) {
                URI uri = getUri(url);
                if (uri != null) {
                    request = new HttpGet(uri);
                    request.setHeader("Connection", "Keep-Alive");
                    if (headers != null && headers.size() > 0) {
                        for (Map.Entry<String, String> header : headers.entrySet()) {
                            request.setHeader(header.getKey(), header.getValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }

    public void close() {
        if (instance == null || client == null) {
            return;
        }
        HttpClientUtils.closeQuietly(client);
        instance = null;
    }

    public static void main(String[] args) throws ApiTimeOutException, IOException, HttpException {
        System.out.println(HttpRequest.instance().get("http://192.168.10.60:9380/dataexchange/query/devicebrands"));
    }
}