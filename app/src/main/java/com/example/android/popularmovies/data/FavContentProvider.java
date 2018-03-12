package com.example.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.android.popularmovies.data.FavContract.FavEntry;

/**
 * Created by Mohamed on 3/5/2018.
 *
 */

public class FavContentProvider extends ContentProvider {

    private static final int FAVOURITE = 100;
    private static final int FAVOURITE_MOVIE_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FavContract.AUTHORITY, FavContract.PATH_FAVOURITE, FAVOURITE);
        uriMatcher.addURI(FavContract.AUTHORITY, FavContract.PATH_FAVOURITE + "/#", FAVOURITE_MOVIE_ID);

        return uriMatcher;
    }

    private FavDbHelper favDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();

        favDbHelper = new FavDbHelper(context);

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = favDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case FAVOURITE:
                retCursor =  db.query(FavEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVOURITE_MOVIE_ID:
                selection = FavEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                retCursor = db.query(FavEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null)
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = favDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FAVOURITE:
                long id = db.insert(FavEntry.TABLE_NAME, null, values);
                if (id > 0){
                    returnUri = ContentUris.withAppendedId(FavEntry.CONTENT_URI, id);
                }else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = favDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case FAVOURITE:
                rowsDeleted = db.delete(FavEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FAVOURITE_MOVIE_ID:
                selection = FavEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                rowsDeleted = db.delete(FavEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0 && getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (values.size() == 0)
            return 0;

        final SQLiteDatabase db = favDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match){
            case FAVOURITE:
                rowsUpdated = db.update(FavEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case FAVOURITE_MOVIE_ID:
                selection = FavEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                rowsUpdated = db.update(FavEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

        if (rowsUpdated != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    /**
     * No need to implement below method as this provider isn't exported (needed by other app)
     * Plus this app itself doesn't need the MIME Type so I guess, It's ok not to implement it.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("No need to be implemented");
    }
}
