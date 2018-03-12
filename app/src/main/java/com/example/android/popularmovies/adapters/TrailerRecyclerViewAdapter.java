package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.model.Trailer;
import com.example.android.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Mohamed on 2/27/2018.
 *
 */

public class TrailerRecyclerViewAdapter extends
        RecyclerView.Adapter<TrailerRecyclerViewAdapter.TrailerViewHolder> {

    private final Context context;

    private ArrayList<Trailer> trailers;

    private final TrailerItemListener listener;

    public TrailerRecyclerViewAdapter(Context context, ArrayList<Trailer> trailers, TrailerItemListener listener) {
        this.context = context;

        this.trailers = trailers;

        this.listener = listener;
    }

    // ---- interface for the item

    public interface TrailerItemListener {
        void onClick(int trailerIndex);
    }

    // ---- Must be Overridden Methods

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutRes = R.layout.trailer_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutRes, parent, false);

        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        Trailer trailer = trailers.get(position);

        String trailerName = trailer.getName();
        holder.trailerNameTextView.setText(trailerName);

        Picasso.with(context)
                .load(NetworkUtils.buildYoutubeVideoImageUri(trailer.getKey()))
                .placeholder(R.drawable.ic_play_circle_outline_black_24dp)
                .error(context.getResources().getDrawable(R.drawable.error_poster_image_load))
                .into(holder.trailerVideoImageView);
    }

    @Override
    public int getItemCount() {
        return (trailers == null) ? 0 : trailers.size();
    }

    // ---- View Holder

    class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final AppCompatTextView trailerNameTextView;
        private final ImageView trailerVideoImageView;

        TrailerViewHolder(View itemView) {
            super(itemView);

            trailerNameTextView = itemView.findViewById(R.id.movieTrailerNameTextView);
            trailerVideoImageView = itemView.findViewById(R.id.movieTrailerImageView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(getAdapterPosition());
        }
    }

    // ---- Swap Cursor

    public void swapCursor(ArrayList<Trailer> trailers){
        this.trailers = trailers;

        notifyDataSetChanged();
    }
}
