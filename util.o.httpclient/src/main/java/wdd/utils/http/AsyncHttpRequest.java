package wdd.utils.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wdd.utils.commons.StringUtils;
import wdd.utils.http.exception.HttpException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * HTTP请求发送器
 */
public class AsyncHttpRequest {
    private static Logger logger = LoggerFactory.getLogger(AsyncHttpRequest.class);
    private DefaultHttpAsyncClient client;
    private int maxThreads = 10000;
    private int maxTimeout = 1000;
    private FutureCallback<HttpResponse> callback;
    private CountDownLatch latch;

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public AsyncHttpRequest(FutureCallback<HttpResponse> callback, int tMaxThreads, int tMaxTimeout) throws IOReactorException {
        maxThreads = tMaxThreads;
        maxTimeout = tMaxTimeout;
        init(callback);
    }

    public AsyncHttpRequest(FutureCallback<HttpResponse> callback, CountDownLatch latch, int tMaxThreads, int tMaxTimeout) throws IOReactorException {
        maxThreads = tMaxThreads;
        maxTimeout = tMaxTimeout;
        this.latch = latch;
        init(callback);
    }

    private void init(FutureCallback<HttpResponse> tcallback) throws IOReactorException {
        callback = tcallback;
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");
        HttpProtocolParams.setUseExpectContinue(params, true);
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, maxTimeout);
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, maxTimeout);

        PoolingClientAsyncConnectionManager pccm = new PoolingClientAsyncConnectionManager(new DefaultConnectingIOReactor());
        pccm.setDefaultMaxPerRoute(maxThreads); //每个主机的最大并行链接数
        pccm.setMaxTotal(maxThreads);          //客户端总并行链接最大数

        client = new DefaultHttpAsyncClient(pccm);
        client.setParams(params);
    }

    public AsyncHttpRequest(FutureCallback<HttpResponse> callback, CountDownLatch latch) throws IOReactorException {
        this.latch = latch;
        init(callback);
    }

    /**
     * 发送参数格式的请求
     *
     * @param url
     * @param params
     * @return
     */
    public Future<HttpResponse> post(String url, final Map<String, String> params) {
        return post(url, new ArrayList<NameValuePair>() {{
            for (Map.Entry<String, String> entry : params.entrySet())
                add(new BasicNameValuePair(entry.getKey(), entry.getValue()));

        }});
    }

    public Future<HttpResponse> post(String url, List<NameValuePair> params) {
        String result = "";
        HttpPost request = postRequest(url);
        try {
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");

            request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

        } catch (Exception e) {
            logger.error("HttpRequest - send " + e.getLocalizedMessage());
        }
        return prepareRequest(request);
    }

    /**
     * 发送JSON格式的请求
     *
     * @param url
     * @param json
     * @return
     */
    public Future<HttpResponse> send(String url, Map json) {
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
        return prepareRequest(request);
    }

    public static String excute(Future<HttpResponse> f_response, String encode) throws IOException, HttpException, ExecutionException, InterruptedException {
        String result = "";
        HttpResponse response = f_response.get();
        if (response != null) {
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine() != null)
                if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                    result = EntityUtils.toString(entity, encode);
                    EntityUtils.consume(entity);
                } else {
                    logger.error(String.valueOf(response.getStatusLine().getStatusCode()));
                    EntityUtils.consume(entity);
                    throw new HttpException(response.getStatusLine());
                }
        }
        return result;
    }

    private Future<HttpResponse> prepareRequest(HttpUriRequest request) {
        return client.execute(request, callback);
    }

    public static byte[] excute(Future<HttpResponse> f_response) throws IOException, ExecutionException, InterruptedException {
        byte[] result = null;
        HttpResponse response = f_response.get();
        if (response != null) {
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine() != null)
                if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                    result = EntityUtils.toByteArray(entity);
                } else {
                    logger.error(String.valueOf(response.getStatusLine().getStatusCode()));
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
    public Future<HttpResponse> send(String url, JSONObject json) {
        return send(url, json.toJSONString());
    }

    public Future<HttpResponse> send(String url, String json) {
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
        return prepareRequest(request);
    }

    public Future<HttpResponse> sendForm(String url, Map<String, String> json) {
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
        return prepareRequest(request);
    }

    /**
     * 发送PROTOBUF格式的请求
     *
     * @param url
     * @param data
     * @return
     */
    public Future<HttpResponse> send(String url, byte[] data) {
        Future<HttpResponse> result = null;
        try {
            HttpPost request = postRequest(url);
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");

            request.setHeader("Content-Type", "application/x-protobuf");
            request.setEntity(new ByteArrayEntity(data));

            result = prepareRequest(request);

        } catch (Exception e) {
            logger.error("HttpRequest - send " + e.getLocalizedMessage());
        }
        return result;
    }

    public Future<HttpResponse> send(String url, byte[] data, String token) {
        Future<HttpResponse> result = null;
        try {
            HttpPost request = postRequest(url);
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");

            request.setHeader("token", token);
            request.setHeader("Content-Type", "application/x-protobuf");
            request.setEntity(new ByteArrayEntity(data));
            result = prepareRequest(request);
        } catch (Exception e) {
            logger.error("HttpRequest - send " + e.getLocalizedMessage());
        }
        return result;
    }

    public Future<HttpResponse> get(String url) {
        return get(url, null);
    }

    public Future<HttpResponse> get(String url, Map<String, String> headers) {
        String result = "";
        HttpGet request = getRequest(url, headers);
        try {
            if (request == null) throw new Exception("invalid request URI (" + (url == null ? "NULL" : url) + ")");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return prepareRequest(request);
    }

    protected HttpPost postRequest(String url) {
        HttpPost request = null;
        try {
            if (StringUtils.nonEmpty(url)) {
                request = new HttpPost(url);
                request.setHeader("Connection", "Keep-Alive");
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
                request = new HttpGet(url);
                request.setHeader("Connection", "Keep-Alive");
                if (headers != null && headers.size() > 0) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        request.setHeader(header.getKey(), header.getValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }

    public void close() {
        try {
            client.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client = null;
    }


    public void wait_close() {
        try {
            if (latch != null)
                latch.wait(latch.getCount() * 10000);
            client.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client = null;
    }
}