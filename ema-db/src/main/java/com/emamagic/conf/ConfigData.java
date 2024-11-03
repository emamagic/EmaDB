package com.emamagic.conf;

public record ConfigData(IConfig confWrapper, DB db) {
    public String url() {
        return switch (db) {
            case DB.POSTGRESQL ->
                    String.format("jdbc:%s://%s:%s/%s", db.name().toLowerCase(), confWrapper.getHost(), confWrapper.getPort(), confWrapper.getDBName());
            case DB.MONGODB ->
                    String.format("%s://%s:%s@%s:%s", db.name().toLowerCase(), confWrapper.getUsername(), confWrapper.getPassword(), confWrapper.getHost(), confWrapper.getPort());
        };
    }
}
