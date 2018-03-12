package com.example.android.popularmovies;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.android.popularmovies.adapters.PosterPathRecyclerViewAdapter;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.model.Review;
import com.example.android.popularmovies.model.Trailer;
import com.example.android.popularmovies.utils.JsonUtils;
import com.example.android.popularmovies.utils.NetworkUtils;
import com.example.android.popularmovies.utils.PreferenceUtils;

import java.util.ArrayList;

import com.example.android.popularmovies.data.FavContract.FavEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<Movie>> ,
        PosterPathRecyclerViewAdapter.PosterPathItemListener {

    private static final int POSTER_PATH_AND_JSON_RESPONSE_LOADER_ID = 23;

    private RecyclerView recyclerView;
    private PosterPathRecyclerViewAdapter adapter;

    private ProgressBar loadingProgressBar;

    private ArrayList<Movie> movies = null;

    private static final String SAVED_INSTANCE_STATE_KEY_MOVIES = "SAVED_INSTANCE_STATE_KEY_MOVIES";

    private ConstraintLayout noInternetConnectionLayout;
    private AppCompatTextView refreshInternetConnection;

    private static final int HALF_SECOND_IN_MILLIS = 500;

    private String prefPath;
    private int recyclerViewGridSpanCountPortrait;
    private int recyclerViewGridSpanCountLandscape;

    private int recyclerViewGridSpanCountCurrentOrientation;

    private static final int FAVOURITE_MOVIES_LOADER_ID = 863;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        noInternetConnectionLayout = findViewById(R.id.no_internet_connection_constraint_layout);
        refreshInternetConnection = findViewById(R.id.refresh_internet_connection);

        initializePreferences();
        setupRecyclerViewGridSpanCountAccToOrientation();

        if (savedInstanceState != null){
            movies = savedInstanceState.getParcelableArrayList(SAVED_INSTANCE_STATE_KEY_MOVIES);

            viewsSetups();

            return;
        }

        viewsSetups();

        // NOTE We always check if there is internet connectivity before any call to API
        // its done in onCreateLoader()
        getLoaderManager().initLoader(getLoaderIdAccToPrefPath(), null, this);
    }

    private void initializePreferences(){
        prefPath = PreferenceUtils.getSortMoviesBy(this);
        recyclerViewGridSpanCountPortrait = PreferenceUtils.getColumnsInPortrait(this);
        recyclerViewGridSpanCountLandscape = PreferenceUtils.getColumnsInLandscape(this);
    }

    /**
     * getResources().getConfiguration().orientation; was searched from stack-overflow link
     * while the rest of possibilities in screen-orientations was searched in android developers
     * link ->
     * https://developer.android.com/reference/android/content/pm/ActivityInfo.html#screenOrientation
     */
    private void setupRecyclerViewGridSpanCountAccToOrientation(){
        int currentScreenOrientation = getResources().getConfiguration().orientation;
        ArrayList<Integer> allPossiblePortraitScreenOrientations = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            allPossiblePortraitScreenOrientations.add(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        }
        allPossiblePortraitScreenOrientations.add(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        allPossiblePortraitScreenOrientations.add(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        allPossiblePortraitScreenOrientations.add(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        // for better UI in portrait- width is smaller than in landscape-orientation for sure
        if (allPossiblePortraitScreenOrientations.contains(currentScreenOrientation)){
            recyclerViewGridSpanCountCurrentOrientation = recyclerViewGridSpanCountPortrait;
        }else {
            // I think 4 is enough, as 5 will lead to a crowded-screen
            recyclerViewGridSpanCountCurrentOrientation = recyclerViewGridSpanCountLandscape;
        }
    }

    private void viewsSetups(){
        // --- recycler view setups
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(
                this, recyclerViewGridSpanCountCurrentOrientation);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new PosterPathRecyclerViewAdapter(
                this, this, movies);
        recyclerView.setAdapter(adapter);

        // --- no internet connection setups
        refreshInternetConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noInternetConnectionLayout.setVisibility(View.INVISIBLE);

                // No need to use method to get loader ID as this view is only visible
                // if we are in any movies category except for favourite movies
                getLoaderManager().restartLoader(POSTER_PATH_AND_JSON_RESPONSE_LOADER_ID, null,
                        MainActivity.this);
            }
        });
    }

    /**
     * Check if there is a change in prefs from {@link SettingsActivity}
     * And make corresponding changes to prefs
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (prefPath != null){
            if ( ! (recyclerViewGridSpanCountPortrait == PreferenceUtils.getColumnsInPortrait(this)
                    && recyclerViewGridSpanCountLandscape == PreferenceUtils.getColumnsInLandscape(this))){
                recyclerViewGridSpanCountPortrait = PreferenceUtils.getColumnsInPortrait(this);
                recyclerViewGridSpanCountLandscape = PreferenceUtils.getColumnsInLandscape(this);

                setupRecyclerViewGridSpanCountAccToOrientation();

                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(
                        this, recyclerViewGridSpanCountCurrentOrientation);
                recyclerView.setLayoutManager(layoutManager);
            }

            // only restartLoader(get new data from API) if sortBy changes
            if (! prefPath.equals(PreferenceUtils.getSortMoviesBy(this))){
                prefPath = PreferenceUtils.getSortMoviesBy(this);

                restartLoaderAndDestroyOthersIfExists();
            }

            String mainActivityTitle;
            switch (prefPath){
                case NetworkUtils.PATH_POPULAR:
                    mainActivityTitle = getString(R.string.popular_movies_title);
                    break;
                case NetworkUtils.PATH_TOP_RATED:
                    mainActivityTitle = getString(R.string.top_rated_movies_title);
                    break;
                case NetworkUtils.PATH_FAVOURITE:
                    mainActivityTitle = getString(R.string.favourite_movies_title);
                    break;
                default:
                    mainActivityTitle = getString(R.string.unknown_movies_category_title);
                    break;
            }
            setTitle(mainActivityTitle);
        }
    }

    /** {@link #onResume()} */

    private void restartLoaderAndDestroyOthersIfExists(){
        // made in case when you start the app and popular loading from API then you go to setting
        // and get back but as favourite then loader of popular gets result this will make
        // favourite look like popular so we avoid that by using this method
        int toBeRestartedLoader = getLoaderIdAccToPrefPath();
        int toBeDestroyedLoader;
        if (toBeRestartedLoader == POSTER_PATH_AND_JSON_RESPONSE_LOADER_ID){
            toBeDestroyedLoader = FAVOURITE_MOVIES_LOADER_ID;
        }else {
            toBeDestroyedLoader = POSTER_PATH_AND_JSON_RESPONSE_LOADER_ID;
        }

        getLoaderManager().destroyLoader(toBeDestroyedLoader);
        getLoaderManager().restartLoader(getLoaderIdAccToPrefPath(), null,
                this);
    }

    /** @see LoaderManager.LoaderCallbacks<String> Implemented Methods */

    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {
        findViewById(R.id.emptyViewTextView).setVisibility(View.INVISIBLE);

        showLoadingProcessAndHideResults();

        boolean isCurrentlyOnline = NetworkUtils.isCurrentlyOnline(this);

        if (id == POSTER_PATH_AND_JSON_RESPONSE_LOADER_ID){
            return new FetchJsonResponseIntoMovieListAsyncTaskLoader(this,
                    prefPath, isCurrentlyOnline);
        }else if (id == FAVOURITE_MOVIES_LOADER_ID){
            return new FetchMoviesFromFavDBAsyncTaskLoader(this);
        }else {
            Log.e(MainActivity.class.getName(), "Unknown Loader ID = " + String.valueOf(id));

            return new FetchJsonResponseIntoMovieListAsyncTaskLoader(this,
                    null, isCurrentlyOnline);
        }
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> movies) {
        if ((movies == null || movies.size() == 0)
                && ! NetworkUtils.isCurrentlyOnline(this)
                && loader.getId() != FAVOURITE_MOVIES_LOADER_ID) {
            // giving user ability to refresh if there was no internet connection instead of
            // closing and re-opening the app
            noInternetConnectionLayout.setVisibility(View.VISIBLE);

            Log.i(MainActivity.class.getName(), "User is offline so," +
                    "\nLoader will not start background fetching data from API");
        }

        this.movies = movies;

        hideLoadingProcessAndShowResults();

        adapter.swapMovies(movies);

        if (this.movies == null || this.movies.size() == 0){
            findViewById(R.id.emptyViewTextView).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.emptyViewTextView).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {
        this.movies = null;

        adapter.swapMovies(null);
    }

    // ---- Methods Used in above Implemented Methods

    private void showLoadingProcessAndHideResults(){
        loadingProgressBar.setVisibility(View.VISIBLE);

        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideLoadingProcessAndShowResults(){
        loadingProgressBar.setVisibility(View.INVISIBLE);

        recyclerView.setVisibility(View.VISIBLE);
    }

    // ---- Async Task Loaders

    private static class FetchJsonResponseIntoMovieListAsyncTaskLoader
            extends AsyncTaskLoader<ArrayList<Movie>> {

        private ArrayList<Movie> movies = null;

        private final String path;

        private final boolean isCurrentlyOnline;

        FetchJsonResponseIntoMovieListAsyncTaskLoader(Context context, String path,
                                                      boolean isCurrentlyOnline) {
            super(context);

            this.path = path;

            this.isCurrentlyOnline = isCurrentlyOnline;
        }

        @Override
        protected void onStartLoading() {
            if (movies != null) {
                deliverResult(movies);
            } else {
                forceLoad();
            }
        }

        @Override
        public ArrayList<Movie> loadInBackground() {
            if (! isCurrentlyOnline){
                // just to show user trying to refresh is occurred since if no sleep occurs
                // user will not see loadingProgressBar or the invisibility & visibility
                // change that occurs to noInternetConnectionLayout
                // so, User might think that click doesn't work & there is programming/app issue
                // PLUS
                // the delay 300 maybe internet was just connecting so, when getting data
                // internet is available ( I know it is very rare, but might happen )

                try {
                    Thread.sleep(HALF_SECOND_IN_MILLIS);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            try {
                String response =  NetworkUtils.getJsonResponseFromPath(path);

                return JsonUtils.getMoviesArrayList(response);
            }catch (Exception e){
                // Although every possible Exception is handled in NetworkUtils,
                // We use this try-catch in case of any non-predicted error that would occur.
                e.printStackTrace();

                return null;
            }
        }

        @Override
        public void deliverResult(ArrayList<Movie> movies) {
            this.movies = movies;
            super.deliverResult(movies);
        }
    }

    private static class FetchMoviesFromFavDBAsyncTaskLoader
            extends AsyncTaskLoader<ArrayList<Movie>> {

        private ArrayList<Movie> movies = null;

        FetchMoviesFromFavDBAsyncTaskLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            if (movies != null) {
                deliverResult(movies);
            } else {
                forceLoad();
            }
        }

        @Override
        public ArrayList<Movie> loadInBackground() {
            try {
                String[] projection = new String[]{ FavEntry.COLUMN_MOVIE_JSON_OBJECT };

                Cursor cursor = getContext().getContentResolver().query(FavEntry.CONTENT_URI,
                        projection, null, null, null);

                ArrayList<Movie> movies = new ArrayList<>();
                if (cursor != null){
                    String movieJsonObjectString;
                    Movie movie;
                    if (cursor.moveToFirst()){
                        do {
                            movieJsonObjectString = cursor.getString(
                                    cursor.getColumnIndex(FavEntry.COLUMN_MOVIE_JSON_OBJECT));

                            movie = JsonUtils.transformJsonObjectStringToMovie(movieJsonObjectString);

                            movies.add(movie);
                        }while (cursor.moveToNext());
                    }

                    cursor.close();
                }

                return movies;
            }catch (Exception e){
                // Although every possible Exception is handled in NetworkUtils,
                // We use this try-catch in case of any non-predicted error that would occur.
                e.printStackTrace();

                return null;
            }
        }

        @Override
        public void deliverResult(ArrayList<Movie> movies) {
            this.movies = movies;

            super.deliverResult(movies);
        }

    }

    /** @see PosterPathRecyclerViewAdapter.PosterPathItemListener */

    @Override
    public void onClick(int movieIndex) {
        if (movies == null || movies.size() == 0){
            Log.e(MainActivity.class.getName(), "Empty or null movies");

            return;
        }else if (movies.size() <= movieIndex) {
            // Note ==> this if condition to avoid IndexOutOfBoundsException
            // can be by try-catch block but i prefer if-cond
            Log.e(MainActivity.class.getName(), "indexOutOfBoundsException should occur\n" +
                    "size: " + movies.size() + " index: " + movieIndex);

            return;
        }

        Movie movie = movies.get(movieIndex);

        Intent movieDetailsIntent = new Intent(this, MovieDetailsActivity.class);
        movieDetailsIntent.putExtra(MovieDetailsActivity.INTENT_KEY_MOVIE_OBJECT, movie);
        movieDetailsIntent.putExtra(MovieDetailsActivity.INTENT_KEY_DO_NOT_REFRESH_MOVIES, true);
        movieDetailsIntent.putExtra(MovieDetailsActivity.INTENT_KEY_CAME_FROM_FAVOURITES,
                prefPath.equals(NetworkUtils.PATH_FAVOURITE));

        if (prefPath.equals(NetworkUtils.PATH_FAVOURITE)){
            // pass in trailers & reviews as well
            String[] projection = new String[]{
                    FavEntry.COLUMN_TRAILER_JSON_OBJECT, FavEntry.COLUMN_REVIEW_JSON_OBJECT};

            Uri movieUri = FavEntry.CONTENT_URI.buildUpon()
                    .appendPath( String.valueOf(movie.getId()) )
                    .build();

            Cursor cursor = getContentResolver().query(movieUri, projection,
                    null, null, null);

            ArrayList<Trailer> trailers = new ArrayList<>();
            ArrayList<Review> reviews = new ArrayList<>();
            String jsonArrayStringTrailers;
            String jsonArrayStringReviews;
            if (cursor != null){
                if (cursor.moveToFirst()){
                    jsonArrayStringTrailers = cursor.getString(cursor.getColumnIndex(
                            FavEntry.COLUMN_TRAILER_JSON_OBJECT));

                    trailers = JsonUtils.transformJsonArrayStringToTrailers(jsonArrayStringTrailers);

                    jsonArrayStringReviews = cursor.getString(cursor.getColumnIndex(
                            FavEntry.COLUMN_REVIEW_JSON_OBJECT));

                    reviews = JsonUtils.transformJsonArrayStringToReviews(jsonArrayStringReviews);
                }

                cursor.close();
            }

            movieDetailsIntent.putParcelableArrayListExtra(
                    MovieDetailsActivity.INTENT_KEY_TRAILERS_ARRAY_LIST, trailers);
            movieDetailsIntent.putParcelableArrayListExtra(
                    MovieDetailsActivity.INTENT_KEY_REVIEWS_ARRAY_LIST, reviews);
        }

        startActivity(movieDetailsIntent);
    }

    // --- Saving Temp Values ex. case of screen Rotation

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVED_INSTANCE_STATE_KEY_MOVIES, movies);

        super.onSaveInstanceState(outState);
    }

    // ---- Menu Handling

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings_action:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ---- Global Helper Methods

    private int getLoaderIdAccToPrefPath(){
        switch (prefPath){
            case NetworkUtils.PATH_TOP_RATED:
            case NetworkUtils.PATH_POPULAR:
                return POSTER_PATH_AND_JSON_RESPONSE_LOADER_ID;
            case NetworkUtils.PATH_FAVOURITE:
                return FAVOURITE_MOVIES_LOADER_ID;
            default:
                return -713;
        }
    }
}