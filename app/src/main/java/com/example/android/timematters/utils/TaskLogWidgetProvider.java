package com.example.android.timematters.utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.timematters.R;
import com.example.android.timematters.database.TaskStatus;

import java.util.Date;

public class TaskLogWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, TmActivityInfo activityInfo, TaskLogInfo taskLog) {

        Log.d("TaskLogJobIntentService", "updateAppWidget: starts");

        // Create an Intent to launch ExampleActivity
        PendingIntent pendingIntent = null;

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.task_log_widget);
        views.setImageViewResource(R.id.widget_background_iv, R.drawable.widget_background);

        if (taskLog != null) {
            if (activityInfo != null) {
                pendingIntent = AppUtils.getTaskLogPendingIntent(context, activityInfo);
            }
            views.setTextViewText(R.id.task_name_widget_tv, taskLog.getTaskName());
            views.setTextViewText(R.id.task_category_name_widget_tv, taskLog.getCategoryName());
            if ( taskLog.getIcon() == 0 ) {
                views.setImageViewResource(R.id.task_icon_widget_iv, R.mipmap.ic_launcher);
            } else {
                views.setImageViewResource(R.id.task_icon_widget_iv, AppResources.getInstance().getIcon(taskLog.getActivityId(), taskLog.getIcon()));
            }
            PendingIntent playPausePendingIntent = AppUtils.getPlayPauseTaskPendingIntent(context, taskLog);
            views.setOnClickPendingIntent(R.id.play_pause_widget_iv, playPausePendingIntent);
            views.setImageViewResource(R.id.stop_widget_iv, R.drawable.ic_stop_24);
            PendingIntent stopPendingIntent = AppUtils.getStopTaskPendingIntent(context, activityInfo, taskLog);
            views.setOnClickPendingIntent(R.id.stop_widget_iv, stopPendingIntent);
            views.setViewVisibility(R.id.task_category_name_widget_tv, View.VISIBLE);
            views.setViewVisibility(R.id.run_layout, View.VISIBLE);
            long lastBase;
            boolean started = false;
            Log.d("TaskLogJobIntentService", "updateAppWidget: check task status");
            if (TaskStatus.IN_PROGRESS.equals(taskLog.getStatus())) {
                Log.d("TaskLogJobIntentService", "updateAppWidget: in progress");
                views.setImageViewResource(R.id.play_pause_widget_iv, R.drawable.ic_pause_24);
                views.setViewVisibility(R.id.chronometer_widget, View.VISIBLE);
                views.setViewVisibility(R.id.paused_time_widget_tv, View.INVISIBLE);
                lastBase = taskLog.getDate().getTime() - taskLog.getRealSpentTime();
                started = true;
            } else {
                Log.d("TaskLogJobIntentService", "updateAppWidget: on pause");
                views.setImageViewResource(R.id.play_pause_widget_iv, R.drawable.ic_play_24);
                views.setViewVisibility(R.id.chronometer_widget, View.INVISIBLE);
                views.setViewVisibility(R.id.paused_time_widget_tv, View.VISIBLE);
                lastBase = new Date().getTime() - taskLog.getRealSpentTime();
                views.setTextViewText(R.id.paused_time_widget_tv, taskLog.getSpentTimeWithSeconds());
            }
            long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            long base = lastBase - elapsedRealtimeOffset;
            views.setChronometer(R.id.chronometer_widget, base, null, started);
        } else {
            Log.d("TaskLogJobIntentService", "updateAppWidget: No task in progress");
            pendingIntent = AppUtils.getMainPendingIntent(context);
            views.setImageViewResource(R.id.task_icon_widget_iv, R.mipmap.ic_launcher);
            views.setTextViewText(R.id.task_name_widget_tv, context.getString(R.string.widget_no_task_in_progress));
            views.setViewVisibility(R.id.task_category_name_widget_tv, View.INVISIBLE);
            views.setViewVisibility(R.id.run_layout, View.INVISIBLE);
        }

        if (pendingIntent != null) {
            views.setOnClickPendingIntent(R.id.task_icon_widget_iv, pendingIntent);
        }

        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, TmActivityInfo activityInfo, TaskLogInfo taskLog) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, activityInfo, taskLog);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Intent intent = new Intent(context, AppWidgetBroadcastReceiver.class);
            intent.setAction(AppUtils.ACTION_UPDATE_TASK_LOG_WIDGET);
            context.sendBroadcast(intent);
        } else {
            Intent intent = new Intent();
            intent.setAction(AppUtils.ACTION_UPDATE_TASK_LOG_WIDGET);
            TaskLogJobIntentService.updateTaskLogWidget(context, intent);
        }


    }

}
