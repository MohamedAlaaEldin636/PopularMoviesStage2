package com.example.android.popularmovies.asyncTaskLoaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.example.android.popularmovies.utils.JsonUtils;
import com.example.android.popularmovies.utils.NetworkUtils;

import java.util.ArrayList;

/**
 * Created by Mohamed on 3/3/2018.
 *
 * Order of arrayLists are 1- Trailers 2- Reviews
 * Must be retrieved in same order.
 */

public class FetchJsonResponseIntoArrayList extends AsyncTaskLoader<ArrayList<ArrayList>> {

    private ArrayList<ArrayList> data;

    /** Any negative int */
    private static final int ERROR_ID = -693;

    private int movieId = ERROR_ID;

    private final boolean isCurrentlyOnline;

    private static final int HALF_SECOND_IN_MILLIS = 500;

    public FetchJsonResponseIntoArrayList(Context context, int movieId, boolean isCurrentlyOnline) {
        super(context);

        this.movieId = movieId;

        this.isCurrentlyOnline = isCurrentlyOnline;
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<ArrayList> loadInBackground() {
        if (movieId == ERROR_ID){
            Log.e(FetchJsonResponseIntoArrayList.class.getName(), "Invalid movie id = "
                    + String.valueOf(ERROR_ID));

            return null;
        }

        if (! isCurrentlyOnline){
            try {
                Thread.sleep(HALF_SECOND_IN_MILLIS);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        ArrayList<ArrayList> data = new ArrayList<>();
        try {
            String trailersResponse = NetworkUtils.getJsonResponseFromIdPlusPath(movieId,
                    NetworkUtils.PATH_VIDEOS);
            String reviewsResponse = NetworkUtils.getJsonResponseFromIdPlusPath(movieId,
                    NetworkUtils.PATH_REVIEWS);

            data.add(JsonUtils.getTrailersArrayList(trailersResponse));
            data.add(JsonUtils.getReviewsArrayList(reviewsResponse));

            return data;
        }catch (Exception e){
            // Although every possible Exception is handled in NetworkUtils,
            // We use this try-catch in case of any non-predicted error that would occur.
            e.printStackTrace();

            data.add(null);
            data.add(null);

            return data;
        }
    }

    @Override
    public void deliverResult(ArrayList<ArrayList> data) {
        this.data = data;

        super.deliverResult(data);
    }
}
