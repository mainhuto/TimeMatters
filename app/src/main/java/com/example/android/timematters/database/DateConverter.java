package com.example.android.timematters.database;


import java.util.Date;

import androidx.room.TypeConverter;

public class DateConverter {
    @TypeConverter
    public Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
