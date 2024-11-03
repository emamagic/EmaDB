package com.emamagic.crud.mongo;

import com.emamagic.conf.ConfigData;
import com.emamagic.crud.base.MongoDBCommand;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoRemoveCommand<T> extends MongoDBCommand<T, Boolean> {

    public MongoRemoveCommand(ConfigData config) {
        super(config);
    }

    @Override
    public Boolean execute(T entity) {
        Class<?> clazz = entity.getClass();
        MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName().toLowerCase());

        throw new RuntimeException("implement me");
    }

}
