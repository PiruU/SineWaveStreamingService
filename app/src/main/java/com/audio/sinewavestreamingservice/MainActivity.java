package com.audio.sinewavestreamingservice;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        float soundFrequency  = 440f;
        float soundDuration   = 2.5f;
        int   audioBufferSize =   32;

        SineWaveStreamingService sineWave = new SineWaveStreamingService(
            this, soundFrequency, audioBufferSize, soundDuration
        );
        sineWave.play();
        sineWave.stopAndRelease();
    }


}
