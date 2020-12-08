package com.example.android.timematters.viewmodel;

import com.example.android.timematters.database.AppDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TaskViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final Integer mTaskId;

    public TaskViewModelFactory(AppDatabase db, @Nullable Integer taskId) {
        mDb = db;
        mTaskId = taskId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TaskViewModel(mDb, mTaskId);
    }
}
