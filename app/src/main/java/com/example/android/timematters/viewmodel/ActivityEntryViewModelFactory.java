package com.example.android.timematters.viewmodel;

import com.example.android.timematters.database.AppDatabase;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ActivityEntryViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int mActivityId;

    public ActivityEntryViewModelFactory(AppDatabase db, int activityId) {
        mDb = db;
        mActivityId = activityId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ActivityEntryViewModel(mDb, mActivityId);
    }
}
