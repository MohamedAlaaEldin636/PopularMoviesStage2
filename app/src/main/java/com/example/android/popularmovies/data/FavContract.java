package com.example.android.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Mohamed on 3/5/2018.
 *
 */

public class FavContract {

    static final String AUTHORITY = "com.example.android.popularmovies";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    static final String PATH_FAVOURITE = "favourite";

    public static final class FavEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITE).build();

        static final String TABLE_NAME = "favourites";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_JSON_OBJECT = "movie_json_object";
        public static final String COLUMN_TRAILER_JSON_OBJECT = "trailer_json_object";
        public static final String COLUMN_REVIEW_JSON_OBJECT = "review_json_object";
    }
}
