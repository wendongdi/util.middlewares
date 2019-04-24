package wdd.utils.commons;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class BeansUtil extends BeanUtils {
    public static void copy2Field(Object o, String field, Object value) throws InvocationTargetException, IllegalAccessException {
        copyProperty(o, field.substring(0, 1).toLowerCase() + field.substring(1), value);
    }

    public static Collection<String> classFields(Class clazz) {
        Set<String> columns = new HashSet<>();
        for (Field field : clazz.getDeclaredFields()) {
            columns.add(field.getName());
        }
        return columns;
    }

    public static Map<String, Class> typeFields(Class clazz) {
        Map<String, Class> columns = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            columns.put(field.getName(), field.getType());
        }
        return columns;
    }

    public static void printObject(Object info) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        for (Field field : info.getClass().getDeclaredFields()) {
            String name = field.getName();
            String fix_name = name.substring(0, 1).toLowerCase() + name.substring(1);
            System.out.println(name + ":" + BeansUtil.getProperty(info, fix_name));
        }
    }
}
