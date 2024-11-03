package com.emamagic;

import com.emamagic.annotation.Entity;
import com.emamagic.conf.ConfigLoader;
import com.emamagic.conf.DB;
import com.emamagic.conf.ConfigData;
import com.emamagic.conf.DBCommandFactory;
import com.emamagic.connection.ConnectionManager;
import com.emamagic.crud.base.DBCommand;
import com.emamagic.crud.base.Operation;
import com.emamagic.exception.InstanceOfUtilityClassException;

import java.util.*;

public final class EmaDB {

    private static final Map<DB, ConfigData> configMap = ConfigLoader.loadConfigs();

    private EmaDB() {
        throw new InstanceOfUtilityClassException(getClass().getSimpleName());
    }

    public static <T> T upsert(T entity) {
        return executeCommand(entity, Operation.UPSERT);
    }

    public static <T> boolean delete(T entity) {
        return executeCommand(entity, Operation.REMOVE);
    }

    public static <T> List<T> read(Class<T> entityType) {
        return executeReadCommand(entityType);
    }

    private static <T, R> R executeCommand(T entity, Operation operation) {
        DBCommand<T, R> command = createCommandForOperation(entity.getClass(), operation);
        return command.execute(entity);
    }

    private static <T> List<T> executeReadCommand(Class<T> entityType) {
        DBCommand<Class<T>, List<T>> command = createCommandForOperation(entityType, Operation.READ);
        return command.execute(entityType);
    }

    private static <T, R> DBCommand<T, R> createCommandForOperation(Class<?> entityClass, Operation operation) {
        ConfigData config = getConfigForEntity(entityClass);
        DBCommand<T, R> command = DBCommandFactory.createCommand(config, operation);

        if (command == null) {
            throw new UnsupportedOperationException("Unsupported operation for the given database type: ");
        }

        return command;
    }

    private static ConfigData getConfigForEntity(Class<?> entityClass) {
        Entity entityAnnotation = entityClass.getDeclaredAnnotation(Entity.class);

        if (entityAnnotation == null) {
            throw new IllegalArgumentException("Entity class must be annotated with @Entity");
        }

        return Optional.ofNullable(configMap.get(entityAnnotation.db()))
                .orElseThrow(() -> new UnsupportedOperationException("Configuration not found for database type: " + entityAnnotation.db()));
    }

    public static void close() {
        ConnectionManager.getInstance().closeConnections();
    }
}