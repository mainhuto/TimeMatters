package com.example.android.timematters.database;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_log")
public class TaskLogEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "activity_id")
    private int activityId;
    @ColumnInfo(name = "category_id")
    private int categoryId;
    @ColumnInfo(name = "task_id")
    private int taskId;
    private Date date;
    @ColumnInfo(name = "hours_duration_forecast")
    private int hoursDurationForecast;
    @ColumnInfo(name = "minutes_duration_forecast")
    private int minutesDurationForecast;
    private boolean notification;
    @ColumnInfo(name = "start_date")
    private Date startDate;
    @ColumnInfo(name = "real_spent_time")
    private long realSpentTime;
    private TaskStatus status;
    private int rating;

    @Ignore
    public TaskLogEntry(int activityId, int categoryId, int taskId, Date date, int hoursDurationForecast, int minutesDurationForecast, boolean notification, Date startDate, long realSpentTime, TaskStatus status, int rating) {
        this.activityId = activityId;
        this.categoryId = categoryId;
        this.taskId = taskId;
        this.date = date;
        this.hoursDurationForecast = hoursDurationForecast;
        this.minutesDurationForecast = minutesDurationForecast;
        this.notification = notification;
        this.startDate = startDate;
        this.realSpentTime = realSpentTime;
        this.status = status;
        this.rating = rating;
    }

    public TaskLogEntry(int id, int activityId, int categoryId, int taskId, Date date, int hoursDurationForecast, int minutesDurationForecast, boolean notification, Date startDate, long realSpentTime, TaskStatus status, int rating) {
        this.id = id;
        this.activityId = activityId;
        this.categoryId = categoryId;
        this.taskId = taskId;
        this.date = date;
        this.hoursDurationForecast = hoursDurationForecast;
        this.minutesDurationForecast = minutesDurationForecast;
        this.notification = notification;
        this.startDate = startDate;
        this.realSpentTime = realSpentTime;
        this.status = status;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getHoursDurationForecast() {
        return hoursDurationForecast;
    }

    public void setHoursDurationForecast(int hoursDurationForecast) {
        this.hoursDurationForecast = hoursDurationForecast;
    }

    public int getMinutesDurationForecast() {
        return minutesDurationForecast;
    }

    public void setMinutesDurationForecast(int minutesDurationForecast) {
        this.minutesDurationForecast = minutesDurationForecast;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public long getRealSpentTime() {
        return realSpentTime;
    }

    public void setRealSpentTime(long realSpentTime) {
        this.realSpentTime = realSpentTime;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
