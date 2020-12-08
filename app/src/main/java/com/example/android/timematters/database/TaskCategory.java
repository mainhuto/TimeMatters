package com.example.android.timematters.database;

import java.util.Locale;

import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;

@DatabaseView(value = "SELECT task.id, task.activity_id, task.category_id, task.name, task.hoursDuration, task.minutesDuration, task.notification, " +
        "task.color, task.icon, category.name AS categoryName FROM task " +
        "INNER JOIN category ON task.category_id = category.id", viewName = "task_category_view" )
public class TaskCategory {

    public int id;
    @ColumnInfo(name = "activity_id")
    public int activityId;
    @ColumnInfo(name = "category_id")
    public int categoryId;
    public String name;
    public int hoursDuration;
    public int minutesDuration;
    public boolean notification;
    public int color;
    public int icon;
    public String categoryName;

    public String getDuration() {
        return String.format(Locale.getDefault(), "%02d:%02d", hoursDuration, minutesDuration);
    }

    public int getTotalMinutesDuration() {
        return hoursDuration * 60 + minutesDuration;
    }

}
