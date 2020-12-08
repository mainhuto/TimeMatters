package com.example.android.timematters.database;

import java.util.Locale;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "category")
public class CategoryEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "activity_id")
    private int activityId;
    private String name;
    private int hoursDuration;
    private int minutesDuration;
    private boolean notification;
    private int color;
    private int icon;

    @Ignore
    public CategoryEntry(int activityId, String name, int hoursDuration, int minutesDuration, boolean notification, int color, int icon) {
        this.activityId =activityId;
        this.name = name;
        this.hoursDuration = hoursDuration;
        this.minutesDuration = minutesDuration;
        this.notification = notification;
        this.color = color;
        this.icon = icon;
    }

    public CategoryEntry(int id, int activityId, String name, int hoursDuration, int minutesDuration, boolean notification, int color, int icon) {
        this.id = id;
        this.activityId =activityId;
        this.name = name;
        this.hoursDuration = hoursDuration;
        this.minutesDuration = minutesDuration;
        this.notification = notification;
        this.color = color;
        this.icon = icon;
    }

    public String getDuration() {
        return String.format(Locale.getDefault(), "%02d:%02d", hoursDuration, minutesDuration);
    }

    public int getTotalMinutesDuration() {
        return hoursDuration * 60 + minutesDuration;
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
}
