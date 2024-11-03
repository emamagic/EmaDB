package com.emamagic.util;

import com.emamagic.annotation.Column;
import com.emamagic.annotation.Entity;
import com.emamagic.annotation.Id;
import com.emamagic.annotation.Transient;
import com.emamagic.exception.InstanceOfUtilityClassException;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class EntityHelper {

    private EntityHelper() {
        throw new InstanceOfUtilityClassException(getClass().getSimpleName());
    }

    public record IdData(String name, Object value, Field field) {
    }

    public static <T> String getColumnNames(T entity, boolean includeId) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return shouldIncludeField(field, includeId, entity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(EntityHelper::findFiledName)
                .collect(Collectors.joining(", "));
    }

    public static <T> String getPlaceholders(T entity, boolean includeId) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return shouldIncludeField(field, includeId, entity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(field -> "?")
                .collect(Collectors.joining(", "));
    }

    public static <T> String getUpdateFields(T entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return shouldIncludeField(field, false, entity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(field -> EntityHelper.findFiledName(field) + " = ?")
                .collect(Collectors.joining(", "));
    }

    public static <T> int setStatementParameters(PreparedStatement stmt, T entity, boolean includeId) throws SQLException, IllegalAccessException {
        int paramIndex = 1;
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (shouldIncludeField(field, includeId, entity)) {
                stmt.setObject(paramIndex++, field.get(entity));
            }
        }
        return paramIndex;
    }

    private static <T> boolean shouldIncludeField(Field field, boolean includeId, T entity) throws IllegalAccessException {
        return !field.isAnnotationPresent(Transient.class) && (includeId || !field.isAnnotationPresent(Id.class)) && field.get(entity) != null;
    }

    public static <T> String findTableName(Class<T> clazz) {
        Entity entityAnnotation = clazz.getAnnotation(Entity.class);
        if (entityAnnotation.name().isBlank()) {
            return clazz.getSimpleName().toLowerCase().concat("s");
        }
        return entityAnnotation.name();
    }

    public static <T> IdData findIdField(T entity) throws IllegalAccessException {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                return new IdData(findFiledName(field), field.get(entity), field);
            }
        }

        throw new RuntimeException("no @Id found on fields in class: " + clazz.getSimpleName());
    }

    public static String findFiledName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return (column.name().isBlank()) ? field.getName() : column.name();
        }
        return field.getName();
    }
}