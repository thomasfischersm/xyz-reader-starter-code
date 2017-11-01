package com.example.xyzreader;

import android.content.Context;

import com.example.xyzreader.util.BasePreferences;

/**
 * Preferences for this application.
 */
public class XYZReaderPreferences {

    private static final String PREFS_NAME = "XyzPreferences";

    private static final String HAS_SEEN_SWIPE_SNACK_BAR_KEY = "hasSeenSwipeSnackBar";

    private static final boolean HAS_SEEN_SWIPE_SNACK_BAR_DEFAULT = false;

    private static BasePreferences basePreferences = new BasePreferences(PREFS_NAME);

    public static boolean hasSeenSwipeSnackBar(Context context) {
        return basePreferences.getBoolean(
                context,
                HAS_SEEN_SWIPE_SNACK_BAR_KEY,
                HAS_SEEN_SWIPE_SNACK_BAR_DEFAULT);
    }

    public static void setHasSeenSwipeSnackBar(Context context, boolean hasSeen) {
        basePreferences.setBoolean(context, HAS_SEEN_SWIPE_SNACK_BAR_KEY, hasSeen);
    }
}
