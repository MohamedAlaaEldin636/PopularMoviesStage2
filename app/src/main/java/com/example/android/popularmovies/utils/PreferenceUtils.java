package com.example.android.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.popularmovies.R;

/**
 * Created by Mohamed on 2/19/2018.
 *
 */

public class PreferenceUtils {

    public static String getSortMoviesBy(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.sort_movies_by_key);
        String defaultValue = context.getString(R.string.popular_value);
        String prefPath = prefs.getString(key, defaultValue);
        if (prefPath.equals(defaultValue)){
            return NetworkUtils.PATH_POPULAR;
        }else if (prefPath.equals(context.getString(R.string.top_rated_value))){
            return NetworkUtils.PATH_TOP_RATED;
        }else {
            return NetworkUtils.PATH_FAVOURITE;
        }
    }

    public static int getColumnsInPortrait(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.columns_in_portrait_key);
        String defaultValue = context.getString(R.string.columns_in_portrait_default);
        return Integer.parseInt(
                prefs.getString(key, defaultValue));
    }

    public static int getColumnsInLandscape(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.columns_in_landscape_key);
        String defaultValue = context.getString(R.string.columns_in_landscape_default);
        return Integer.parseInt(
                prefs.getString(key, defaultValue));
    }

}
