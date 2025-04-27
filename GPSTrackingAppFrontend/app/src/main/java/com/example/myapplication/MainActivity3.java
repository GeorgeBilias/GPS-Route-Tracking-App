package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity3 extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        TextView tx = findViewById(R.id.final_rslts);
        ImageButton back = findViewById(R.id.back_button);
        final MediaPlayer mp = MediaPlayer.create(this,R.raw.click);

        tx.setText(MainActivity.results);

        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mp.start();
                Intent intent = new Intent(MainActivity3.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
