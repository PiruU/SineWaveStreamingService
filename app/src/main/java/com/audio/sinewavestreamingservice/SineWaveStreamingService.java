package com.audio.sinewavestreamingservice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

class SineWaveStreamingService {
    private AudioTrack _audioTrack;
    private Context _context;
    private int _audioBufferSize;
    private float _soundFrequency;
    private float _soundPhase;
    private float _deviceAudioFrameRate;
    private int _buffersWrittenCount;
    private int _writtenBufferIndex;


    public LinkedBlockingDeque<float[]> _bufferQueue;


    public SineWaveStreamingService(Context context, float soundFrequencyInHz, int audioBufferSize, float soundLengthInSeconds) {
        this._context = context;
        this._audioBufferSize = audioBufferSize;
        this._soundFrequency = soundFrequencyInHz;
        this._deviceAudioFrameRate = (float) this.deviceAudioFrameRate();
        this._buffersWrittenCount = (int) (soundLengthInSeconds * (float) this.deviceAudioFrameRate() / (float) this._audioBufferSize);
        this.initAudioTrack(this._audioBufferSize);

        this._bufferQueue = new LinkedBlockingDeque<>(10);
    }

    public void play() {
        this._audioTrack.play();

        Thread updater = new Thread(new UpdaterRunnable(this));
        Thread writer = new Thread(new WriterRunnable(this));

        updater.start();
        writer.start();

        try { updater.join(); } catch(Exception e) {}
        try { writer.join(); } catch(Exception e) {}

        this.stopAndRelease();
    }

    public int countBuffers() {
        return this._buffersWrittenCount;
    }

    private void stopAndRelease() {
        this._audioTrack.stop();
        this._audioTrack.release();
    }

    private void updateNextAudioBuffer() {
        float[] bufferToQueue = new float[this._audioBufferSize];

        for (int frameIndex = 0; frameIndex < this._audioBufferSize; ++frameIndex) {
            bufferToQueue[frameIndex] = (float) Math.sin(this._soundPhase);
            this.updateSoundPhase();
        }
        try {
            this._bufferQueue.put(bufferToQueue);
        } catch (Exception e) {

        }
    }

    private void writeCurrentAudioOutput()  throws RuntimeException {
        this._audioTrack.write(
                this.writtenBuffer(), 0, this._audioBufferSize, AudioTrack.WRITE_BLOCKING
        );
    }

    private float[] writtenBuffer() {
        long timeout = 10;
        try {
            return this._bufferQueue.poll(timeout, TimeUnit.SECONDS);
        } catch(Exception e) {
            throw new RuntimeException();
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

    private class UpdaterRunnable implements Runnable {
        private SineWaveStreamingService _streamingService;

        public UpdaterRunnable(SineWaveStreamingService streamingService) {
            this._streamingService = streamingService;
        }

        @Override
        public void run() {
            for(int iBuffer = 0; iBuffer < this._streamingService.countBuffers(); ++iBuffer) {
                this._streamingService.updateNextAudioBuffer();
            }
        }
    }

    private class WriterRunnable implements Runnable {
        private SineWaveStreamingService _streamingService;

        public WriterRunnable(SineWaveStreamingService streamingService) {
            this._streamingService = streamingService;
        }

        @Override
        public void run() {
            for(int iBuffer = 0; iBuffer < this._streamingService.countBuffers(); ++iBuffer) {
                try {
                    this.writeCurrentAudioOutput();
                } catch(Exception e) {
                    return;
                }
            }
        }

        private void writeCurrentAudioOutput() {
            this._streamingService.writeCurrentAudioOutput();
        }
    }


}



