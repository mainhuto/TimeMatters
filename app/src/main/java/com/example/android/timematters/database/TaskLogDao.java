package com.example.android.timematters.database;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TaskLogDao {

    @Query("SELECT * FROM task_log_view WHERE id = :Id")
    TaskLogView loadTaskLogViewById(int Id);

    @Query("SELECT * FROM task_log_view WHERE status != 3")
    TaskLogView loadTaskLogInProgress();

    @Insert
    void insertTask(TaskLogEntry taskLogEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTaskLog(TaskLogEntry taskLogEntry);

    @Query("DELETE FROM task_log WHERE date < :date")
    void deleteTaskLogByDate(Date date);

    @Query("SELECT * FROM task_log_view WHERE activity_id = :activityId AND (date >= :date OR status != :taskStatus)")
    LiveData<List<TaskLogView>> loadActivityTaskLogsView(int activityId, Date date, TaskStatus taskStatus);

    @Query("SELECT * FROM task_log_view WHERE status != 3")
    LiveData<TaskLogView> loadLiveTaskLogInProgress();


}
