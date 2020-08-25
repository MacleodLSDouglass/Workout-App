package com.example.stopwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stopwatch.models.SetsModel;

import java.util.ArrayList;
import java.util.List;

public class Exercise extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {
    private static final String TAG = "ExerciseActivity";
    private MyRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // data to populate the RecyclerView with
        ArrayList<SetsModel> sets = new ArrayList<>();
        sets.add(new SetsModel (1, 2, 3));
        sets.add(new SetsModel (11, 22, 33));
        sets.add(new SetsModel (111, 222, 333));
        sets.add(new SetsModel (1111, 2222, 3333));
        sets.add(new SetsModel (11111, 22222, 33333));

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, sets);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        // find and set text views
        final EditText exerciseText = findViewById(R.id.exerciseTextExercise);
        final EditText setNumber = findViewById(R.id.setNumberExercise);

        // focus the first input field on creation
        exerciseText.requestFocus();

        // try to take the number of sets from the sets text view and format the layout to display
        // number of set groups equal to the sets value
//        setNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(!hasFocus) {
//                    int count;
//                    if (setNumber.getText() != null) {
//                        try {
//                            count = Integer.parseInt(setNumber.getText().toString());
//                        } catch(Exception e) {
//                            Log.e(TAG, e.getMessage());
//                            finish();
//                        }
//                    }
//                }
//            }
//        });

//        mAdapter.onCreateViewHolder(recyclerView, 0);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number "
        + position, Toast.LENGTH_SHORT).show();
    }

    public void onClickSave(View view)  {
        finish();
    }
}