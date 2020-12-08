package com.example.android.timematters.viewmodel;

import com.example.android.timematters.database.AppDatabase;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TaskLogListViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDatabase;
    private final int mActivityId;
    private final int mDays;

    public TaskLogListViewModelFactory(AppDatabase database, int activityId, int days) {
        this.mDatabase = database;
        this.mActivityId = activityId;
        this.mDays = days;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TaskLogListViewModel(mDatabase, mActivityId, mDays);
    }
}
