package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity2 extends Activity {
    ImageButton button3;
    TextView textViewRouteStats;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        button3 = findViewById(R.id.button3);
        final MediaPlayer mp = MediaPlayer.create(this,R.raw.click);
        textViewRouteStats = findViewById(R.id.textViewRouteStats); // Replace with the actual id of your TextView

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.start();
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Retrieve the route_stats_string from the intent extras
        String routeStats = getIntent().getStringExtra("route_stats");

        // Set the route_stats_string as the text for the TextView
        textViewRouteStats.setText(routeStats);

    }

}
