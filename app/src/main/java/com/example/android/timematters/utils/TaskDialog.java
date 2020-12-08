package com.example.android.timematters.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.android.timematters.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

public class TaskDialog extends DialogFragment {

    private static final String TAG = "TaskLogActivity";

    private static final String ARG_TASK_LOG_INFO = "task_log_info";
    private static final String ARG_ACTIVITY_INFO = "activity_info";

    public interface TaskDialogListener {
        void onTaskDialogPositiveClick(TaskLogInfo taskLogInfo, String tag);
        void onTaskDialogNegativeClick(String tag);
    }

//    private String mActionMessage;
    private TaskLogInfo mTaskLogInfo;
    private SwitchCompat mNotificationSwitch;
    private TmActivityInfo mActivityInfo;

    private int[] mStarStatus = new int[] {0,0,0,0,0};
    private ImageView mStar1;
    private ImageView mStar2;
    private ImageView mStar3;
    private ImageView mStar4;
    private ImageView mStar5;

    private TaskDialogListener mListener;

    public static TaskDialog newInstance(TmActivityInfo activityInfo, TaskLogInfo taskLogInfo) {
        Log.d(TAG, "newInstance: taskId=" + taskLogInfo.getTaskId());
        Log.d(TAG, "newInstance: taskHours=" + taskLogInfo.getHoursDurationForecast());
        Log.d(TAG, "newInstance: taskMinutes=" + taskLogInfo.getMinutesDurationForecast());
        Log.d(TAG, "newInstance: taskNotification=" + taskLogInfo.isNotification());
        TaskDialog dialogFragment = new TaskDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ACTIVITY_INFO, activityInfo);
        args.putParcelable(ARG_TASK_LOG_INFO, taskLogInfo);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Retrieve parameters
        if ( (mTaskLogInfo == null) && (getArguments() != null) ) {
            if (getArguments().containsKey(ARG_ACTIVITY_INFO)) {
                mActivityInfo = getArguments().getParcelable(ARG_ACTIVITY_INFO);
                mTaskLogInfo = getArguments().getParcelable(ARG_TASK_LOG_INFO);
            }
        }

        setRetainInstance(true);

        // Inflate layout
        int dialogLayout;
        if ("NEW".equals(getTag())) {
            dialogLayout = R.layout.dialog_start_task;
        } else {
            dialogLayout = R.layout.dialog_finish_task;
        }

        View view = getActivity().getLayoutInflater().inflate(dialogLayout, null);
        TextView taskNameTextView = view.findViewById(R.id.task_dialog_name_tv);
        taskNameTextView.setText(mTaskLogInfo.getTaskName());
        ImageView taskIconImageView = view.findViewById(R.id.task_dialog_icon_iv);
        taskIconImageView.setImageResource(AppResources.getInstance().getIcon(mTaskLogInfo.getActivityId(), mTaskLogInfo.getIcon()));

        String title;
        if ("NEW".equals(getTag())) {
            mNotificationSwitch = view.findViewById(R.id.notification_switch);
            mNotificationSwitch.setChecked(mTaskLogInfo.isNotification());

            final NumberPicker hourPiker = view.findViewById(R.id.hour_picker_np);
            hourPiker.setMaxValue(23);
            hourPiker.setMinValue(0);
            hourPiker.setValue(mTaskLogInfo.getHoursDurationForecast());
            hourPiker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                    Log.d(TAG, "onValueChange: " + oldValue + " - " + newValue);
                    mTaskLogInfo.setHoursDurationForecast(newValue);
                }
            });

            final NumberPicker minutePiker = view.findViewById(R.id.minute_picker_np);
            minutePiker.setMaxValue(59);
            minutePiker.setMinValue(0);
            minutePiker.setValue(mTaskLogInfo.getMinutesDurationForecast());
            minutePiker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                    Log.d(TAG, "onValueChange: " + oldValue + " - " + newValue);
                    mTaskLogInfo.setMinutesDurationForecast(newValue);
                }
            });

            title = getString(R.string.start_activity_title);

        } else {


            TextView taskDuration = view.findViewById(R.id.time_duration_dialog_tv);
            taskDuration.setText(mTaskLogInfo.getSpentTime());

            mStar1 = view.findViewById(R.id.star_1_iv);
            mStar2 = view.findViewById(R.id.star_2_iv);
            mStar3 = view.findViewById(R.id.star_3_iv);
            mStar4 = view.findViewById(R.id.star_4_iv);
            mStar5 = view.findViewById(R.id.star_5_iv);

            if (mActivityInfo.isRateTask()) {
                View.OnClickListener starListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mTaskLogInfo.setRating(calcRating(view.getTag()));
                        updateStarImages();
                        Log.d(TAG, "onClick: star " + view.getTag() + " - " + mTaskLogInfo.getRating());
                    }
                };
                mStar1.setOnClickListener(starListener);
                mStar2.setOnClickListener(starListener);
                mStar3.setOnClickListener(starListener);
                mStar4.setOnClickListener(starListener);
                mStar5.setOnClickListener(starListener);
                updateStarImages();
            } else {
                mStar1.setVisibility(View.INVISIBLE);
                mStar2.setVisibility(View.INVISIBLE);
                mStar3.setVisibility(View.INVISIBLE);
                mStar4.setVisibility(View.INVISIBLE);
                mStar5.setVisibility(View.INVISIBLE);
            }

            title = getString(R.string.finish_activity_title);
        }

        // Create and return the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(title)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if ("NEW".equals(getTag())) {
                            mTaskLogInfo.setNotification(mNotificationSwitch.isChecked());
                        }
                        mListener.onTaskDialogPositiveClick(mTaskLogInfo, getTag());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onTaskDialogNegativeClick(getTag());
                    }
                });
        return builder.create();

    }

    private void updateStarImages() {
        mStar1.setImageResource(AppResources.getInstance().getStarImage(mStarStatus[0]));
        mStar2.setImageResource(AppResources.getInstance().getStarImage(mStarStatus[1]));
        mStar3.setImageResource(AppResources.getInstance().getStarImage(mStarStatus[2]));
        mStar4.setImageResource(AppResources.getInstance().getStarImage(mStarStatus[3]));
        mStar5.setImageResource(AppResources.getInstance().getStarImage(mStarStatus[4]));
    }

    private int calcRating(Object tag) {
        int rating = 0;
        int selected = Integer.parseInt(tag.toString()) - 1;
        for (int i = 0; i < mStarStatus.length; i++) {
            if (i < selected) {
                mStarStatus[i] = 2;
                rating += mStarStatus[i];
            } else {
                if (i == selected) {
                    mStarStatus[i] = ++mStarStatus[i] % 3;
                    rating += mStarStatus[i];
                } else {
                    mStarStatus[i] = 0;
                }
            }
        }
        return rating;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (TaskDialog.TaskDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString() + " must implement " + TaskDialog.TaskDialogListener.class);
        }
    }

}
