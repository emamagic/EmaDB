package com.emamagic.conf;

import com.emamagic.annotation.Config;

@Config(db = DB.MONGODB)
public class MongoConfig implements IConfig {

    @Override
    public String getHost() {
        return "localhost";
    }

    @Override
    public String getPort() {
        return "27017";
    }

    @Override
    public String getDBName() {
        return "test";
    }

    @Override
    public String getUsername() {
        return "emamagic";
    }

    @Override
    public String getPassword() {
        return "1377";
    }
}
