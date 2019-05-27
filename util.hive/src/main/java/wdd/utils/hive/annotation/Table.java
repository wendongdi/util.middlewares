package wdd.middle.util.hive.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Table {
    String db();
    String table();
}
