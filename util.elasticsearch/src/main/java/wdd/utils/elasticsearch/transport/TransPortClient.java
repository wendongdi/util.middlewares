package wdd.utils.elasticsearch.transport;

import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import wdd.middleware.util.AppConfig;
import wdd.utils.elasticsearch.annotation.Document;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransPortClient {
    private static TransportClient client = null;

    public TransPortClient() throws UnknownHostException {
        client = init();
    }

    private static TransportClient init() throws UnknownHostException {
        String clusterName = AppConfig.instance().getProperty("es.cluster.name", "zhiziyun");
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", clusterName).put("client.transport.sniff", true).build();
        String hosts = AppConfig.instance().getProperty("es.hosts");
        TransportClient client = TransportClient.builder().settings(settings).build();
        for (String host : hosts.split(",")) {
            String[] hp = host.split(":");
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hp[0]), Integer.valueOf(hp[1])));
        }
        return client;
    }

    public static TransPortClient instance() throws UnknownHostException {
        if (client == null) {
            synchronized (TransPortClient.class) {
                if (null == client) {
                    client = init();
                }
            }
        }
        return new TransPortClient();
    }

    public <T> List<T> scanAll(String query, Class<? extends T> clazz) throws UnknownHostException, IllegalAccessException, InstantiationException {
        Document doc = clazz.getAnnotation(Document.class);
        List<String> fields = new ArrayList<String>();
        for (Field field : clazz.getDeclaredFields()) {
            fields.add(field.getName());
        }
        SearchResponse scrollResp = client.prepareSearch(doc.index()).setTypes(doc.type()).setQuery(query)
                .setSearchType(SearchType.QUERY_AND_FETCH).setScroll(new TimeValue(doc.scroll())).setSize(5000).setFetchSource(fields.toArray(new String[0]), null)
                .execute().actionGet();

        ArrayList<T> ress = new ArrayList<T>();
        do {
            for (SearchHit hit : scrollResp.getHits()) {
                Map source = hit.getSource();
                T res = clazz.newInstance();
                try {
                    BeanUtils.copyProperty(res, "id", hit.getId());
                } catch (InvocationTargetException ignored) {
                }
                for (String field : fields) {
                    try {
                        BeanUtils.copyProperty(res, field, source.get(field));
                    } catch (InvocationTargetException ignored) {
                    }
                }
                ress.add(res);
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(doc.scroll())).execute().actionGet();
        } while (scrollResp.getHits().getHits().length != 0);
        return ress;
    }

    public void close() {
        client.close();
    }
}
