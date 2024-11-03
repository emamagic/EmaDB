package com.emamagic.crud.postgres;

import com.emamagic.annotation.Transient;
import com.emamagic.conf.ConfigData;
import com.emamagic.crud.base.PostgresDBCommand;
import com.emamagic.util.EntityHelper;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresRemoveCommand<T> extends PostgresDBCommand<T, Boolean> {

    public PostgresRemoveCommand(ConfigData config) {
        super(config);
    }

    @Override
    public Boolean execute(T entity) {
        try {
            DeleteQuery deleteQuery = buildDeleteQuery(entity);
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery.query())) {
                setQueryParameters(statement, deleteQuery.params());
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Failed to execute delete command", e);
        }
    }

    private void setQueryParameters(PreparedStatement stmt, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            stmt.setObject(i + 1, values.get(i));
        }
    }

    private DeleteQuery buildDeleteQuery(T entity) throws IllegalAccessException {
        Class<?> clazz = entity.getClass();
        Field[] fields = clazz.getDeclaredFields();

        StringBuilder whereClause = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Transient.class)) continue;
            Object value = field.get(entity);
            if (value != null) {
                if (!whereClause.isEmpty()) {
                    whereClause.append(" AND ");
                }
                whereClause.append(EntityHelper.findFiledName(field)).append(" = ?");
                parameters.add(value);
            }
        }

        if (whereClause.isEmpty()) {
            throw new IllegalArgumentException("No fields specified for deletion");
        }

        String query = "DELETE FROM " + EntityHelper.findTableName(clazz) + " WHERE " + whereClause;
        return new DeleteQuery(query, parameters);
    }

    public record DeleteQuery(String query, List<Object> params) {
    }
}
