package com.example.android.timematters.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;

public class AppWidgetBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "AppWidgetBroadcastRecei";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Play/Pause action");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            final PendingResult pendingResult = goAsync();
            Task asyncTask = new Task(pendingResult, intent, context);
            asyncTask.execute();
        } else {
            TaskLogJobIntentService.updateTaskLogWidget(context, intent);
        }
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
//            AppUtils.onHandleWork(context, intent);
            if (weakContext != null) {
                AppUtils.onHandleWork(weakContext.get(), intent);
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
