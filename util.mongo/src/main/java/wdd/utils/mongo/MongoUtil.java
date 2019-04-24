package wdd.utils.mongo;

import java.io.IOException;

public class MongoUtil {
    private static MongoClient client;

    public static MongoClient instance() throws IOException {
        if (client == null) {
            synchronized (MongoUtil.class) {
                if (null == client) {
                    client = new MongoClient();
                }
            }
        }
        return client;
    }

    public static void close() {
        client.close();
    }
}
