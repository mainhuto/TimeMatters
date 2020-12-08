package com.example.android.timematters;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.timematters.database.CategoryEntry;
import com.example.android.timematters.utils.AppResources;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>{

    private List<CategoryEntry> mCategories;
    private final CategoryAdapterOnClickHandler mClickHandler;
    private final boolean mSelectorMode;

    public interface CategoryAdapterOnClickHandler {
        void onClick(CategoryEntry category);
    }

    public CategoryAdapter(CategoryAdapterOnClickHandler clickHandler, boolean selectorMode) {
        mClickHandler = clickHandler;
        mSelectorMode = selectorMode;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (mSelectorMode) {
            view = inflater.inflate(R.layout.category_item_selector, parent, false);
        } else {
            view = inflater.inflate(R.layout.category_item, parent, false);
        }
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryEntry categoryEntry = mCategories.get(position);
        if (categoryEntry.getColor() == 0) {
            holder.categoryItemLayout.setBackground(ContextCompat.getDrawable(holder.categoryItemLayout.getContext(), AppResources.getInstance().getColor(categoryEntry.getColor())));
        } else {
        holder.categoryItemLayout.setBackground(ContextCompat.getDrawable(holder.categoryItemLayout.getContext(), R.drawable.degraded));
            int colorFilter = holder.categoryItemLayout.getResources().getColor(AppResources.getInstance().getColor(categoryEntry.getColor()));
            holder.categoryItemLayout.getBackground().setColorFilter(colorFilter, PorterDuff.Mode.SRC_ATOP);
        }
        if (categoryEntry.getIcon() == 0) {
            holder.iconImageView.setVisibility(View.INVISIBLE);
        } else {
            holder.iconImageView.setImageResource(AppResources.getInstance().getIcon(categoryEntry.getActivityId(), categoryEntry.getIcon()));
            holder.iconImageView.setVisibility(View.VISIBLE);
        }
        holder.nameTextView.setText(categoryEntry.getName());
        if (!mSelectorMode) {
            if (categoryEntry.getTotalMinutesDuration() > 0) {
                holder.durationTextView.setText(categoryEntry.getDuration());
                holder.durationTextView.setVisibility(View.VISIBLE);
            } else {
                holder.durationTextView.setVisibility(View.INVISIBLE);
            }
            if (!categoryEntry.isNotification()) {
                holder.notificationImageView.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_notifications_off_24, null));
            } else {
                holder.notificationImageView.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_notifications_active_24, null));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mCategories == null) {
            return 0;
        }
        return mCategories.size();
    }

    public void setCategories(List<CategoryEntry> categories) {
        mCategories = categories;
        notifyDataSetChanged();
    }

    public List<CategoryEntry> getCategories() {
        return mCategories;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ConstraintLayout categoryItemLayout;
        final ImageView iconImageView;
        final TextView nameTextView;
        final TextView durationTextView;
        final ImageView notificationImageView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryItemLayout = itemView.findViewById(R.id.category_item_cl);
            iconImageView = itemView.findViewById(R.id.category_icon_iv);
            nameTextView = itemView.findViewById(R.id.category_name_tv);
            if (mSelectorMode) {
                durationTextView = null;
                notificationImageView = null;
            } else {
                durationTextView = itemView.findViewById(R.id.category_duration_tv);
                notificationImageView = itemView.findViewById(R.id.category_notification_iv);
            }
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            CategoryEntry category = mCategories.get(position);
            mClickHandler.onClick(category);
        }
    }
}
