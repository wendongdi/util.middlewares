package wdd.utils.mysql;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import wdd.utils.commons.AppConfig;

import java.io.IOException;
import java.sql.SQLException;

public class JDBCTemplateInstance {
    private static JdbcTemplate instance = null;
    private static BasicDataSource dataSource = null;

    public static JdbcTemplate instance() throws IOException {
        if (instance == null) {
            synchronized (JDBCTemplateInstance.class) {
                if (null == instance) {
                    init();
                }
            }
        }
        return instance;
    }

    private static void init() throws IOException {
        if (!AppConfig.instance().allProperties().containsKey("jdbc.console.url"))
            AppConfig.instance().addProperties("mysql-config.properties");
        if (null == dataSource)
            dataSource = new BasicDataSource() {{
                setDriverClassName(AppConfig.instance().getProperty("jdbc.driverClassName"));
                setUrl(AppConfig.instance().getProperty("jdbc.console.url"));
                setUsername(AppConfig.instance().getProperty("jdbc.common.username"));
                setPassword(AppConfig.instance().getProperty("jdbc.common.password"));
                setMaxActive(AppConfig.instance().getIntegerProperty("jdbc.maxActive", 100000));
                setMaxIdle(AppConfig.instance().getIntegerProperty("jdbc.maxIdle", 30));
                setMaxWait(AppConfig.instance().getIntegerProperty("jdbc.maxWait", 100000));
                setDefaultAutoCommit(AppConfig.instance().getBooleanProperty("jdbc.defaultAutoCommit", true));
                setValidationQuery("SELECT 1");
                setTestOnBorrow(true);
            }};
        instance = new JdbcTemplate(dataSource);
    }

    public static void close() throws SQLException {
        if (null != dataSource)
            dataSource.close();
    }
}
