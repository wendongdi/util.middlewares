package wdd.utils.http;

import wdd.utils.commons.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.*;

public class HttpUtils {
    public static void setP3PHeader(HttpServletResponse res) {
        res.addHeader("P3P", "CP=\"CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR\"");
    }

    public static void setNoCacheHeaders(HttpServletResponse res) {
        res.addHeader("Pragma", "no-cache");
        res.addHeader("Cache-Control", "private, max-age=0, no-cache");
    }

    public static String getRealIp(HttpServletRequest request) {
        String rip = request.getHeader("X-Real-IP");
        if (StringUtils.isEmpty(rip)) {
            rip = request.getRemoteAddr();
        }
        return rip;
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if ((StringUtils.isEmpty(ipAddress)) || ("unknown".equalsIgnoreCase(ipAddress))) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if ((StringUtils.isEmpty(ipAddress)) || ("unknown".equalsIgnoreCase(ipAddress))) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if ((StringUtils.isEmpty(ipAddress)) || ("unknown".equalsIgnoreCase(ipAddress))) {
            ipAddress = request.getHeader("X-Real-IP");
        }

        if ((StringUtils.isEmpty(ipAddress)) || ("unknown".equalsIgnoreCase(ipAddress))) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1")) {
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException localUnknownHostException) {
                }
            }
        }
        if (StringUtils.nonEmpty(ipAddress)) {
            String[] ipAry = StringUtils.split(ipAddress, ",");
            for (String strIp : ipAry) {
                if ((!"unknown".equalsIgnoreCase(strIp)) && (isIPv4Address(strIp)) && (!isInnerIP(strIp))) {
                    ipAddress = strIp;
                    break;
                }
            }
        }
        return ipAddress;
    }

    public static boolean isIPv4Address(String ip) {
        String[] array = StringUtils.split(ip, ".");
        if ((array == null) || (array.length != 4)) return false;

        for (String s : array) {
            if (!StringUtils.isNumeric(s)) return false;
            int d = Integer.parseInt(s);
            if ((d > 255) || (d < 0)) return false;
        }
        return true;
    }

    public static boolean isInnerIP(String ipAddress) {
        boolean isInnerIp = false;
        long ipNum = getIpNum(ipAddress);

        long aBegin = getIpNum("10.0.0.0");
        long aEnd = getIpNum("10.255.255.255");
        long bBegin = getIpNum("172.16.0.0");
        long bEnd = getIpNum("172.31.255.255");
        long cBegin = getIpNum("192.168.0.0");
        long cEnd = getIpNum("192.168.255.255");
        long dBegin = getIpNum("127.0.0.0");
        long dEnd = getIpNum("127.255.255.255");

        isInnerIp = (isInner(ipNum, aBegin, aEnd)) || (isInner(ipNum, bBegin, bEnd)) || (isInner(ipNum, cBegin, cEnd)) || (isInner(ipNum, dBegin, dEnd));
        return isInnerIp;
    }

    private static long getIpNum(String ipAddress) {
        String[] ip = ipAddress.split("\\.");
        long a = Integer.parseInt(ip[0]);
        long b = Integer.parseInt(ip[1]);
        long c = Integer.parseInt(ip[2]);
        long d = Integer.parseInt(ip[3]);
        long ipNum = a * 256L * 256L * 256L + b * 256L * 256L + c * 256L + d;
        return ipNum;
    }

    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    public static Map<String, Cookie> getCookies(HttpServletRequest req) {
        Map cookieMap = new HashMap();
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return cookieMap;
        }

        for (Cookie cookie : cookies) {
            if (cookie != null) {
                cookieMap.put(cookie.getName(), cookie);
            }
        }
        return cookieMap;
    }

    public static Map<String, String> getParameterMap(String queryString) {
        Map map = new HashMap();
        if (!StringUtils.isEmpty(queryString)) {
            queryString = StringUtils.replace(queryString, "&amp;", "&");
            String[] params = queryString.split("&");

            for (String s : params) {
                String[] kv = s.split("=", 2);
                if (kv.length == 2) {
                    map.put(kv[0], kv[1]);
                }
            }
        }
        return map;
    }

    public static String getQueryString(String url) {
        if (StringUtils.isEmpty(url)) return null;
        String[] arr = url.split("\\?", 2);
        return arr.length > 1 ? arr[1] : null;
    }

    public static String decodeURL(String url) {
        try {
            if (StringUtils.isEmpty(url)) return null;
            return URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
        }
        return url;
    }

    public static String encodeURL(String url) {
        try {
            if (StringUtils.isEmpty(url)) return null;
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
        }
        return url;
    }

    public static String getUserAgent(HttpServletRequest req) {
        return req.getHeader("User-Agent");
    }

    public static boolean validCharset(String charset) {
        String[] standard_charsets = {"US-ASCII", "Shift_JIS", "KOI8-R", "ISO-8859-9", "ISO-8859-8-I", "ISO-8859-8-E", "ISO-8859-8", "ISO-8859-7", "ISO-8859-6-I", "ISO-8859-6-E", "ISO-8859-6", "ISO-8859-5", "ISO-8859-4", "ISO-8859-3", "ISO-8859-2", "ISO-8859-10", "ISO-8859-1", "ISO-2022-KR", "ISO-2022-JP-2", "ISO-2022-JP", "GB2312", "EUC-KR", "EUC-JP", "BIG5", "GBK", "UTF-8"};

        Set set = new HashSet();
        set.addAll(Arrays.asList(standard_charsets));
        return set.contains(charset.toUpperCase());
    }

    public static int getContentLength(HttpServletRequest req) {
        String s = req.getHeader("Content-Length");
        if (StringUtils.nonEmpty(s)) return Integer.parseInt(s);
        return -1;
    }
}