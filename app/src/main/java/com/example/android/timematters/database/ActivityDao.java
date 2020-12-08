package com.example.android.timematters.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ActivityDao {

    @Query("SELECT * FROM activity ORDER BY id")
    LiveData<List<ActivityEntry>> loadAllActivities();

    @Query("SELECT * FROM activity WHERE active = :active ORDER BY id")
    LiveData<List<ActivityEntry>> loadActivitiesByActive(boolean active);

    @Query("SELECT * FROM activity WHERE active = 1 AND  ( (latitude is not null AND longitude is not null) OR (start_time is not null AND finish_time is not null) )")
    LiveData<List<ActivityEntry>> loadLinkedActivities();

    @Query("SELECT * FROM activity WHERE id = :id")
    LiveData<ActivityEntry> loadActivityById(int id);

    @Query("SELECT * FROM activity WHERE id = :id")
    ActivityEntry getActivityById(int id);

    @Insert
    void insertActivity(ActivityEntry activityEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateActivity(ActivityEntry activityEntry);

}
