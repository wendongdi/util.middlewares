package wdd.utils.hbase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import wdd.utils.commons.AppConfig;
import wdd.utils.commons.StringUtils;
import wdd.utils.hbase.annotation.HBaseColumn;
import wdd.utils.hbase.annotation.HBaseTable;
import wdd.utils.hbase.entity.Column;
import wdd.utils.hbase.exception.HBaseConnectionException;
import wdd.utils.hbase.exception.HBaseRunTimeException;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class HBaseClient implements Serializable {
    private static HBaseClient instance;
    private TablePool tablePool;
    private Connection connection;
    private Admin admin;

    public TablePool getTablePool() {
        return tablePool;
    }

    public void setTablePool(TablePool tablePool) {
        this.tablePool = tablePool;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public HBaseClient() throws HBaseConnectionException {
        if (connection == null)
            connection = HBaseConnectioner.getConnection();
        if (admin == null)
            try {
                admin = connection.getAdmin();
            } catch (IOException e) {
                throw new HBaseConnectionException(e);
            }
        if (tablePool == null) {
            tablePool = new TablePool();
        }
    }

    public static HBaseClient instance() throws HBaseConnectionException {
        if (instance == null) {
            synchronized (HBaseClient.class) {
                if (null == instance) {
                    instance = new HBaseClient();
                }
            }
        }
        return instance;
    }

    public static void printResultScanner(Result[] rs) {
        for (Result result : rs) {
            System.out.println(Bytes.toString(result.getRow()));
            printResult(result);
        }
    }

    public static void printResult(Result r) {
        if (r.isEmpty())
            return;
        for (Cell cell : r.listCells()) {
            System.out.println(Bytes.toString(CellUtil.cloneRow(cell)) + "/" + Bytes.toString(CellUtil.cloneFamily(cell)) + "/" + Bytes.toString(CellUtil.cloneQualifier(cell)) + "/" + Bytes.toString(CellUtil.cloneValue(cell)));
        }
    }

    public void close() throws IOException {
        instance = null;
        if (tablePool != null) {
            tablePool.closeAll();
            tablePool = null;
        }
        if (connection != null) {
            connection.close();
            connection = null;
        }
        if (admin != null) {
            admin.close();
            admin = null;
        }
    }

    public <T> HBaseTable annotateTable(Class<T> clazz) {
        HBaseTable tblann = clazz.getAnnotation(HBaseTable.class);
        Assert.assertNotNull("clazz must be @HBaseTable type", tblann);
        return tblann;
    }

    public Table getTable(TableName tableName) throws IOException {
        return tablePool.getTable(tableName);
    }

    public FilterList filters(HBaseTable tblann) {
        FilterList fls = new FilterList();
        String startWith = tblann.startWith();
        if (StringUtils.nonEmpty(startWith)) {
            fls.addFilter(new RowFilter(CompareFilter.CompareOp.EQUAL,
                    new BinaryPrefixComparator(startWith.getBytes())));
        }
        String[] contains = tblann.contains();
        for (String c : contains) {
            fls.addFilter(new RowFilter(CompareFilter.CompareOp.EQUAL,
                    new SubstringComparator(c)));
        }
        String regex = tblann.regex();
        if (fls.getFilters().size() == 0 && StringUtils.nonEmpty(regex)) {
            fls.addFilter(new RowFilter(CompareFilter.CompareOp.EQUAL,
                    new RegexStringComparator(regex)));
        }
        return fls;
    }

    public <T> List<T> scan(Class<T> clazz) throws HBaseRunTimeException {
        HBaseTable tblann = annotateTable(clazz);
        String tableName = tblann.tableName();
        Scan scan = new Scan();
        //set start & stop rows
        String startRow = tblann.startRow();
        String endRow = tblann.stopRow();
        if (StringUtils.nonEmpty(startRow))
            scan.setStartRow(startRow.getBytes());
        if (StringUtils.nonEmpty(endRow))
            scan.setStopRow(endRow.getBytes());
        /*
        update filters
        */
        scan.setFilter(filters(tblann));

        int nextRows = tblann.next();
        List<T> to = new ArrayList<>();
        try {
            for (Result result : scan(tableName, scan).next(nextRows)) {
                T t = rowMapping(result, clazz);
                if (t != null)
                    to.add(t);
            }
        } catch (IOException e) {
            throw new HBaseRunTimeException(e);
        }
        return to;
    }

    public ResultScanner scan(String tableName, Scan scan) throws HBaseRunTimeException {
        try {
            Table table = tablePool.getTable(tableName);
            return table.getScanner(scan);
        } catch (IOException e) {
            throw new HBaseRunTimeException(e);
        }
    }

    public void update(Row row) throws HBaseRunTimeException {
        update(Collections.singletonList(row));
    }

    public void update(List<Row> datas) throws HBaseRunTimeException {
        Class clazz = datas.get(0).getClass();
        HBaseTable htblann = annotateTable(clazz);
        String tableName = htblann.tableName();
        List<Column> fields = mappingColumns(clazz);
        Map<String, List<Column>> adddatas = new HashMap<>();
        try {
            for (Row data : datas) {
                String rowkey = BeanUtils.getProperty(data, "row");
                if (rowkey == null)
                    continue;
                List<Column> temp = new ArrayList<>();
                for (Column field : fields) {
                    String value = BeanUtils.getProperty(data, field.getValue());
                    if (value != null) {
                        temp.add(new Column(field.getFamily(), field.getQualifier(), value));
                    }
                }
                adddatas.put(rowkey, temp);
            }

            if (adddatas.size() == 0)
                return;

            Table table = tablePool.getTable(tableName);
            for (String key : adddatas.keySet()) {
                List<Column> cids = adddatas.get(key);
                Put p1 = new Put(Bytes.toBytes(key));
                Delete del = new Delete(Bytes.toBytes(key));
                boolean delflag = false;
                for (Column cid : cids) {
                    if (StringUtils.nonEmpty(cid.getValue()))
                        p1.addColumn(Bytes.toBytes(cid.getFamily()), Bytes.toBytes(cid.getQualifier()), Bytes.toBytes(cid.getValue()));
                    else {
                        del.addColumn(Bytes.toBytes(cid.getFamily()), Bytes.toBytes(cid.getQualifier()));
                        delflag = true;
                    }
                }
                table.put(p1);
                if (delflag) table.delete(del);
            }
        } catch (Exception e) {
            throw new HBaseRunTimeException(e);
        }
    }

    public void put(String tableName, Put put) throws IOException {
        Table table = tablePool.getTable(tableName);
        table.put(put);
    }

    public void addColumn(String tableName, String row, String family, String qualifiar, String value) throws IOException {
        addColumn(tableName, row.getBytes(), family.getBytes(), qualifiar.getBytes(), value.getBytes());
    }

    public void addColumn(String tableName, byte[] row, byte[] family, byte[] qualifiar, byte[] value) throws IOException {
        Put put = new Put(row);
        put.addColumn(family, qualifiar, value);
        put(tableName, put);
    }

    public void putRow(String tableName, String row, List<String[]> datas) throws IOException {
        Put put = new Put(row.getBytes());
        for (String[] data : datas) {
            put.addColumn(data[0].getBytes(), data[1].getBytes(), data[2].getBytes());
        }
        put(tableName, put);
    }

    public void put(TableName tableName, Put put) throws IOException {
        Table table = tablePool.getTable(tableName);
        table.put(put);
    }

    public void putSync(TableName tableName, List<Put> puts) throws IOException {
        Table table = tablePool.getTable(tableName);
        table.put(puts);
    }

    public void putAsync(String tableName, List<Put> puts) throws IOException {
        BufferedMutatorParams params = new BufferedMutatorParams(TableName.valueOf(tableName));
        params.writeBufferSize(AppConfig.instance().getLongProperty("hbase.mutator.buffersize", 6 * 1024 * 1024));
        BufferedMutator mutator = connection.getBufferedMutator(params);
        mutator.mutate(puts);
        mutator.flush();
        mutator.close();
    }

    public <T extends Row> T get(T row) throws HBaseRunTimeException {
        HBaseTable hbtlann = annotateTable(row.getClass());
        String tableName = hbtlann.tableName();
        Result result = get(tableName, row.getRow());
        Row t = rowMapping(result, row.getClass());
        return (T) t;
    }

    public Result get(String tableName, String rowKey) throws HBaseRunTimeException {
        return get(tableName, rowKey.getBytes());
    }

    public Result get(TableName tableName, String rowKey) throws HBaseRunTimeException {
        return get(tableName, rowKey.getBytes());
    }

    public Result get(String tableName, byte[] rowKey) throws HBaseRunTimeException {
        return get(TableName.valueOf(tableName), rowKey);
    }

    public Result get(TableName tableName, byte[] rowKey) throws HBaseRunTimeException {
        try {
            Get get = new Get(rowKey);
            Table table = tablePool.getTable(tableName);
            return table.get(get);
        } catch (IOException e) {
            throw new HBaseRunTimeException(e);
        }
    }

    public void delete(Row row) throws HBaseRunTimeException {
        HBaseTable htblann = annotateTable(row.getClass());
        String tableName = htblann.tableName();
        delete(tableName, row.getRow());
    }

    public void delete(String tableName, String rowKey) throws HBaseRunTimeException {
        delete(tableName, rowKey.getBytes());
    }

    public void deleteColumn(String tableName, String rowKey, String family, String qualifiar) throws HBaseRunTimeException {
        try {
            Table table = tablePool.getTable(tableName);
            Delete delete = new Delete(rowKey.getBytes());
            delete.addColumn(family.getBytes(), qualifiar.getBytes());
            table.delete(delete);
        } catch (IOException e) {
            throw new HBaseRunTimeException(e);
        }
    }

    public void delete(String tableName, byte[] rowKey) throws HBaseRunTimeException {
        try {
            Table table = tablePool.getTable(tableName);
            Delete delete = new Delete(rowKey);
            table.delete(delete);
        } catch (IOException e) {
            throw new HBaseRunTimeException(e);
        }
    }


    public void delete(TableName tableName, List<Delete> dels) throws HBaseRunTimeException {
        try {
            Table table = tablePool.getTable(tableName);
            table.delete(dels);
        } catch (IOException e) {
            throw new HBaseRunTimeException(e);
        }
    }

    public void delete(String tableName, List<Delete> dels) throws HBaseRunTimeException {
        try {
            Table table = tablePool.getTable(tableName);
            table.delete(dels);
        } catch (IOException e) {
            throw new HBaseRunTimeException(e);
        }
    }

    private List<Column> mappingColumns(Class clazz) {
        List<Column> columns = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            HBaseColumn hColumn = field.getAnnotation(HBaseColumn.class);
            if (hColumn != null) {
                if (StringUtils.nonEmpty(hColumn.family()) && StringUtils.nonEmpty(hColumn.qualifier()))
                    columns.add(new Column(hColumn.family(), hColumn.qualifier(), field.getName()));
            }
        }
        return columns;
    }

    private <T> T rowMapping(Result result, Class<T> clazz) throws HBaseRunTimeException {
        /*
        mapping
        */
        List<Column> columns = mappingColumns(clazz);

        if (columns.size() == 0) {
            return null;
        }

        try {
            boolean addflag = false;
            T t = clazz.newInstance();
            for (Column column : columns) {
                String value = Bytes.toString(result.getValue(column.getFamily().getBytes(), column.getQualifier().getBytes()));
                if (StringUtils.nonEmpty(value)) {
                    BeanUtils.copyProperty(t, column.getValue(), value);
                    addflag = true;
                }
            }
            if (addflag) {
                BeanUtils.copyProperty(t, "row", Bytes.toString(result.getRow()));
                return t;
            } else
                return null;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new HBaseRunTimeException(e);
        }
    }

    public ResultScanner scanAll(String tableName) throws HBaseRunTimeException {
        try {
            Scan scan = new Scan();
            Table table = tablePool.getTable(tableName);
            return table.getScanner(scan);
        } catch (IOException e) {
            throw new HBaseRunTimeException(e);
        }
    }

    public boolean createTable(TableName tableName, byte[] start, byte[] end, int regionNum) throws HBaseRunTimeException {
        String familyDefault = AppConfig.instance().getProperty("family.default", "info");
        // 表存在
        try {
            if (admin.tableExists(tableName)) {
                return false;
            }
            // 创建新表
            HTableDescriptor h_table = new HTableDescriptor(tableName);
            // 新列族名
            HColumnDescriptor h_clomun = new HColumnDescriptor(familyDefault);
            h_clomun.setBlocksize(65536);
            h_clomun.setBlockCacheEnabled(true);
            h_clomun.setMaxVersions(2);
            h_table.addFamily(h_clomun);
            // 开始创建表
            if (start == null) {
                admin.createTable(h_table, null);
            } else {
                admin.createTable(h_table, start, end, regionNum);
            }
        } catch (Exception e) {
            throw new HBaseRunTimeException(e);
        }
        return true;
    }

    public boolean createTable(TableName tableName, byte[][] splitBytes) throws HBaseRunTimeException {
        String familyDefault = AppConfig.instance().getProperty("family.default", "info");
        // 表存在
        try {
            if (admin.tableExists(tableName)) {
                return false;
            }
            // 创建新表
            HTableDescriptor h_table = new HTableDescriptor(tableName);
            // 新列族名
            HColumnDescriptor h_clomun = new HColumnDescriptor(familyDefault);
            h_clomun.setBlocksize(65536);
            h_clomun.setBlockCacheEnabled(true);
            h_clomun.setMaxVersions(2);
            h_table.addFamily(h_clomun);
            // 开始创建表
            admin.createTable(h_table, splitBytes);
        } catch (Exception e) {
            throw new HBaseRunTimeException(e);
        }
        return true;
    }

    public boolean createTable(TableName tableName) throws HBaseRunTimeException {
        return createTable(tableName, null, null, 0);
    }

}
