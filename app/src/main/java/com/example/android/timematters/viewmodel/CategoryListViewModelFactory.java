package com.example.android.timematters.viewmodel;

import com.example.android.timematters.database.AppDatabase;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class CategoryListViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDatabase;
    private final int mActivityId;

    public CategoryListViewModelFactory(AppDatabase database, int activityId) {
        this.mDatabase = database;
        this.mActivityId = activityId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CategoryListViewModel(mDatabase, mActivityId);
    }
}
