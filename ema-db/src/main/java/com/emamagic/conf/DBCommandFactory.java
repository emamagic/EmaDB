package com.emamagic.conf;

import com.emamagic.crud.base.DBCommand;
import com.emamagic.crud.base.Operation;
import com.emamagic.crud.mongo.MongoRemoveCommand;
import com.emamagic.crud.mongo.MongoReadCommand;
import com.emamagic.crud.mongo.MongoUpsertCommand;
import com.emamagic.crud.postgres.PostgresRemoveCommand;
import com.emamagic.crud.postgres.PostgresReadCommand;
import com.emamagic.crud.postgres.PostgresUpsertCommand;
import com.emamagic.exception.InstanceOfUtilityClassException;

@SuppressWarnings("unchecked")
public final class DBCommandFactory {

    private DBCommandFactory() {
        throw new InstanceOfUtilityClassException(getClass().getSimpleName());
    }

    public static <T, R> DBCommand<T, R> createCommand(ConfigData config, Operation operation) {
        return switch (config.db()) {
            case DB.MONGODB -> createMongoCommand(operation, config);
            case DB.POSTGRESQL -> createPostgresCommand(operation, config);
        };
    }

    private static <T, R> DBCommand<T, R> createMongoCommand(Operation operation, ConfigData config) {
        return switch (operation) {
            case Operation.UPSERT -> (DBCommand<T, R>) new MongoUpsertCommand<T>(config);
            case Operation.REMOVE -> (DBCommand<T, R>) new MongoRemoveCommand<T>(config);
            case Operation.READ -> (DBCommand<T, R>) new MongoReadCommand<T>(config);
        };
    }

    private static <T, R> DBCommand<T, R> createPostgresCommand(Operation operation, ConfigData config) {
        return switch (operation) {
            case Operation.UPSERT -> (DBCommand<T, R>) new PostgresUpsertCommand<>(config);
            case Operation.REMOVE -> (DBCommand<T, R>) new PostgresRemoveCommand<>(config);
            case Operation.READ -> (DBCommand<T, R>) new PostgresReadCommand<>(config);
        };
    }
}
