package com.example.android.timematters.viewmodel;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class ActivityEntryViewModel extends ViewModel {

    private final LiveData<ActivityEntry> mActivityEntry;

    public ActivityEntryViewModel(AppDatabase database, int activityId) {
        mActivityEntry = database.activityDao().loadActivityById(activityId);
    }

    public LiveData<ActivityEntry> getActivityEntry() {
        return mActivityEntry;
    }
}
