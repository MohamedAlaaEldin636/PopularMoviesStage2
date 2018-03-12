package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.model.Review;

import java.util.ArrayList;

/**
 * Created by Mohamed on 3/2/2018.
 *
 */

public class ReviewRecyclerViewAdapter
        extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewViewHolder> {

    private static final String UNKNOWN_AUTHOR = "Unknown Author";
    private static final String UNKNOWN_CONTENT = "No Content Provided.";

    private ArrayList<Review> reviews;

    public ReviewRecyclerViewAdapter(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    // ---- Must be Overridden Methods

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutRes = R.layout.review_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutRes, parent, false);

        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        String author = review.getAuthor();
        if (author == null || author.isEmpty()){
            author = UNKNOWN_AUTHOR;
        }
        holder.authorTextView.setText(author);

        String content = review.getContent();
        if (content == null || content.isEmpty()){
            content = UNKNOWN_CONTENT;
        }
        holder.contentTextView.setText(content);
    }

    @Override
    public int getItemCount() {
        return (reviews == null) ? 0 : reviews.size();
    }

    // ---- View Holder

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        private final TextView authorTextView;
        private final TextView contentTextView;

        ReviewViewHolder(View itemView) {
            super(itemView);

            authorTextView = itemView.findViewById(R.id.authorTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
        }
    }

    // ---- Swap Cursor

    public void swapCursor(ArrayList<Review> reviews){
        this.reviews = reviews;

        notifyDataSetChanged();
    }

}
