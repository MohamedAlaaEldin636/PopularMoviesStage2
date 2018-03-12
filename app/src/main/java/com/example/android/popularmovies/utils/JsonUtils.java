package com.example.android.popularmovies.utils;

import android.util.Log;

import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.model.Review;
import com.example.android.popularmovies.model.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mohamed on 2/17/2018.
 *
 * No need to check if jsonObject has key as we use .opt method instead of .get
 */

public class JsonUtils {

    private static final String JSON_KEY_RESULTS = "results";

    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_ORIGINAL_TITLE = "original_title";
    private static final String JSON_KEY_POSTER_PATH = "poster_path";
    private static final String JSON_KEY_OVERVIEW = "overview";
    private static final String JSON_KEY_VOTE_AVERAGE = "vote_average";
    private static final String JSON_KEY_RELEASE_DATE = "release_date";

    private static final String JSON_KEY_TYPE = "type";
    private static final String VIDEO_TYPE_TRAILER = "Trailer";
    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_SITE = "site";
    private static final String JSON_KEY_KEY = "key";

    private static final String JSON_KEY_AUTHOR = "author";
    private static final String JSON_KEY_CONTENT = "content";

    /** Any negative value */
    private static final int ERROR_ID = -734;

    /** Any negative value */
    private static final double ERROR_VOTE_AVERAGE = -5;

    public static ArrayList<Movie> getMoviesArrayList(String response) {
        if (response == null || response.isEmpty()){
            Log.e(JsonUtils.class.getName(), "Null or empty response inside " +
                    "getMoviesArrayList(response) method");

            return null;
        }

        try {
            JSONObject rootJsonObject = new JSONObject(response);

            JSONArray results = rootJsonObject.optJSONArray(JSON_KEY_RESULTS);

            if (results != null){
                ArrayList<Movie> movies = new ArrayList<>();

                JSONObject movie;

                int id;
                String originalTitle;
                String posterPath;
                String overview;
                double voteAverage;
                String releaseDate;

                for (int i=0; i<results.length(); i++){
                    movie = results.optJSONObject(i);
                    if (movie == null)
                        continue;

                    id = movie.optInt(JSON_KEY_ID, ERROR_ID);
                    originalTitle = movie.optString(JSON_KEY_ORIGINAL_TITLE);
                    posterPath = movie.optString(JSON_KEY_POSTER_PATH);
                    overview = movie.optString(JSON_KEY_OVERVIEW);
                    voteAverage = movie.optDouble(JSON_KEY_VOTE_AVERAGE);
                    releaseDate = movie.optString(JSON_KEY_RELEASE_DATE);

                    // poster_path MUST be checked if valid
                    if (posterPath.isEmpty())
                        continue;

                    movies.add(new Movie(id, originalTitle, posterPath, overview, voteAverage,
                            releaseDate));
                }

                return movies;
            }else {
                Log.e(JsonUtils.class.getName(), "Null results JsonArray object");

                return null;
            }
        }catch (JSONException e){
            e.printStackTrace();

            return null;
        }
    }

    public static ArrayList<Trailer> getTrailersArrayList(String response){
        if (response == null || response.isEmpty()){
            Log.e(JsonUtils.class.getName(), "Null or empty response inside " +
                    "getTrailersArrayList(response) method");

            return null;
        }

        try {
            JSONObject rootJsonObject = new JSONObject(response);

            JSONArray results = rootJsonObject.optJSONArray(JSON_KEY_RESULTS);

            if (results != null){
                ArrayList<Trailer> trailers = new ArrayList<>();

                JSONObject trailer;

                String name;
                String site;
                String key;
                String videoType;

                for (int i=0; i<results.length(); i++){
                    trailer = results.optJSONObject(i);
                    if (trailer == null)
                        continue;

                    videoType = trailer.optString(JSON_KEY_TYPE);
                    if (! videoType.equals(VIDEO_TYPE_TRAILER) || videoType.isEmpty())
                        continue;

                    name = trailer.optString(JSON_KEY_NAME);
                    if (name.isEmpty())
                        continue;

                    site = trailer.optString(JSON_KEY_SITE);
                    if (site.isEmpty())
                        continue;

                    key = trailer.getString(JSON_KEY_KEY);
                    if (key.isEmpty())
                        continue;

                    trailers.add(new Trailer(name, site, key));
                }

                return trailers;
            }else {
                Log.e(JsonUtils.class.getName(), "Null results JsonArray object" +
                        "\nInside getTrailersArrayList(String response)");

                return null;
            }
        }catch (JSONException e){
            e.printStackTrace();

            return null;
        }
    }

    public static ArrayList<Review> getReviewsArrayList(String response){
        if (response == null || response.isEmpty()){
            Log.e(JsonUtils.class.getName(), "Null or empty response inside " +
                    "getReviewsArrayList(response) method");

            return null;
        }

        try {
            JSONObject rootJsonObject = new JSONObject(response);

            JSONArray results = rootJsonObject.optJSONArray(JSON_KEY_RESULTS);

            if (results != null){
                ArrayList<Review> reviews = new ArrayList<>();

                JSONObject review;

                String author;
                String content;

                for (int i=0; i<results.length(); i++){
                    review = results.optJSONObject(i);
                    if (review == null)
                        continue;

                    author = review.optString(JSON_KEY_AUTHOR);
                    if (author.isEmpty())
                        continue;

                    content = review.optString(JSON_KEY_CONTENT);
                    if (content.isEmpty())
                        continue;

                    reviews.add(new Review(author, content));
                }

                return reviews;
            }else {
                Log.e(JsonUtils.class.getName(), "Null results JsonArray object" +
                        "\nInside getReviewsArrayList(String response)");

                return null;
            }
        }catch (JSONException e){
            e.printStackTrace();

            return null;
        }
    }

    /** For Database */

    public static String transformMovieToJsonObjectString(Movie movie){
        if (movie == null){
            Log.e(JsonUtils.class.getName(), "Null Movie in convertMovieToJsonObjectString()");

            return null;
        }

        try {
            JSONObject movieJsonObject = new JSONObject();

            movieJsonObject.put(JSON_KEY_ID, movie.getId());
            movieJsonObject.put(JSON_KEY_ORIGINAL_TITLE, movie.getOriginalTitle());
            movieJsonObject.put(JSON_KEY_POSTER_PATH, movie.getPosterPath());
            movieJsonObject.put(JSON_KEY_OVERVIEW, movie.getOverview());
            movieJsonObject.put(JSON_KEY_VOTE_AVERAGE, movie.getVoteAverage());
            movieJsonObject.put(JSON_KEY_RELEASE_DATE, movie.getReleaseDate());

            return movieJsonObject.toString();
        }catch (JSONException e){
            e.printStackTrace();

            return null;
        }
    }

    public static Movie transformJsonObjectStringToMovie(String jsonObjectString){
        if (jsonObjectString == null || jsonObjectString.isEmpty()){
            Log.e(JsonUtils.class.getName(), "Null or empty string method param in " +
                    "transformJsonObjectStringToMovie()");

            return null;
        }

        try {
            JSONObject movieJsonObject = new JSONObject(jsonObjectString);

            int id = movieJsonObject.optInt(JSON_KEY_ID, ERROR_ID);
            String originalTitle = movieJsonObject.optString(JSON_KEY_ORIGINAL_TITLE);
            String posterPath = movieJsonObject.optString(JSON_KEY_POSTER_PATH);
            String overview = movieJsonObject.optString(JSON_KEY_OVERVIEW);
            double voteAverage = movieJsonObject.optDouble(JSON_KEY_VOTE_AVERAGE, ERROR_VOTE_AVERAGE);
            String releaseDate = movieJsonObject.optString(JSON_KEY_RELEASE_DATE);

            if (id == ERROR_ID || voteAverage == ERROR_VOTE_AVERAGE){
                Log.e(JsonUtils.class.getName(), "ERROR_ID || ERROR_VOTE_AVERAGE while " +
                        "transforming to movie");

                return null;
            }

            return new Movie(id, originalTitle, posterPath, overview, voteAverage, releaseDate);
        }catch (JSONException e){
            e.printStackTrace();

            return null;
        }
    }

    public static String transformTrailersToJsonArrayString(ArrayList<Trailer> trailers){
        if (trailers == null || trailers.size() == 0){
            Log.e(JsonUtils.class.getName(), "Null or empty trailers while transforming to jsonObject");

            return null;
        }

        try {
            JSONArray trailersJsonArray = new JSONArray();

            JSONObject jsonObject;
            Trailer trailer;
            for (int i = 0; i < trailers.size(); i++){
                jsonObject = new JSONObject();

                trailer = trailers.get(i);

                jsonObject.put(JSON_KEY_NAME, trailer.getName());
                jsonObject.put(JSON_KEY_SITE, trailer.getSite());
                jsonObject.put(JSON_KEY_KEY, trailer.getKey());

                trailersJsonArray.put(jsonObject);
            }

            return trailersJsonArray.toString();
        }catch (JSONException e){
            e.printStackTrace();

            return null;
        }
    }

    public static ArrayList<Trailer> transformJsonArrayStringToTrailers(String jsonArrayString){
        if (jsonArrayString == null || jsonArrayString.isEmpty()){
            Log.e(JsonUtils.class.getName(), "Null or empty string method param while " +
                    "converting to trailers");

            return null;
        }

        try {
            JSONArray trailersJsonArray = new JSONArray(jsonArrayString);

            ArrayList<Trailer> trailers = new ArrayList<>();

            JSONObject jsonObject;
            String name;
            String site;
            String key;
            for (int i = 0; i < trailersJsonArray.length(); i++){
                jsonObject = trailersJsonArray.optJSONObject(i);
                if (jsonObject == null)
                    continue;

                name = jsonObject.optString(JSON_KEY_NAME);
                site = jsonObject.optString(JSON_KEY_SITE);
                key = jsonObject.getString(JSON_KEY_KEY);

                trailers.add(new Trailer(name, site, key));
            }

            return trailers;
        }catch (JSONException e){
            e.printStackTrace();

            return null;
        }
    }

    public static String transformReviewsToJsonArrayString(ArrayList<Review> reviews){
        if (reviews == null || reviews.size() == 0){
            Log.e(JsonUtils.class.getName(), "Null or empty reviews while transforming to jsonObject");

            return null;
        }

        try {
            JSONArray reviewsJsonArray = new JSONArray();

            JSONObject jsonObject;
            Review review;
            for (int i = 0; i < reviews.size(); i++){
                jsonObject = new JSONObject();

                review = reviews.get(i);

                jsonObject.put(JSON_KEY_AUTHOR, review.getAuthor());
                jsonObject.put(JSON_KEY_CONTENT, review.getContent());

                reviewsJsonArray.put(jsonObject);
            }

            return reviewsJsonArray.toString();
        }catch (JSONException e){
            e.printStackTrace();

            return null;
        }
    }

    public static ArrayList<Review> transformJsonArrayStringToReviews(String jsonArrayString){
        if (jsonArrayString == null || jsonArrayString.isEmpty()){
            Log.e(JsonUtils.class.getName(), "Null or empty string method param while " +
                    "converting to reviews");

            return null;
        }

        try {
            JSONArray reviewsJsonArray = new JSONArray(jsonArrayString);

            ArrayList<Review> reviews = new ArrayList<>();

            JSONObject jsonObject;
            String author;
            String content;
            for (int i = 0; i < reviewsJsonArray.length(); i++){
                jsonObject = reviewsJsonArray.optJSONObject(i);
                if (jsonObject == null)
                    continue;

                author = jsonObject.optString(JSON_KEY_AUTHOR);
                content = jsonObject.optString(JSON_KEY_CONTENT);

                reviews.add(new Review(author, content));
            }

            return reviews;
        }catch (JSONException e){
            e.printStackTrace();

            return null;
        }
    }
}