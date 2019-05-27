package wdd.utils.commons;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;

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

    public static void printObject(Object info) {
        List<Field> fieldList = new ArrayList<>();
        Class tempClass = info.getClass();
        while (tempClass != null) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }

        for (Field field : fieldList) {
            String name = field.getName();
            String fixName = name.substring(0, 1).toLowerCase() + name.substring(1);
            try {
                System.out.println(name + ":" + BeansUtil.getProperty(info, fixName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Object> obj2Map(Object info) {
        Map<String, Object> map = new HashMap<>();

        List<Field> fieldList = new ArrayList<>();
        Class tempClass = info.getClass();
        while (tempClass != null) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }

        for (Field field : fieldList) {
            String name = field.getName();
            String fixName = name.substring(0, 1).toLowerCase() + name.substring(1);
            try {
                Object value = BeanUtilsBean.getInstance().getPropertyUtils().getNestedProperty(info, fixName);
                if (value != null) {
                    map.put(name, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static void copyProperty(final Object bean, final String name, final Object value)
            throws IllegalAccessException, InvocationTargetException {



        BeanUtilsBean.getInstance().copyProperty(bean, name, value);
    }
}
