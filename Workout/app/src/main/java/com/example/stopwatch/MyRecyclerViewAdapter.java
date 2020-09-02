package com.example.stopwatch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.stopwatch.models.SetsModel;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    private List<SetsModel> sets;
    private LayoutInflater mInflater;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<SetsModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.sets = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.set_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SetsModel set = sets.get(position);

        holder.reps.setText(String.valueOf(set.reps));
        holder.weight.setText(String.valueOf(set.weight));
        holder.time.setText(String.valueOf(set.time));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return sets.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        EditText reps;
        EditText weight;
        EditText time;

        ViewHolder(View itemView) {
            super(itemView);
            reps = itemView.findViewById(R.id.setReps);
            weight = itemView.findViewById(R.id.setWeight);
            time = itemView.findViewById(R.id.setTime);
        }

        @Override
        public void onClick(View view) {
        }
    }

    public void insertAt(int position) {
        sets.add(new SetsModel());
        notifyItemInserted(position);
        notifyItemRangeChanged(position, sets.size());
    }

    public void removeAt(int position) {
        sets.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, sets.size());
    }
}
