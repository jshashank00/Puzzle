package msu.edu.cse476.jayara17.assignment2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onStartPuzzle(View view) {
        Intent intent = new Intent(this, PuzzleActivity.class);
        startActivity(intent);
    }
}