package wdd.utils.mongo;

import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import wdd.utils.commons.AppConfig;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MongoAccessor {
    private static com.mongodb.MongoClient client = null;

    public static com.mongodb.MongoClient instance() throws UnknownHostException {
        if (client == null) {
            synchronized (MongoAccessor.class) {
                if (null == client) {
                    client = createClient();
                }
            }
        }
        return client;
    }

    public static void close() {
        if (null != client)
            client.close();
    }

    public static com.mongodb.MongoClient createClient() throws UnknownHostException {
        if (AppConfig.instance().getProperty("mongo.console.url") == null) {
            AppConfig.instance().addProperties("mongo-config.properties");
        }
        MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder()
                .connectionsPerHost(50)
                .threadsAllowedToBlockForConnectionMultiplier(10)
                .connectTimeout(5000)
                .maxWaitTime(1500)
                .socketTimeout(5000)
                .readPreference(ReadPreference.nearest())
                .writeConcern(new WriteConcern(1, 0, true))
                .build();
        List<ServerAddress> serverAddresses = new ArrayList<>();
        for (String hostport : AppConfig.instance().getProperty("mongo.console.url")
                .split(",")
                ) {
            String[] hp = hostport.split(":");
            if (hp.length == 1)
                serverAddresses.add(new ServerAddress(hostport));
            else
                serverAddresses.add(new ServerAddress(hp[0], Integer.parseInt(hp[1])));
        }
        return new com.mongodb.MongoClient(serverAddresses, mongoClientOptions);
    }
}