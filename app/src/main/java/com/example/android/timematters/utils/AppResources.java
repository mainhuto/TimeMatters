package com.example.android.timematters.utils;

import com.example.android.timematters.ActivityType;
import com.example.android.timematters.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppResources {

    private List<Integer> mActivityColors;
    private List<Integer> mColors;
    private HashMap<Integer, List<Integer>> mIcons;
    private List<Integer> mStarImages;
    private static final AppResources ourInstance = new AppResources();

    public static AppResources getInstance() {
        return ourInstance;
    }

    private AppResources() {
        createActivityColorList();
        createColorList();
        createIconsList();
        createStarImageList();
    }

    private void createColorList() {

        mColors = new ArrayList<>();

        mColors.add(R.color.color01);
        mColors.add(R.color.color02);
        mColors.add(R.color.color03);
        mColors.add(R.color.color04);
        mColors.add(R.color.color05);
        mColors.add(R.color.color06);
        mColors.add(R.color.color07);
        mColors.add(R.color.color08);
        mColors.add(R.color.color09);
        mColors.add(R.color.color10);
        mColors.add(R.color.color11);
        mColors.add(R.color.color12);
        mColors.add(R.color.color13);
        mColors.add(R.color.color14);
        mColors.add(R.color.color15);
        mColors.add(R.color.color16);
        mColors.add(R.color.color17);
        mColors.add(R.color.color18);
        mColors.add(R.color.color19);
        mColors.add(R.color.color20);

    }

    private void createActivityColorList() {

        mActivityColors = new ArrayList<>();

        mActivityColors.add(15);
        mActivityColors.add(13);
        mActivityColors.add(8);
        mActivityColors.add(19);

    }

    private void createIconsList() {

        mIcons = new HashMap<>();

        // Create and add Fitness icons
        List<Integer> icons = new ArrayList<>();
        icons.add(R.drawable.no_icon_visible);
        icons.add(R.drawable.fitness01);
        icons.add(R.drawable.fitness02);
        icons.add(R.drawable.fitness03);
        icons.add(R.drawable.fitness04);
        icons.add(R.drawable.fitness05);
        icons.add(R.drawable.fitness06);
        icons.add(R.drawable.fitness07);
        icons.add(R.drawable.fitness08);
        icons.add(R.drawable.fitness09);
        icons.add(R.drawable.fitness10);
        icons.add(R.drawable.fitness11);
        icons.add(R.drawable.fitness12);
        icons.add(R.drawable.fitness13);
        icons.add(R.drawable.fitness14);
        icons.add(R.drawable.fitness15);
        icons.add(R.drawable.fitness16);
        icons.add(R.drawable.fitness17);
        icons.add(R.drawable.fitness18);
        icons.add(R.drawable.fitness19);
        icons.add(R.drawable.fitness20);
        mIcons.put(ActivityType.FITNESS.ordinal(), icons);

        // Create and add Study icons
        icons = new ArrayList<>();
        icons.add(R.drawable.no_icon_visible);
        icons.add(R.drawable.study01);
        icons.add(R.drawable.study02);
        icons.add(R.drawable.study03);
        icons.add(R.drawable.study04);
        icons.add(R.drawable.study05);
        icons.add(R.drawable.study06);
        icons.add(R.drawable.study07);
        icons.add(R.drawable.study08);
        icons.add(R.drawable.study09);
        icons.add(R.drawable.study10);
        icons.add(R.drawable.study11);
        icons.add(R.drawable.study12);
        icons.add(R.drawable.study13);
        icons.add(R.drawable.study14);
        icons.add(R.drawable.study15);
        icons.add(R.drawable.study16);
        mIcons.put(ActivityType.STUDY.ordinal(), icons);

        // Create and add Hobbies icons
        icons = new ArrayList<>();
        icons.add(R.drawable.no_icon_visible);
        icons.add(R.drawable.hobbies01);
        icons.add(R.drawable.hobbies02);
        icons.add(R.drawable.hobbies03);
        icons.add(R.drawable.hobbies04);
        icons.add(R.drawable.hobbies05);
        icons.add(R.drawable.hobbies06);
        icons.add(R.drawable.hobbies07);
        icons.add(R.drawable.hobbies08);
        icons.add(R.drawable.hobbies09);
        icons.add(R.drawable.hobbies10);
        icons.add(R.drawable.study02);
        icons.add(R.drawable.study04);
        icons.add(R.drawable.study16);
        icons.add(R.drawable.work04);
        icons.add(R.drawable.work07);
        icons.add(R.drawable.work19);
        mIcons.put(ActivityType.HOBBIES.ordinal(), icons);

        // Create and add Work icons
        icons = new ArrayList<>();
        icons.add(R.drawable.no_icon_visible);
        icons.add(R.drawable.work01);
        icons.add(R.drawable.work02);
        icons.add(R.drawable.work03);
        icons.add(R.drawable.work04);
        icons.add(R.drawable.work05);
        icons.add(R.drawable.work06);
        icons.add(R.drawable.work07);
        icons.add(R.drawable.work08);
        icons.add(R.drawable.work09);
        icons.add(R.drawable.work10);
        icons.add(R.drawable.work11);
        icons.add(R.drawable.work12);
        icons.add(R.drawable.work13);
        icons.add(R.drawable.work14);
        icons.add(R.drawable.work15);
        icons.add(R.drawable.work16);
        icons.add(R.drawable.work17);
        icons.add(R.drawable.work18);
        icons.add(R.drawable.work19);
//        icons.add(R.drawable.work20);
        mIcons.put(ActivityType.WORK.ordinal(), icons);

    }

    private void createStarImageList() {

        // Create and add star images
        mStarImages = new ArrayList<>();
        mStarImages.add(R.drawable.star_empty_24);
        mStarImages.add(R.drawable.star_half_24);
        mStarImages.add(R.drawable.star_full_24);

    }


    public List<Integer> getIconPalette(int activityId) {
        return mIcons.get(activityId);
    }

    public List<Integer> getColors() {
        return mColors;
    }

    public int getColor(int colorId) {
        if (mColors.size() > colorId) {
            return mColors.get(colorId);
        }
        return 0;
    }

    public int getActivityColor(int activityId) {
        return mActivityColors.size() > activityId ? mActivityColors.get(activityId) : 0;
    }

    public int getIcon(int activityId, int iconId) {
        if (mIcons.size() > activityId) {
            List<Integer> icons = mIcons.get(activityId);
            if ( (icons != null) && (icons.size() > iconId) ) {
                return icons.get(iconId);
            }
        }
        return 0;
    }

    public int getStarImage(int starStatus) {
        if ( (mStarImages != null) && (mStarImages.size() > starStatus) ) {
            return mStarImages.get(starStatus);
        }
        return 0;
    }

}
