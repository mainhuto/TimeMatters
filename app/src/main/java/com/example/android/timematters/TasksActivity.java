package com.example.android.timematters;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.example.android.timematters.database.TaskCategory;
import com.example.android.timematters.utils.TaskLogInfo;
import com.example.android.timematters.utils.TmActivityInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class TasksActivity extends AppCompatActivity implements TaskAdapter.TaskAdapterOnClickHandler {

    private static final String TAG = "TasksActivity";

    public static final String ARG_ACTIVITY_INFO = "activity_info";
    public static final String ARG_SELECTOR_MODE = "selector_mode";
    public static final String EXTRA_TASK_INFO = "extra_task_info";

    private TmActivityInfo mActivityInfo;
    private boolean mSelectorMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        if ( getIntent().hasExtra(ARG_ACTIVITY_INFO) ) {
            mActivityInfo = getIntent().getParcelableExtra(ARG_ACTIVITY_INFO);
        }

        mSelectorMode = getIntent().getBooleanExtra(ARG_SELECTOR_MODE, false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mSelectorMode) {
            getSupportActionBar().setTitle(getString(R.string.task_selection_title));
            getSupportActionBar().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.color.colorAccent, null));
        } else {
            getSupportActionBar().setTitle(getString(R.string.task_list_title, mActivityInfo.getName()));
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment tasksFragment = fragmentManager.findFragmentById(R.id.task_list_container);
        if (tasksFragment == null) {
            tasksFragment = TasksFragment.newInstance(mActivityInfo, mSelectorMode);
            fragmentManager.beginTransaction()
                    .add(R.id.task_list_container, tasksFragment)
                    .commit();
        }

        FloatingActionButton fab = findViewById(R.id.task_fab);

        if (mSelectorMode) {
            fab.hide();
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Log.d(TAG, "onClick: new task");
                Intent taskIntent = new Intent(getApplicationContext(), TaskDetailActivity.class);
                Bundle taskArgs = new Bundle();
                taskArgs.putInt(TaskDetailActivity.ARG_ACTION, TaskDetailActivity.Action.INSERT.ordinal());
                taskArgs.putParcelable(TaskDetailActivity.ARG_ACTIVITY_INFO, mActivityInfo);
                taskIntent.putExtras(taskArgs);
                startActivity(taskIntent);
                   }
            });
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onClick(TaskCategory taskCategory) {
        Log.d(TAG, "onClick: " + taskCategory.name);

        if (mSelectorMode) {
            Intent resultIntent = new Intent();
            TaskLogInfo taskLogInfo = TaskLogInfo.newTaskLogInfo(taskCategory);
            resultIntent.putExtra(EXTRA_TASK_INFO, taskLogInfo);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Intent taskIntent = new Intent(this, TaskDetailActivity.class);
            Bundle taskArgs = new Bundle();
            taskArgs.putInt(TaskDetailActivity.ARG_ACTION, TaskDetailActivity.Action.UPDATE.ordinal());
            taskArgs.putParcelable(TaskDetailActivity.ARG_ACTIVITY_INFO, mActivityInfo);
            taskArgs.putInt(TaskDetailActivity.ARG_TASK_ID, taskCategory.id);
            taskIntent.putExtras(taskArgs);
            startActivity(taskIntent);
        }

    }
}