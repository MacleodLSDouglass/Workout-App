package com.example.stopwatch;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Exercises extends AppCompatActivity {
    final String fileName = "exercises";
    public TextView exerciseView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        exerciseView = (TextView)findViewById(R.id.exercises_view);

        // Reads the file "exercises" to display contents to the screen.
        ReadFile();
    }

    // Save contents of exerciseView to file called exercises when Save button is clicked.
    public void OnClickSaveFile(View view) {
        String fileName = "exercises";
        String fileContents = exerciseView.getText().toString();

        // Try to open output to file, then write to file.
        try (FileOutputStream fos = this.openFileOutput(fileName, this.MODE_PRIVATE)) {
            fos.write(fileContents.getBytes());
        } catch (Exception e)  {
            System.out.println(e.getMessage());
        }

        finish();
    }

    // Reads the file when activity is created.
    private void ReadFile() {
        FileInputStream fis = null;
        try {
            fis = this.openFileInput(fileName);
        }
        catch(Exception e) {
            return;
        }
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(isr)) {
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {

        } finally {
            String contents = sb.toString();
            exerciseView.setText(contents);
        }

        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}