package com.example.android.timematters.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.timematters.MainActivity;
import com.example.android.timematters.R;
import com.example.android.timematters.TaskLogActivity;
import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.TaskLogEntry;
import com.example.android.timematters.database.TaskLogView;
import com.example.android.timematters.database.TaskStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

public class AppUtils {

    private static final String APP_PROPERTIES_DATE_FORMAT = "yyyy.MM.dd";
    public static final String PROPERTY_LAST_CLEAN_DATE = "last_clean_date";

    private static final String CHANNEL_ID = "channel_task_completed";
    private static final int REQUEST_CODE_MAIN_ACTIVITY = 0;
    private static final int REQUEST_CODE_TASK_LOG_ACTIVITY = 1;
    private static final int REQUEST_CODE_PLAY_PAUSE_TASK = 2;
    private static final int REQUEST_CODE_STOP_TASK = 3;

    public static final String ACTION_UPDATE_TASK_LOG_WIDGET = "UPDATE_TASK_LOG_WIDGET";
    public static final String ACTION_PAUSE_TASK_FROM_WIDGET = "PAUSE_TASK_FROM_WIDGET";
    public static final String ARG_TASK_LOG_INFO = "task_log_info";


    public static void scheduleAlarm(Context context, int taskLogId, int minutes) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.putExtra(NotificationBroadcastReceiver.EXTRA_TASK_LOG_ID, taskLogId);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("NotificationBroadcastRe", "schedule: " + minutes);
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                            minutes * 60 * 1000, alarmIntent);
        } else {
            alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                            minutes * 60 * 1000, alarmIntent);
        }
    }


    public static void sendNotification(Context context, TmActivityInfo activityInfo, TaskLogInfo taskLog) {

        PendingIntent pendingIntent = getTaskLogPendingIntent(context, activityInfo);
        PendingIntent stopPendingIntent = getStopTaskPendingIntent(context, activityInfo, taskLog);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.alarm_24)
                .addAction(R.drawable.ic_stop_24, "Stop", stopPendingIntent)  // #1
                .setContentTitle(context.getString(R.string.task_time_completed))
                .setContentText(context.getString(R.string.task_alarm_content_text, taskLog.getTaskName(), taskLog.getCategoryName()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (taskLog.getIcon() > 0) {
            Bitmap icon = getBitmapFromVectorDrawable(context, AppResources.getInstance().getIcon(activityInfo.getId(), taskLog.getIcon()));
            builder.setLargeIcon(icon);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(taskLog.getId(), builder.build());

    }

    public static PendingIntent getMainPendingIntent(Context context) {
        return PendingIntent.getActivity(context, REQUEST_CODE_MAIN_ACTIVITY, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public static PendingIntent getTaskLogPendingIntent(Context context, TmActivityInfo activityInfo) {
        Intent intent = new Intent(context, TaskLogActivity.class);
        Bundle args = new Bundle();
        args.putParcelable(TaskLogActivity.ARG_ACTIVITY_INFO, activityInfo);
        intent.putExtras(args);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        return stackBuilder.getPendingIntent(REQUEST_CODE_TASK_LOG_ACTIVITY, PendingIntent.FLAG_UPDATE_CURRENT);
    }



    public static PendingIntent getStopTaskPendingIntent(Context context, TmActivityInfo activityInfo,TaskLogInfo taskLog) {
        Intent intent = new Intent(context, TaskLogActivity.class);
        Bundle args = new Bundle();
        args.putParcelable(TaskLogActivity.ARG_STOP_TASK_LOG_INFO, taskLog);
        args.putParcelable(TaskLogActivity.ARG_ACTIVITY_INFO, activityInfo);
        intent.putExtras(args);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        return stackBuilder.getPendingIntent(REQUEST_CODE_STOP_TASK, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getPlayPauseTaskPendingIntent(Context context, TaskLogInfo taskLog) {
        Intent intent = new Intent(context, AppWidgetBroadcastReceiver.class);
        Bundle args = new Bundle();
        args.putParcelable(TaskLogJobIntentService.ARG_TASK_LOG_INFO, taskLog);
        intent.putExtras(args);
        if ( TaskStatus.IN_PROGRESS.equals(taskLog.getStatus()) ) {
            intent.setAction(TaskLogJobIntentService.ACTION_PAUSE_TASK_FROM_WIDGET);
        } else {
            intent.setAction(TaskLogJobIntentService.ACTION_PLAY_TASK_FROM_WIDGET);
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE_PLAY_PAUSE_TASK, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void onHandleWork(Context context, @NonNull Intent intent) {

        AppDatabase database = AppDatabase.getInstance(context);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TaskLogWidgetProvider.class));
        TmActivityInfo activityInfo = null;
        TaskLogInfo taskLogInfo = null;

        if ( intent.hasExtra(ARG_TASK_LOG_INFO) ) {
            taskLogInfo = intent.getParcelableExtra(ARG_TASK_LOG_INFO);
            if ( taskLogInfo != null ) {
                if ( ACTION_PAUSE_TASK_FROM_WIDGET.equals(intent.getAction()) ) {
                    taskLogInfo.calcRealSpentMinutes(new Date());
                    taskLogInfo.setStatus(TaskStatus.ON_PAUSE);
                } else {
                    taskLogInfo.setStatus(TaskStatus.IN_PROGRESS);
                    taskLogInfo.setDate(new Date());
                }
                TaskLogEntry taskLogEntry = taskLogInfo.newTaskLogEntry();
                taskLogEntry.setId(taskLogInfo.getId());
                database.taskLogDao().updateTaskLog(taskLogEntry);
            }

        } else {

            TaskLogView taskLogView = database.taskLogDao().loadTaskLogInProgress();
            if (taskLogView != null) {
                taskLogInfo = TaskLogInfo.newTaskLogInfo(taskLogView);
            }

        }

        if ( taskLogInfo != null ) {
            ActivityEntry activityEntry = database.activityDao().getActivityById(taskLogInfo.getActivityId());
            if (activityEntry != null) {
                activityInfo = TmActivityInfo.newActivityInfo(activityEntry);
            }
        }

        TaskLogWidgetProvider.updateAppWidgets(context, appWidgetManager, appWidgetIds, activityInfo, taskLogInfo);

    }

    public static boolean isCleanDatabase(Activity activity) {
        SimpleDateFormat formatter = new SimpleDateFormat(APP_PROPERTIES_DATE_FORMAT);
        String now = formatter.format(new Date());
        return !now.equals(getProperty(activity, PROPERTY_LAST_CLEAN_DATE));
    }

    public static void setLastCleanDate(Activity activity, Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(APP_PROPERTIES_DATE_FORMAT);
        String lastCleanDate = formatter.format(date);
        setProperty(activity, PROPERTY_LAST_CLEAN_DATE, lastCleanDate);
    }

    public static String getProperty(Activity activity, String key) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(key, "");
    }

    public static void setProperty(Activity activity, String key, String value) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

}
