package wdd.utils.hbase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wdd.utils.hbase.exception.HBaseConnectionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class TableBean {
    Table table;
    long lastUse;
}

public class TablePool {
    private static Logger log = LoggerFactory.getLogger(TablePool.class);
    private List<TableBean> pool;
    private Connection connection;

    public TablePool() throws HBaseConnectionException {
        pool = new ArrayList<>();
        this.connection = HBaseConnectioner.getConnection();
    }

    public synchronized Table getTable(String tableName) throws IOException {
        return getTable(TableName.valueOf(tableName));
    }

    public synchronized Table getTable(TableName tableName) throws IOException {
        TableBean target = null;
        for (Iterator<TableBean> ti = pool.iterator(); ti.hasNext(); ) {
            TableBean table = ti.next();
            if (table.table.getName().equals(tableName)) {
                table.lastUse = System.currentTimeMillis();
                target = table;
            }
            if (System.currentTimeMillis() - table.lastUse > 60000) {
                try {
                    table.table.close();
                    ti.remove();
                } catch (IOException e) {
                    log.error("table close", e);
                }
            }
        }
        if (null == target) {
            target = new TableBean();
            target.table = connection.getTable(tableName);
            target.lastUse = System.currentTimeMillis();
        }
        return target.table;
    }

    public void closeAll() throws IOException {
        for (TableBean tableBean : pool) {
            try {
                tableBean.table.close();
            } catch (IOException e) {
                log.error("table close", e);
            }
        }
        if (!connection.isClosed())
            connection.close();
        connection = null;
    }
}
