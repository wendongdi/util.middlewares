package wdd.utils.mongo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import wdd.utils.mongo.annotation.Document;
import wdd.utils.mongo.entity.BaseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MongoClient {
    private com.mongodb.MongoClient client;

    public MongoClient(com.mongodb.MongoClient client) {
        this.client = client;
    }

    public MongoClient() throws IOException {
        this.client = MongoAccessor.createClient();
    }

    public <T extends BaseEntity> List<T> queryAll(Class<T> entityClass) throws IOException {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        return toBaseEntity(collection(entityClass).find(), entityClass);
    }

    private <T> DBCollection collection(Class<T> clazz) throws IOException {
        Document doc = clazz.getAnnotation(Document.class);
        String document = doc == null ? clazz.getName() : doc.document();
        String db = doc == null ? clazz.getPackage().getName() : doc.db();
        document = "" + document.substring(0, 1).toLowerCase() + document.substring(1);
        return client.getDB(db).getCollection(document);
    }

    private <T> DBCollection collection(String db,String document) throws IOException {
        document = "" + document.substring(0, 1).toLowerCase() + document.substring(1);
        return client.getDB(db).getCollection(document);
    }

    private JSONArray toBaseEntity(DBCursor cursor) {
        JSONArray ress = new JSONArray();
        for (DBObject dbObject : cursor) {
            ress.add(new JSONObject(dbObject.toMap()));
        }
        return ress;
    }

    private <T extends BaseEntity> List<T> toBaseEntity(DBCursor cursor, Class<T> entityClass) {
        List<T> ress = new ArrayList<>();
        for (DBObject dbObject : cursor) {
            ress.add(new JSONObject(dbObject.toMap()).toJavaObject(entityClass));
        }
        return ress;
    }

    public <T extends BaseEntity> List<T> query(String key, Object value, Class<T> entityClass) throws IOException {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        return toBaseEntity(collection(entityClass).find(new BasicDBObject(key, value)), entityClass);
    }

    public <T extends BaseEntity> List<T> queryByID(String value, Class<T> entityClass) throws IOException {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        return toBaseEntity(collection(entityClass).find(new BasicDBObject("_id", value)), entityClass);
    }

    public JSONArray queryByID(String value, String entityClass) throws IOException {
        String[] doc = entityClass.split("\\.");
        assert doc.length > 1;
        return toBaseEntity(collection(doc[0],doc[1]).find(new BasicDBObject("_id", value)));
    }

    public <T extends BaseEntity> List<T> query(Map<String, Object> query, Class<T> entityClass) throws IOException {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        return toBaseEntity(collection(entityClass).find(new BasicDBObject(query)), entityClass);
    }

    public JSONArray query(Map<String, Object> query, String entityClass) throws IOException {
        String[] doc = entityClass.split("\\.");
        assert doc.length > 1;
        return toBaseEntity(collection(doc[0],doc[1]).find(new BasicDBObject(query)));
    }

    public <T extends BaseEntity> void save(T entity) throws IOException {
        assert entity.getClass().isAssignableFrom(BaseEntity.class);
        BasicDBObject update = new BasicDBObject(JSON.parseObject(JSON.toJSONString(entity))).append("_id",EncryptUtil.newId()).append("createTime", new Date()).append("updateTime", new Date());
        collection(entity.getClass()).insert(update);
    }

    public <T extends BaseEntity> void remove(Map<String, Object> query, Class<T> entityClass) throws IOException {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        collection(entityClass).remove(new BasicDBObject(query));
    }

    public <T extends BaseEntity> void set(Map<String, Object> query, Map<String, Object> update, Class<T> entityClass) throws IOException {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        collection(entityClass).update(new BasicDBObject(query), new BasicDBObject("$set", update), false, true);
    }

    public <T extends BaseEntity> void update(Map<String, Object> query, Map<String, Object> update, Class<T> entityClass) throws IOException {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        collection(entityClass).update(new BasicDBObject(query), new BasicDBObject("$set", update).append("$currentDate", new BasicDBObject("updateTime", true)), false, true);
    }

    public <T extends BaseEntity> void replace(Map<String, Object> query, Map<String, Object> update, Class<T> entityClass) throws IOException {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        collection(entityClass).update(new BasicDBObject(query), new BasicDBObject("$set", update).append("$currentDate", new BasicDBObject("createTime", true).append("updateTime", true)), true, true);
    }

    public void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}
