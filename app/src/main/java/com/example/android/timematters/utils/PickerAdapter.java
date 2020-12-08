package com.example.android.timematters.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class PickerAdapter extends BaseAdapter {

    private static final int ITEM_SIZE = 64;

    private final List<Integer> mItems;
    private final Context mContext;

    public PickerAdapter(Context context, List<Integer> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public int getCount() {
        if (mItems == null) {
            return 0;
        }
        return mItems.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ImageView imageView;

        if (view == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(convertDpToPixels(ITEM_SIZE), convertDpToPixels(ITEM_SIZE)));
        } else {
            imageView = (ImageView) view;
        }

        imageView.setImageResource(mItems.get(position));

        return imageView;
    }

    private int convertDpToPixels(float dp){
        Resources resources = mContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
