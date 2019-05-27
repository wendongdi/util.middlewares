package wdd.utils.mongo;

import com.mongodb.*;
import org.apache.commons.beanutils.BeanUtils;
import redis.clients.jedis.JedisCluster;
import wdd.utils.commons.AppConfig;
import wdd.utils.commons.BeansUtil;
import wdd.utils.commons.StringUtils;
import wdd.utils.mongo.annotation.Document;
import wdd.utils.mongo.entity.BaseEntity;
import wdd.utils.redis.RedisClient;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class MongoClient {
    private com.mongodb.MongoClient client;
    private static JedisCluster redisClient = null;

    public MongoClient(com.mongodb.MongoClient client) throws IOException {
        this.client = client;
        //配置内置的缓存
        synchronized (MongoClient.class) {
            if (redisClient == null) {
                redisClient = RedisClient.newRedisClient(AppConfig.listProperties("mongoredis.properties"));
            }
        }
    }

    public MongoClient() throws Exception {
        this.client = MongoAccessor.createClient();
        //配置内置的缓存
        synchronized (MongoClient.class) {
            if (redisClient == null) {
                redisClient = RedisClient.newRedisClient(AppConfig.listProperties("mongoredis.properties"));
            }
        }
    }

    public <T extends BaseEntity> List<T> queryAll(Class<T> entityClass) throws Exception {
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

    private <T> DBCollection collection(String db, String document) throws IOException {
        document = "" + document.substring(0, 1).toLowerCase() + document.substring(1);
        return client.getDB(db).getCollection(document);
    }

    private List<BasicDBObject> toBaseEntity(DBCursor cursor) {
        List<BasicDBObject> ress = new ArrayList<>();
        for (DBObject dbObject : cursor) {
            ress.add(new BasicDBObject(dbObject.toMap()));
        }
        return ress;
    }

    private <T extends BaseEntity> List<T> toBaseEntity(DBCursor cursor, Class<T> entityClass) throws Exception {
        List<T> ress = new ArrayList<>();
        for (DBObject dbObject : cursor) {
            ress.add(toObject(dbObject, entityClass));
        }
        return ress;
    }

    public <T> T toObject(Object dbObject, Class<T> entityClass) throws Exception {
        if (dbObject instanceof DBObject) {
            T res = entityClass.newInstance();
            for (Object obj : ((DBObject) dbObject).toMap().entrySet()) {
                Map.Entry kv = (Map.Entry) obj;
                String key = kv.getKey().toString();
                Object value = kv.getValue();
                if (value instanceof BasicDBObject) {
                    BasicDBObject fieldVal = (BasicDBObject) value;
                    Field field = res.getClass().getField(key);
                    Class feildType = field.getType();
                    BeanUtils.copyProperty(res, key, toObject(fieldVal, feildType));
                } else if (value instanceof BasicDBList) {
                    BasicDBList fieldVal = (BasicDBList) value;
                    Field field = res.getClass().getField(key);
                    Class feildType = field.getType();
                    if (feildType.isArray()) {
                        Class compType = feildType.getComponentType();
                        Object array = Array.newInstance(compType, fieldVal.size());
                        for (int i = 0; i < fieldVal.size(); i++) {
                            Array.set(array, i, toObject(fieldVal.get(i), compType));
                        }
                        BeanUtils.copyProperty(res, key, array);
                    } else {
                        Type fc = field.getGenericType();
                        Class compType;
                        if (fc == null) {
                            compType = Object.class;
                        } else {
                            ParameterizedType pt = (ParameterizedType) fc;
                            compType = (Class) pt.getActualTypeArguments()[0];
                        }
                        Collection collection;

                        if (feildType.equals(List.class)) {
                            collection = new ArrayList();
                        } else if (feildType.equals(Set.class)) {
                            collection = new HashSet();
                        } else {
                            collection = (Collection) feildType.newInstance();
                        }

                        for (Object o : fieldVal) {
                            collection.add(toObject(o, compType));
                        }
                        BeanUtils.copyProperty(res, key, collection);
                    }
                } else {
                    BeanUtils.copyProperty(res, key, kv.getValue());
                }
            }
            return res;
        } else {
            return entityClass.cast(dbObject);
        }
    }

    public <T extends BaseEntity> List<T> query(String key, Object value, Class<T> entityClass) throws Exception {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        return toBaseEntity(collection(entityClass).find(new BasicDBObject(key, value)), entityClass);
    }

    public <T extends BaseEntity> List<T> queryByID(String value, Class<T> entityClass) throws Exception {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        return toBaseEntity(collection(entityClass).find(new BasicDBObject("_id", value)), entityClass);
    }

    public List<BasicDBObject> queryByID(String value, String entityClass) throws Exception {
        String[] doc = entityClass.split("\\.");
        assert doc.length > 1;
        return toBaseEntity(collection(doc[0], doc[1]).find(new BasicDBObject("_id", value)));
    }

    public <T extends BaseEntity> List<T> query(Map<String, Object> query, Class<T> entityClass) throws Exception {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        return toBaseEntity(collection(entityClass).find(new BasicDBObject(query)), entityClass);
    }

    private <T extends BaseEntity> List<T> queryCache(Map<String, Object> query, Class<T> entityClass) throws Exception {
        Document doc = entityClass.getAnnotation(Document.class);
        if (doc != null && doc.isCache()) {
            return query(query, entityClass);
        } else {
            return new ArrayList<>();
        }
    }

    public List<BasicDBObject> query(Map<String, Object> query, String entityClass) throws Exception {
        String[] doc = entityClass.split("\\.");
        assert doc.length > 1;
        return toBaseEntity(collection(doc[0], doc[1]).find(new BasicDBObject(query)));
    }

    public <T extends BaseEntity> void save(T entity) throws Exception {
        assert entity.getClass().isAssignableFrom(BaseEntity.class);
        if (StringUtils.isEmpty(entity.get_id())) {
            entity.set_id(EncryptUtil.newId());
        }
        Map entityMap = BeansUtil.obj2Map(entity);
        BasicDBObject update = new BasicDBObject(entityMap).append("createTime", new Date()).append("updateTime", new Date());
        collection(entity.getClass()).insert(update);
    }

    public <T extends BaseEntity> void remove(Map<String, Object> query, Class<T> entityClass) throws Exception {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        List<T> entities = queryCache(query, entityClass);
        collection(entityClass).remove(new BasicDBObject(query));
        for (T t : entities) {
            cacheClear(t);
        }
    }

    public <T extends BaseEntity> void set(Map<String, Object> query, Map<String, Object> update, Class<T> entityClass) throws Exception {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        List<T> entities = queryCache(query, entityClass);
        collection(entityClass).update(new BasicDBObject(query), new BasicDBObject("$set", update), false, true);
        for (T t : entities) {
            cacheClear(t);
        }
    }

    public <T extends BaseEntity> void update(Map<String, Object> query, Map<String, Object> update, Class<T> entityClass) throws Exception {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        List<T> entities = queryCache(query, entityClass);
        collection(entityClass).update(new BasicDBObject(query), new BasicDBObject("$set", update).append("$currentDate", new BasicDBObject("updateTime", true)), false, true);
        for (T t : entities) {
            cacheClear(t);
        }
    }

    public <T extends BaseEntity> void updateNotSet(Map<String, Object> query, Map<String, Object> update, Class<T> entityClass) throws Exception {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        List<T> entities = queryCache(query, entityClass);
        collection(entityClass).update(new BasicDBObject(query), new BasicDBObject(update), false, true);
        for (T t : entities) {
            cacheClear(t);
        }
    }

    public <T extends BaseEntity> void replace(Map<String, Object> query, Map<String, Object> update, Class<T> entityClass) throws Exception {
        assert entityClass.isAssignableFrom(BaseEntity.class);
        List<T> entities = queryCache(query, entityClass);
        collection(entityClass).update(new BasicDBObject(query), new BasicDBObject("$set", update).append("$currentDate", new BasicDBObject("createTime", true).append("updateTime", true)), true, true);
        for (T t : entities) {
            cacheClear(t);
        }
    }

    public void cacheClear(BaseEntity entity) {
        redisClient.del(entity.get_class() + entity.get_id());
    }

    public String cacheString(BaseEntity entity) {
        return redisClient.get(entity.get_class() + entity.get_id());
    }


    public void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}
