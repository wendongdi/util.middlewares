package wdd.utils.mongo.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Document {
    String db();

    String document();

    boolean isCache();
}
