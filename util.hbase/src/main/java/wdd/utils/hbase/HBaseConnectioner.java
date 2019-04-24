package wdd.utils.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wdd.utils.commons.AppConfig;
import wdd.utils.commons.StringUtils;
import wdd.utils.hbase.exception.HBaseConnectionException;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class HBaseConnectioner {
    private static Logger log = LoggerFactory.getLogger(HBaseConnectioner.class);

    public static Connection getConnection() throws HBaseConnectionException {
        try {
            if (AppConfig.instance().getProperty("hbase.zookeeper.quorum") == null) {
                AppConfig.instance().addProperties("hbase-config.properties");
            }
        } catch (Exception ignored) {
        }

        Configuration configuration = HBaseConfiguration.create();
        Properties properties = AppConfig.instance().allProperties();
        Enumeration<?> props = properties.propertyNames();
        String remoteUser = "";
        while (props.hasMoreElements()) {
            String key = props.nextElement().toString();
            if (key.equals("remoteUser")) {
                remoteUser = properties.getProperty(key);
                continue;
            }
            configuration.set(key, properties.getProperty(key));
        }
        try {

            if (StringUtils.nonEmpty(remoteUser)) {
                User user = User.create(UserGroupInformation.createRemoteUser(remoteUser));
                return ConnectionFactory.createConnection(configuration, user);

            } else {
                return ConnectionFactory.createConnection(configuration);
            }
        } catch (IOException e) {
            throw new HBaseConnectionException(e);
        }
    }
}
