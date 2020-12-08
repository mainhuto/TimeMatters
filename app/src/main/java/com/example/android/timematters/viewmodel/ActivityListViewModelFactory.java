package com.example.android.timematters.viewmodel;

import com.example.android.timematters.database.AppDatabase;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ActivityListViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDatabase;
    private final boolean mActiveActivities;

    public ActivityListViewModelFactory(AppDatabase database, boolean activeActivities) {
        this.mDatabase = database;
        this.mActiveActivities = activeActivities;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ActivityListViewModel(mDatabase, mActiveActivities);
    }
}
