package wdd.utils.geohash;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.WGS84Point;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.junit.Assert;
import wdd.utils.commons.Md5Code;
import wdd.utils.commons.ShortCut;
import wdd.utils.commons.StringUtils;
import wdd.utils.hbase.HBaseClient;
import wdd.utils.hbase.exception.HBaseConnectionException;
import wdd.utils.hbase.exception.HBaseRunTimeException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.hadoop.hbase.filter.FilterList.Operator.MUST_PASS_ONE;

/**
 * @author wdd
 */
public class GeoHash {
    private static String table_device_geo = "dataexchange_device_geo";
    private static String table_geo_device = "dataexchange_geo_device";

    private static int default_hashstr_len = 7;
    private static long dayMillisSeconds = 86400000L;

    private static SimpleDateFormat daydf = new SimpleDateFormat("yyyyMMdd");


    public static String hash(double lat, double lon) {
        return ch.hsr.geohash.GeoHash.geoHashStringWithCharacterPrecision(lat, lon, default_hashstr_len);
    }

    public static String hash(double lat, double lon, int hashLen) {
        return ch.hsr.geohash.GeoHash.geoHashStringWithCharacterPrecision(lat, lon, hashLen);
    }

    public static ch.hsr.geohash.GeoHash[][] hashAreas(double lat, double lon, int radius) {
        return hashAreas(lat, lon, radius, 7);
    }

    public static List<String> hashList(double lat, double lon, int radius) {
        List<String> hashs = new ArrayList<>();
        for (ch.hsr.geohash.GeoHash[] area : hashAreas(lat, lon, radius, 7)) {
            for (ch.hsr.geohash.GeoHash geoHash : area) {
                hashs.add(geoHash.toBase32());
            }
        }
        return hashs;
    }

    public static ch.hsr.geohash.GeoHash[][] hashAreas(double lat, double lon, int radius, int hashLen) {
        Assert.assertTrue(level_error.containsKey(hashLen));
        int error = level_error.get(hashLen);
        int timeRaw = (int) Math.ceil(1.0 * radius / error);
        if (timeRaw % 2 == 0) {
            timeRaw += 1;
        }
        int partLen = (timeRaw - 1) / 2;
        ch.hsr.geohash.GeoHash[][] areas = new ch.hsr.geohash.GeoHash[timeRaw][timeRaw];
        ch.hsr.geohash.GeoHash ehMid = ch.hsr.geohash.GeoHash.withCharacterPrecision(lat, lon, default_hashstr_len);

        areas[partLen][partLen] = ehMid;

        for (int i = 0; i <= partLen; i++) {
            if (i > 0) {
                areas[partLen + i][partLen] = areas[partLen + i - 1][partLen].getEasternNeighbour();
                areas[partLen - i][partLen] = areas[partLen - i + 1][partLen].getWesternNeighbour();
            }
            for (int j = 1; j <= partLen; j++) {
                areas[partLen + i][partLen + j] = areas[partLen + i][partLen + j - 1].getNorthernNeighbour();
                areas[partLen + i][partLen - j] = areas[partLen + i][partLen - j + 1].getSouthernNeighbour();
                areas[partLen - i][partLen + j] = areas[partLen - i][partLen + j - 1].getNorthernNeighbour();
                areas[partLen - i][partLen - j] = areas[partLen - i][partLen - j + 1].getSouthernNeighbour();
            }
        }
        return areas;
    }

    public static List<Scan> hashAreaScan(List<String> hashAreas, String startDate, String endDate) throws ParseException {
        Set<String> statDates = new HashSet<>();
        Date sDay = StringUtils.isEmpty(startDate) ? null : daydf.parse(startDate);
        Date eDay = StringUtils.isEmpty(endDate) ? null : daydf.parse(endDate);

        if (sDay != null && eDay != null) {
            Assert.assertTrue(sDay.getTime() <= eDay.getTime());
            statDates.add(startDate);
            statDates.add(endDate);
            int aCount = 1;
            while (sDay.getTime() + aCount * dayMillisSeconds < eDay.getTime()) {
                statDates.add(daydf.format(new Date(sDay.getTime() + aCount * dayMillisSeconds)));
                aCount += 1;
            }
        }

        List<Scan> scans = new ArrayList<>();

        for (String hashArea : hashAreas) {
            FilterList fs = new FilterList(MUST_PASS_ONE);
            String startrow = hashArea;
            String endrow = hashArea;
            if (sDay != null) {
                if (eDay != null) {
                    if (hashArea.length() == 7) {
                        startrow += ("_" + startDate);
                        endrow += ("_" + endDate);
                    } else {
                        for (String statDate : statDates) {
                            fs.addFilter(new RowFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator(statDate)));
                        }
                    }
                } else {
                    if (hashArea.length() == 7) {
                        startrow += ("_" + startDate);
                        endrow = startrow;
                    } else {
                        fs.addFilter(new RowFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator(startDate)));
                    }
                }
            }
            endrow += "~";

            Scan scan = new Scan(startrow.getBytes(), endrow.getBytes());
            if (fs.getFilters().size() > 0) {
                scan.setFilter(fs);
            }
            scans.add(scan);
        }
        return scans;
    }

    public static List<Result> geoScanRows(Scan scan) throws HBaseRunTimeException, HBaseConnectionException {
        List<Result> rows = new ArrayList<>();
        for (Result result : HBaseClient.instance().scan(table_geo_device, scan)) {
            rows.add(result);
        }
        return rows;
    }

    public static List<Result> deviceScanRows(Scan scan) throws HBaseRunTimeException, HBaseConnectionException {
        List<Result> rows = new ArrayList<>();
        for (Result result : HBaseClient.instance().scan(table_device_geo, scan)) {
            rows.add(result);
            if (scan.getCaching() > 0 && rows.size() >= scan.getCaching()) {
                break;
            }
        }
        return rows;
    }

    private static byte[] famInfo = "info".getBytes();
//    private static byte[] qualCount = "count".getBytes();

    public static List<GeoData> hashAreaSearch(List<Scan> scans) throws HBaseRunTimeException, HBaseConnectionException {
        List<GeoData> datas = new ArrayList<>();
        for (Scan scan : scans) {
            for (Result hashAreaRow : geoScanRows(scan)) {
                String row = new String(hashAreaRow.getRow());
                String[] geoStatDid = row.split("_");
                int count = 0;
                for (Cell cell : hashAreaRow.listCells()) {
                    String countStr = new String(cell.getValue());
                    if (countStr.matches("\\d+")) {
                        count += Integer.valueOf(new String(cell.getValue()));
                    }
                }
                if (count > 0) {
                    if (geoStatDid.length == 3) {
                        datas.add(new GeoData(geoStatDid[0], geoStatDid[1], geoStatDid[2], count));
                    }
                }
            }
        }
        return datas;
    }


    public static List<GeoData> didHashSearch(Scan scan) throws HBaseRunTimeException, HBaseConnectionException {
        List<GeoData> datas = new ArrayList<>();
        for (Result hashAreaRow : deviceScanRows(scan)) {
            String row = new String(hashAreaRow.getRow());
            String[] didStatGeo = row.split("_");
            int count = 0;
            for (Cell cell : hashAreaRow.listCells()) {
                String countStr = new String(cell.getValue());
                if (countStr.matches("\\d+")) {
                    count += Integer.valueOf(new String(cell.getValue()));
                }
            }
            if (count > 0) {
                if (didStatGeo.length == 3) {
                    datas.add(new GeoData(didStatGeo[2], didStatGeo[1], new StringBuilder(didStatGeo[0]).reverse().toString(), count));
                }
            }
        }
        return datas;
    }


    /**
     * @param lat        经纬度
     * @param lon        经纬度
     * @param radius     查询半径
     * @param startDate  起始时间，可为空
     * @param endDate    结束时间，可为空
     * @param lowerLimit 频次下限
     * @return 设备号列表 Set[did]
     * @throws HBaseRunTimeException    hbase运行时错误
     * @throws HBaseConnectionException hbase连接错误
     */
    public static Set<String> rangeScan(double lat, double lon, int radius, String startDate, String endDate, int lowerLimit) throws HBaseRunTimeException, HBaseConnectionException, ParseException {
        ch.hsr.geohash.GeoHash[][] areas = hashAreas(lat, lon, radius);

        List<String> hashs = new ArrayList<>();
        for (ch.hsr.geohash.GeoHash[] area : areas) {
            for (ch.hsr.geohash.GeoHash geoHash : area) {
                hashs.add(geoHash.toBase32());
            }
        }

        List<Scan> scans = hashAreaScan(hashs, startDate, endDate);

        List<GeoData> datas = hashAreaSearch(scans);
        Map<String, Integer> didCounts = new HashMap<>();
        for (GeoData data : datas) {
            if (!didCounts.containsKey(data.did)) {
                didCounts.put(data.did, data.count);
            } else {
                didCounts.put(data.did, didCounts.get(data.did) + data.count);
            }
        }

        if (lowerLimit == 0) {
            return didCounts.keySet();
        } else {
            Set<String> dids = new HashSet<>();
            for (String did : didCounts.keySet()) {
                if (didCounts.get(did) > lowerLimit) {
                    dids.add(did);
                }
            }
            return dids;
        }
    }

    public static int sampleCount(double lat, double lon, int radius, String startDate, String endDate) throws HBaseRunTimeException, HBaseConnectionException, ParseException {
        ch.hsr.geohash.GeoHash[][] areas = hashAreas(lat, lon, radius);

        int error = level_error.get(7);
        int timeRaw = (int) Math.ceil(1.0 * radius / error);
        if (timeRaw % 2 == 0) {
            timeRaw += 1;
        }
        int partLen = (timeRaw - 1) / 2;

        List<String> samples = new ArrayList<>();
        samples.add(areas[partLen][partLen].toBase32());
        samples.add(areas[partLen - 1][partLen].toBase32());
        samples.add(areas[partLen][partLen - 1].toBase32());
        samples.add(areas[partLen + 1][partLen].toBase32());
        samples.add(areas[partLen][partLen + 1].toBase32());
        samples.add(areas[partLen - 1][partLen - 1].toBase32());
        samples.add(areas[partLen - 1][partLen + 1].toBase32());
        samples.add(areas[partLen + 1][partLen + 1].toBase32());
        samples.add(areas[partLen + 1][partLen - 1].toBase32());

        if (partLen > 15) {
            samples.add(areas[partLen - 2][partLen - 2].toBase32());
            samples.add(areas[partLen - 2][partLen - 1].toBase32());
            samples.add(areas[partLen - 2][partLen].toBase32());
            samples.add(areas[partLen - 2][partLen + 1].toBase32());
            samples.add(areas[partLen - 2][partLen + 2].toBase32());
            samples.add(areas[partLen - 1][partLen - 2].toBase32());
            samples.add(areas[partLen - 1][partLen + 2].toBase32());
            samples.add(areas[partLen][partLen - 2].toBase32());
            samples.add(areas[partLen][partLen + 2].toBase32());
            samples.add(areas[partLen + 1][partLen - 2].toBase32());
            samples.add(areas[partLen + 1][partLen + 2].toBase32());
            samples.add(areas[partLen + 2][partLen - 2].toBase32());
            samples.add(areas[partLen + 2][partLen - 1].toBase32());
            samples.add(areas[partLen + 2][partLen].toBase32());
            samples.add(areas[partLen + 2][partLen + 1].toBase32());
            samples.add(areas[partLen + 2][partLen + 2].toBase32());
        }

        List<Scan> scans = hashAreaScan(samples, startDate, endDate);

        List<GeoData> datas = hashAreaSearch(scans);
        Set<String> didCounts = new HashSet<>();
        for (GeoData data : datas) {
            didCounts.add(data.did);
        }
        System.out.println(String.format("hashSize:%s\tsampleSize:%s", timeRaw * timeRaw, samples.size()));
        return (int) (1.0 * didCounts.size() * timeRaw * timeRaw / samples.size());
    }


    /**
     * geohash位数与精度误差
     */
    public static Map<Integer, Integer> level_error = new HashMap<>();

    static {
        level_error.put(4, 20000);
        level_error.put(5, 2400);
        level_error.put(6, 610);
        level_error.put(7, 76);
    }

    public static int roughCount(double lat, double lon, int radius, String startDate, String endDate, int levelUp) throws HBaseRunTimeException, HBaseConnectionException, ParseException {
        int level = 4;
        for (int ki = 4; ki < 8; ki++) {
            level = ki;
            if (level_error.get(ki) < radius) {
                break;
            }
        }
        if (levelUp > 0) {
            level = level + levelUp < 7 ? level + levelUp : 7;
        }
        int error = level_error.get(level);
        double multi = Math.pow(1.0 * radius / error, 2);
        String geoHash = GeoHash.hash(lat, lon, level);

        List<Scan> scans = hashAreaScan(Collections.singletonList(geoHash), startDate, endDate);

        Set<String> dids = new HashSet<>();

        for (GeoData geoData : hashAreaSearch(scans)) {
            dids.add(geoData.did);
        }

        return (int) (dids.size() * multi);
    }

    /**
     * @param did       设备号
     * @param startDate 时间范围
     * @param endDate
     * @return 坐标点
     * @throws HBaseRunTimeException
     * @throws HBaseConnectionException
     */
    public static List<WGS84Point> trace(String did, String startDate, String endDate, Integer limit) throws HBaseRunTimeException, HBaseConnectionException {
        Set<String> hashSet = new HashSet<>();
        for (Map<String, Integer> hashs : did2statHash(did, startDate, endDate, limit).values()) {
            hashSet.addAll(hashs.keySet());
        }
        List<WGS84Point> ress = new ArrayList<>();
        for (String hash : hashSet) {
            BoundingBox bbox = hash2box(hash);
            double centerLatitude = (bbox.getMaxLat() - bbox.getMinLat()) * Math.random() + bbox.getMinLat();
            double centerLongitude = (bbox.getMaxLon() - bbox.getMinLon()) * Math.random() + bbox.getMinLon();
            ress.add(new WGS84Point(centerLatitude, centerLongitude));
        }
        return ress;
    }

    /**
     * @param did       设备号
     * @param startDate 时间范围
     * @param endDate
     * @return 坐标点
     * @throws HBaseRunTimeException
     * @throws HBaseConnectionException
     */
    public static Map<WGS84Point, Integer> traceRough(String did, String startDate, String endDate, Integer limit) throws HBaseRunTimeException, HBaseConnectionException {
        Map<String, Integer> hashMap = new HashMap<>();
        for (Map<String, Integer> hashs : did2statHash(did, startDate, endDate, -1).values()) {
            for (String hash : hashs.keySet()) {
                String roughKey = hash.substring(0, 5);
                if (hashMap.containsKey(roughKey)) {
                    hashMap.put(roughKey, hashMap.get(roughKey) + hashs.get(hash));
                } else {
                    hashMap.put(roughKey, hashs.get(hash));
                }
            }
        }

        List<Map.Entry<String, Integer>> sorts = new ArrayList<>(hashMap.entrySet());

        Collections.sort(sorts, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });

        Map<WGS84Point, Integer> ress = new HashMap<>();
        for (Map.Entry<String, Integer> hash : sorts.subList(0, Math.min(limit, sorts.size()))) {
            BoundingBox bbox = hash2box(hash.getKey());
            double centerLatitude = (bbox.getMaxLat() + bbox.getMinLat()) / 2;
            double centerLongitude = (bbox.getMaxLon() + bbox.getMinLon()) / 2;
            ress.put(new WGS84Point(centerLatitude, centerLongitude), hash.getValue());
        }
        return ress;
    }

    /**
     * 根据设备号和时间范围查询设备号轨迹
     *
     * @param did       设备号
     * @param startDate 起始日期，可为空
     * @param endDate   结束日期，可为空
     * @return Map[statdate, Set[geoHash]]
     * @throws HBaseRunTimeException    hbase运行时错误
     * @throws HBaseConnectionException hbase连接错误
     */
    public static Map<String, Map<String, Integer>> did2statHash(String did, String startDate, String endDate, Integer limit) throws HBaseRunTimeException, HBaseConnectionException {
        Map<String, Map<String, Integer>> ress = new HashMap<>();
        String rdid = ShortCut.didStandard(did, "").replace(":", "").toUpperCase();
        if (rdid.length() == 36) {
            rdid = Md5Code.getMd5(rdid).toUpperCase();
        }
        String ddid = new StringBuilder(rdid).reverse().toString();
        String startrow = ddid;
        String endrow = ddid;
        if (StringUtils.nonEmpty(startDate)) {
            startrow += ("_" + startDate);
            if (StringUtils.nonEmpty(endDate)) {
                endrow += ("_" + endDate);
            } else {
                endrow += ("_" + startDate);
            }
        }

        startrow += "_";
        endrow += "~";
        Scan scan = new Scan(startrow.getBytes(), endrow.getBytes());
        if (limit > 0) {
            scan.setCaching(limit);
        }

        for (GeoData geoData : didHashSearch(scan)) {
            if (!ress.containsKey(geoData.statdate)) {
                ress.put(geoData.statdate, new HashMap<String, Integer>());
            }
            ress.get(geoData.statdate).put(geoData.geoHash, geoData.count);
        }
        return ress;
    }

    public static BoundingBox hash2box(String geohash) {
        return ch.hsr.geohash.GeoHash.fromGeohashString(geohash).getBoundingBox();
    }


    public static void main(String[] args) throws HBaseRunTimeException, HBaseConnectionException, IOException, ParseException {
        did2statHash("D54D1702AD0F8326224B817C796763C9", null, null, 100);
    }
}
