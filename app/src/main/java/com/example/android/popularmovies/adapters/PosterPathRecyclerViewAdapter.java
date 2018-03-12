package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Mohamed on 2/17/2018.
 *
 */

public class PosterPathRecyclerViewAdapter extends
        RecyclerView.Adapter<PosterPathRecyclerViewAdapter.PosterPathViewHolder> {

    private final Context context;

    private final PosterPathItemListener itemListener;

    private ArrayList<Movie> movies;

    public PosterPathRecyclerViewAdapter(Context context, PosterPathItemListener itemListener,
                                         ArrayList<Movie> movies) {
        this.context = context;
        this.itemListener = itemListener;
        this.movies = movies;
    }

    public interface PosterPathItemListener {
        void onClick(int movieIndex);
    }

    // ---- Must be overridden methods

    @Override
    public PosterPathViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutRes = R.layout.poster_path_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutRes, parent, false);

        return new PosterPathViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PosterPathViewHolder holder, int position) {
        Movie movie = movies.get(position);

        String posterPath = movie.getPosterPath();
        String fullPosterPath = NetworkUtils.fullPosterPathFromThirdPartPath(posterPath);

        Picasso.with(context)
                .load(fullPosterPath)
                .error(context.getResources().getDrawable(R.drawable.error_poster_image_load))
                .into(holder.posterImageView);
    }

    @Override
    public int getItemCount() {
        return (movies == null) ? 0 : movies.size();
    }

    // ---- View Holder

    class PosterPathViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final ImageView posterImageView;

        PosterPathViewHolder(View itemView) {
            super(itemView);

            posterImageView = itemView.findViewById(R.id.posterImageView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemListener.onClick(getAdapterPosition());
        }
    }

    // --- Making changes/swaps methods

    public void swapMovies(ArrayList<Movie> movies) {
        this.movies = movies;

        notifyDataSetChanged();
    }
}
