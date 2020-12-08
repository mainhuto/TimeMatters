package com.example.android.timematters.viewmodel;

import android.util.Log;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class ActivityListViewModel extends ViewModel {

    public static final String TAG = "ActivityListViewModel";

    private final LiveData<List<ActivityEntry>> mActivities;

    public ActivityListViewModel(AppDatabase database, boolean activeActivities) {
        Log.d(TAG, "Actively retrieving the activities from the DataBase with =" + activeActivities);
        mActivities = database.activityDao().loadActivitiesByActive(activeActivities);
    }

    public LiveData<List<ActivityEntry>> getActivities() {
        return mActivities;
    }
}
