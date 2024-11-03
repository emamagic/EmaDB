package com.emamagic.crud.mongo;

import com.emamagic.conf.ConfigData;
import com.emamagic.crud.base.MongoDBCommand;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.lang.reflect.Field;

public class MongoUpsertCommand<T> extends MongoDBCommand<T, T> {

    public MongoUpsertCommand(ConfigData config) {
        super(config);
    }

    @Override
    public T execute(T entity) {
        Class<?> clazz = entity.getClass();
        MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName().toLowerCase());

        throw new RuntimeException("Implement me");
    }

}