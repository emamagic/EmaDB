package com.emamagic.connection;

import com.emamagic.conf.ConfigData;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class ConnectionFactory {

    static MongoClient createMongoClient(ConfigData config) {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecProvider)
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(config.url()))
                .codecRegistry(pojoCodecRegistry)
                .build();

        try {
            return MongoClients.create(settings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Connection createPostgresConnection(ConfigData config) {
        try {
            return DriverManager.getConnection(config.url(), config.confWrapper().getUsername(), config.confWrapper().getPassword());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
