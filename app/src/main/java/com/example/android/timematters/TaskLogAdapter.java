package com.example.android.timematters;

import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.timematters.database.TaskLogView;
import com.example.android.timematters.database.TaskStatus;
import com.example.android.timematters.utils.AppResources;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class TaskLogAdapter extends RecyclerView.Adapter<TaskLogAdapter.TaskLogViewHolder>{

    private static final String TAG = "TaskLogAdapter";

    private static final int VIEW_TYPE_IN_PROGRESS = 0;
    private static final int VIEW_TYPE_FINISHED = 1;

    private List<TaskLogView> mTaskLogs;
    private final TaskLogAdapterOnClickHandler mClickHandler;

    public interface TaskLogAdapterOnClickHandler {
        void onClickStop(TaskLogView taskLogView);
        void onClickPause(TaskLogView taskLogView);
        void onClickPlay(TaskLogView taskLogView);
        boolean isRateTask();
    }

    public TaskLogAdapter(TaskLogAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public TaskLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int layoutId;

        switch (viewType) {
            case VIEW_TYPE_IN_PROGRESS:
                layoutId = R.layout.task_in_progress;
                break;
            case VIEW_TYPE_FINISHED:
                layoutId = R.layout.task_log_item;
                break;
            default:
                throw new IllegalArgumentException(parent.getContext().getString(R.string.invalid_view_type) + viewType);
        }

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(layoutId, parent, false);

        return new TaskLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskLogViewHolder holder, int position) {
        TaskLogView taskLogInfo = mTaskLogs.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_IN_PROGRESS) {
            holder.startTimeTextView.setText(taskLogInfo.getStartTime());
            Log.d(TAG, "onBindViewHolder: elapsedRealtime=" + SystemClock.elapsedRealtime());
            Log.d(TAG, "onBindViewHolder: Date()=" + new Date().getTime());

            if (!taskLogInfo.notification) {
                holder.notificationImageView.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_notifications_off_24, null));
            } else {
                holder.notificationImageView.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_notifications_active_24, null));
            }
            holder.durationTextView.setText(taskLogInfo.getDurationForecast());

            long lastBase;
            long elapsedRealtimeOffset;
            if (TaskStatus.IN_PROGRESS.equals(taskLogInfo.status)) {
                holder.playPauseImageView.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_pause_24, null));
                lastBase = taskLogInfo.date.getTime() - taskLogInfo.realSpentTime;
                elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
                holder.chronometer.setBase(lastBase - elapsedRealtimeOffset);
                holder.chronometer.start();
            } else {
                holder.playPauseImageView.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_play_24, null));
                lastBase = new Date().getTime() - taskLogInfo.realSpentTime;
                elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
                holder.chronometer.setBase(lastBase - elapsedRealtimeOffset);
                holder.chronometer.stop();
            }

        } else {
            if (taskLogInfo.color == 0) {
                holder.taskItemLayout.setBackground(ContextCompat.getDrawable(holder.taskItemLayout.getContext(), AppResources.getInstance().getColor(taskLogInfo.color)));
            } else {
                holder.taskItemLayout.setBackground(ContextCompat.getDrawable(holder.taskItemLayout.getContext(), R.drawable.degraded));
                int colorFilter = holder.taskItemLayout.getResources().getColor(AppResources.getInstance().getColor(taskLogInfo.color));
                holder.taskItemLayout.getBackground().setColorFilter(colorFilter, PorterDuff.Mode.SRC_ATOP);
            }
            if (taskLogInfo.realSpentTime > 0) {
                holder.durationTextView.setText(taskLogInfo.getRealSpentTime());
                holder.durationTextView.setVisibility(View.VISIBLE);
            } else {
                holder.durationTextView.setVisibility(View.INVISIBLE);
            }
            if (taskLogInfo.occurrence > 1) {
                holder.occurrencesFrameLayout.setVisibility(View.VISIBLE);
                holder.occurrencesTextView.setText(Integer.toString(taskLogInfo.occurrence));
                holder.occurrencesImageView.setColorFilter(holder.occurrencesImageView.getResources().getColor(R.color.colorAccent));
            } else {
                holder.occurrencesFrameLayout.setVisibility(View.INVISIBLE);
            }

            if (mClickHandler.isRateTask()) {
                int[] starStatus = getStarStatus(taskLogInfo.rating);
                Log.d(TAG, "onBindViewHolder: " + taskLogInfo.rating + " - " + starStatus[0] + "," + starStatus[1] + "," + starStatus[2] + "," + starStatus[3] + "," + starStatus[4] );
                holder.star1.setImageResource(AppResources.getInstance().getStarImage(starStatus[0]));
                holder.star2.setImageResource(AppResources.getInstance().getStarImage(starStatus[1]));
                holder.star3.setImageResource(AppResources.getInstance().getStarImage(starStatus[2]));
                holder.star4.setImageResource(AppResources.getInstance().getStarImage(starStatus[3]));
                holder.star5.setImageResource(AppResources.getInstance().getStarImage(starStatus[4]));
                holder.star1.setVisibility(View.VISIBLE);
                holder.star2.setVisibility(View.VISIBLE);
                holder.star3.setVisibility(View.VISIBLE);
                holder.star4.setVisibility(View.VISIBLE);
                holder.star5.setVisibility(View.VISIBLE);
            } else {
                holder.star1.setVisibility(View.INVISIBLE);
                holder.star2.setVisibility(View.INVISIBLE);
                holder.star3.setVisibility(View.INVISIBLE);
                holder.star4.setVisibility(View.INVISIBLE);
                holder.star5.setVisibility(View.INVISIBLE);
            }
        }

        if (taskLogInfo.icon == 0) {
            holder.iconImageView.setVisibility(View.INVISIBLE);
        } else {
            holder.iconImageView.setImageResource(AppResources.getInstance().getIcon(taskLogInfo.activityId, taskLogInfo.icon));
            holder.iconImageView.setVisibility(View.VISIBLE);
        }
        holder.nameTextView.setText(taskLogInfo.taskName);
        holder.categoryNameTextView.setText(taskLogInfo.categoryName);
    }

    @Override
    public int getItemCount() {
        if (mTaskLogs == null) {
            return 0;
        }
        return mTaskLogs.size();
    }

    private int[] getStarStatus(int rating) {
        int[] starStatus = new int[] {0,0,0,0,0};
        int posRating = 0;
        for (int i = 0; i < starStatus.length; i++) {
            posRating++;
            if (posRating < rating) {
                starStatus[i] = 2;
                posRating++;
                if (posRating == rating) {
                    break;
                }
            } else {
                if (posRating == rating) {
                    starStatus[i] = 1;
                    break;
                }
            }
        }
        return starStatus;
    }

    @Override
    public int getItemViewType(int position) {
        if ( ( position == 0) && (TaskStatus.IN_PROGRESS.equals( mTaskLogs.get(0).status) || TaskStatus.ON_PAUSE.equals( mTaskLogs.get(0).status) ) ) {
           return VIEW_TYPE_IN_PROGRESS;
        }
        return VIEW_TYPE_FINISHED;
    }

    public void setTaskLogs(List<TaskLogView> taskLogs) {
        mTaskLogs = taskLogs;
        notifyDataSetChanged();
    }

    public class TaskLogViewHolder extends RecyclerView.ViewHolder {

        final ConstraintLayout taskItemLayout;
        final ImageView iconImageView;
        final TextView nameTextView;
        final TextView durationTextView;
        final TextView categoryNameTextView;

        final TextView startTimeTextView;
        final Chronometer chronometer;
        final ImageView notificationImageView;

        final ImageView stopImageView;
        final ImageView playPauseImageView;

        final private ImageView star1;
        final private ImageView star2;
        final private ImageView star3;
        final private ImageView star4;
        final private ImageView star5;

        final private FrameLayout occurrencesFrameLayout;
        final private ImageView occurrencesImageView;
        final private TextView occurrencesTextView;

        public TaskLogViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "TaskLogViewHolder: starts");
            taskItemLayout = itemView.findViewById(R.id.task_item_cl);
            iconImageView = itemView.findViewById(R.id.task_icon_iv);
            nameTextView = itemView.findViewById(R.id.task_name_tv);
            durationTextView = itemView.findViewById(R.id.task_duration_tv);
            categoryNameTextView = itemView.findViewById(R.id.task_category_name_tv);
            startTimeTextView = itemView.findViewById(R.id.start_time_tv);
            notificationImageView = itemView.findViewById(R.id.task_notification_iv);
            chronometer = itemView.findViewById(R.id.chronometer);

            stopImageView = itemView.findViewById(R.id.stop_iv);
            Log.d(TAG, "TaskLogViewHolder: findView stopImageView");
            if (stopImageView != null) {
                Log.d(TAG, "TaskLogViewHolder: set OnClickListener");
                stopImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: " + getAdapterPosition());
                        mClickHandler.onClickStop(mTaskLogs.get(getAdapterPosition()));

                    }
                });
            }

            playPauseImageView = itemView.findViewById(R.id.play_pause_iv);
            if (playPauseImageView != null) {
                playPauseImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: " + getAdapterPosition());
                        TaskLogView taskLogView = mTaskLogs.get(getAdapterPosition());
                        if ( TaskStatus.IN_PROGRESS.equals(taskLogView.status) ) {
                            mClickHandler.onClickPause(taskLogView);
                        } else {
                            mClickHandler.onClickPlay(taskLogView);
                        }

                    }
                });
            }

            star1 = itemView.findViewById(R.id.star_1_iv);
            star2 = itemView.findViewById(R.id.star_2_iv);
            star3 = itemView.findViewById(R.id.star_3_iv);
            star4 = itemView.findViewById(R.id.star_4_iv);
            star5 = itemView.findViewById(R.id.star_5_iv);

            occurrencesImageView = itemView.findViewById(R.id.circle_occurrences_im);
            occurrencesTextView = itemView.findViewById(R.id.occurrences_tv);
            occurrencesFrameLayout = itemView.findViewById(R.id.occurrences_fl);

        }

    }
}
