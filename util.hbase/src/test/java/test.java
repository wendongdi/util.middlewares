import wdd.utils.hbase.HBaseClient;
import wdd.utils.hbase.exception.HBaseConnectionException;
import wdd.utils.hbase.exception.HBaseRunTimeException;

public class test {
    public static void main(String[] args) throws HBaseConnectionException, HBaseRunTimeException {
        HBaseClient.instance().scanAll("dataexchange_device_brands");
    }
}
