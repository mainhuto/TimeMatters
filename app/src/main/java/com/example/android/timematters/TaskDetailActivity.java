package com.example.android.timematters;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;

import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.CategoryEntry;
import com.example.android.timematters.database.DbExecutor;
import com.example.android.timematters.database.TaskEntry;
import com.example.android.timematters.utils.AppResources;
import com.example.android.timematters.utils.PickerDialog;
import com.example.android.timematters.utils.TmActivityInfo;
import com.example.android.timematters.utils.ValidationDialog;
import com.example.android.timematters.viewmodel.CategoryViewModel;
import com.example.android.timematters.viewmodel.CategoryViewModelFactory;
import com.example.android.timematters.viewmodel.TaskViewModel;
import com.example.android.timematters.viewmodel.TaskViewModelFactory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class TaskDetailActivity extends AppCompatActivity implements PickerDialog.PickerDialogListener {

    private static final String TAG = "TaskDetailActivity";

    public static final String ARG_ACTION = "action";
    public static final String ARG_TASK_ID = "task_id";
    public static final String ARG_ACTIVITY_INFO = "activity_info";
    public static final int CATEGORY_REQUEST_CODE = 1;


    public enum Action {INSERT, UPDATE}

    private AppDatabase mDb;
    private Action mAction;
    private Integer mTaskId;
    private TmActivityInfo mActivityInfo;
    private CategoryEntry mCategoryEntry;
    private TaskEntry mTaskEntry;
    private int mTaskColorId;
    private int mTaskIconId;
    private boolean mSaveEntry;

    private EditText mTaskNameEditText;
    private Button mCategoryButton;
    private ConstraintLayout mTaskDataConstraintLayout;
    private SwitchCompat mNotificationSwitch;
    private NumberPicker mHourPiker;
    private NumberPicker mMinutePiker;
    private ImageView mColorImageView;
    private ImageView mIconImageView;

    private TaskViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        mDb = AppDatabase.getInstance(this);

        mSaveEntry = false;

        Intent intent = getIntent();
        if (intent != null) {
            if ( intent.hasExtra(ARG_ACTION) ) {
                int intAction = getIntent().getIntExtra(ARG_ACTION, 0);
                if (Action.values().length > intAction) {
                    mAction = Action.values()[intAction];
                }
            }
            if ( intent.hasExtra(ARG_ACTIVITY_INFO) ) {
                mActivityInfo = getIntent().getParcelableExtra(ARG_ACTIVITY_INFO);
                setTitle();
            }
            if ( intent.hasExtra(ARG_TASK_ID) ) {
                mTaskId = getIntent().getIntExtra(ARG_TASK_ID,0);
            }

            Log.d(TAG, "onCreate: mAction=" + mAction +  " - mTaskId=" + mTaskId);

            TaskViewModelFactory factory = new TaskViewModelFactory(mDb, mTaskId);
            mViewModel = new ViewModelProvider(this, factory).get(TaskViewModel.class);

            initViews();

            if (mViewModel.isCategorySet()) {
                retrieveCategoryEntry(mViewModel.getCategoryId());
                restoreUI();
            }

            if ( Action.UPDATE.equals(mAction) ) {
                mViewModel.getTaskEntry().observe(this, new Observer<TaskEntry>() {
                    @Override
                    public void onChanged(@Nullable TaskEntry taskEntry) {
                        mViewModel.getTaskEntry().removeObserver(this);
                        if (taskEntry != null) {
                            mTaskEntry = taskEntry;
                            if (!mViewModel.isCategorySet()) {
                                retrieveCategoryEntry(mTaskEntry.getCategoryId());
                                populateUI();
                            }
                        }
                    }
                });
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected: " + id + " - " + item.getTitle());
        switch (id) {
            case R.id.action_accept:
                if (validate()) {
                    mSaveEntry = true;
                    finish();
                }
                break;
            case R.id.action_categories:
                Intent categoriesIntent = new Intent(this, CategoriesActivity.class);
                Bundle categoriesArgs = new Bundle();
                categoriesArgs.putParcelable(CategoriesActivity.ARG_ACTIVITY_INFO, mActivityInfo);
                categoriesIntent.putExtras(categoriesArgs);
                startActivity(categoriesIntent);
                return true;
            case R.id.action_cancel:
                mSaveEntry = false;
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ( mSaveEntry ) {
            switch (mAction) {
                case INSERT:
                    insertTaskEntry();
                    break;
                case UPDATE:
                    updateTaskEntry();
                    break;
            }
        }
    }

    @Override
    public void onPickerDialogPositiveClick(int position, String tag) {
        if (PickerDialog.PickerType.COLOR.name().equals(tag)) {
            changeColor(position);
        } else {
            if (PickerDialog.PickerType.ICON.name().equals(tag)) {
                changeIcon(position);
            }
        }
    }

    @Override
    public void onPickerDialogNegativeClick(String tag) {}

    private void initViews() {
        Log.d(TAG, "initViews: starts");
        mTaskNameEditText = findViewById(R.id.task_name_pt);
        mCategoryButton = findViewById(R.id.task_category_bt);
        mCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCategory();
            }
        });
        mTaskDataConstraintLayout = findViewById(R.id.task_data_cl);
        if (!mViewModel.isCategorySet()) {
            mTaskDataConstraintLayout.setVisibility(View.GONE);
            mCategoryButton.setEnabled(true);
        } else {
            disableCategoryButton();
        }
        mNotificationSwitch = findViewById(R.id.notification_switch);
        mNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean active) {
                Log.d(TAG, "onCheckedChanged: " + active);
                mViewModel.setNotificationActive(active);
            }
        });
        mHourPiker = findViewById(R.id.hour_picker_np);
        mHourPiker.setMaxValue(23);
        mHourPiker.setMinValue(0);
        mHourPiker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                Log.d(TAG, "onValueChange: " + oldValue + " - " + newValue);
                mViewModel.setHoursDuration(newValue);
                checkNotificationSwitch();
            }
        });
        mMinutePiker = findViewById(R.id.minute_picker_np);
        mMinutePiker.setMaxValue(59);
        mMinutePiker.setMinValue(0);
        mMinutePiker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                Log.d(TAG, "onValueChange: " + oldValue + " - " + newValue);
                mViewModel.setMinutesDuration(newValue);
                checkNotificationSwitch();
            }
        });
        mColorImageView = findViewById(R.id.task_color_iv);
        mColorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = PickerDialog.newInstance(PickerDialog.PickerType.COLOR, mActivityInfo.getId());
                newFragment.show(getSupportFragmentManager(), PickerDialog.PickerType.COLOR.name());
            }
        });
        mIconImageView = findViewById(R.id.task_icon_iv);
        mIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = PickerDialog.newInstance(PickerDialog.PickerType.ICON, mActivityInfo.getId());
                newFragment.show(getSupportFragmentManager(), PickerDialog.PickerType.ICON.name());
            }
        });

        checkNotificationSwitch();

        String subtitle;
        if (Action.INSERT.equals(mAction)) {
            subtitle = getString(R.string.new_task_title);
        } else {
            subtitle = getString(R.string.task_detail_title);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }

    }

    private void disableCategoryButton() {
        Log.d(TAG, "disableCategoryButton: starts");
        mTaskDataConstraintLayout.setVisibility(View.VISIBLE);
        mCategoryButton.setEnabled(false);
        mCategoryButton.setBackground(null);
        mCategoryButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
    }

    private void populateUI() {
        Log.d(TAG, "populateUI: starts");
        mTaskNameEditText.setText(mTaskEntry.getName());
        mNotificationSwitch.setChecked(mTaskEntry.isNotification());
        mHourPiker.setValue(mTaskEntry.getHoursDuration());
        mViewModel.setHoursDuration(mTaskEntry.getHoursDuration());
        mMinutePiker.setValue(mTaskEntry.getMinutesDuration());
        mViewModel.setMinutesDuration(mTaskEntry.getMinutesDuration());
        checkNotificationSwitch();
        changeColor(mTaskEntry.getColor());
        changeIcon(mTaskEntry.getIcon());

        disableCategoryButton();
    }

    private void  restoreUI() {
        changeColor(mViewModel.getColor());
        changeIcon(mViewModel.getIcon());
        mHourPiker.setValue(mViewModel.getHoursDuration());
        mMinutePiker.setValue(mViewModel.getMinutesDuration());
        checkNotificationSwitch();
    }

    private void displayCategoryName() {
        Log.d(TAG, "displayCategoryName: starts");
        mCategoryButton.setText(mCategoryEntry.getName());
    }

    private void setInheritedData() {
        Log.d(TAG, "setInheritedData: category id=" + mCategoryEntry.getId());
        displayCategoryName();
        mNotificationSwitch.setChecked(mCategoryEntry.isNotification());
        mHourPiker.setValue(mCategoryEntry.getHoursDuration());
        mViewModel.setHoursDuration(mCategoryEntry.getHoursDuration());
        mMinutePiker.setValue(mCategoryEntry.getMinutesDuration());
        mViewModel.setMinutesDuration(mCategoryEntry.getMinutesDuration());
        checkNotificationSwitch();
        changeColor(mCategoryEntry.getColor());
        changeIcon(mCategoryEntry.getIcon());
    }

    private void setTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mActivityInfo.getName());
        }
    }

    private void selectCategory() {
        Intent categoriesIntent = new Intent(this, CategoriesActivity.class);
        Bundle categoriesArgs = new Bundle();
        categoriesArgs.putParcelable(CategoriesActivity.ARG_ACTIVITY_INFO, mActivityInfo);
        categoriesArgs.putBoolean(CategoriesActivity.ARG_SELECTOR_MODE, true);
        categoriesIntent.putExtras(categoriesArgs);
        startActivityForResult(categoriesIntent, CATEGORY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: starts");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CATEGORY_REQUEST_CODE) {
            if  ( (resultCode == RESULT_OK) && ( data != null) ) {
                int categoryId = data.getIntExtra(CategoriesActivity.EXTRA_CATEGORY_ID, -1);
                Log.d(TAG, "onActivityResult: categoryId=" + categoryId);
                retrieveCategoryEntry(categoryId);
                disableCategoryButton();
            }
        }
    }

    private boolean validate() {
        if (mTaskNameEditText.getText().toString().isEmpty()) {
            ValidationDialog validationDialog = ValidationDialog.newInstance(getString(R.string.task_name_mandatory));
            validationDialog.show(getSupportFragmentManager(), null);
            return false;
        } else {
            if (mCategoryEntry == null) {
                ValidationDialog validationDialog = ValidationDialog.newInstance(getString(R.string.category_mandatory));
                validationDialog.show(getSupportFragmentManager(), null);
                return false;
            }
        }
        return true;
    }

    private void updateTaskEntry() {
        mTaskEntry.setName(mTaskNameEditText.getText().toString());
        mTaskEntry.setNotification(mNotificationSwitch.isChecked());
        mTaskEntry.setHoursDuration(mHourPiker.getValue());
        mTaskEntry.setMinutesDuration(mMinutePiker.getValue());
        mTaskEntry.setColor(mTaskColorId);
        mTaskEntry.setIcon(mTaskIconId);
        DbExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.taskDao().updateTask(mTaskEntry);
            }
        });
    }

    private void insertTaskEntry() {
        String name = mTaskNameEditText.getText().toString();
        int hours = mHourPiker.getValue();
        int minutes = mMinutePiker.getValue();
        boolean notification = mNotificationSwitch.isChecked();
        mTaskEntry = new TaskEntry(mActivityInfo.getId(), mCategoryEntry.getId(), name, hours, minutes, notification, mTaskColorId, mTaskIconId, null);
        DbExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.taskDao().insertTask(mTaskEntry);
            }
        });
    }

    private void checkNotificationSwitch(){
        if ( (mHourPiker.getValue() > 0 ) || (mMinutePiker.getValue() > 0) ) {
            mNotificationSwitch.setEnabled(true);
            mNotificationSwitch.setChecked(mViewModel.isNotificationActive());
        } else {
            mNotificationSwitch.setEnabled(false);
            boolean notificationActive = mViewModel.isNotificationActive();
            mNotificationSwitch.setChecked(false);
            mViewModel.setNotificationActive(notificationActive);
        }
    }

    private void changeColor(int colorId) {
        mTaskColorId = colorId;
        if (colorId == 0) {
            mColorImageView.setColorFilter(null);
        } else {
            mColorImageView.setColorFilter(getResources().getColor(AppResources.getInstance().getColor(colorId)));
        }
        mViewModel.setColor(colorId);
    }

    private void changeIcon(int iconId) {
        mTaskIconId = iconId;
        mIconImageView.setImageResource(AppResources.getInstance().getIcon(mActivityInfo.getId(), iconId));
        mViewModel.setIcon(iconId);
    }

    private void retrieveCategoryEntry(int categoryId) {
        Log.d(TAG, "retrieveCategoryEntry: id=" + categoryId);
        CategoryViewModelFactory factory = new CategoryViewModelFactory(mDb, categoryId);
        final CategoryViewModel viewModel = new ViewModelProvider(this, factory).get(CategoryViewModel.class);
        viewModel.getCategoryEntry().observe(this, new Observer<CategoryEntry>() {
            @Override
            public void onChanged(@Nullable CategoryEntry categoryEntry) {
                viewModel.getCategoryEntry().removeObserver(this);
                if (categoryEntry != null) {
                    mCategoryEntry = categoryEntry;
                    if ( (Action.INSERT.equals(mAction)) && !mViewModel.isCategorySet() ) {
                        setInheritedData();
                    } else {
                        displayCategoryName();
                    }
                    mViewModel.setCategoryId(mCategoryEntry.getId());
                }
            }
        });
    }

}

