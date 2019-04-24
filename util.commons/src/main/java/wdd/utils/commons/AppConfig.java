package wdd.utils.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static AppConfig instance = null;
    private Properties properties = new Properties();

    public void addProperties(Properties newProperties) {
        properties.putAll(newProperties);
    }

    public static String[] findConfigs(String re) {
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();
        return findSources(urls, "config.*properties").toArray(new String[0]);
    }

    public static Set<String> findSources(String url, String re) {
        Set<String> meets = new HashSet<>();
        File path_or_file = new File(url);
        if (path_or_file.isDirectory()) {
            String[] paths = path_or_file.list();
            if (paths != null && paths.length > 0)
                for (String path : paths) {
                    meets.addAll(findSources(path, re));
                }

        } else {
            if (url.matches(re))
                meets.add(url);
        }
        return meets;
    }

    public static Set<String> findSources(String url) {
        String re = "^.+$";
        return findSources(url, re);
    }

    public static Set<String> findSources(URL[] urls) {
        String re = "^.+$";
        return findSources(urls, re);
    }

    public static Set<String> findSources(URL[] urls, String re) {
        Set<String> sources = new HashSet<>();
        for (URL url : urls) {
            sources.addAll(findSources(url.getFile(), re));
        }
        return sources;
    }

    public void addProperties(String path) {
        try {
            properties.putAll(listProperties(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AppConfig() {
        try {
            Set<String> resources = new HashSet<>(Arrays.asList(findConfigs("config.*\\.properties")));
            resources.add("config.properties");
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            for (String resource : resources) {
                InputStream inputstream = loader.getResourceAsStream(resource);
                if (inputstream != null)
                    this.properties.load(inputstream);
            }
        } catch (IOException e) {
            logger.error("wdd.middleware.util.AppConfig - constructor");
        }
    }

    public static Properties listProperties(String configFile) throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        InputStream inputstream = loader.getResourceAsStream(configFile);
        if (inputstream != null) {
            try {
                properties.load(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.error("wdd.middleware.util.AppConfig - listProperties");
            }
        } else {
            throw new IOException("file '" + configFile + "' is not existed");
        }
        return properties;

    }

    public static AppConfig instance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (null == instance) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        System.out.println(AppConfig.instance().properties);
    }

    public String getProperty(String k) {
        return this.properties.getProperty(k);
    }

    public String getProperty(String k, String v1) {
        String v = this.properties.getProperty(k);
        return v == null ? v1 : v;
    }

    public String[] getProperties(String k, String d) {
        String v = (String) this.properties.get(k);
        return k.split(d);
    }

    public int getIntegerProperty(String k) {
        String v = getProperty(k);
        return v == null ? 0 : Integer.parseInt(v);
    }

    public byte getByteProperty(String k) {
        String v = getProperty(k);
        return v == null ? 0 : Byte.parseByte(v);
    }

    public boolean getBooleanProperty(String k) {
        String v = getProperty(k);
        return Boolean.parseBoolean(v);
    }

    public boolean getBooleanProperty(String k, boolean v1) {
        String v = getProperty(k);
        return v == null ? v1 : Boolean.parseBoolean(v);
    }

    public double getDoubleProperty(String k) {
        String v = getProperty(k);
        return v == null ? 0.0D : Double.parseDouble(v);
    }

    public int getIntegerProperty(String k, int d) {
        String v = getProperty(k);
        return v == null ? d : Integer.parseInt(v);
    }

    public long getLongProperty(String k, long d) {
        String v = getProperty(k);
        return v == null ? d : Long.parseLong(v);
    }

    public Enumeration<?> propertyNames() {
        return properties.propertyNames();
    }

    public Properties allProperties() {
        return properties;
    }
}