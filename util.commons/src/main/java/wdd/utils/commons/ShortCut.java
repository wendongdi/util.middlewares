package wdd.utils.commons;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class ShortCut {
    private static Pattern regex = Pattern.compile("[^A-F0-9]");

    /**
     * 拆分集合
     *
     * @param <T>           泛型对象
     * @param resList       需要拆分的集合
     * @param subListLength 每个子集合的元素个数
     * @return 返回拆分后的各个集合组成的列表
     * 代码里面用到了guava和common的结合工具类
     **/
    public static <T> List<List<T>> listSplit(List<T> resList, int subListLength) {
        if (CollectionUtils.isEmpty(resList) || subListLength <= 0) {
            return new ArrayList<>();
        }
        resList = new ArrayList<>(new HashSet<>(resList));
        List<List<T>> ret = new ArrayList<>();
        int size = resList.size();
        if (size <= subListLength) {
            // 数据量不足 subListLength 指定的大小
            ret.add(resList);
        } else {
            int pre = size / subListLength;
            int last = size % subListLength;
            // 前面pre个集合，每个大小都是 subListLength 个元素
            for (int i = 0; i < pre; i++) {
                List<T> itemList = new ArrayList<>();
                for (int j = 0; j < subListLength; j++) {
                    itemList.add(resList.get(i * subListLength + j));
                }
                ret.add(itemList);
            }
            // last的进行处理
            if (last > 0) {
                List<T> itemList = new ArrayList<>();
                for (int i = 0; i < last; i++) {
                    itemList.add(resList.get(pre * subListLength + i));
                }
                ret.add(itemList);
            }
        }
        return ret;
    }

    public static int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    /**
     * 返回手机号码
     */
    private static String[] telFirst = "134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");

    public static String getTel() {
        int index = getNum(0, telFirst.length - 1);
        String first = telFirst[index];
        String second = String.valueOf(getNum(1, 888) + 10000).substring(1);
        String third = String.valueOf(getNum(1, 9100) + 10000).substring(1);
        return first + second + third;

    }

    public static <T> List<List<T>> listSplit(List<T> resList, int subListLength, int max) {
        List<List<T>> restemp = listSplit(resList, subListLength);
        if (restemp.size() <= 1) {
            return restemp;
        }
        List<T> tz = restemp.get(restemp.size() - 1);
        if (tz.size() < subListLength) {
            for (List<T> ts : restemp.subList(0, restemp.size() - 1)) {
                int addlen = max - ts.size();
                if (addlen > tz.size()) {
                    ts.addAll(tz);
                    break;
                } else {
                    ts.addAll(tz.subList(0, addlen));
                    tz = tz.subList(addlen, tz.size());
                }
            }
        }
        restemp.set(restemp.size() - 1, tz);
        return restemp;
    }


    public static void main(String[] args) {
        while (true) {
            System.out.println(makeErrorIdfa());
        }
    }

    public static String makeErrorImei() {
        StringBuilder res = new StringBuilder();
        char[] cs = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (int i = 0; i < 15; i++) {
            res.append(cs[(int) (Math.random() * 10)]);
        }
        return res.toString();
    }

    public static String makeErrorImei(int count) {
        StringBuilder res = new StringBuilder();
        char[] cs = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (int i = 0; i < count; i++) {
            res.append(cs[(int) (Math.random() * 10)]);
        }
        return res.toString();
    }

    public static String makeFakeMeim(int count) {
        StringBuilder res = new StringBuilder();
        char[] cs = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        for (int i = 0; i < count; i++) {
            res.append(cs[(int) (Math.random() * cs.length)]);
        }
        return res.toString();
    }

    public static String makeErrorIdfa() {
        return makeFakeMeim(8) + "-" + makeFakeMeim(4) + "-" + makeFakeMeim(4) + "-" + makeFakeMeim(4) + "-" + makeFakeMeim(12);
    }

    public static String logerr(Logger log, String msg) {
        log.error(msg);
        return msg;
    }

    public static String logerr(Logger log, String msg, Exception e) {
        log.error(msg, e);
        return msg;
    }

    public static List<String[]> readCsv(String filename) throws Exception {
        File file = new File(filename);
        FileReader fReader = new FileReader(file);
        CSVReader csvReader = new CSVReader(fReader);
        List list = csvReader.readAll();
        csvReader.close();
        return list;
    }

    public static String macStandard(String mac) {
        if (StringUtils.nonEmpty(mac)) {
            String res = mac.toUpperCase();
            res = regex.matcher(res).replaceAll("");
            if (res.length() == 12) {
                StringBuilder op = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    op.append(res.charAt(i * 2)).append(res.charAt(i * 2 + 1));
                    if (i != 5) {
                        op.append(':');
                    }
                }
                return op.toString();
            }
        }
        return "";
    }

    public static String didStandard(String did, String type) {
        String dtype = "";
        String ddid = did.replace(":", "");
        if (ddid.isEmpty()) {
            return "";
        }
        if (type.isEmpty()) {
            if (ddid.length() == 36) {
                dtype = "idfa";
            } else if (ddid.length() == 12) {
                dtype = "mac";
            } else {
                dtype = "imei";
            }
        }
        if ("idfa".equals(dtype)) {
            return ddid.toUpperCase();
        } else if ("mac".equals(dtype)) {
            return macStandard(ddid);
        } else if ("imei".equals(dtype)) {
            if (ddid.matches("\\d{14,15}")) {
                return Md5Code.getMd5(ImeiUtil.format(ddid));
            } else if (ddid.length() == 32) {
                return ddid;
            }
        }
        return "";
    }

}