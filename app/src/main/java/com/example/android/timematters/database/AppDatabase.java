package com.example.android.timematters.database;

import android.content.Context;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {ActivityEntry.class, CategoryEntry.class, TaskEntry.class, TaskLogEntry.class}, views = {TaskCategory.class, TaskLogView.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class, StatusConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "timematters";
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract ActivityDao activityDao();
    public abstract CategoryDao categoryDao();
    public abstract TaskDao taskDao();
    public abstract TaskLogDao taskLogDao();

}
