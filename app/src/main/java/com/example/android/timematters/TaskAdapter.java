package com.example.android.timematters;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.timematters.database.TaskCategory;
import com.example.android.timematters.utils.AppResources;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private List<TaskCategory> mTasks;
    private final TaskAdapterOnClickHandler mClickHandler;

    public interface TaskAdapterOnClickHandler {
        void onClick(TaskCategory task);
    }

    public TaskAdapter(TaskAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskCategory taskCategory = mTasks.get(position);
        if (taskCategory.color == 0) {
            holder.taskItemLayout.setBackground(ContextCompat.getDrawable(holder.taskItemLayout.getContext(), AppResources.getInstance().getColor(taskCategory.color)));
        } else {
        holder.taskItemLayout.setBackground(ContextCompat.getDrawable(holder.taskItemLayout.getContext(), R.drawable.degraded));
            int colorFilter = holder.taskItemLayout.getResources().getColor(AppResources.getInstance().getColor(taskCategory.color));
            holder.taskItemLayout.getBackground().setColorFilter(colorFilter, PorterDuff.Mode.SRC_ATOP);
        }
        if (taskCategory.icon == 0) {
            holder.iconImageView.setVisibility(View.INVISIBLE);
        } else {
            holder.iconImageView.setImageResource(AppResources.getInstance().getIcon(taskCategory.activityId, taskCategory.icon));
            holder.iconImageView.setVisibility(View.VISIBLE);
        }
        holder.nameTextView.setText(taskCategory.name);
        holder.categoryNameTextView.setText(taskCategory.categoryName);
        if (taskCategory.getTotalMinutesDuration() > 0) {
            holder.durationTextView.setText(taskCategory.getDuration());
            holder.durationTextView.setVisibility(View.VISIBLE);
        } else {
            holder.durationTextView.setVisibility(View.INVISIBLE);
        }
        if (!taskCategory.notification) {
            holder.notificationImageView.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_notifications_off_24, null));
        } else {
            holder.notificationImageView.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_notifications_active_24, null));
        }
    }

    @Override
    public int getItemCount() {
        if (mTasks == null) {
            return 0;
        }
        return mTasks.size();
    }

    public void setTasks(List<TaskCategory> tasks) {
        mTasks = tasks;
        notifyDataSetChanged();
    }

    public List<TaskCategory> getTasks() {
        return mTasks;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ConstraintLayout taskItemLayout;
        final ImageView iconImageView;
        final TextView nameTextView;
        final TextView durationTextView;
        final TextView categoryNameTextView;
        final ImageView notificationImageView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskItemLayout = itemView.findViewById(R.id.task_item_cl);
            iconImageView = itemView.findViewById(R.id.task_icon_iv);
            nameTextView = itemView.findViewById(R.id.task_name_tv);
            durationTextView = itemView.findViewById(R.id.task_duration_tv);
            notificationImageView = itemView.findViewById(R.id.task_notification_iv);
            categoryNameTextView = itemView.findViewById(R.id.task_category_name_tv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            TaskCategory task = mTasks.get(position);
            mClickHandler.onClick(task);
        }
    }

}
