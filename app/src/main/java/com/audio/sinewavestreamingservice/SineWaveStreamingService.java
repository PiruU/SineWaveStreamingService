package com.audio.sinewavestreamingservice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

class SineWaveStreamingService {
    AudioTrack _audioTrack;
    Context _context;
    float[] _audioBuffer;
    int _audioBufferSize;
    float _soundFrequency;
    float  _soundPhase;
    float _deviceAudioFrameRate;
    int _buffersWrittenCount;

    public SineWaveStreamingService(Context context, float soundFrequencyInHz, int audioBufferSize, float soundLengthInSeconds) {
        this._context              = context;
        this._audioBufferSize      = audioBufferSize;
        this._audioBuffer          = new float[audioBufferSize];
        this._soundFrequency       = soundFrequencyInHz;
        this._deviceAudioFrameRate = (float)this.deviceAudioFrameRate();
        this._buffersWrittenCount  = (int)(soundLengthInSeconds * (float)this.deviceAudioFrameRate() / (float)this._audioBufferSize);
        this.initAudioTrack(this._audioBufferSize);
    }

    public void play() {
        this._audioTrack.play();
        for(int bufferIndex = 0; bufferIndex < this._buffersWrittenCount; ++bufferIndex) {
            this.updateAudioBuffer();
            this.writeAudioOutput();
        }
    }

    public void stopAndRelease() {
        this._audioTrack.stop();
        this._audioTrack.release();
    }

    private void updateAudioBuffer() {
        for(int iSample = 0; iSample < this._audioBufferSize; ++iSample) {
            this._audioBuffer[iSample] = (float)Math.sin(this._soundPhase);
            this.updateSoundPhase();
        }
    }

    private void writeAudioOutput() {
        this._audioTrack.write(this._audioBuffer, 0, this._audioBufferSize, AudioTrack.WRITE_BLOCKING);
    }

    private void updateSoundPhase() {
        this._soundPhase += 2f * Math.PI * this._soundFrequency / this._deviceAudioFrameRate;
    }

    private void initAudioTrack(int bufferSize) {
        this._audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC             ,
                this.deviceAudioFrameRate()          ,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_FLOAT        ,
                bufferSize, AudioTrack.MODE_STREAM
        );
    }

    private int deviceAudioFrameRate() {
        AudioManager audioManager = (AudioManager)this._context.getSystemService(Context.AUDIO_SERVICE);
        return Integer.parseInt(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
    }
}

