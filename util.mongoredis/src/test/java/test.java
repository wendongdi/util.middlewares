import com.mongodb.BasicDBObject;
import wdd.utils.mongo.MongoUtil;

public class test {

    public static void main(String[] args) throws Exception {
        MongoUtil.instance().update(new BasicDBObject("_id", "I3ANz096QMM"), new BasicDBObject("count", 5000), CustomMobileTag.class);
    }
}
