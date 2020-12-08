package com.example.android.timematters.viewmodel;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.TaskLogView;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class TaskLogViewModel extends ViewModel {

    private final LiveData<TaskLogView> mTaskLogView;
    private final LiveData<ActivityEntry> mActivityEntry;
    private int mNotifiedTaskLogId;

    public TaskLogViewModel(AppDatabase database, int activityId) {
        mTaskLogView = database.taskLogDao().loadLiveTaskLogInProgress();
        mActivityEntry = database.activityDao().loadActivityById(activityId);
        mNotifiedTaskLogId = -1;
    }

    public LiveData<TaskLogView> getTaskLogView() {
        return mTaskLogView;
    }

    public LiveData<ActivityEntry> getActivityEntry() {
        return mActivityEntry;
    }

    public int getNotifiedTaskLogId() {
        return mNotifiedTaskLogId;
    }

    public void setNotifiedTaskLogId(int notifiedTaskLogId) {
        mNotifiedTaskLogId = notifiedTaskLogId;
    }
}
