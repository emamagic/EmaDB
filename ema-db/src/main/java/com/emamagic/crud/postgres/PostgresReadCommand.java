package com.emamagic.crud.postgres;

import com.emamagic.conf.ConfigData;
import com.emamagic.crud.base.PostgresDBCommand;
import com.emamagic.util.EntityHelper;
import com.emamagic.util.EntityMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresReadCommand<T> extends PostgresDBCommand<Class<T>, List<T>> {

    public PostgresReadCommand(ConfigData config) {
        super(config);
    }

    @Override
    public List<T> execute(Class<T> entityClass) {
        String query = "SELECT * FROM " + EntityHelper.findTableName(entityClass);
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet resultSet = stmt.executeQuery();

            List<T> entities = new ArrayList<>();
            while (resultSet.next()) {
                entities.add(EntityMapper.from(resultSet).to(entityClass));
            }

            return entities;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
