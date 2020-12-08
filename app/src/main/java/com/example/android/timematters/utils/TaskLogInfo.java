package com.example.android.timematters.utils;


import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.timematters.database.TaskCategory;
import com.example.android.timematters.database.TaskLogEntry;
import com.example.android.timematters.database.TaskLogView;
import com.example.android.timematters.database.TaskStatus;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TaskLogInfo implements Parcelable {

    private int id;
    private int activityId;
    private int categoryId;
    private int taskId;
    private Date date;
    private int hoursDurationForecast;
    private int minutesDurationForecast;
    private boolean notification;
    private Date startDate;
    private long realSpentTime;
    private TaskStatus status;
    private int rating;
    private String taskName;
    private int color;
    private int icon;
    private String categoryName;

    public TaskLogInfo() {
        id = -1;
        status = TaskStatus.NULL;
    }

    protected TaskLogInfo(Parcel in) {
        id = in.readInt();
        activityId = in.readInt();
        categoryId = in.readInt();
        taskId = in.readInt();
        long longDate = in.readLong();
        if (longDate > 0) {
            date = new Date(longDate);
        }
        hoursDurationForecast = in.readInt();
        minutesDurationForecast = in.readInt();
        notification = in.readByte() != 0;
        long longStartDate = in.readLong();
        if (longDate > 0) {
            startDate = new Date(longStartDate);
        }
        realSpentTime = in.readLong();
        int intStatus = in.readInt();
        status = TaskStatus.values().length > intStatus ? TaskStatus.values()[intStatus] : TaskStatus.NULL;
        rating = in.readInt();
        taskName = in.readString();
        color = in.readInt();
        icon = in.readInt();
        categoryName = in.readString();
    }

    public static TaskLogInfo newTaskLogInfo(TaskCategory taskCategory) {
        TaskLogInfo taskLogInfo = new TaskLogInfo();
        taskLogInfo.setId(-1);
        taskLogInfo.setActivityId(taskCategory.activityId);
        taskLogInfo.setCategoryId(taskCategory.categoryId);
        taskLogInfo.setTaskId(taskCategory.id);
        taskLogInfo.setTaskName(taskCategory.name);
        taskLogInfo.setCategoryName(taskCategory.categoryName);
        taskLogInfo.setIcon(taskCategory.icon);
        taskLogInfo.setColor(taskCategory.color);
        taskLogInfo.setHoursDurationForecast(taskCategory.hoursDuration);
        taskLogInfo.setMinutesDurationForecast(taskCategory.minutesDuration);
        taskLogInfo.setNotification(taskCategory.notification);
        taskLogInfo.setStatus(TaskStatus.IN_PROGRESS);
        taskLogInfo.setRating(0);
        return taskLogInfo;
    }

    public static TaskLogInfo newTaskLogInfo(TaskLogView taskLogView) {
        TaskLogInfo taskLogInfo = new TaskLogInfo();
        taskLogInfo.setId(taskLogView.id);
        taskLogInfo.setActivityId(taskLogView.activityId);
        taskLogInfo.setCategoryId(taskLogView.categoryId);
        taskLogInfo.setTaskId(taskLogView.taskId);
        taskLogInfo.setDate(taskLogView.date);
        taskLogInfo.setHoursDurationForecast(taskLogView.hoursDurationForecast);
        taskLogInfo.setMinutesDurationForecast(taskLogView.minutesDurationForecast);
        taskLogInfo.setNotification(taskLogView.notification);
        taskLogInfo.setStartDate(taskLogView.startDate);
        taskLogInfo.setRealSpentTime(taskLogView.realSpentTime);
        taskLogInfo.setStatus(taskLogView.status);
        taskLogInfo.setRating(taskLogView.rating);
        taskLogInfo.setTaskName(taskLogView.taskName);
        taskLogInfo.setColor(taskLogView.color);
        taskLogInfo.setIcon(taskLogView.icon);
        taskLogInfo.setCategoryName(taskLogView.categoryName);
        taskLogInfo.setRating(0);
        return taskLogInfo;
    }

    public void calcRealSpentMinutes(Date now) {
        if (TaskStatus.IN_PROGRESS.equals(status)) {
            realSpentTime += now.getTime() - date.getTime();
        }
        date = now;
    }

    public TaskLogEntry newTaskLogEntry() {

        if (date == null) {
            date = new Date();
            startDate = date;
        }

        return new TaskLogEntry(
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

    public String getSpentTime() {
        long spentTime = realSpentTime;
        if (TaskStatus.IN_PROGRESS.equals(status)) {
            spentTime += new Date().getTime() - date.getTime();
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(spentTime);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes /60, minutes %60);
    }

    public String getSpentTimeWithSeconds() {
        long spentTime = realSpentTime;
        if (TaskStatus.IN_PROGRESS.equals(status)) {
            spentTime += new Date().getTime() - date.getTime();
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(spentTime);
        if (seconds < 3600) {
            return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        } else {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(spentTime);
            return String.format(Locale.getDefault(), "%d:%02d:%02d", minutes/60, minutes % 60, seconds % 60);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(activityId);
        dest.writeInt(categoryId);
        dest.writeInt(taskId);
        long longDate = 0;
        if (date != null) {
            longDate = date.getTime();
        }
        dest.writeLong(longDate);
        dest.writeInt(hoursDurationForecast);
        dest.writeInt(minutesDurationForecast);
        dest.writeByte((byte) (notification ? 1 : 0));
        long longStartDate = 0;
        if (startDate != null) {
            longStartDate = startDate.getTime();
        }
        dest.writeLong(longStartDate);
        dest.writeLong(realSpentTime);
        dest.writeInt(status == null ? TaskStatus.NULL.ordinal() : status.ordinal());
        dest.writeInt(rating);
        dest.writeString(taskName);
        dest.writeInt(color);
        dest.writeInt(icon);
        dest.writeString(categoryName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TaskLogInfo> CREATOR = new Creator<TaskLogInfo>() {
        @Override
        public TaskLogInfo createFromParcel(Parcel in) {
            return new TaskLogInfo(in);
        }

        @Override
        public TaskLogInfo[] newArray(int size) {
            return new TaskLogInfo[size];
        }
    };

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

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public long getRealSpentTime() {
        return realSpentTime;
    }

    public void setRealSpentTime(long realSpentTime) {
        this.realSpentTime = realSpentTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
}
