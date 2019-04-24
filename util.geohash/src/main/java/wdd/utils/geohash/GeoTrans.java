package wdd.utils.geohash;

import com.alibaba.fastjson.JSONObject;
import wdd.utils.httpclient.HttpRequest;
import wdd.utils.httpclient.exception.ApiTimeOutException;
import wdd.utils.httpclient.exception.HttpException;

import java.io.IOException;

/**
 * @author wdd
 */
public class GeoTrans {
    /**
     * http://lbsyun.baidu.com/index.php?title=webapi/guide/changeposition
     */
    public static JSONObject toBaiDuType(Double lat, Double lon) throws ApiTimeOutException, IOException, HttpException {
        return JSONObject.parseObject(HttpRequest.instance().get(String.format("http://api.map.baidu.com/geoconv/v1/?coords=%s,%s&from=1&to=5&ak=mxf9p8x8dtXF7NDDTUe75DdL", lon, lat)));
    }
}
