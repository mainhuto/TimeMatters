package com.example.android.timematters.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class TaskLogJobIntentService extends JobIntentService {

    private static final String TAG = "TaskLogJobIntentService";

    public static final String ACTION_PAUSE_TASK_FROM_WIDGET = "PAUSE_TASK_FROM_WIDGET";
    public static final String ACTION_PLAY_TASK_FROM_WIDGET = "PLAY_TASK_FROM_WIDGET";

    public static final String ARG_TASK_LOG_INFO = "task_log_info";

    static final int JOB_ID = 1000;

    public static void updateTaskLogWidget(Context context, Intent intent) {
        Log.d(TAG, "playPauseTask: action=" + intent.getAction());
        enqueueWork(context, TaskLogJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "onHandleWork: action=" + intent.getAction());
        AppUtils.onHandleWork(this, intent);
    }

}
