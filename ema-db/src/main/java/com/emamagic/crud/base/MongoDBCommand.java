package com.emamagic.crud.base;

import com.emamagic.conf.ConfigData;
import com.emamagic.connection.ConnectionManager;
import com.mongodb.client.MongoDatabase;

public abstract class MongoDBCommand<T, R> implements DBCommand<T, R> {
    protected MongoDatabase database;

    public MongoDBCommand(ConfigData config) {
        database = ConnectionManager.getInstance().getMongoDatabase(config);
    }
}
