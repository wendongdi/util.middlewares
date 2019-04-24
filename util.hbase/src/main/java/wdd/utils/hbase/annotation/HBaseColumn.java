package wdd.utils.hbase.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface HBaseColumn {
    String family();

    String qualifier();
}
