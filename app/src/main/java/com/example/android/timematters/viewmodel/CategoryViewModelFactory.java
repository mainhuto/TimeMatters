package com.example.android.timematters.viewmodel;

import com.example.android.timematters.database.AppDatabase;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class CategoryViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final Integer mCategoryId;

    public CategoryViewModelFactory(AppDatabase db, Integer categoryId) {
        mDb = db;
        mCategoryId = categoryId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CategoryViewModel(mDb, mCategoryId);
    }
}
