package com.example.stopwatch.activities.Exercise;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.example.stopwatch.R;
import com.example.stopwatch.models.SetModel;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // collection of values to the be set into Views
    public List<SetModel> sets;

    private LayoutInflater mInflater;

    ExerciseAdapter(Context context, List<SetModel> data) {
        this.sets = data;
        this.mInflater = LayoutInflater.from(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.set_layout, parent, false);
        return new ExerciseAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ExerciseAdapter.ViewHolder nHolder = (ExerciseAdapter.ViewHolder) holder;

        SetModel set = sets.get(position);

        nHolder.reps.setText(String.valueOf(set.reps));
        nHolder.weight.setText(String.valueOf(set.weight));
        nHolder.time.setText(String.valueOf(set.time));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return sets.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText reps;
        EditText weight;
        EditText time;

        ViewHolder(View itemView) {
            super(itemView);
            reps = itemView.findViewById(R.id.reps);
            weight = itemView.findViewById(R.id.weight);
            time = itemView.findViewById(R.id.time);
        }
    }

    // inserts ViewHolder at specified position
    public void insertAt(int position) {
        sets.add(new SetModel());
        notifyItemInserted(position);
        notifyItemRangeChanged(position, sets.size());
    }

    // removes ViewHolder at specified position
    public void removeAt(int position) {
        sets.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, sets.size());
    }
}
