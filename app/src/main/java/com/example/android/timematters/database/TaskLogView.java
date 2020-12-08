package com.example.android.timematters.database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;
import androidx.room.Ignore;

@DatabaseView(value = "SELECT task_log.id, task_log.activity_id, task_log.category_id, task_log.task_id, task_log.date, " +
        "task_log.hours_duration_forecast, task_log.minutes_duration_forecast, task_log.notification, task_log.start_date, task_log.real_spent_time, " +
        "task_log.status, task_log.rating, task.name AS task_name, task.color AS color, task.icon AS icon, category.name AS category_name FROM task_log " +
        "INNER JOIN task ON task_log.task_id = task.id INNER JOIN category ON task_log.category_id = category.id", viewName = "task_log_view" )
public class TaskLogView {

    public int id;
    @ColumnInfo(name = "activity_id")
    public int activityId;
    @ColumnInfo(name = "category_id")
    public int categoryId;
    @ColumnInfo(name = "task_id")
    public int taskId;
    public Date date;
    @ColumnInfo(name = "hours_duration_forecast")
    public int hoursDurationForecast;
    @ColumnInfo(name = "minutes_duration_forecast")
    public int minutesDurationForecast;
    public boolean notification;
    @ColumnInfo(name = "start_date")
    public Date startDate;
    @ColumnInfo(name = "real_spent_time")
    public long realSpentTime;
    public TaskStatus status;
    public int rating;
    @ColumnInfo(name = "task_name")
    public String taskName;
    public int color;
    public int icon;
    @ColumnInfo(name = "category_name")
    public String categoryName;
    @Ignore
    public int occurrence;

    public String getDurationForecast() {
        return String.format(Locale.getDefault(), "%02d:%02d", hoursDurationForecast, minutesDurationForecast);
    }

    public int getTotalMinutesForecast() {
        return (hoursDurationForecast * 60) + minutesDurationForecast;
    }

    public String getRealSpentTime() {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(realSpentTime);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes /60, minutes %60);
    }

    public String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.format(date);
    }

    public String getStartTime() {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
        return dateFormat.format(startDate);
    }

    public void addSpentTime(long time) {
        this.realSpentTime += time;
    }

    public TaskLogEntry newTaskLogEntry() {
        return new TaskLogEntry(
                id,
                activityId,
                categoryId,
                taskId,
                date,
                hoursDurationForecast,
                minutesDurationForecast,
                notification,
                startDate,
                realSpentTime,
                status,
                rating);
    }

    public void calcRealSpentMinutes(Date now) {
        if (TaskStatus.IN_PROGRESS.equals(status)) {
            realSpentTime += now.getTime() - date.getTime();
        }
        date = now;
    }

    public TaskLogView copy() {
        TaskLogView newTaskLogView = new TaskLogView();
        newTaskLogView.id = id;
        newTaskLogView.activityId = activityId;
        newTaskLogView.categoryId =categoryId;
        newTaskLogView.taskId = taskId;
        newTaskLogView.date = date;
        newTaskLogView.hoursDurationForecast = hoursDurationForecast;
        newTaskLogView.minutesDurationForecast = minutesDurationForecast;
        newTaskLogView.notification = notification;
        newTaskLogView.startDate = startDate;
        newTaskLogView.realSpentTime = realSpentTime;
        newTaskLogView.status = status;
        newTaskLogView.rating = rating;
        newTaskLogView.taskName = taskName;
        newTaskLogView.color = color;
        newTaskLogView.icon = icon;
        newTaskLogView.categoryName = categoryName;
        newTaskLogView.occurrence = occurrence;
        return newTaskLogView;
    }

}
