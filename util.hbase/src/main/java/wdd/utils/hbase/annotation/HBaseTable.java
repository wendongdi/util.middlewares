package wdd.utils.hbase.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface HBaseTable {
    String tableName();

    String startRow() default "";

    String stopRow() default "";
    /*
    work alone
    */
    String regex() default "";

    String[] contains() default "";

    String startWith() default "";

    int next() default 1000;
}
