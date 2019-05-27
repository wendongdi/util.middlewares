package wdd.utils.geohash.util;

public class CoordinateConverter {
    public static final double X_PI = 52.35987755982988D;
    static final double RADIANS_PER_DEGREE = Math.toRadians(1.0D);

    public static Coordinate bd09_to_gcj02(Coordinate bd09_coor) {
        double x = bd09_coor.getLongitude() - 0.0065D;
        double y = bd09_coor.getLatitude() - 0.006D;
        double z = Math.sqrt(x * x + y * y) - 2.0E-5D * Math.sin(y * 52.35987755982988D);
        double theta = Math.atan2(y, x) - 3.0E-6D * Math.cos(x * 52.35987755982988D);
        return new Coordinate(z * Math.sin(theta), z * Math.cos(theta));
    }

    public static Coordinate gcj02_to_bd09(Coordinate gcj02_coor) {
        double x = gcj02_coor.getLongitude();
        double y = gcj02_coor.getLatitude();
        double z = Math.sqrt(x * x + y * y) + 2.0E-5D * Math.sin(y * 52.35987755982988D);
        double theta = Math.atan2(y, x) + 3.0E-6D * Math.cos(x * 52.35987755982988D);
        return new Coordinate(z * Math.sin(theta) + 0.006D, z * Math.cos(theta) + 0.0065D);
    }

    public static Coordinate wgs84_to_gcj02(Coordinate wgs84_coor) {
        double a = 6378245.0D;
        double ee = 0.006693421622965943D;

        double lat = wgs84_coor.getLatitude();
        double lon = wgs84_coor.getLongitude();

        double dLat = transformLat(lon - 105.0D, lat - 35.0D);
        double dLon = transformLon(lon - 105.0D, lat - 35.0D);
        double radLat = lat / 180.0D * 3.141592653589793D;
        double magic = Math.sin(radLat);
        magic = 1.0D - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = dLat * 180.0D / (a * (1.0D - ee) / (magic * sqrtMagic) * 3.141592653589793D);
        dLon = dLon * 180.0D / (a / sqrtMagic * Math.cos(radLat) * 3.141592653589793D);

        return new Coordinate(lat + dLat, lon + dLon);
    }

    public static Coordinate gcj02_to_wgs84(Coordinate gcj02_coor) {
        Coordinate gpt = wgs84_to_gcj02(gcj02_coor);
        double lat = gpt.getLatitude() - gcj02_coor.getLatitude();
        double lon = gpt.getLongitude() - gcj02_coor.getLongitude();
        return new Coordinate(gcj02_coor.getLatitude() - lat, gcj02_coor.getLongitude() - lon);
    }

    public static Coordinate wgs84_to_bd09(Coordinate wgs84_coor) {
        return gcj02_to_bd09(wgs84_to_gcj02(wgs84_coor));
    }

    public static Coordinate bd09_to_wgs84(Coordinate bd09_coor) {
        return gcj02_to_wgs84(bd09_to_gcj02(bd09_coor));
    }

    public static Coordinate mercator_to_wgs84(double x, double y) {
        double ax = x / 6378137.0D / 3.141592653589793D * 180.0D;
        double ay = y / 6378137.0D / 3.141592653589793D * 180.0D;
        ay = 57.29577951308232D * (2.0D * Math.atan(Math.exp(ay * 3.141592653589793D / 180.0D)) - 1.5707963267948966D);
        return new Coordinate(ay, ax);
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0D + 2.0D * x + 3.0D * y + 0.2D * y * y + 0.1D * x * y + 0.2D * Math.sqrt(Math.abs(x));
        ret += (20.0D * Math.sin(6.0D * x * 3.141592653589793D) + 20.0D * Math.sin(2.0D * x * 3.141592653589793D)) * 2.0D / 3.0D;
        ret += (20.0D * Math.sin(y * 3.141592653589793D) + 40.0D * Math.sin(y / 3.0D * 3.141592653589793D)) * 2.0D / 3.0D;
        ret += (160.0D * Math.sin(y / 12.0D * 3.141592653589793D) + 320.0D * Math.sin(y * 3.141592653589793D / 30.0D)) * 2.0D / 3.0D;
        return ret;
    }

    private static double transformLon(double x, double y) {
        double ret = 300.0D + x + 2.0D * y + 0.1D * x * x + 0.1D * x * y + 0.1D * Math.sqrt(Math.abs(x));
        ret += (20.0D * Math.sin(6.0D * x * 3.141592653589793D) + 20.0D * Math.sin(2.0D * x * 3.141592653589793D)) * 2.0D / 3.0D;
        ret += (20.0D * Math.sin(x * 3.141592653589793D) + 40.0D * Math.sin(x / 3.0D * 3.141592653589793D)) * 2.0D / 3.0D;
        ret += (150.0D * Math.sin(x / 12.0D * 3.141592653589793D) + 300.0D * Math.sin(x / 30.0D * 3.141592653589793D)) * 2.0D / 3.0D;
        return ret;
    }

    private static double fitx(double x) {
        return 1.088E-5D * x + 4.832E-7D;
    }

    private static double fity(double y) {
        return 2.686E-4D + -9.39E-5D * y + 1.073E-6D * y * y;
    }
}
