package com.example.android.timematters.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.TaskLogView;
import com.example.android.timematters.database.TaskStatus;

import java.lang.ref.WeakReference;


public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationBroadcastRe";

    public static final String EXTRA_TASK_LOG_ID = "task_log_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra(EXTRA_TASK_LOG_ID, -1);
        Log.d(TAG, "onReceive: New notification for the task " + taskId);

        final PendingResult pendingResult = goAsync();
        Task asyncTask = new Task(pendingResult, intent, context);
        asyncTask.execute();

        Log.d(TAG, "onReceive: ends");
    }

    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final Intent intent;
//        private final Context context;
        private WeakReference<Context> weakContext;

        private Task(PendingResult pendingResult, Intent intent, Context context) {
            this.pendingResult = pendingResult;
            this.intent = intent;
//            this.context = context;
            weakContext = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (weakContext != null) {
                int taskId = intent.getIntExtra(EXTRA_TASK_LOG_ID, -1);
                Log.d(TAG, "doInBackground: taskId=" + taskId);

//            AppDatabase database = AppDatabase.getInstance(context);
                AppDatabase database = AppDatabase.getInstance(weakContext.get());
                TaskLogView taskLogView = database.taskLogDao().loadTaskLogViewById(taskId);
                if ( (taskLogView != null) && (TaskStatus.IN_PROGRESS.equals(taskLogView.status) || TaskStatus.ON_PAUSE.equals(taskLogView.status)) ) {
                    Log.d(TAG, "doInBackground: taskName=" + taskLogView.taskName);
                    ActivityEntry activityEntry = database.activityDao().getActivityById(taskLogView.activityId);
                    if (activityEntry != null) {
                        Log.d(TAG, "doInBackground: activityEntry not null");
                        TmActivityInfo activityInfo = TmActivityInfo.newActivityInfo(activityEntry);
                        Log.d(TAG, "doInBackground: activityName=" + activityInfo.getName());
//                    AppUtils.sendNotification(context, activityInfo, TaskLogInfo.newTaskLogInfo(taskLogView));
                        AppUtils.sendNotification(weakContext.get(), activityInfo, TaskLogInfo.newTaskLogInfo(taskLogView));
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: starts");
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish();
        }
    }

}
