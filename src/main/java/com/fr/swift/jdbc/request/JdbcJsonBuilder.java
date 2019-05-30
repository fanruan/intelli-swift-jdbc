package com.fr.swift.jdbc.request;

import com.fr.swift.api.info.RequestInfo;
import com.fr.swift.base.json.annotation.JsonProperty;
import com.fr.swift.util.ReflectUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;

/**
 * @author yee
 */
public class JdbcJsonBuilder {

    public static String build(RequestInfo requestInfo) {
        StringBuffer buffer = new StringBuffer("{");
        buildClassFieldJson(buffer, requestInfo.getClass(), requestInfo);
        if (buffer.toString().endsWith(",")) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("}");
        return buffer.toString();
    }

    private static void buildClassFieldJson(StringBuffer buffer, Class clazz, Object o) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(JsonProperty.class)) {
                JsonProperty property = field.getAnnotation(JsonProperty.class);
                String propertyName = property.value();
                try {
                    field.setAccessible(true);
                    Object obj = field.get(o);
                    if (null != obj) {
                        buffer.append("\"").append(propertyName).append("\": ").append(getString(obj)).append(",");
                    }
                } catch (IllegalAccessException ignore) {
                }
            }
        }
        if (null != clazz.getSuperclass()) {
            buildClassFieldJson(buffer, clazz.getSuperclass(), o);
        }
    }

    /**
     * @param object
     * @return
     */
    private static String getString(Object object) {
        if (object instanceof Date) {
            return String.valueOf(((Date) object).getTime());
        }
        Class clazz = object.getClass();
        if (ReflectUtils.isPrimitiveOrWrapper(clazz)) {
            return object.toString();
        }
        StringBuffer buffer = new StringBuffer();
        if (object instanceof Collection) {
            buffer.append("[");
            for (Object o : ((Collection) object)) {
                buffer.append(getString(o)).append(",");
            }
            buffer.setLength(buffer.length() - 1);
            buffer.append("]");
            return buffer.toString();
        }
        if (object instanceof Enum) {
            return "\"" + ((Enum) object).name() + "\"";
        }
        buildClassFieldJson(buffer, clazz, object);
        if (buffer.length() > 0) {
            buffer.insert(0, "{");
            buffer.setLength(buffer.length() - 1);
            buffer.append("}");
            return buffer.toString();
        }
        return "\"" + object.toString() + "\"";
    }

}
