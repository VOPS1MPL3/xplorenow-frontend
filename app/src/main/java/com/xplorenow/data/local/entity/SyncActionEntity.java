package com.xplorenow.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_actions")
public class SyncActionEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String type;
    private long targetId;
    private long timestamp;

    public SyncActionEntity(String type, long targetId) {
        this.type = type;
        this.targetId = targetId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters y Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getTargetId() { return targetId; }
    public void setTargetId(long targetId) { this.targetId = targetId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
