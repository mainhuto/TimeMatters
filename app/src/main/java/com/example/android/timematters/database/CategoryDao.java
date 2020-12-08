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
public interface CategoryDao {

    @Query("SELECT * FROM category WHERE activity_id = :activityId")
    LiveData<List<CategoryEntry>> loadActivityCategories(int activityId);

    @Query("SELECT * FROM category WHERE id = :id")
    LiveData<CategoryEntry> loadCategoryById(int id);

    @Query("SELECT category.id, Count(task.id) AS counter FROM category LEFT JOIN task ON category.id = task.category_id " +
            "WHERE category.activity_id = :id GROUP BY category.id")
    LiveData<List<Dependencies>> loadCategoryDependencies(int id);

    @Insert
    void insertCategory(CategoryEntry categoryEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCategory(CategoryEntry categoryEntry);

    @Query("DELETE FROM category WHERE id = :id")
    void deleteCategoryById(int id);

}
