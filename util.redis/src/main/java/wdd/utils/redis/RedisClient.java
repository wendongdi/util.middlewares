package wdd.utils.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import wdd.utils.commons.AppConfig;
import wdd.utils.commons.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class RedisClient {
    private static RedisClient instance;
    public JedisCluster jedisCluster;

    public RedisClient() throws IOException {
        Properties properties = AppConfig.instance().allProperties();
        if (!properties.containsKey("redis.cluster.nodes"))
            properties = AppConfig.listProperties("redis-config.properties");
        JedisPoolConfig config = new JedisPoolConfig();
        int maxTotal = Integer.valueOf(properties.getProperty("redis.cluster.max_total", "200"));
        config.setMaxTotal(maxTotal);
        int maxIdle = Integer.valueOf(properties.getProperty("redis.cluster.max_idle", "20"));
        config.setMaxIdle(maxIdle);
        int maxWait = Integer.valueOf(properties.getProperty("redis.cluster.max_wait", "1000"));
        config.setMaxWaitMillis(maxWait);
        config.setTestOnBorrow(false);
        config.setTestWhileIdle(true);
        config.setMinEvictableIdleTimeMillis(120000);
        config.setTimeBetweenEvictionRunsMillis(120000);
        config.setNumTestsPerEvictionRun(-1);
        config.setBlockWhenExhausted(false);

        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        String[] hnps = properties.getProperty("redis.cluster.nodes").split(",");
        for (String hnp : hnps) {
            String[] hps = hnp.split(":");
            if (hps.length == 2) {
                nodes.add(new HostAndPort(hps[0], Integer.parseInt(hps[1])));
            }
        }

        int timeout = Integer.valueOf(properties.getProperty("redis.cluster.timeout", "1000"));
        jedisCluster = new JedisCluster(nodes, timeout, config);
    }

    public void add(String key, String data, int Expiry) {
        if (StringUtils.isEmpty(key)) {
            jedisCluster.set(key, data);
            jedisCluster.expire(key, Expiry);
        }
    }

    public void get(String key) {
        if (StringUtils.isEmpty(key))
            jedisCluster.smembers(key);
    }

    public static RedisClient instance() throws IOException {
        if (instance == null || instance.jedisCluster == null) {
            synchronized (RedisClient.class) {
                if (instance == null || instance.jedisCluster == null) {
                    instance = new RedisClient();
                }
            }
        }
        return instance;
    }

    public void close() {
        if (jedisCluster != null) {
            jedisCluster.close();
            jedisCluster = null;
        }
    }
}
