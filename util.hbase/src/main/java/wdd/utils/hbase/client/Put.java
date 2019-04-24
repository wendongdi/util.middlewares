package wdd.utils.hbase.client;

import java.nio.ByteBuffer;

public class Put extends org.apache.hadoop.hbase.client.Put {
    public Put(byte[] row) {
        super(row);
    }

    public Put(byte[] row, long ts) {
        super(row, ts);
    }

    public Put(byte[] rowArray, int rowOffset, int rowLength) {
        super(rowArray, rowOffset, rowLength);
    }

    public Put(ByteBuffer row, long ts) {
        super(row, ts);
    }

    public Put(ByteBuffer row) {
        super(row);
    }

    public Put(byte[] rowArray, int rowOffset, int rowLength, long ts) {
        super(rowArray, rowOffset, rowLength, ts);
    }

    public Put(org.apache.hadoop.hbase.client.Put putToCopy) {
        super(putToCopy);
    }

    public Put addColumn(String family, String qualifier, String value) {
        return (Put) addColumn(family.getBytes(), qualifier.getBytes(), value.getBytes());
    }

    public Put add(String family, String qualifier, String value) {
        return addColumn(family, qualifier, value);
    }
}
