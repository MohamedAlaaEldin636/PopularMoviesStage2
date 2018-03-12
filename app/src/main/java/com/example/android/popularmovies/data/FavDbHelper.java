package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.data.FavContract.FavEntry;

/**
 * Created by Mohamed on 3/5/2018.
 *
 */

class FavDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favouritesDb.db";

    private static final int DATABASE_VERSION = 1;

    FavDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * easier to add movie class as json object and same for trailers and reviews
     * instead of having a lot of columns which may mess things up.
     *
     * also last 3 columns (of 4) are not made as NOT NULL that's in case if there was
     * no trailers found and/or reviews so it will be null so by that we prevent crash of app due to
     * Null constraint of sql Database (just wanted to clarify).
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE "  + FavEntry.TABLE_NAME + " (" +
                FavEntry.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                FavEntry.COLUMN_MOVIE_JSON_OBJECT + " TEXT, " +
                FavEntry.COLUMN_TRAILER_JSON_OBJECT + " TEXT, " +
                FavEntry.COLUMN_REVIEW_JSON_OBJECT + " TEXT);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavEntry.TABLE_NAME);
        onCreate(db);
    }
}
