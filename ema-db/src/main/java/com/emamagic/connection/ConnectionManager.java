package com.emamagic.connection;

import com.emamagic.conf.ConfigData;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    private volatile static ConnectionManager instance;
    private static MongoDatabase mongoDatabase;
    private static Connection postgresConnection;
    private static MongoClient mongoClient;

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        if (instance == null) synchronized (ConnectionManager.class) {
            if (instance == null) {
                instance = new ConnectionManager();
            }
        }

        return instance;
    }

    public MongoDatabase getMongoDatabase(ConfigData config) {
        if (mongoDatabase == null) {
            mongoClient = ConnectionFactory.createMongoClient(config);
            mongoDatabase = mongoClient.getDatabase(config.confWrapper().getDBName());
        }

        return mongoDatabase;
    }

    public Connection getPostgresConnection(ConfigData config) {
        if (postgresConnection == null) {
            postgresConnection = ConnectionFactory.createPostgresConnection(config);
        }

        return postgresConnection;
    }

    public void closeConnections() {
        if (mongoClient != null) {
            mongoClient.close();
        }
        if (postgresConnection != null) {
            try {
                postgresConnection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}