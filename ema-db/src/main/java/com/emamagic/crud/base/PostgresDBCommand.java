package com.emamagic.crud.base;

import com.emamagic.conf.ConfigData;
import com.emamagic.connection.ConnectionManager;

import java.sql.Connection;

public abstract class PostgresDBCommand<T, R> implements DBCommand<T, R> {
    protected Connection connection;

    public PostgresDBCommand(ConfigData config) {
        connection = ConnectionManager.getInstance().getPostgresConnection(config);
    }
}
