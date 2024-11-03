package com.emamagic.util;

import com.emamagic.annotation.Transient;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

public class EntityMapper {
    private final ResultSet resultSet;

    private EntityMapper(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public static EntityMapper from(ResultSet resultSet) {
        return new EntityMapper(resultSet);
    }

    public <T> T to(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (!field.isAnnotationPresent(Transient.class)) {
                    var columnName = EntityHelper.findFiledName(field);
                    if (resultSetHasColumn(resultSet, columnName)) {
                        Object value = resultSet.getObject(columnName);
                        if (field.getType().isEnum() && value != null) {
                            value = Enum.valueOf(field.getType().asSubclass(Enum.class), value.toString());
                        }
                        // @Id
                        if (value instanceof UUID) {
                            value = value.toString();
                        }
                        field.set(instance, value);
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean resultSetHasColumn(ResultSet resultSet, String columnName) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (metaData.getColumnName(i).equalsIgnoreCase(columnName)) {
                return true;
            }
        }
        return false;
    }
}