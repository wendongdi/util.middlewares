package wdd.utils.geohash;

public class GeoData {
    public String geoHash;
    public String statdate;
    public String did;
    public Integer count = 0;

    public GeoData(String geoHash, String statdate, String did) {
        this.geoHash = geoHash;
        this.statdate = statdate;
        this.did = did;
    }

    public GeoData(String geoHash, String statdate, String did, Integer count) {
        this.geoHash = geoHash;
        this.statdate = statdate;
        this.did = did;
        this.count = count;
    }

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    public String getStatdate() {
        return statdate;
    }

    public void setStatdate(String statdate) {
        this.statdate = statdate;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
