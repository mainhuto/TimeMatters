package com.example.android.timematters.viewmodel;

import android.util.Log;

import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.CategoryEntry;
import com.example.android.timematters.utils.Dependencies;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class CategoryListViewModel extends ViewModel {

    private static final String TAG = "CategoryListViewModel";

    private final LiveData<List<CategoryEntry>> categories;
    private final LiveData<List<Dependencies>> dependencies;

    public CategoryListViewModel(AppDatabase database, int activityId) {
        Log.d(TAG, "Actively retrieving the categories from the DataBase for activityId=" + activityId);
        categories = database.categoryDao().loadActivityCategories(activityId);
        dependencies = database.categoryDao().loadCategoryDependencies(activityId);
    }

    public LiveData<List<CategoryEntry>> getCategories() {
        return categories;
    }

    public LiveData<List<Dependencies>> getDependencies() {
        return dependencies;
    }
}
