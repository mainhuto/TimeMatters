package com.example.android.timematters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.TaskLogView;
import com.example.android.timematters.database.TaskStatus;
import com.example.android.timematters.utils.TmActivityInfo;
import com.example.android.timematters.viewmodel.TaskLogListViewModel;
import com.example.android.timematters.viewmodel.TaskLogListViewModelFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TaskLogsFragment extends Fragment {

    private static final String TAG = "TaskLogsFragment";

    private static final String ARG_ACTIVITY_ID = "activity_id";
    private static final String ARG_TAB_TAG = "tag_tab";

    private int mActivityId;
    private TmActivityInfo mActivityInfo;
    private int mDays;

    private AppDatabase mDb;

    private TaskLogAdapter mTaskLogAdapter;
    private LinearLayout mNoTasksTextView;
//    private RecyclerView mRecyclerView;

    TaskLogAdapter.TaskLogAdapterOnClickHandler mClickHandler;


    public TaskLogsFragment() {
        // Required empty public constructor
    }

    public static TaskLogsFragment newInstance(int activityId, int days) {
        TaskLogsFragment fragment = new TaskLogsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTIVITY_ID, activityId);
        args.putInt(ARG_TAB_TAG, days);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mActivityId = getArguments().getInt(ARG_ACTIVITY_ID);
            mDays = getArguments().getInt(ARG_TAB_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_task_logs, container, false);

        mNoTasksTextView = rootView.findViewById(R.id.no_task_logs_tv);
//        mRecyclerView = rootView.findViewById(R.id.task_log_list_rv);

        RecyclerView taskLogsRecyclerView = rootView.findViewById(R.id.task_log_list_rv);
        Activity activity = getActivity();
        if (activity != null) {

            mDb = AppDatabase.getInstance(activity.getApplicationContext());

            GridLayoutManager layoutManager = new GridLayoutManager(activity.getApplicationContext(), 1);
            taskLogsRecyclerView.setLayoutManager(layoutManager);
            mTaskLogAdapter = new TaskLogAdapter(mClickHandler);
            taskLogsRecyclerView.setAdapter(mTaskLogAdapter);

            setupLogTaskListViewModel();
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mClickHandler = (TaskLogAdapter.TaskLogAdapterOnClickHandler) context;
        }
    }

    private void setupLogTaskListViewModel() {
        Log.d(TAG, "setupLogTaskListViewModel: starts");
        TaskLogListViewModelFactory factory = new TaskLogListViewModelFactory(mDb, mActivityId, mDays);
        TaskLogListViewModel viewModel = new ViewModelProvider(this, factory).get(TaskLogListViewModel.class);
        viewModel.getTasks().observe(getViewLifecycleOwner(), new Observer<List<TaskLogView>>() {
            @Override
            public void onChanged(List<TaskLogView> taskLogs) {
                Log.d(TAG, "onChanged: list tasks " + taskLogs.size());
                List<TaskLogView> reportTaskLogs = accumulateTaskLogs(taskLogs);
                mTaskLogAdapter.setTaskLogs(reportTaskLogs);
                if ( (taskLogs.size() == 0) && (mDays == 1) ) {
                    showNoTasksMessage();
                } else {
                    hideNoTasksMessage();
                }
            }
        });

        viewModel.getActivityEntry().observe(getViewLifecycleOwner(), new Observer<ActivityEntry>() {
            @Override
            public void onChanged(ActivityEntry activityEntry) {
                if ( (mActivityInfo != null) && (mActivityInfo.isRateTask() != activityEntry.isRateTask()) ) {
                    mTaskLogAdapter.notifyDataSetChanged();
                }
                mActivityInfo = TmActivityInfo.newActivityInfo(activityEntry);
            }
        });

    }

    private List<TaskLogView> accumulateTaskLogs(List<TaskLogView> taskLogs) {
        List<TaskLogView> reportTaskLogs = new ArrayList<>();
        HashMap<Integer, TaskLogView> taskList = new HashMap<>();
        HashMap<Integer, Integer> ratingList = new HashMap<>();
        for (TaskLogView taskLogView: taskLogs) {
           if (TaskStatus.FINISHED.equals(taskLogView.status)) {
                if (taskList.containsKey(taskLogView.taskId)) {
                    TaskLogView accumulateTask = taskList.get(taskLogView.taskId);
                    accumulateTask.occurrence++;
                    accumulateTask.addSpentTime(taskLogView.realSpentTime);
                    ratingList.put(taskLogView.taskId, ratingList.get(taskLogView.taskId) + taskLogView.rating);
                    accumulateTask.rating =  ratingList.get(taskLogView.taskId) / accumulateTask.occurrence;
                } else {
                    taskLogView.occurrence = 1;
                    TaskLogView accumulateTask = taskLogView.copy();
                    taskList.put(taskLogView.taskId, accumulateTask);
                    ratingList.put(accumulateTask.taskId, accumulateTask.rating);
                    reportTaskLogs.add(accumulateTask);
                }
            } else {
                if (mDays == 1) {
                    reportTaskLogs.add(0, taskLogView);
                }
            }
        }
        return reportTaskLogs;
    }

    private void showNoTasksMessage() {
        mNoTasksTextView.setVisibility(View.VISIBLE);
    }

    private void hideNoTasksMessage() {
        mNoTasksTextView.setVisibility(View.INVISIBLE);
    }

}