package wdd.utils.mongo.entity;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonDateSerializer extends JsonSerializer<Date> {
    public JsonDateSerializer() {
    }

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(format(date, "yyyy-MM-dd HH:mm:ss"));
    }

    public static String format(Date date, String pattern) {
        if (date == null) {
            return "null";
        } else {
            if (pattern == null || "".equals(pattern) || "null".equals(pattern)) {
                pattern = "yyyy-MM-dd HH:mm:ss";
            }

            return (new SimpleDateFormat(pattern)).format(date);
        }
    }
}
