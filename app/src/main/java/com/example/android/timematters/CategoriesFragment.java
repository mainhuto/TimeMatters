package com.example.android.timematters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.CategoryEntry;
import com.example.android.timematters.database.DbExecutor;
import com.example.android.timematters.utils.Dependencies;
import com.example.android.timematters.viewmodel.ActivityListViewModel;
import com.example.android.timematters.viewmodel.CategoryListViewModel;
import com.example.android.timematters.viewmodel.CategoryListViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private static final String ARG_ACTIVITY_ID = "activity_id";
    private static final String ARG_ACTIVITY_NAME = "activity_name";
    private static final String ARG_SELECTOR_MODE = "selector_mode";

    private int mActivityId;
    private boolean mSelectorMode;
    private CategoryEntry mCategoryDeleted;
    private HashMap<Integer,Integer> mDependencies;

    private AppDatabase mDb;

    private CategoryAdapter mCategoryAdapter;
    private TextView mNoCategoriesTextView;

    private CategoryAdapter.CategoryAdapterOnClickHandler mClickHandler;


    public CategoriesFragment() {
        // Required empty public constructor
    }

    public static CategoriesFragment newInstance(int activityId, String activityName, boolean selectorMode) {
        CategoriesFragment fragment = new CategoriesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTIVITY_ID, activityId);
        args.putString(ARG_ACTIVITY_NAME, activityName);
        args.putBoolean(ARG_SELECTOR_MODE, selectorMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mActivityId = getArguments().getInt(ARG_ACTIVITY_ID);
            mSelectorMode = getArguments().getBoolean(ARG_SELECTOR_MODE, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_categories, container, false);

        mNoCategoriesTextView = rootView.findViewById(R.id.no_categories_tv);

        RecyclerView categoriesRecyclerView = rootView.findViewById(R.id.category_list_rv);
        Activity activity = getActivity();
        if (activity != null) {

            mDb = AppDatabase.getInstance(activity.getApplicationContext());

            GridLayoutManager layoutManager = new GridLayoutManager(activity.getApplicationContext(), 1);
            categoriesRecyclerView.setLayoutManager(layoutManager);
            mCategoryAdapter = new CategoryAdapter(mClickHandler, mSelectorMode);
            categoriesRecyclerView.setAdapter(mCategoryAdapter);

            // If the fragment is on selector mode attach a touch helper to remove when a user swipes
            if (!mSelectorMode) {
                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        int position = viewHolder.getAdapterPosition();
                        List<CategoryEntry> categories = mCategoryAdapter.getCategories();
                        final CategoryEntry categoryEntry = categories.get(position);
                        int dependencies = getDependencies(categoryEntry);
                        if (dependencies == 0) {
                            deleteCategory(categoryEntry);
                            Snackbar.make(viewHolder.itemView, getString(R.string.delete_confirmation, getString(R.string.category), categoryEntry.getName()), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo_tag, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            undoDeleteCategory();
                                        }
                                    }).show();
                        } else {
                            mCategoryAdapter.notifyDataSetChanged();
                            String deleteNotAllowed = getResources().getQuantityString(R.plurals.category_delete_not_allowed, dependencies, getString(R.string.category), categoryEntry.getName(), dependencies);
                            Snackbar.make(viewHolder.itemView, deleteNotAllowed, Snackbar.LENGTH_LONG)
                                    .setAction("", null)
                                    .show();
                        }
                    }
                }).attachToRecyclerView(categoriesRecyclerView);
            }

            setupCategoryListViewModel();
        }

        return rootView;
    }

    private void deleteCategory(final CategoryEntry categoryEntry) {
        DbExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mCategoryDeleted = categoryEntry;
                mDb.categoryDao().deleteCategoryById(categoryEntry.getId());
            }
        });
    }

    private void undoDeleteCategory() {
        if (mCategoryDeleted != null) {
            DbExecutor.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.categoryDao().insertCategory(mCategoryDeleted);
                    mCategoryDeleted = null;
                }
            });
        }
    }

    private int getDependencies(CategoryEntry categoryEntry) {
        if ( (categoryEntry != null) && (mDependencies != null) && mDependencies.containsKey(categoryEntry.getId())) {
            return mDependencies.get(categoryEntry.getId());
        } else {
            return -1;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mClickHandler = (CategoryAdapter.CategoryAdapterOnClickHandler) context;
        }
    }

    private void setupCategoryListViewModel() {
        CategoryListViewModelFactory factory = new CategoryListViewModelFactory(mDb, mActivityId);
        final CategoryListViewModel viewModel = new ViewModelProvider(this, factory).get(CategoryListViewModel.class);
        viewModel.getCategories().observe(getViewLifecycleOwner(), new Observer<List<CategoryEntry>>() {
            @Override
            public void onChanged(List<CategoryEntry> categoryEntries) {
                Log.d(ActivityListViewModel.TAG, "onChanged: list categories");
                mCategoryAdapter.setCategories(categoryEntries);
                if (categoryEntries.size() == 0) {
                    showNoCategoriesMessage();
                } else {
                    hideNoCategoriesMessage();
                }
            }
        });
        viewModel.getDependencies().observe(getViewLifecycleOwner(), new Observer<List<Dependencies>>() {
            @Override
            public void onChanged(List<Dependencies> categoryDependencies) {
                mDependencies = new HashMap<>();
                for (Dependencies dependencies: categoryDependencies) {
                    mDependencies.put(dependencies.id, dependencies.counter);
                }
            }
        });
    }

    private void showNoCategoriesMessage() {
        mNoCategoriesTextView.setVisibility(View.VISIBLE);
    }

    private void hideNoCategoriesMessage() {
        mNoCategoriesTextView.setVisibility(View.INVISIBLE);
    }

}