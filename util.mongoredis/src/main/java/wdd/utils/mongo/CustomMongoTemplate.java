package wdd.utils.mongo;

import com.mongodb.MongoClient;
import com.mongodb.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import wdd.utils.commons.AppConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CustomMongoTemplate {
    private static MongoTemplate instance = null;

    public static MongoTemplate instance() {
        if (instance == null) {
            synchronized (CustomMongoTemplate.class) {
                if (null == instance) {
                    init();
                }
            }
        }
        return instance;
    }

    private static void init() {
        MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder()
                .connectionsPerHost(50)
                .threadsAllowedToBlockForConnectionMultiplier(10)
                .connectTimeout(1000)
                .maxWaitTime(1500)
                .socketTimeout(250000)
                .readPreference(ReadPreference.nearest())
                .writeConcern(WriteConcern.UNACKNOWLEDGED.withW(1).withWTimeout(0, TimeUnit.MILLISECONDS).withJournal(true))
                .build();
        List<ServerAddress> serverAddresses = new ArrayList<>();
        for (String hostport : AppConfig.instance().getProperty("mongo.console.url")
                .split(",")
                ) {
            serverAddresses.add(new ServerAddress(hostport));
        }

        instance = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(serverAddresses, mongoClientOptions),
                AppConfig.instance().getProperty("mongo.console.dbname", "dmp-data")));
    }
}