package com.myacceleration.myacceleration;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myacceleration.myacceleration.ScoreFragment.OnListFragmentInteractionListener;
import com.myacceleration.myacceleration.db.Score;

import java.util.List;

public class MyScoreRecyclerViewAdapter extends RecyclerView.Adapter<MyScoreRecyclerViewAdapter.ViewHolder> {

    private List<Score> scores;
    private final OnListFragmentInteractionListener mListener;

    public MyScoreRecyclerViewAdapter(List<Score> scores, OnListFragmentInteractionListener listener) {
        this.scores = scores;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mScore = scores.get(position);
        holder.mIdView.setText(""+(position+1));
        holder.mUserNameView.setText(scores.get(position).getUsername());
        holder.mScoreView.setText(scores.get(position).getValue().toString());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mScore);
                }
            }
        });
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mUserNameView;
        public final TextView mScoreView;
        public Score mScore;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.rank_num);
            mUserNameView = (TextView) view.findViewById(R.id.user_name);
            mScoreView = (TextView) view.findViewById(R.id.score_text);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUserNameView.getText() + "'";
        }
    }
}
