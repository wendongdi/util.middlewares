package wdd.utils.hive;

import wdd.utils.commons.AppConfig;
import wdd.utils.commons.BeansUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class HiveExecutor {
    private Connection connection;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private Properties properties;

    public HiveExecutor() throws ClassNotFoundException, SQLException, IOException {
        properties = AppConfig.listProperties("hive-config.properties");
        Class.forName(properties.getProperty("jdbc.driverClassName"));
        this.connection = DriverManager.getConnection(properties.getProperty("jdbc.console.url"), properties.getProperty("jdbc.common.username"), properties.getProperty("jdbc.common.password"));
    }

    public <T> List<T> query(String sql, Class<? extends T> clazz) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException {
        ResultSet rs = connection.createStatement().executeQuery(sql);
        Map<String, Class> fields = BeansUtil.typeFields(clazz);
        List<T> ress = new ArrayList<>();
        while (rs.next()) {
            T t = clazz.newInstance();
            for (String field : fields.keySet()) {
                Class f_class = fields.get(field);
                Object o = null;
                try {
                    o = rs.getObject(field);
                } catch (Exception ignored) {
                }
                if (o != null) {
                    if (f_class.equals(Date.class)) {
                        BeansUtil.copy2Field(t, field, rs.getTimestamp(field));
                    } else {
                        BeansUtil.copy2Field(t, field, rs.getObject(field));
                    }
                }

            }
            ress.add(t);
        }
        return ress;
    }

    public ResultSet query(String sql) throws SQLException {
        return connection.createStatement().executeQuery(sql);
    }


    public boolean execute(String sql) throws SQLException {
        return connection.createStatement().execute(sql);
    }

    public void close() throws SQLException {
        connection.close();
    }
}
