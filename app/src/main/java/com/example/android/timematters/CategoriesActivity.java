package com.example.android.timematters;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.example.android.timematters.database.CategoryEntry;
import com.example.android.timematters.utils.TmActivityInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CategoriesActivity extends AppCompatActivity implements CategoryAdapter.CategoryAdapterOnClickHandler {

    private static final String TAG = "CategoriesActivity";

    public static final String ARG_ACTIVITY_INFO = "activity_info";
    public static final String ARG_SELECTOR_MODE = "selector_mode";
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";

    private TmActivityInfo mActivityInfo;
    private boolean mSelectorMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        if ( getIntent().hasExtra(ARG_ACTIVITY_INFO) ) {
            mActivityInfo = getIntent().getParcelableExtra(ARG_ACTIVITY_INFO);
        }
        mSelectorMode = getIntent().getBooleanExtra(ARG_SELECTOR_MODE, false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mSelectorMode) {
            getSupportActionBar().setTitle(getString(R.string.category_selection_title));
            getSupportActionBar().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.color.colorAccent, null));
        } else {
            getSupportActionBar().setTitle(getString(R.string.category_list_title, mActivityInfo.getName()));
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment categoriesFragment = fragmentManager.findFragmentById(R.id.category_list_container);
        if (categoriesFragment == null) {
            categoriesFragment = CategoriesFragment.newInstance(mActivityInfo.getId(), mActivityInfo.getName(), mSelectorMode);
            fragmentManager.beginTransaction()
                    .add(R.id.category_list_container, categoriesFragment)
                    .commit();
        }

        FloatingActionButton fab = findViewById(R.id.category_fab);

        if (mSelectorMode) {
            fab.hide();
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Log.d(TAG, "onClick: new category");
                Intent categoryIntent = new Intent(getApplicationContext(), CategoryDetailActivity.class);
                Bundle categoryArgs = new Bundle();
                categoryArgs.putInt(CategoryDetailActivity.ARG_ACTION, CategoryDetailActivity.Action.INSERT.ordinal());
                categoryArgs.putParcelable(CategoryDetailActivity.ARG_ACTIVITY_INFO, mActivityInfo);
                categoryIntent.putExtras(categoryArgs);
                startActivity(categoryIntent);
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
    public void onClick(CategoryEntry category) {
        Log.d(TAG, "onClick: " + category.getName());

        if (mSelectorMode) {
            int categoryId = category.getId();
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_CATEGORY_ID, categoryId);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Intent categoryIntent = new Intent(this, CategoryDetailActivity.class);
            Bundle categoryArgs = new Bundle();
            categoryArgs.putInt(CategoryDetailActivity.ARG_ACTION, CategoryDetailActivity.Action.UPDATE.ordinal());
            categoryArgs.putParcelable(CategoryDetailActivity.ARG_ACTIVITY_INFO, mActivityInfo);
            categoryArgs.putInt(CategoryDetailActivity.ARG_CATEGORY_ID, category.getId());
            categoryIntent.putExtras(categoryArgs);
            startActivity(categoryIntent);
        }

    }
}