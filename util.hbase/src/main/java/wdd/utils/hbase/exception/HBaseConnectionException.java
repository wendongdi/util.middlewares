package wdd.utils.hbase.exception;

public class HBaseConnectionException extends Exception {
    public HBaseConnectionException(Exception e) {
        super(e);
    }

    public HBaseConnectionException(String msg){
        super(msg);
    }
}
