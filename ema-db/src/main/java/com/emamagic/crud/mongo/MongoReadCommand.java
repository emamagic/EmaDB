package com.emamagic.crud.mongo;

import com.emamagic.conf.ConfigData;
import com.emamagic.crud.base.MongoDBCommand;
import com.emamagic.util.EntityHelper;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.List;

public class MongoReadCommand<T> extends MongoDBCommand<Class<T>, List<T>> {

    public MongoReadCommand(ConfigData config) {
        super(config);
    }

    @Override
    public List<T> execute(Class<T> entityClass) {
        MongoCollection<Document> collection = database.getCollection(EntityHelper.findTableName(entityClass));

        throw new RuntimeException("Implement me");
    }

}
