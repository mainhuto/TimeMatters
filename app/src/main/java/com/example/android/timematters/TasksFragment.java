package com.example.android.timematters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.DbExecutor;
import com.example.android.timematters.database.TaskCategory;
import com.example.android.timematters.utils.Dependencies;
import com.example.android.timematters.utils.TmActivityInfo;
import com.example.android.timematters.viewmodel.TaskListViewModel;
import com.example.android.timematters.viewmodel.TaskListViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class TasksFragment extends Fragment {

    private static final String TAG = "TasksFragment";

    private static final String ARG_ACTIVITY_INFO = "activity_info";
    private static final String ARG_SELECTOR_MODE = "selector_mode";

    private TmActivityInfo mActivityInfo;
    private boolean mSelectorMode;
    private HashMap<Integer,Integer> mDependencies;

    private AppDatabase mDb;

    private TaskAdapter mTaskAdapter;
    private TextView mNoTasksTextView;

    TaskAdapter.TaskAdapterOnClickHandler mClickHandler;


    public TasksFragment() {
        // Required empty public constructor
    }

    public static TasksFragment newInstance(TmActivityInfo activityInfo, boolean selectorMode) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ACTIVITY_INFO, activityInfo);
        args.putBoolean(ARG_SELECTOR_MODE, selectorMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mActivityInfo = getArguments().getParcelable(ARG_ACTIVITY_INFO);
            mSelectorMode = getArguments().getBoolean(ARG_SELECTOR_MODE, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_tasks, container, false);

        mNoTasksTextView = rootView.findViewById(R.id.no_tasks_tv);

        RecyclerView tasksRecyclerView = rootView.findViewById(R.id.task_list_rv);
        Activity activity = getActivity();
        if (activity != null) {

            mDb = AppDatabase.getInstance(activity.getApplicationContext());

            GridLayoutManager layoutManager = new GridLayoutManager(activity.getApplicationContext(), 1);
            tasksRecyclerView.setLayoutManager(layoutManager);
            mTaskAdapter = new TaskAdapter(mClickHandler);
            tasksRecyclerView.setAdapter(mTaskAdapter);

            // If the fragment is not on selector mode, attach a touch helper to remove when a user swipes
            if (!mSelectorMode) {
                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        final int position = viewHolder.getAdapterPosition();
                        final List<TaskCategory> tasks = mTaskAdapter.getTasks();
                        final TaskCategory taskCategory = tasks.get(position);
                        int dependencies = getDependencies(tasks.get(position).id);
                        if (dependencies == 0) {
                            tasks.remove(position);
                            mTaskAdapter.notifyDataSetChanged();
                            Log.d(TAG, "onSwiped: deleteTask(taskCategory)");
                            Snackbar.make(viewHolder.itemView, getString(R.string.delete_confirmation, getString(R.string.task), taskCategory.name), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo_tag, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d(TAG, "onClick: undoDeleteTask()");
                                            tasks.add(position, taskCategory);
                                            mTaskAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addCallback(new Snackbar.Callback() {
                                        @Override
                                        public void onDismissed(Snackbar transientBottomBar, int event) {
                                            Log.d(TAG, "onDismissed: event=" + event);
                                            if (event == DISMISS_EVENT_TIMEOUT) {
                                                deleteTask(taskCategory);
                                            }
                                        }
                                    }).show();
                        } else {
                            mTaskAdapter.notifyDataSetChanged();
                            String deleteNotAllowed = getResources().getQuantityString(R.plurals.task_delete_not_allowed, dependencies, getString(R.string.task), taskCategory.name, dependencies);
                            Snackbar.make(viewHolder.itemView, deleteNotAllowed, Snackbar.LENGTH_LONG)
                                    .setAction("", null)
                                    .show();
                        }
                    }

                }).attachToRecyclerView(tasksRecyclerView);
            }


            setupTaskListViewModel();
        }

        return rootView;
    }

    private void deleteTask(final TaskCategory taskCategory) {
        DbExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.taskDao().deleteTaskById(taskCategory.id);
            }
        });
    }

    private int getDependencies(int taskId) {
        if (mDependencies.containsKey(taskId)) {
            return mDependencies.get(taskId);
        } else {
            return -1;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mClickHandler = (TaskAdapter.TaskAdapterOnClickHandler) context;
        }
    }

    private void setupTaskListViewModel() {
        Log.d(TAG, "setupTaskListViewModel: starts");
        TaskListViewModelFactory factory = new TaskListViewModelFactory(mDb, mActivityInfo.getId());
        TaskListViewModel viewModel = new ViewModelProvider(this, factory).get(TaskListViewModel.class);
        viewModel.getTasks().observe(getViewLifecycleOwner(), new Observer<List<TaskCategory>>() {
            @Override
            public void onChanged(List<TaskCategory> categoryWithTasks) {
                Log.d(TAG, "onChanged: list tasks " + categoryWithTasks.size());
                mTaskAdapter.setTasks(categoryWithTasks);
                if (categoryWithTasks.size() == 0) {
                    showNoTasksMessage();
                } else {
                    hideNoTasksMessage();
                }
            }
        });
        viewModel.getDependencies().observe(getViewLifecycleOwner(), new Observer<List<Dependencies>>() {
            @Override
            public void onChanged(List<Dependencies> taskDependencies) {
                mDependencies = new HashMap<>();
                for (Dependencies dependencies: taskDependencies) {
                    mDependencies.put(dependencies.id, dependencies.counter);
                }
            }
        });
    }

    private void showNoTasksMessage() {
        mNoTasksTextView.setVisibility(View.VISIBLE);
    }

    private void hideNoTasksMessage() {
        mNoTasksTextView.setVisibility(View.INVISIBLE);
    }

}