package com.example.android.timematters.database;

import com.example.android.timematters.utils.Dependencies;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM task WHERE activity_id = :activityId")
    LiveData<List<TaskEntry>> loadActivityTasks(int activityId);

    @Query("SELECT * FROM task WHERE id = :Id")
    LiveData<TaskEntry> loadTaskById(int Id);

    @Insert
    void insertTask(TaskEntry taskEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTask(TaskEntry taskEntry);

    @Query("DELETE FROM task WHERE id = :id")
    void deleteTaskById(int id);

    @Query("SELECT * FROM task_category_view WHERE activity_id = :activityId")
    LiveData<List<TaskCategory>> loadActivityTasksCategory(int activityId);

    @Query("SELECT task.id, Count(task_log.id) AS counter FROM task LEFT JOIN task_log ON task.id = task_log.task_id " +
            "WHERE task.activity_id = :id GROUP BY task.id")
    LiveData<List<Dependencies>> loadTaskDependencies(int id);

}
