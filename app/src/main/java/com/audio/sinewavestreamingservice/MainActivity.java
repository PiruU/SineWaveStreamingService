package com.audio.sinewavestreamingservice;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.playSineWave();
    }

    private void playSineWave() {
        new SineWaveStreamingService(
                this, 440f, 128, 5.5f
        ).play();
    }
}