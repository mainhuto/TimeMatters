package com.example.android.timematters.database;

import androidx.room.TypeConverter;

public class StatusConverter {
    @TypeConverter
    public TaskStatus toStatus(int value) {
        return TaskStatus.values().length > value ? TaskStatus.values()[value] : null;
    }

    @TypeConverter
    public int toInteger(TaskStatus status) {
        return status == null ? TaskStatus.NULL.ordinal() : status.ordinal();
    }
}
