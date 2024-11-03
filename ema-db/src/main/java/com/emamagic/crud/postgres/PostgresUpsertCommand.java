package com.emamagic.crud.postgres;

import com.emamagic.conf.ConfigData;
import com.emamagic.crud.base.PostgresDBCommand;
import com.emamagic.crud.base.TableCreator;
import com.emamagic.util.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresUpsertCommand<T> extends PostgresDBCommand<T, T> {
    private final TableCreator tableCreator;

    public PostgresUpsertCommand(ConfigData config) {
        super(config);
        this.tableCreator = TableCreator.getInstance(connection);
    }

    @Override
    public T execute(T entity) {
        try {
            tableCreator.createTableIfNotExist(entity);
            boolean isExist = isRowExists(entity);
            return isExist ? updateRow(entity) : insertRow(entity);
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Database operation failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private T insertRow(T entity) throws SQLException, IllegalAccessException {
        String query = buildInsertQuery(entity);
        try (PreparedStatement stmt = prepareStatementForInsert(query, entity)) {
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return (T) EntityMapper.from(resultSet).to(entity.getClass());
            }
            throw new RuntimeException("Insertion operation failed");
        }
    }

    @SuppressWarnings("unchecked")
    private T updateRow(T entity) throws SQLException, IllegalAccessException {
        String query = buildUpdateQuery(entity);
        try (PreparedStatement stmt = prepareStatementForUpdate(query, entity)) {
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return (T) EntityMapper.from(resultSet).to(entity.getClass());
            }
            throw new RuntimeException("Update operation failed");
        }
    }

    private String buildInsertQuery(T entity) throws IllegalAccessException {
        String columns = EntityHelper.getColumnNames(entity, false);
        String placeholders = EntityHelper.getPlaceholders(entity, false);
        return "INSERT INTO " + EntityHelper.findTableName(entity.getClass()) + " (" + columns + ") VALUES (" + placeholders + ") RETURNING *";
    }

    private String buildUpdateQuery(T entity) throws IllegalAccessException {
        String updateFields = EntityHelper.getUpdateFields(entity);
        String idField = EntityHelper.findIdField(entity).name();
        return "UPDATE " + EntityHelper.findTableName(entity.getClass()) + " SET " + updateFields + " WHERE " + idField + " = ? RETURNING *";
    }

    private PreparedStatement prepareStatementForInsert(String query, T entity) throws SQLException, IllegalAccessException {
        PreparedStatement stmt = connection.prepareStatement(query);
        EntityHelper.setStatementParameters(stmt, entity, false);
        return stmt;

    }

    private PreparedStatement prepareStatementForUpdate(String query, T entity) throws SQLException, IllegalAccessException {
        PreparedStatement stmt = connection.prepareStatement(query);
        int paramIndex = EntityHelper.setStatementParameters(stmt, entity, false);
        stmt.setObject(paramIndex, EntityHelper.findIdField(entity).value());
        return stmt;

    }

    private boolean isRowExists(T entity) throws SQLException, IllegalAccessException {
        Object id = EntityHelper.findIdField(entity).value();
        if (id == null) return false;

        String query = "SELECT 1 FROM " + EntityHelper.findTableName(entity.getClass()) + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, id);
            if (stmt.executeQuery().next()) return true;
            throw new UnsupportedOperationException("Id is auto-increment, you can not set id manually for update operation");
        }
    }
}