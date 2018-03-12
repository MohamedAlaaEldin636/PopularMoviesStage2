package com.example.android.popularmovies.intentServices;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.popularmovies.model.Review;
import com.example.android.popularmovies.model.Trailer;
import com.example.android.popularmovies.utils.JsonUtils;
import com.example.android.popularmovies.utils.NetworkUtils;

import java.util.ArrayList;

import com.example.android.popularmovies.data.FavContract.FavEntry;

/**
 * Created by Mohamed on 3/9/2018.
 *
 * Usage
 * @see com.example.android.popularmovies.MovieDetailsActivity
 * in case if user adds movie to favorites then exits the activity before trailers and reviews
 * get's result from loader so now that movie's trailers and reviews will be empty in favourites
 * so this service updates what was not completed
 *
 * Note if app is closed from recents apps or killed while this service has not completed it's work
 * then trailers and reviews will still be empty
 * Possible Soultion
 * I thought (and tested) to start broadcast in this service's onTaskRemoved() in case if work has
 * not been finished then that broadcast will start service to fill trailers and reviews
 * But
 * I though it is not necessary so much it's enough that this service is made
 * also if there are any updates for a movie in favourites the refresh menu item will appear
 * so that user can update info if he wants to do that
 */
public class UpdateMovieInFavouritesIntentService extends IntentService {

    public static final String INTENT_KEY_MOVIE_ID = "INTENT_KEY_MOVIE_ID";

    private static final int ERROR_ID = -4446;

    private static final String LOG_TAG = UpdateMovieInFavouritesIntentService.class.getName();

    public UpdateMovieInFavouritesIntentService() {
        super("UpdateMovieInFavouritesIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && intent.hasExtra(INTENT_KEY_MOVIE_ID)){
            int movieId = intent.getIntExtra(INTENT_KEY_MOVIE_ID, ERROR_ID);
            if (movieId == ERROR_ID){
                Log.e(LOG_TAG, "Error movie_id");

                return;
            }

            ArrayList<Trailer> trailers;
            ArrayList<Review> reviews;
            try {
                String trailersResponse = NetworkUtils.getJsonResponseFromIdPlusPath(movieId,
                        NetworkUtils.PATH_VIDEOS);
                String reviewsResponse = NetworkUtils.getJsonResponseFromIdPlusPath(movieId,
                        NetworkUtils.PATH_REVIEWS);

                trailers = JsonUtils.getTrailersArrayList(trailersResponse);
                reviews = JsonUtils.getReviewsArrayList(reviewsResponse);
            }catch (Exception e){
                // Although every possible Exception is handled in NetworkUtils,
                // We use this try-catch in case of any non-predicted error that would occur.
                e.printStackTrace();

                return;
            }

            // --- Update Movie in favourites with trailers & reviews
            ContentValues contentValues = new ContentValues();
            contentValues.put(FavEntry.COLUMN_TRAILER_JSON_OBJECT, JsonUtils.transformTrailersToJsonArrayString(trailers));
            contentValues.put(FavEntry.COLUMN_REVIEW_JSON_OBJECT, JsonUtils.transformReviewsToJsonArrayString(reviews));

            Uri uriToBeUpdated = FavEntry.CONTENT_URI.buildUpon()
                    .appendPath( String.valueOf(movieId) )
                    .build();
            int rowsUpdated = getContentResolver().update(uriToBeUpdated, contentValues, null, null);

            // Just for me to ensure that the movie has been updated or not
            Log.i(LOG_TAG, "Rows Updated = " + String.valueOf(rowsUpdated));
        }else {
            Log.e(LOG_TAG, "Null or has no intent_key error");
        }
    }
}
