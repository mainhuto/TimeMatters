package com.example.android.timematters.viewmodel;

import android.app.Application;
import android.util.Log;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = "ViewModelTest";

    private final LiveData<List<ActivityEntry>> activities;
    private final LiveData<List<ActivityEntry>> linkedActivities;
    private boolean mActiveActivities;

    public MainViewModel(@NonNull Application application) {
        super(application);

        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the non active activities from the DataBase");
        activities = database.activityDao().loadActivitiesByActive(false);
        linkedActivities = database.activityDao().loadLinkedActivities();
        mActiveActivities = true;
    }

    public LiveData<List<ActivityEntry>> getActivities() {
        return activities;
    }

    public LiveData<List<ActivityEntry>> getLinkedActivities() {
        return linkedActivities;
    }

    public boolean isActiveActivities() {
        return mActiveActivities;
    }

    public void setActiveActivities(boolean activeActivities) {
        mActiveActivities = activeActivities;
    }
}
