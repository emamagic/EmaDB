package com.emamagic.entity;

import com.emamagic.annotation.Entity;
import com.emamagic.annotation.Id;
import com.emamagic.conf.DB;
import org.bson.types.ObjectId;

import java.time.Instant;

@Entity(db = DB.MONGODB, name = "server_logs")
public class Log {
    @Id
    private ObjectId id;
    private String action;
    private Instant createdAt;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
