package com.example.android.timematters.viewmodel;

import android.util.Log;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.TaskLogView;
import com.example.android.timematters.database.TaskStatus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class TaskLogListViewModel extends ViewModel {

    private static final String TAG = "TaskLogListViewModel";

    private final LiveData<List<TaskLogView>> mTasks;
    private final LiveData<ActivityEntry> mActivityEntry;


    public TaskLogListViewModel(AppDatabase database, int activityId, int days) {
        Log.d(TAG, "Actively retrieving the tasks from the DataBase for activityId=" + activityId);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if ( days > 1 ) {
            calendar.add(Calendar.DATE, (days -1) * -1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = calendar.getTime();

        mTasks = database.taskLogDao().loadActivityTaskLogsView(activityId, date, TaskStatus.FINISHED);
        mActivityEntry = database.activityDao().loadActivityById(activityId);
    }

    public LiveData<List<TaskLogView>> getTasks() {
        return mTasks;
    }

    public LiveData<ActivityEntry> getActivityEntry() {
        return mActivityEntry;
    }

}
