
import ch.hsr.geohash.GeoHash;
import wdd.utils.commons.AppConfig;

import java.io.IOException;
import java.util.*;

/**
 * created by wdd
 */
public class GeoHashTest {
    public static void main(String[] args) throws IOException {
        Properties geo_yx = AppConfig.listProperties("geo-loc.properties");

        Map<String, List<String>> city_hash = new HashMap<>();

        for (String city : geo_yx.stringPropertyNames()) {
            String[] lat_lon = geo_yx.getProperty(city).split(",");
            String hash = GeoHash.geoHashStringWithCharacterPrecision(Double.valueOf(lat_lon[0]), Double.valueOf(lat_lon[1]), 7);
            List<String> citys = city_hash.get(hash);
            if (citys == null)
                citys = new ArrayList<>();
            citys.add(city);
            city_hash.put(hash, citys);
        }

        //check hash map several cities
//        for (String hash : city_hash.keySet()) {
//            List<String> citys = city_hash.get(hash);
//            if (citys.size() > 1) {
//                System.out.println(hash + "\t" + citys);
//            }
//        }

        for (String hash : city_hash.keySet()) {
            System.out.println(hash + "," + city_hash.get(hash).get(0));
        }
    }

    /**
     * @param lat1 起始地纬度
     * @param lon1 起始地经度
     * @param lat2 目的地纬度
     * @param lon2 目的地经度
     * @return
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        if (Math.abs(lat1) > 90 || Math.abs(lon1) > 180
                || Math.abs(lat2) > 90 || Math.abs(lon2) > 180) {
            throw new IllegalArgumentException("The supplied coordinates are out of range.");
        }
        int R = 6378137;
        double latRad1 = Math.toRadians(lat1);
        double lonRad1 = Math.toRadians(lon1);
        double latRad2 = Math.toRadians(lat2);
        double lonRad2 = Math.toRadians(lon2);
//结果
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((latRad1 - latRad2) / 2), 2)
                + Math.cos(latRad1) * Math.cos(latRad2) * Math.pow(Math.sin((lonRad1 - lonRad2) / 2), 2))) * R;
        distance = Math.round(distance * 10000) / 10000;
        return Math.round(distance);
    }
}