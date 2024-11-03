package com.emamagic.crud.base;

import com.emamagic.annotation.Column;
import com.emamagic.annotation.Id;
import com.emamagic.annotation.Transient;
import com.emamagic.annotation.Unique;
import com.emamagic.exception.UnSupportedIdTypeException;
import com.emamagic.util.EntityHelper;
import com.emamagic.util.SqlTypeMapper;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class TableCreator {
    private static volatile TableCreator instance;
    private final Connection connection;

    private TableCreator(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "Connection cannot be null");
    }

    public static TableCreator getInstance(Connection connection) {
        if (instance == null) {
            synchronized (TableCreator.class) {
                if (instance == null) {
                    instance = new TableCreator(connection);
                }
            }
        }
        return instance;
    }

    public <T> void createTableIfNotExist(T entity) throws SQLException, IllegalAccessException {
        Class<?> clazz = entity.getClass();
        Field[] fields = clazz.getDeclaredFields();

        String tableName = EntityHelper.findTableName(clazz);
        String idName = EntityHelper.findIdField(entity).name();

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" (")
                .append(idName);

        appendPrimaryKeyDefinition(queryBuilder, entity);

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Transient.class) || field.isAnnotationPresent(Id.class)) continue;
            String columnName = EntityHelper.findFiledName(field);
            String columnType = SqlTypeMapper.mapJavaTypeToSqlType(field.getType());
            queryBuilder.append(columnName)
                    .append(" ")
                    .append(columnType);
            if (field.isAnnotationPresent(Unique.class)) {
                queryBuilder.append(" UNIQUE");
            }
            if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).nullable()) {
                queryBuilder.append(" NOT NULL");
            }
            queryBuilder.append(", ");
        }
        // Remove last comma
        queryBuilder.setLength(queryBuilder.length() - 2);
        queryBuilder.append(")");

        executeStatement(queryBuilder.toString());
    }

    private <T> void appendPrimaryKeyDefinition(StringBuilder queryBuilder, T entity) throws IllegalAccessException {
        Field id = EntityHelper.findIdField(entity).field();
        if (id.getType().equals(Integer.class) || id.getType().equals(int.class)) {
            queryBuilder.append(" SERIAL PRIMARY KEY, ");
        } else if (id.getType().equals(String.class)) {
            queryBuilder.append(" UUID PRIMARY KEY DEFAULT gen_random_uuid(), ");
        } else {
            throw new UnSupportedIdTypeException("Unsupported primary key type, Supported types: Integer and String");
        }
    }

    private void executeStatement(String sql) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
}

