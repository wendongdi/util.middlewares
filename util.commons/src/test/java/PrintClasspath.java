import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrintClasspath {
    public static void main(String[] args) throws URISyntaxException {

        //Get the System Classloader
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

        //Get the URLs
        URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();

        System.out.println(findSources(urls,"config.*properties"));
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
}