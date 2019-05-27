package wdd.utils.elasticsearch.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Document {
    String index();

    String type();

    String id() default "";

    int scroll() default 60000;
}
