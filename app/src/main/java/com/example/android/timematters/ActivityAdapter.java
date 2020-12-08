package com.example.android.timematters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.utils.AppResources;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>{

    private List<ActivityEntry> mActivities;
    private final ActivityAdapterOnClickHandler mClickHandler;

    public interface ActivityAdapterOnClickHandler {
        void onClick(ActivityEntry activity);
    }

    public ActivityAdapter(ActivityAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public ActivityAdapter.ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.activity_item, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityAdapter.ActivityViewHolder holder, int position) {
        ActivityEntry activity = mActivities.get(position);
        holder.nameTextView.setText(activity.getName());
        int colorFilter = holder.colorImageView.getResources().getColor(AppResources.getInstance().getColor(activity.getColor()));
        holder.colorImageView.setColorFilter(colorFilter);
    }

    @Override
    public int getItemCount() {
        if (mActivities == null) {
            return 0;
        }
        return mActivities.size();
    }

    public void setActivities(List<ActivityEntry> activities) {
        mActivities = activities;
        notifyDataSetChanged();
    }

    public List<ActivityEntry> getActivities() {
        return mActivities;
    }

    public class ActivityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

//        final ConstraintLayout activityItemLayout;
        final TextView nameTextView;
        final ImageView colorImageView;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
//            activityItemLayout = (ConstraintLayout) itemView.findViewById(R.id.activity_item_cl);
            nameTextView = itemView.findViewById(R.id.activity_name_tv);
            colorImageView = itemView.findViewById(R.id.activity_color_iv);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            ActivityEntry activity = mActivities.get(position);
            mClickHandler.onClick(activity);
        }
    }
}
