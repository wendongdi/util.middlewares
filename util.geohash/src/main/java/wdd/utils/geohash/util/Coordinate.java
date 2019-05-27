package wdd.utils.geohash.util;

import wdd.utils.commons.StringUtils;

public class Coordinate {
    private double latitude;
    private double longitude;
    public static final String Delimeter = ",";

    public Coordinate() {
    }

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double distance(Coordinate coordinate) {
        double dLat = Math.toRadians(this.latitude - coordinate.latitude);
        double dLng = Math.toRadians(this.longitude - coordinate.longitude);
        double a = Math.pow(Math.sin(dLat / 2.0D), 2.0D) + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(coordinate.latitude)) *
                Math.pow(Math.sin(dLng / 2.0D), 2.0D);
        double c = 2.0D * Math.atan2(Math.sqrt(a), Math.sqrt(1.0D - a));
        return 6378137.0D * c;
    }

    public Coordinate[] expandBox(double range) {
        double c = range / 6378137.0D / 2.0D;
        double N = Math.tan(c) * Math.tan(c);
        double a = Math.sqrt(N / (1.0D + N));
        double x_offset = Math.toDegrees(Math.asin(a / Math.cos(Math.toRadians(this.latitude))) * 2.0D);
        double y_offset = Math.toDegrees(Math.asin(a) * 2.0D);
        Coordinate[] array = new Coordinate[2];
        array[0] = new Coordinate(this.latitude - y_offset, this.longitude - x_offset);
        array[1] = new Coordinate(this.latitude + y_offset, this.longitude + x_offset);
        return array;
    }

    public Coordinate toFixed(int digit) {
        double p = Math.pow(10.0D, digit);
        this.latitude = (Math.round(this.latitude * p) / p);
        this.longitude = (Math.round(this.longitude * p) / p);
        return this;
    }

    public String msg(boolean fixed) {
        if (fixed) {
            return String.format("%.6f", new Object[]{Double.valueOf(this.latitude)}) + "," + String.format("%.6f", new Object[]{Double.valueOf(this.longitude)});
        }
        return this.latitude + "," + this.longitude;
    }

    @Override
    public String toString() {
        return msg(true);
    }

    public static Coordinate parse(String str) {
        return parse(str, ",");
    }

    public static Coordinate parse(String str, String delimeter) {
        String[] arr = StringUtils.split(str, delimeter);
        return new Coordinate(Double.parseDouble(arr[0]), Double.parseDouble(arr[1]));
    }
}
