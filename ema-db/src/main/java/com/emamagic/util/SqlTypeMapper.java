package com.emamagic.util;

import com.emamagic.exception.InstanceOfUtilityClassException;
import com.emamagic.exception.UnSupportedIdTypeException;
import java.util.HashMap;
import java.util.Map;

public final class SqlTypeMapper {
    private static final Map<Class<?>, String> typeMapping;

    private SqlTypeMapper() {
        throw new InstanceOfUtilityClassException(getClass().getSimpleName());
    }

    static {
        typeMapping = new HashMap<>();
        initializeDefaultMappings();
    }

    private static void initializeDefaultMappings() {
        typeMapping.put(String.class, "VARCHAR(255)");
        typeMapping.put(Integer.class, "INTEGER");
        typeMapping.put(int.class, "INTEGER");
        typeMapping.put(Boolean.class, "BOOLEAN");
        typeMapping.put(boolean.class, "BOOLEAN");
    }

    public static String mapJavaTypeToSqlType(Class<?> type) {
        String sqlType = typeMapping.get(type);
        if (sqlType == null) {
            throw new UnSupportedIdTypeException("Unsupported SQL type mapping for Java type: " + type.getSimpleName());
        }
        return sqlType;
    }
}
