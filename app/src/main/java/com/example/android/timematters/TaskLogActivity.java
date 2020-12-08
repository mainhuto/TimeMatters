package com.example.android.timematters;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.DbExecutor;
import com.example.android.timematters.database.TaskLogEntry;
import com.example.android.timematters.database.TaskLogView;
import com.example.android.timematters.database.TaskStatus;
import com.example.android.timematters.utils.AppUtils;
import com.example.android.timematters.utils.AppWidgetBroadcastReceiver;
import com.example.android.timematters.utils.TaskDialog;
import com.example.android.timematters.utils.TaskLogInfo;
import com.example.android.timematters.utils.TaskLogJobIntentService;
import com.example.android.timematters.utils.TmActivityInfo;
import com.example.android.timematters.viewmodel.TaskLogViewModel;
import com.example.android.timematters.viewmodel.TaskLogViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.Date;

public class TaskLogActivity extends AppCompatActivity implements TaskDialog.TaskDialogListener, TaskLogAdapter.TaskLogAdapterOnClickHandler, TabLayout.OnTabSelectedListener  {

    private static final String TAG = "TaskLogActivity";

    public static final String ARG_ACTIVITY_INFO = "activity_info";
    public static final String ARG_STOP_TASK_LOG_INFO = "task_log_info";
    public static final int TASK_REQUEST_CODE = 1;
    public static final String TASK_DIALOG_NEW_TAG = "NEW";
    public static final String TASK_DIALOG_END_TAG = "END";

    private TmActivityInfo mActivityInfo;
    private TaskLogView mTaskInProgress;
    private TaskLogInfo mNewTaskLogInfo;
    private boolean mScheduleNotification;

    private AppDatabase mDb;
    private TaskLogViewModel mViewModel;

    private FloatingActionButton mFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_log);

        Log.d(TAG, "onCreate: starts");

        if ( getIntent().hasExtra(ARG_ACTIVITY_INFO) ) {
            Log.d(TAG, "onCreate: has activity info");
            mActivityInfo = getIntent().getParcelableExtra(ARG_ACTIVITY_INFO);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(mActivityInfo.getName());
        }

        mDb = AppDatabase.getInstance(getApplicationContext());

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment collectionTaskLogFragment = fragmentManager.findFragmentById(R.id.task_log_container);
        if (collectionTaskLogFragment == null) {
            collectionTaskLogFragment = CollectionTaskLogFragment.newInstance(mActivityInfo.getId());
            fragmentManager.beginTransaction()
                    .add(R.id.task_log_container, collectionTaskLogFragment)
                    .commit();
        }

        mFab = findViewById(R.id.task_log_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Log.d(TAG, "onClick: select task");
            if ( (mTaskInProgress != null) && mTaskInProgress.activityId != mActivityInfo.getId()) {
                Snackbar.make(findViewById(R.id.task_log_container), getString(R.string.task_in_progress_on_other_activity), Snackbar.LENGTH_LONG)
                        .setAction(R.string.go_to_activity, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                goToActivityInProgress();
                            }
                        }).show();
            } else {
                Intent tasksIntent = new Intent(getApplicationContext(), TasksActivity.class);
                Bundle tasksArgs = new Bundle();
                tasksArgs.putParcelable(TasksActivity.ARG_ACTIVITY_INFO, mActivityInfo);
                tasksArgs.putBoolean(TasksActivity.ARG_SELECTOR_MODE, true);
                tasksIntent.putExtras(tasksArgs);
                startActivityForResult(tasksIntent, TASK_REQUEST_CODE);
            }
            }
        });

        setupViewModel();

    }

    private void setupViewModel() {
        TaskLogViewModelFactory factory = new TaskLogViewModelFactory(mDb, mActivityInfo.getId());
        mViewModel = new ViewModelProvider(this, factory).get(TaskLogViewModel.class);
        mViewModel.getTaskLogView().observe(this, new Observer<TaskLogView>() {
            @Override
            public void onChanged(TaskLogView taskLogView) {
                Log.d(TAG, "onChanged: TaskLogViewModel");
                if (taskLogView == null) {
                    Log.d(TAG, "onChanged: No task in progress");
                } else {
                    Log.d(TAG, "onChanged: task in progress - " +  taskLogView.taskName);
                    if ( getIntent().hasExtra(ARG_STOP_TASK_LOG_INFO) ) {
                        TaskLogInfo stopTaskLogInfo = getIntent().getParcelableExtra(ARG_STOP_TASK_LOG_INFO);
                        Log.d(TAG, "onChanged: Stop task id: " + stopTaskLogInfo.getId() +  ". In Progress task id: " + taskLogView.id + ". Notified task id: " + mViewModel.getNotifiedTaskLogId());
                        if ( (stopTaskLogInfo.getId() != mViewModel.getNotifiedTaskLogId()) && (stopTaskLogInfo.getId() == taskLogView.id) ) {
                            showTaskDialog(stopTaskLogInfo, TASK_DIALOG_END_TAG);
                            mViewModel.setNotifiedTaskLogId(stopTaskLogInfo.getId());
                        }
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(TaskLogActivity.this);
                        notificationManager.cancel(stopTaskLogInfo.getId());
                    }
                    if (mScheduleNotification) {
                        Log.d("NotificationBroadcastRe", "created task id: "+  taskLogView.id);
                        AppUtils.scheduleAlarm(TaskLogActivity.this, taskLogView.id, taskLogView.getTotalMinutesForecast());
                        mScheduleNotification = false;
                    }
                }
                mTaskInProgress = taskLogView;
            }
        });

        mViewModel.getActivityEntry().observe(this, new Observer<ActivityEntry>() {
            @Override
            public void onChanged(ActivityEntry activityEntry) {
                mActivityInfo = TmActivityInfo.newActivityInfo(activityEntry);
                updateWidget();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected: " + id + " - " + item.getTitle());
        switch (id) {
            case R.id.action_activity_settings:
                Intent settingsIntent = new Intent(this, ActivityEntrySettingsActivity.class);
                Bundle settingsArgs = new Bundle();
                settingsArgs.putInt(ActivityEntrySettingsActivity.ARG_ACTIVITY_ID, mActivityInfo.getId());
                settingsIntent.putExtras(settingsArgs);
                startActivity(settingsIntent);
                return true;
            case R.id.action_categories:
                Intent categoriesIntent = new Intent(this, CategoriesActivity.class);
                Bundle categoriesArgs = new Bundle();
                categoriesArgs.putParcelable(CategoriesActivity.ARG_ACTIVITY_INFO, mActivityInfo);
                categoriesIntent.putExtras(categoriesArgs);
                startActivity(categoriesIntent);
                return true;
            case R.id.action_tasks:
                Intent tasksIntent = new Intent(this, TasksActivity.class);
                Bundle tasksArgs = new Bundle();
                tasksArgs.putParcelable(TasksActivity.ARG_ACTIVITY_INFO, mActivityInfo);
                tasksIntent.putExtras(tasksArgs);
                startActivity(tasksIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: starts");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TASK_REQUEST_CODE) {
            if  ( (resultCode == RESULT_OK) && ( data != null) ) {
                TaskLogInfo taskLogInfo = data.getParcelableExtra(TasksActivity.EXTRA_TASK_INFO);
                if (mTaskInProgress != null) {
                    mNewTaskLogInfo = taskLogInfo;
                    showTaskDialog(TaskLogInfo.newTaskLogInfo(mTaskInProgress), TASK_DIALOG_END_TAG);
                } else {
                    showTaskDialog(taskLogInfo, TASK_DIALOG_NEW_TAG);
                }
            }
        }
    }

    private void showTaskDialog(TaskLogInfo taskLogInfo, String tag) {
        DialogFragment newFragment = TaskDialog.newInstance(mActivityInfo, taskLogInfo);
        newFragment.show(getSupportFragmentManager(), tag);
    }

    @Override
    public void onTaskDialogPositiveClick(TaskLogInfo taskLogInfo, String tag) {
        Log.d(TAG, "onTaskDialogPositiveClick: " + tag);
        if (TextUtils.equals(tag, TASK_DIALOG_NEW_TAG)) {
            createTaskLog(taskLogInfo);
        } else {
            if (mTaskInProgress != null) {
                mTaskInProgress.rating = taskLogInfo.getRating();
                stopTask(mTaskInProgress, new Date());
            }
            if (mNewTaskLogInfo != null) {
                showTaskDialog(mNewTaskLogInfo, TASK_DIALOG_NEW_TAG);
                mNewTaskLogInfo = null;
            }
        }
    }

    private void createTaskLog(final TaskLogInfo taskLogInfo) {
        Log.d(TAG, "createTaskLog: starts");
        final TaskLogEntry taskLogEntry = taskLogInfo.newTaskLogEntry();
        taskLogEntry.setStatus(TaskStatus.IN_PROGRESS);
        DbExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (taskLogEntry.isNotification()) {
                    mScheduleNotification = true;
                }
                Log.d(TAG, "run: create new tak");
                mDb.taskLogDao().insertTask(taskLogEntry);
                updateWidget();
            }
        });
    }

    private void updateTaskLog(TaskLogView taskLogView) {
        Log.d(TAG, "updateTaskLog: starts");
        final TaskLogEntry taskLogEntry = taskLogView.newTaskLogEntry();
        DbExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: create new tak log");
                mDb.taskLogDao().updateTaskLog(taskLogEntry);
                Log.d("TaskLogJobIntentService", "before enqueueWork");
                updateWidget();
                Log.d("TaskLogJobIntentService", "after enqueueWork");
            }
        });
    }

    private void updateWidget() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Intent intent = new Intent(getApplicationContext(), AppWidgetBroadcastReceiver.class);
            intent.setAction(AppUtils.ACTION_UPDATE_TASK_LOG_WIDGET);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent();
            intent.setAction(AppUtils.ACTION_UPDATE_TASK_LOG_WIDGET);
            TaskLogJobIntentService.updateTaskLogWidget(getApplicationContext(), intent);
        }
    }

    @Override
    public void onTaskDialogNegativeClick(String tag) {
        Log.d(TAG, "onTaskDialogNegativeClick: " + tag);
        mNewTaskLogInfo = null;
    }

    @Override
    public void onClickStop(TaskLogView taskLogView) {
        Log.d(TAG, "onClickStop: " +  taskLogView.taskName + " - " + taskLogView.date);
        TaskLogInfo taskLogInfo = TaskLogInfo.newTaskLogInfo(taskLogView);
        showTaskDialog(taskLogInfo, TASK_DIALOG_END_TAG);
    }

    private void stopTask(TaskLogView taskLogView, Date date) {
        if (TaskStatus.IN_PROGRESS.equals(taskLogView.status)) {
            taskLogView.calcRealSpentMinutes(date);
        }
        taskLogView.status = TaskStatus.FINISHED;
        updateTaskLog(taskLogView);
    }

    @Override
    public void onClickPause(TaskLogView taskLogView) {
        Log.d(TAG, "onClickPause: " +  taskLogView.taskName + " - " + taskLogView.date);
        taskLogView.calcRealSpentMinutes(new Date());
        taskLogView.status = TaskStatus.ON_PAUSE;
        updateTaskLog(taskLogView);
    }

    @Override
    public void onClickPlay(TaskLogView taskLogView) {
        Log.d(TAG, "onClickPause: " +  taskLogView.taskName + " - " + taskLogView.date);
        taskLogView.status = TaskStatus.IN_PROGRESS;
        taskLogView.date = new Date();
        updateTaskLog(taskLogView);
    }

    @Override
    public boolean isRateTask() {
        return mActivityInfo.isRateTask();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if ( (tab.getTag() != null) && (tab.getTag().equals(1)) ) {
            mFab.show();
        } else {
            mFab.hide();
        }
    }

    private void goToActivityInProgress() {
        DbExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ActivityEntry activityEntry = mDb.activityDao().getActivityById(mTaskInProgress.activityId);
                TmActivityInfo activityInfo = TmActivityInfo.newActivityInfo(activityEntry);
                Intent intent = new Intent(getApplicationContext(), TaskLogActivity.class);
                Bundle args = new Bundle();
                args.putParcelable(TaskLogActivity.ARG_ACTIVITY_INFO, activityInfo);
                intent.putExtras(args);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {}

}