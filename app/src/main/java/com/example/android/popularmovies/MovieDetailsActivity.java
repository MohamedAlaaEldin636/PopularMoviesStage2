package com.example.android.popularmovies;

import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.popularmovies.adapters.ReviewRecyclerViewAdapter;
import com.example.android.popularmovies.adapters.TrailerRecyclerViewAdapter;
import com.example.android.popularmovies.asyncTaskLoaders.FetchJsonResponseIntoArrayList;
import com.example.android.popularmovies.databinding.ActivityMovieDetailsBinding;
import com.example.android.popularmovies.intentServices.UpdateMovieInFavouritesIntentService;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.model.Review;
import com.example.android.popularmovies.model.Trailer;
import com.example.android.popularmovies.utils.JsonUtils;
import com.example.android.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;

import com.example.android.popularmovies.data.FavContract.FavEntry;

public class MovieDetailsActivity extends AppCompatActivity
        implements TrailerRecyclerViewAdapter.TrailerItemListener ,
        LoaderManager.LoaderCallbacks<ArrayList<ArrayList>>{

    public static final String INTENT_KEY_MOVIE_OBJECT = "INTENT_KEY_MOVIE_OBJECT";
    public static final String INTENT_KEY_TRAILERS_ARRAY_LIST = "INTENT_KEY_TRAILERS_ARRAY_LIST";
    public static final String INTENT_KEY_REVIEWS_ARRAY_LIST = "INTENT_KEY_REVIEWS_ARRAY_LIST";
    public static final String INTENT_KEY_CAME_FROM_FAVOURITES = "INTENT_KEY_CAME_FROM_FAVOURITES";

    private boolean cameFromFavourites = false;

    private ActivityMovieDetailsBinding binding;

    /** When get back to {@link MainActivity}, Loader won't make request for movies if true */
    public static final String INTENT_KEY_DO_NOT_REFRESH_MOVIES = "INTENT_KEY_DO_NOT_REFRESH_MOVIES";

    private boolean doNotRefreshMovies = false;

    private TrailerRecyclerViewAdapter trailersAdapter;

    private ArrayList<Trailer> trailers = null;

    private static final String YOUTUBE_SITE = "YouTube";

    private ReviewRecyclerViewAdapter reviewsAdapter;

    private ArrayList<Review> reviews = null;

    private Movie movie;

    private static final int LOADER_ID_FETCH_DATA_FROM_API = 4508;
    private static final int LOADER_ID_CHECK_FOR_UPDATES = 374;

    private boolean pendingUpdateMovieInFavourites = false;

    private MenuItem menuItemUpdate;
    private MenuItem menuItemShare;

    private ArrayList<Trailer> updatedTrailers;
    private ArrayList<Review> updatedReviews;

    // --- On Save Instance State keys ( SAVED_INSTANCE_STATE_KEY == S_I_S_K )

    private static final String S_I_S_K_MOVIE = "S_I_S_K_MOVIE";
    private static final String S_I_S_K_DO_NOT_REFRESH_MOVIES = "S_I_S_K_DO_NOT_REFRESH_MOVIES";
    private static final String S_I_S_K_TRAILERS = "S_I_S_K_TRAILERS";
    private static final String S_I_S_K_REVIEWS = "S_I_S_K_REVIEWS";
    private static final String S_I_S_K_PENDING_UPDATE_MOVIE_IN_FAVOURITES = "S_I_S_K_PENDING_UPDATE_MOVIE_IN_FAVOURITES";
    private static final String S_I_S_K_TRAILERS_EXPANDED = "S_I_S_K_TRAILERS_EXPANDED";
    private static final String S_I_S_K_REVIEWS_EXPANDED = "S_I_S_K_REVIEWS_EXPANDED";
    private static final String S_I_S_K_MENU_ITEM_UPDATE_VISIBLE = "S_I_S_K_MENU_ITEM_UPDATE_VISIBLE";
    private static final String S_I_S_K_MENU_ITEM_SHARE_VISIBLE = "S_I_S_K_MENU_ITEM_SHARE_VISIBLE";
    private static final String S_I_S_K_UPDATED_TRAILERS = "S_I_S_K_UPDATED_TRAILERS";
    private static final String S_I_S_K_UPDATED_REVIEWS = "S_I_S_K_UPDATED_REVIEWS";
    private static final String S_I_S_K_TRAILERS_REVIEWS_STATE = "S_I_S_K_TRAILERS_REVIEWS_STATE";
    private static final String S_I_S_K_NESTED_SCROLL_VIEW_Y = "S_I_S_K_NESTED_SCROLL_VIEW_y";
    private static final String S_I_S_K_CAME_FROM_FAVOURITES = "S_I_S_K_CAME_FROM_FAVOURITES";
    private static final String S_I_S_K_DIALOG_TRAILERS_CHOOSER_VISIBLE = "S_I_S_K_DIALOG_TRAILERS_CHOOSER_VISIBLE";

    private Runnable menuItemUpdateRunnable = null;
    private Runnable menuItemShareRunnable = null;

    private Toast toast;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);

        // now you can handle up click in onOptionItemSelected(MenuItem) android.R.id.home
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // In case of device rotation
        if (savedInstanceState != null){
            updateUIAccToSavedInstanceState(savedInstanceState);

            return;
        }

        Intent intent = getIntent();
        if (intent != null
                && intent.hasExtra(INTENT_KEY_MOVIE_OBJECT)){
            movie = intent.getParcelableExtra(INTENT_KEY_MOVIE_OBJECT);

            if (intent.hasExtra(INTENT_KEY_DO_NOT_REFRESH_MOVIES)){
                doNotRefreshMovies = intent.getBooleanExtra(
                        INTENT_KEY_DO_NOT_REFRESH_MOVIES, false);
            }

            if (intent.hasExtra(INTENT_KEY_CAME_FROM_FAVOURITES)){
                cameFromFavourites = intent.getBooleanExtra(INTENT_KEY_CAME_FROM_FAVOURITES,
                        false);
            }

            populateUi();

            setupViews();

            if (intent.hasExtra(INTENT_KEY_TRAILERS_ARRAY_LIST)
                    && intent.hasExtra(INTENT_KEY_REVIEWS_ARRAY_LIST)){
                trailers = intent
                        .getParcelableArrayListExtra(INTENT_KEY_TRAILERS_ARRAY_LIST);
                reviews = intent
                        .getParcelableArrayListExtra(INTENT_KEY_REVIEWS_ARRAY_LIST);

                trailersAdapter.swapCursor(trailers);
                reviewsAdapter.swapCursor(reviews);
                // --- check if any of recyclerViews are empty
                boolean emptyTrailers = trailers == null || trailers.size() == 0;
                boolean emptyReviews = reviews == null || reviews.size() == 0;
                if (emptyTrailers || emptyReviews){
                    if (emptyTrailers){
                        showTrailersEmptyView();
                    }
                    if (emptyReviews){
                        showReviewsEmptyView();
                    }
                }

                getLoaderManager().initLoader(LOADER_ID_CHECK_FOR_UPDATES, null, this);
            }else {
                getLoaderManager().initLoader(LOADER_ID_FETCH_DATA_FROM_API, null, this);
            }
        }else {
            String detailedError;
            if (intent == null)
                detailedError = "null Intent";
            else {
                detailedError = "intent doesn't have INTENT_KEY_MOVIE_OBJECT";
            }

            Log.e(MovieDetailsActivity.class.getName(), detailedError);
            // inform user error has occurred that's why no details showed
            showToast("Error Occurred Showing Movie Details");

            finish();
        }
    }

    /** {@link #onCreate(Bundle)} */

    private void updateUIAccToSavedInstanceState(Bundle savedInstanceState){
        movie = savedInstanceState.getParcelable(S_I_S_K_MOVIE);

        doNotRefreshMovies = savedInstanceState.getBoolean(S_I_S_K_DO_NOT_REFRESH_MOVIES);

        // title / posterPath / overview / rating / releaseDate
        populateUi();

        trailers = savedInstanceState.getParcelableArrayList(S_I_S_K_TRAILERS);
        reviews = savedInstanceState.getParcelableArrayList(S_I_S_K_REVIEWS);

        // both recyclerViews (need trailers && reviews) / collapse onClicks / internetRefresh
        // Buttons onClicks / handle Being in Fav / Fav Button onClick
        setupViews();

        pendingUpdateMovieInFavourites = savedInstanceState.getBoolean(S_I_S_K_PENDING_UPDATE_MOVIE_IN_FAVOURITES);

        boolean trailersExpanded = savedInstanceState.getBoolean(S_I_S_K_TRAILERS_EXPANDED);
        if (trailersExpanded){
            binding.includedTrailersSection.forVisibilityConstraintLayout.setVisibility(View.VISIBLE);

            binding.includedTrailersSection.collapseTrailersImageView.setImageResource(R.drawable.ic_indeterminate_check_box_black_24dp);
        }else {
            binding.includedTrailersSection.forVisibilityConstraintLayout.setVisibility(View.GONE);

            binding.includedTrailersSection.collapseTrailersImageView.setImageResource(R.drawable.ic_add_box_black_24dp);
        }

        boolean reviewsExpanded = savedInstanceState.getBoolean(S_I_S_K_REVIEWS_EXPANDED);
        if (reviewsExpanded){
            binding.includedReviewsSection.forVisibilityConstraintLayout.setVisibility(View.VISIBLE);

            binding.includedReviewsSection.collapseReviewsImageView.setImageResource(R.drawable.ic_indeterminate_check_box_black_24dp);
        }else {
            binding.includedReviewsSection.forVisibilityConstraintLayout.setVisibility(View.GONE);

            binding.includedReviewsSection.collapseReviewsImageView.setImageResource(R.drawable.ic_add_box_black_24dp);
        }

        final boolean menuItemUpdateIsVisible = savedInstanceState.getBoolean(S_I_S_K_MENU_ITEM_UPDATE_VISIBLE);
        final boolean menuItemShareIsVisible = savedInstanceState.getBoolean(S_I_S_K_MENU_ITEM_SHARE_VISIBLE);
        if (menuItemUpdate == null || menuItemShare == null){
            invalidateOptionsMenu();

            menuItemUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    menuItemUpdate.setVisible(menuItemUpdateIsVisible);
                }
            };

            menuItemShareRunnable = new Runnable() {
                @Override
                public void run() {
                    menuItemShare.setVisible(menuItemShareIsVisible);
                }
            };
        }else {
            menuItemUpdate.setVisible(menuItemUpdateIsVisible);

            menuItemShare.setVisible(menuItemShareIsVisible);
        }

        updatedTrailers = savedInstanceState.getParcelableArrayList(S_I_S_K_UPDATED_TRAILERS);
        updatedReviews = savedInstanceState.getParcelableArrayList(S_I_S_K_UPDATED_REVIEWS);

        TrailersReviewsState state = (TrailersReviewsState) savedInstanceState.getSerializable(S_I_S_K_TRAILERS_REVIEWS_STATE);
        if (state != null){
            showOnly(state);
        }

        // Trying to maintain same place scrolled in previous instanceState ex. different screen orientation
        int scrollY = savedInstanceState.getInt(S_I_S_K_NESTED_SCROLL_VIEW_Y);
        binding.nestedScrollView.scrollTo(0, scrollY);

        cameFromFavourites = savedInstanceState.getBoolean(S_I_S_K_CAME_FROM_FAVOURITES);

        boolean dialogIsVisible = savedInstanceState.getBoolean(S_I_S_K_DIALOG_TRAILERS_CHOOSER_VISIBLE);
        if (dialogIsVisible){
            showTrailersShareChooserDialog();
        }
    }

    private void populateUi(){
        String originalTitle = movie.getOriginalTitle();
        if (originalTitle == null || originalTitle.isEmpty())
            originalTitle = "Unknown Title";
        setTitle(originalTitle);

        String fullPosterPath = NetworkUtils.fullPosterPathFromThirdPartPath(movie.getPosterPath());
        Picasso.with(this)
                .load(fullPosterPath)
                .error(getResources().getDrawable(R.drawable.error_poster_image_load))
                .into(binding.posterImageView);

        binding.releaseDate.setText(polishReleaseDate(movie.getReleaseDate()));

        setupRating(movie.getVoteAverage());

        String overview = movie.getOverview();
        if (overview != null)
            binding.overview.setText(overview);
    }

    private void setupViews(){
        // --- Trailers recyclerView setups
        RecyclerView.LayoutManager trailersLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        binding.includedTrailersSection.trailersRecyclerView.setLayoutManager(trailersLayoutManager);
        binding.includedTrailersSection.trailersRecyclerView.setHasFixedSize(true);
        trailersAdapter = new TrailerRecyclerViewAdapter(this, trailers, this);
        binding.includedTrailersSection.trailersRecyclerView.setAdapter(trailersAdapter);
        /* A combination of NestedScrollView & recyclerView.setNestedScrollingEnabled(false)
           Makes scrolling way smoother and better. */
        binding.includedTrailersSection.trailersRecyclerView.setNestedScrollingEnabled(false);

        // --- Reviews recyclerView setups
        RecyclerView.LayoutManager reviewsLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        binding.includedReviewsSection.reviewsRecyclerView.setLayoutManager(reviewsLayoutManager);
        binding.includedReviewsSection.reviewsRecyclerView.setHasFixedSize(true);
        reviewsAdapter = new ReviewRecyclerViewAdapter(reviews);
        binding.includedReviewsSection.reviewsRecyclerView.setAdapter(reviewsAdapter);
        /* A combination of NestedScrollView & recyclerView.setNestedScrollingEnabled(false)
           Makes scrolling way smoother and better. */
        binding.includedReviewsSection.reviewsRecyclerView.setNestedScrollingEnabled(false);

        // --- Collapsing Image View onClick
        binding.includedTrailersSection.collapseTrailersImageView.setOnClickListener(
                toggleCollapseAndExpand(binding.includedTrailersSection.collapseTrailersImageView,
                        binding.includedTrailersSection.forVisibilityConstraintLayout));

        binding.includedReviewsSection.collapseReviewsImageView.setOnClickListener(
                toggleCollapseAndExpand(binding.includedReviewsSection.collapseReviewsImageView,
                        binding.includedReviewsSection.forVisibilityConstraintLayout));

        // --- Handle Refresh Internet Buttons
        binding.includedTrailersSection.refreshInternetConnection.setOnClickListener(
                refreshInternet());

        binding.includedReviewsSection.refreshInternetConnection.setOnClickListener(
                refreshInternet());

        // --- Check being in favourites
        checkMovieInFavourites();

        // --- Favourite button
        binding.favouriteLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMovieInFavourites();
            }
        });
    }

    /** {@link #updateUIAccToSavedInstanceState(Bundle)} Methods */

    private void showOnly(@NonNull TrailersReviewsState state){
        // --- Hide Progress Bar
        binding.includedTrailersSection.trailersLoadingProgressBar.setVisibility(View.GONE);
        binding.includedReviewsSection.reviewsLoadingProgressBar.setVisibility(View.GONE);
        // --- Hide No Internet
        binding.includedTrailersSection.noInternetConnectionConstraintLayout.setVisibility(View.GONE);
        binding.includedReviewsSection.noInternetConnectionConstraintLayout.setVisibility(View.GONE);
        // --- Hide RecyclerView
        binding.includedTrailersSection.trailersRecyclerView.setVisibility(View.GONE);
        binding.includedReviewsSection.reviewsRecyclerView.setVisibility(View.GONE);
        // --- Hide Empty View
        binding.includedTrailersSection.trailersEmptyView.setVisibility(View.GONE);
        binding.includedReviewsSection.reviewsEmptyView.setVisibility(View.GONE);

        switch (state){
            case RECYCLER_VIEWS_PROGRESS_BAR:
                binding.includedTrailersSection.trailersLoadingProgressBar.setVisibility(View.VISIBLE);
                binding.includedReviewsSection.reviewsLoadingProgressBar.setVisibility(View.VISIBLE);
                break;
            case RECYCLER_VIEWS_NO_INTERNET:
                binding.includedTrailersSection.noInternetConnectionConstraintLayout.setVisibility(View.VISIBLE);
                binding.includedReviewsSection.noInternetConnectionConstraintLayout.setVisibility(View.VISIBLE);
                break;
            case TRAILERS_AND_REVIEWS:
                binding.includedTrailersSection.trailersRecyclerView.setVisibility(View.VISIBLE);
                binding.includedReviewsSection.reviewsRecyclerView.setVisibility(View.VISIBLE);
                break;
            case EMPTY_TRAILERS:
                binding.includedTrailersSection.trailersEmptyView.setVisibility(View.VISIBLE);
                binding.includedReviewsSection.reviewsRecyclerView.setVisibility(View.VISIBLE);
                break;
            case EMPTY_REVIEWS:
                binding.includedTrailersSection.trailersRecyclerView.setVisibility(View.VISIBLE);
                binding.includedReviewsSection.reviewsEmptyView.setVisibility(View.VISIBLE);
                break;
        }
    }

    /** {@link #populateUi()} Methods */

    private String polishReleaseDate(String releaseDate){
        releaseDate = releaseDate.replaceAll("-", " / ");

        return releaseDate;
    }

    private void setupRating(double originalRating){
        // check data validity
        if (originalRating > 10 || originalRating < 0)
            return;
        // rating in API max(10) in stars view I made max(5) so divide by 2
        double rating = originalRating /  2;
        // round to nearest 1 decimal
        rating = (double) Math.round(rating * 10) / 10;
        // first show exact number in rating label
        String ratingLabelWIthValue = getString(R.string.rating_label) + " " + rating;
        binding.ratingLabel.setText(ratingLabelWIthValue);
        // then stars view giving ratingLabelWIthValue certainly in format #.#
        // any filled or half filled colored as colorPrimaryDark, otherwise black for better UI
        String ratingString = String.valueOf(rating);
        int starsNumber = Integer.parseInt(ratingString.substring(0, 1));
        int halfStar = Integer.parseInt(ratingString.substring(2));

        ImageView imageView;
        int halfStarIndex = 0; // initiated as zero case if rate was 0.5 / 5
        Drawable drawable;
        int srcColor;
        for (int i = 0; i < starsNumber; i++){
            imageView = (ImageView) binding.ratingContainerLinearLayout.getChildAt(i);

            drawable = getResources().getDrawable(R.drawable.ic_star_black_24dp);
            srcColor = getResources().getColor(R.color.colorPrimaryDark);
            drawable.setColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP);
            imageView.setImageDrawable(drawable);

            halfStarIndex = i + 1;
        }

        if (halfStar >= 5){
            imageView = (ImageView) binding.ratingContainerLinearLayout.getChildAt(halfStarIndex);

            drawable = getResources().getDrawable(R.drawable.ic_star_half_black_24dp);
            srcColor = getResources().getColor(R.color.colorPrimaryDark);
            drawable.setColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP);
            imageView.setImageDrawable(drawable);
        }
    }

    /** {@link #setupViews()} Methods */

    private View.OnClickListener toggleCollapseAndExpand(final ImageView imageView,
                                                         final ConstraintLayout forVisibility){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forVisibility.getVisibility() == View.VISIBLE){
                    imageView.setImageResource(R.drawable.ic_add_box_black_24dp);

                    forVisibility.setVisibility(View.GONE);
                }else {
                    imageView.setImageResource(R.drawable.ic_indeterminate_check_box_black_24dp);

                    forVisibility.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    private View.OnClickListener refreshInternet(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.includedTrailersSection.noInternetConnectionConstraintLayout.setVisibility(View.GONE);
                binding.includedReviewsSection.noInternetConnectionConstraintLayout.setVisibility(View.GONE);

                getLoaderManager().restartLoader(LOADER_ID_FETCH_DATA_FROM_API, null,
                        MovieDetailsActivity.this);
            }
        };
    }

    private void checkMovieInFavourites(){
        Uri uri = FavEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(movie.getId())).build();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                // Movie is in favourites
                binding.favouriteAppCompatTextView.setText(getString(R.string.remove_from_favourites));

                binding.favouriteImageView.setImageResource(R.drawable.ic_favorite_black_24dp);
            }

            cursor.close();
        }
    }

    private void toggleMovieInFavourites(){
        if (binding.favouriteAppCompatTextView.getText().toString().equals(
                getString(R.string.add_to_favourites))){
            doNotRefreshMovies = true;

            ContentValues contentValues = new ContentValues();

            contentValues.put(FavEntry.COLUMN_MOVIE_ID, movie.getId());
            contentValues.put(FavEntry.COLUMN_MOVIE_JSON_OBJECT, JsonUtils.transformMovieToJsonObjectString(movie));
            contentValues.put(FavEntry.COLUMN_TRAILER_JSON_OBJECT, JsonUtils.transformTrailersToJsonArrayString(trailers));
            contentValues.put(FavEntry.COLUMN_REVIEW_JSON_OBJECT, JsonUtils.transformReviewsToJsonArrayString(reviews));

            // ---- See if trailers & reviews are null because they haven't been loaded from API yet
            // ---- OR needed internet connection
            boolean emptyTrailers = trailers == null || trailers.size() == 0;
            if (emptyTrailers &&
                    (binding.includedTrailersSection.trailersLoadingProgressBar.getVisibility() == View.VISIBLE
                            || binding.includedTrailersSection.noInternetConnectionConstraintLayout.getVisibility() == View.VISIBLE)){
                // Surely same scenario occurred to reviews as their data are loaded together
                // Note it might be emptyTrailers because there is no trailers for this movie
                // in that case this if condition will not be met.

                pendingUpdateMovieInFavourites = true;
            }

            Uri uri = getContentResolver().insert(FavEntry.CONTENT_URI, contentValues);

            if(uri != null) {
                showToast(getString(R.string.movie_added_to_favourites_successfully));

                binding.favouriteAppCompatTextView.setText(getString(R.string.remove_from_favourites));

                binding.favouriteImageView.setImageResource(R.drawable.ic_favorite_black_24dp);
            }
        }else {
            doNotRefreshMovies = false;

            String movieIdString = String.valueOf(movie.getId());
            Uri uri = FavEntry.CONTENT_URI.buildUpon().appendPath(movieIdString).build();

            int rowsDeleted = getContentResolver().delete(uri, null, null);

            if (rowsDeleted > 0){
                showToast(getString(R.string.movie_removed_from_favourites_successfully));

                binding.favouriteAppCompatTextView.setText(getString(R.string.add_to_favourites));

                binding.favouriteImageView.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            }
        }
    }

    // ---- Menu Setups

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie_details_activity, menu);
        menuItemUpdate = menu.findItem(R.id.update_action);
        menuItemShare = menu.findItem(R.id.share_action);

        // --- see if it is ok to show share menuItem to share trailer URL
        if (this.trailers != null && this.trailers.size() != 0){
            // Note this if condition will be met only if we are in favourite movies
            // and we need that as there are already trailers before waiting for loader
            menuItemShare.setVisible(true);
        }
        return true;
    }

    /**
     * We use this method to keep menuItem reference in screen rotation
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menuItemUpdateRunnable != null){
            menuItemUpdate = menu.findItem(R.id.update_action);

            menuItemUpdateRunnable.run();
            menuItemUpdateRunnable = null;
        }
        if (menuItemShareRunnable != null){
            menuItemShare = menu.findItem(R.id.share_action);

            menuItemShareRunnable.run();
            menuItemShareRunnable = null;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (doNotRefreshMovies || ! cameFromFavourites){
                    // no loading will happen again, as it already has been loaded before
                    onBackPressed();
                }else {
                    // Below code makes onCreate() in MainActivity be called again
                    // so, Data will be reloaded from internet (from API)
                    NavUtils.navigateUpFromSameTask(this);

                    // Note this might be needed for ex. if another app launches this activity
                    // to show movie details so when get back to MainActivity you will need
                    // to load data from API as there was no data there
                    // I know that doesn't happen here but that is for general rule handling
                }
                return true;
            case R.id.update_action:
                // --- Update UI
                trailers = updatedTrailers;
                reviews = updatedReviews;

                trailersAdapter.swapCursor(trailers);
                reviewsAdapter.swapCursor(reviews);

                boolean emptyTrailers = trailers == null || trailers.size() == 0;
                boolean emptyReviews = reviews == null || reviews.size() == 0;
                // in case if they were hidden and empty Views were visible instead
                showBothRecyclerViewsOnly();
                if (emptyTrailers){
                    showTrailersEmptyView();
                }
                if (emptyReviews){
                    showReviewsEmptyView();
                }

                menuItemUpdate.setVisible(false);

                showToast("Movie Updated Successfully");

                // --- Update DB as well
                ContentValues contentValues = new ContentValues();
                contentValues.put(FavEntry.COLUMN_TRAILER_JSON_OBJECT, JsonUtils.transformTrailersToJsonArrayString(trailers));
                contentValues.put(FavEntry.COLUMN_REVIEW_JSON_OBJECT, JsonUtils.transformReviewsToJsonArrayString(reviews));

                Uri uriToBeUpdated = FavEntry.CONTENT_URI.buildUpon()
                        .appendPath( String.valueOf(movie.getId()) )
                        .build();
                int rowsUpdated = getContentResolver().update(uriToBeUpdated, contentValues, null, null);

                // Just for me to ensure that the movie has been updated or not
                Log.i(MovieDetailsActivity.class.getName(), "Rows Updated = " + String.valueOf(rowsUpdated));

                // --- see if it is ok to show share menuItem to share trailer URL
                if (this.trailers != null && this.trailers.size() != 0){
                    menuItemShare.setVisible(true);
                }else {
                    // else done only here in case if added to favourite while movie has trailers
                    // but in update it doesn't then there is nothing to share plus menuItem was
                    // visible so we make it invisible
                    menuItemShare.setVisible(false);
                }
                return true;
            case R.id.share_action:
                showTrailersShareChooserDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** {@link #onOptionsItemSelected(MenuItem)} */

    private void showBothRecyclerViewsOnly(){
        // --- Hide Loading Progress Bar
        binding.includedTrailersSection.trailersLoadingProgressBar.setVisibility(View.GONE);
        binding.includedReviewsSection.reviewsLoadingProgressBar.setVisibility(View.GONE);
        // --- Hide No Internet Layout
        binding.includedTrailersSection.noInternetConnectionConstraintLayout.setVisibility(View.GONE);
        binding.includedReviewsSection.noInternetConnectionConstraintLayout.setVisibility(View.GONE);
        // --- Hide Empty View
        binding.includedTrailersSection.trailersEmptyView.setVisibility(View.GONE);
        binding.includedReviewsSection.reviewsEmptyView.setVisibility(View.GONE);
        // --- Show RecyclerViews in case if they had been hidden
        binding.includedTrailersSection.trailersRecyclerView.setVisibility(View.VISIBLE);
        binding.includedReviewsSection.reviewsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showTrailersShareChooserDialog(){
        if (trailers.size() == 1){
            // no need to make dialog to choose which trailer as there is only one
            trailerItemInDialogOnClick(0);

            return;
        }

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_trailer_share_chooser);

        // --- setup views and clicks

        // 1- Recycler View
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager trailersLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(trailersLayoutManager);
        recyclerView.setHasFixedSize(true);
        TrailerRecyclerViewAdapter trailersAdapter = new TrailerRecyclerViewAdapter(this, trailers, new TrailerRecyclerViewAdapter.TrailerItemListener() {
            @Override
            public void onClick(int trailerIndex) {
                trailerItemInDialogOnClick(trailerIndex);
            }
        });
        recyclerView.setAdapter(trailersAdapter);
        // 2- Cancel Button
        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //Grab the window of the dialog, and change the width
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        if (window != null){
            lp.copyFrom(window.getAttributes());
            //This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

        dialog.show();
    }

    /** {@link #showTrailersShareChooserDialog()} -> {@link #onOptionsItemSelected(MenuItem)} */
    private void trailerItemInDialogOnClick(int trailerIndex){
        Trailer trailer = trailers.get(trailerIndex);

        String site = trailer.getSite();
        if (! site.equals(YOUTUBE_SITE)){
            showToast("Error cannot share trailer url");

            return;
        }

        String key = trailer.getKey();
        URL url = NetworkUtils.buildYoutubeWatchUrl(key);

        String mimeType = "text/plain";
        String title = "Youtube Trailer Url";
        String text = url.toString();

        Intent intent = ShareCompat.IntentBuilder.from(MovieDetailsActivity.this)
                .setType(mimeType)
                .setChooserTitle(title)
                .setText(text)
                .createChooserIntent();

        if (intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }else {
            showToast("No app can handle text shared");
        }
    }

    // Trailer Item onClick Implementation

    @Override
    public void onClick(int trailerIndex) {
        Trailer trailer = trailers.get(trailerIndex);

        String site = trailer.getSite();
        if (! site.equals(YOUTUBE_SITE)){
            showToast("Error cannot launch trailer");

            return;
        }

        String key = trailer.getKey();
        URL url = NetworkUtils.buildYoutubeWatchUrl(key);

        // --- intent for web-page
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse(url.toString());
        intent.setData(data);
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }else {
            showToast("There is no app can open this web-page");
        }
    }

    // ---- Loader Callbacks

    @Override
    public Loader<ArrayList<ArrayList>> onCreateLoader(int id, Bundle args) {
        boolean isCurrentlyOnline = NetworkUtils.isCurrentlyOnline(this);

        if (id == LOADER_ID_FETCH_DATA_FROM_API){
            // --- Show Loading Progress Bar
            binding.includedTrailersSection.trailersLoadingProgressBar.setVisibility(View.VISIBLE);
            binding.includedReviewsSection.reviewsLoadingProgressBar.setVisibility(View.VISIBLE);

            return new FetchJsonResponseIntoArrayList(this, movie.getId(), isCurrentlyOnline);
        }else if (id == LOADER_ID_CHECK_FOR_UPDATES){
            return new FetchJsonResponseIntoArrayList(this, movie.getId(), isCurrentlyOnline);
        }else {
            Log.e(MovieDetailsActivity.class.getName(), "Unknown Loader ID = "
                    + String.valueOf(id));

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(Loader<ArrayList<ArrayList>> loader, ArrayList<ArrayList> data) {
        if (loader.getId() == LOADER_ID_CHECK_FOR_UPDATES){
            ArrayList<Trailer> trailers = data.get(0);
            ArrayList<Review> reviews = data.get(1);
            boolean emptyTrailers = trailers == null || trailers.size() == 0;
            boolean emptyReviews = reviews == null || reviews.size() == 0;
            // if new values are empty because there is no connection then this is not an update
            if (! NetworkUtils.isCurrentlyOnline(this)
                    && emptyTrailers
                    && emptyReviews){
                return;
            }

            // compare current trailers & reviews if they are the same then no update as well
            boolean areEqual = compareTwoReviewsIfEqual(reviews, this.reviews)
                    && compareTwoTrailersIfEqual(trailers, this.trailers);

            if (! areEqual){
                // --- update UI with refresh menuItem and database if menuItem is clicked
                menuItemUpdate.setVisible(true);

                showToast("New Updates Found For This Movie");

                updatedTrailers = trailers;
                updatedReviews = reviews;
            }

            return;
        }

        try {
            // --- Hide Loading Progress Bar
            binding.includedTrailersSection.trailersLoadingProgressBar.setVisibility(View.GONE);
            binding.includedReviewsSection.reviewsLoadingProgressBar.setVisibility(View.GONE);
            // --- Show RecyclerViews in case if they had been hidden
            binding.includedTrailersSection.trailersRecyclerView.setVisibility(View.VISIBLE);
            binding.includedReviewsSection.reviewsRecyclerView.setVisibility(View.VISIBLE);

            trailers = data.get(0);
            trailersAdapter.swapCursor(trailers);

            reviews = data.get(1);
            reviewsAdapter.swapCursor(reviews);

            boolean emptyTrailers = trailers == null || trailers.size() == 0;
            boolean emptyReviews = reviews == null || reviews.size() == 0;

            if (! NetworkUtils.isCurrentlyOnline(this)
                    && emptyTrailers
                    && emptyReviews){
                showRefreshNoInternetView();
            }else if (emptyTrailers || emptyReviews){
                if (emptyTrailers){
                    showTrailersEmptyView();
                }
                if (emptyReviews){
                    showReviewsEmptyView();
                }
            }

            // --- see if it is ok to show share menuItem to share trailer URL
            if (this.trailers != null && this.trailers.size() != 0){
                menuItemShare.setVisible(true);
            }

            // ---- check if was waiting for favourite updates
            if (pendingUpdateMovieInFavourites){
                pendingUpdateMovieInFavourites = false;

                // If not in favourite anymore
                if (binding.favouriteAppCompatTextView.getText().toString().equals(
                        getString(R.string.add_to_favourites))){
                    return;
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(FavEntry.COLUMN_TRAILER_JSON_OBJECT, JsonUtils.transformTrailersToJsonArrayString(trailers));
                contentValues.put(FavEntry.COLUMN_REVIEW_JSON_OBJECT, JsonUtils.transformReviewsToJsonArrayString(reviews));

                Uri uriToBeUpdated = FavEntry.CONTENT_URI.buildUpon()
                        .appendPath( String.valueOf(movie.getId()) )
                        .build();
                int rowsUpdated = getContentResolver().update(uriToBeUpdated, contentValues, null, null);

                // Just for me to ensure that the movie has been updated or not
                Log.i(MovieDetailsActivity.class.getName(), "Rows Updated = " + String.valueOf(rowsUpdated));
            }
        }catch (Exception e){
            // In case of un-expected issues like data doesn't have the arrayLists in order.
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ArrayList>> loader) {
        trailersAdapter.swapCursor(null);
        reviewsAdapter.swapCursor(null);
    }

    /** {@link #onLoadFinished(Loader, ArrayList)} */

    private void showRefreshNoInternetView(){
        // --- Trailers
        binding.includedTrailersSection.trailersRecyclerView.setVisibility(View.GONE);
        binding.includedTrailersSection.trailersEmptyView.setVisibility(View.GONE);

        binding.includedTrailersSection.noInternetConnectionConstraintLayout.setVisibility(View.VISIBLE);

        // --- Reviews
        binding.includedReviewsSection.reviewsRecyclerView.setVisibility(View.GONE);
        binding.includedReviewsSection.reviewsEmptyView.setVisibility(View.GONE);

        binding.includedReviewsSection.noInternetConnectionConstraintLayout.setVisibility(View.VISIBLE);
    }

    private void showTrailersEmptyView(){
        binding.includedTrailersSection.trailersRecyclerView.setVisibility(View.GONE);
        binding.includedTrailersSection.noInternetConnectionConstraintLayout.setVisibility(View.GONE);

        binding.includedTrailersSection.trailersEmptyView.setVisibility(View.VISIBLE);
    }

    private void showReviewsEmptyView(){
        binding.includedReviewsSection.reviewsRecyclerView.setVisibility(View.GONE);
        binding.includedReviewsSection.noInternetConnectionConstraintLayout.setVisibility(View.GONE);

        binding.includedReviewsSection.reviewsEmptyView.setVisibility(View.VISIBLE);
    }

    private boolean compareTwoTrailersIfEqual(ArrayList<Trailer> trailers1,
                                              ArrayList<Trailer> trailers2){
        if (trailers1 == null)
            trailers1 = new ArrayList<>();
        if (trailers2 == null)
            trailers2 = new ArrayList<>();

        if (trailers1.size() != trailers2.size()){
            // different sizes so surely not equal
            return false;
        }

        Trailer sub1;
        Trailer sub2;
        boolean result;
        for (int i=0; i<trailers1.size(); i++){
            sub1 = trailers1.get(i);
            sub2 = trailers2.get(i);

            result = sub1.getName().equals(sub2.getName())
                    && sub1.getSite().equals(sub2.getSite())
                    && sub1.getKey().equals(sub2.getKey());

            if (! result) {
                return false;
            }
        }

        return true;
    }

    private boolean compareTwoReviewsIfEqual(ArrayList<Review> reviews1,
                                              ArrayList<Review> reviews2){
        if (reviews1 == null)
            reviews1 = new ArrayList<>();
        if (reviews2 == null)
            reviews2 = new ArrayList<>();

        if (reviews1.size() != reviews2.size()){
            // different sizes so surely not equal
            return false;
        }

        Review sub1;
        Review sub2;
        boolean result;
        for (int i=0; i<reviews1.size(); i++){
            sub1 = reviews1.get(i);
            sub2 = reviews1.get(i);

            result = sub1.getAuthor().equals(sub2.getAuthor())
                    && sub1.getContent().equals(sub2.getContent());

            if (! result) {
                return false;
            }
        }

        return true;
    }

    /*private void showTrailersShareChooserItem(){
        menuItemShare.setVisible(true);
    }*/

    // ---- On Back Pressed

    @Override
    public void onBackPressed() {
        if (doNotRefreshMovies || ! cameFromFavourites){
            super.onBackPressed();
        }else {
            NavUtils.navigateUpFromSameTask(this);
        }

        // --- check if movie in favourites has no trailers & reviews because they haven't been
        // --- loaded from API or because there was no internet connection
        if (pendingUpdateMovieInFavourites && binding.favouriteAppCompatTextView.getText()
                .toString().equals(getString(R.string.remove_from_favourites))
                && NetworkUtils.isCurrentlyOnline(this)){
            // If still loading from API start service to make the update for movie in favourites
            // Also same process If there was no internet to load from API
            // Note both above cases are met when the boolean in this if condition is met as well

            Intent intent = new Intent(this, UpdateMovieInFavouritesIntentService.class);
            intent.putExtra(UpdateMovieInFavouritesIntentService.INTENT_KEY_MOVIE_ID, movie.getId());

            startService(intent);
        }
    }

    // ---- Handling On Save Instance State

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(S_I_S_K_MOVIE, movie);

        outState.putBoolean(S_I_S_K_DO_NOT_REFRESH_MOVIES, doNotRefreshMovies);

        outState.putParcelableArrayList(S_I_S_K_TRAILERS, trailers);

        outState.putParcelableArrayList(S_I_S_K_REVIEWS, reviews);

        outState.putBoolean(S_I_S_K_PENDING_UPDATE_MOVIE_IN_FAVOURITES, pendingUpdateMovieInFavourites);

        boolean trailersExpanded = binding.includedTrailersSection.forVisibilityConstraintLayout.getVisibility() == View.VISIBLE;
        outState.putBoolean(S_I_S_K_TRAILERS_EXPANDED, trailersExpanded);

        boolean reviewsExpanded = binding.includedReviewsSection.forVisibilityConstraintLayout.getVisibility() == View.VISIBLE;
        outState.putBoolean(S_I_S_K_REVIEWS_EXPANDED, reviewsExpanded);

        outState.putBoolean(S_I_S_K_MENU_ITEM_UPDATE_VISIBLE, menuItemUpdate.isVisible());
        outState.putBoolean(S_I_S_K_MENU_ITEM_SHARE_VISIBLE, menuItemShare.isVisible());

        outState.putParcelableArrayList(S_I_S_K_UPDATED_TRAILERS, updatedTrailers);

        outState.putParcelableArrayList(S_I_S_K_UPDATED_REVIEWS, updatedReviews);

        TrailersReviewsState state;
        if (binding.includedTrailersSection.trailersLoadingProgressBar.getVisibility() == View.VISIBLE){
            state = TrailersReviewsState.RECYCLER_VIEWS_PROGRESS_BAR;
        }else if (binding.includedTrailersSection.noInternetConnectionConstraintLayout.getVisibility() == View.VISIBLE){
            state = TrailersReviewsState.RECYCLER_VIEWS_NO_INTERNET;
        }else {
            boolean emptyTrailers = trailers == null || trailers.size() == 0;
            boolean emptyReviews = reviews == null || reviews.size() == 0;
            if (! emptyTrailers && ! emptyReviews){
                state = TrailersReviewsState.TRAILERS_AND_REVIEWS;
            }else if (emptyTrailers){
                state = TrailersReviewsState.EMPTY_TRAILERS;
            }else {
                // Surely emptyReviews is true
                state = TrailersReviewsState.EMPTY_REVIEWS;
            }
        }
        outState.putSerializable(S_I_S_K_TRAILERS_REVIEWS_STATE, state);

        outState.putInt(S_I_S_K_NESTED_SCROLL_VIEW_Y, binding.nestedScrollView.getScrollY());

        outState.putBoolean(S_I_S_K_CAME_FROM_FAVOURITES, cameFromFavourites);

        boolean dialogIsVisible = dialog != null && dialog.isShowing();
        outState.putBoolean(S_I_S_K_DIALOG_TRAILERS_CHOOSER_VISIBLE, dialogIsVisible);

        super.onSaveInstanceState(outState);
    }

    // ---- Enum Class

    /**
     * Usage
     * In case of device rotation so this change UI state to correct state
     * Couldn't depend on trailers only being empty as they are empty while still loading from API
     * so we need this enum to indicate that they still load and current state is to show
     * progressBars and same need mechanism for other cases
     * Note
     * Enums are actually serializable
     */
    private enum TrailersReviewsState {
        RECYCLER_VIEWS_PROGRESS_BAR , RECYCLER_VIEWS_NO_INTERNET , TRAILERS_AND_REVIEWS
        , EMPTY_TRAILERS , EMPTY_REVIEWS
    }

    // ---- Global Helper Methods

    private void showToast(String msg){
        if (toast != null){
            toast.cancel();
        }

        toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT);

        toast.show();
    }
}
