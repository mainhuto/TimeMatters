package com.example.android.timematters.viewmodel;

import android.util.Log;

import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.TaskCategory;
import com.example.android.timematters.utils.Dependencies;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class TaskListViewModel extends ViewModel {

    private static final String TAG = "TaskListViewModel";

    private final LiveData<List<TaskCategory>> mTasks;
    private final LiveData<List<Dependencies>> dependencies;

    public TaskListViewModel(AppDatabase database, int activityId) {
        Log.d(TAG, "Actively retrieving the tasks from the DataBase for activityId=" + activityId);
        mTasks = database.taskDao().loadActivityTasksCategory(activityId);
        dependencies = database.taskDao().loadTaskDependencies(activityId);
    }

    public LiveData<List<TaskCategory>> getTasks() {
        return mTasks;
    }

    public LiveData<List<Dependencies>> getDependencies() {
        return dependencies;
    }
}
