package com.example.android.timematters.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "task")
public class TaskEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "activity_id")
    private int activityId;
    @ColumnInfo(name = "category_id")
    private int categoryId;
    private String name;
    private int hoursDuration;
    private int minutesDuration;
    private boolean notification;
    private int color;
    private int icon;
    private String remarks;

    @Ignore
    public TaskEntry(int activityId, int categoryId, String name, int hoursDuration, int minutesDuration, boolean notification, int color, int icon, String remarks) {
        this.activityId = activityId;
        this.categoryId = categoryId;
        this.name = name;
        this.hoursDuration = hoursDuration;
        this.minutesDuration = minutesDuration;
        this.notification = notification;
        this.color = color;
        this.icon = icon;
        this.remarks = remarks;
    }

    public TaskEntry(int id, int activityId, int categoryId, String name, int hoursDuration, int minutesDuration, boolean notification, int color, int icon, String remarks) {
        this.id = id;
        this.activityId =activityId;
        this.categoryId = categoryId;
        this.name = name;
        this.hoursDuration = hoursDuration;
        this.minutesDuration = minutesDuration;
        this.notification = notification;
        this.color = color;
        this.icon = icon;
        this.remarks = remarks;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHoursDuration() {
        return hoursDuration;
    }

    public void setHoursDuration(int hoursDuration) {
        this.hoursDuration = hoursDuration;
    }

    public int getMinutesDuration() {
        return minutesDuration;
    }

    public void setMinutesDuration(int minutesDuration) {
        this.minutesDuration = minutesDuration;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
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

    public String getRemarks() {
        return remarks;
    }

//    public void setRemarks(String remarks) {
//        this.remarks = remarks;
//    }

//    public String getCategoryName() {
//        return categoryName;
//    }
//
//    public void setCategoryName(String categoryName) {
//        this.categoryName = categoryName;
//    }
}
