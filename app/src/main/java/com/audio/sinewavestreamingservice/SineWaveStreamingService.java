package com.audio.sinewavestreamingservice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

class SineWaveStreamingService {
    private AudioTrack _audioTrack;
    private Context _context;
    private int _audioBufferSize;
    private float _soundFrequency;
    private float _soundPhase;
    private float _deviceAudioFrameRate;
    private int _buffersWrittenCount;
    private float[][] _alternateAudioBuffers;
    private int _writtenBufferIndex;


    public SineWaveStreamingService(Context context, float soundFrequencyInHz, int audioBufferSize, float soundLengthInSeconds) {
        this._context = context;
        this._audioBufferSize = audioBufferSize;
        this._soundFrequency = soundFrequencyInHz;
        this._deviceAudioFrameRate = (float) this.deviceAudioFrameRate();
        this._alternateAudioBuffers = new float[2][audioBufferSize];
        this._writtenBufferIndex = 0;
        this._buffersWrittenCount = (int) (soundLengthInSeconds * (float) this.deviceAudioFrameRate() / (float) this._audioBufferSize);
        this.initAudioTrack(this._audioBufferSize);
    }

    public void play() {
        this._audioTrack.play();
        for(int bufferIndex = 0; bufferIndex < this._buffersWrittenCount; ++bufferIndex) {
            this.updateNextAudioBuffer();
            this.writeCurrentAudioOutput();
            this.switchAudioBuffers();
        }
        this.stopAndRelease();
    }

    private void stopAndRelease() {
        this._audioTrack.stop();
        this._audioTrack.release();
    }

    private void updateNextAudioBuffer() {
        float[] updatedBuffer = this.updatedBuffer();
        for (int frameIndex = 0; frameIndex < this._audioBufferSize; ++frameIndex) {
            updatedBuffer[frameIndex] = (float) Math.sin(this._soundPhase);
            this.updateSoundPhase();
        }
    }

    private int updatedBufferIndex() {
        if(this._writtenBufferIndex == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    private void writeCurrentAudioOutput() {
        this._audioTrack.write(
            this.writtenBuffer(), 0, this._audioBufferSize, AudioTrack.WRITE_BLOCKING
        );
    }

    private float[] writtenBuffer() {
        int bufferId = this.writtenBufferIndex();
        return this._alternateAudioBuffers[bufferId];
    }

    private float[] updatedBuffer() {
        int bufferId = this.updatedBufferIndex();
        return this._alternateAudioBuffers[bufferId];
    }

    private int writtenBufferIndex() {
        return this._writtenBufferIndex;
    }

    private void switchAudioBuffers() {
        if (this._writtenBufferIndex == 0) {
            this._writtenBufferIndex = 1;
        } else {
            this._writtenBufferIndex = 0;
        }
    }

    private void updateSoundPhase() {
        this._soundPhase += 2f * Math.PI * this._soundFrequency / this._deviceAudioFrameRate;
    }

    private void initAudioTrack(int bufferSize) {
        this._audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                this.deviceAudioFrameRate(),
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize, AudioTrack.MODE_STREAM
        );
    }

    private int deviceAudioFrameRate() {
        AudioManager audioManager = (AudioManager) this._context.getSystemService(Context.AUDIO_SERVICE);
        return Integer.parseInt(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
    }
}


