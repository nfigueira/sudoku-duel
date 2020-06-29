package com.example.sudokuduel;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void playSingleGame(View view) {
        Intent singleGameIntent = new Intent(this, SingleGame.class);
        startActivity(singleGameIntent);
    }
}
