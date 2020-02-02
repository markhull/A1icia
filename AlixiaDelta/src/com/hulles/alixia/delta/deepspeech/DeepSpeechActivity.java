package com.hulles.alixia.delta.deepspeech;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.audio.AudioBytePlayer;

public class DeepSpeechActivity {
    private final static Logger LOGGER = LoggerFactory.getLogger(DeepSpeechActivity.class);
    private DeepSpeechModel model;
    private final int BEAM_WIDTH = 50;
    @SuppressWarnings("unused")
    private final float LM_ALPHA = 0.75f;
    @SuppressWarnings("unused")
    private final float LM_BETA = 1.85f;

    private static char readLEChar(RandomAccessFile f) throws IOException {
        
        SharedUtils.checkNotNull(f);
        byte b1 = f.readByte();
        byte b2 = f.readByte();
        return (char)((b2 << 8) | b1);
    }

    private static int readLEInt(RandomAccessFile f) throws IOException {
        
        SharedUtils.checkNotNull(f);
        byte b1 = f.readByte();
        byte b2 = f.readByte();
        byte b3 = f.readByte();
        byte b4 = f.readByte();
        return ((b1 & 0xFF) | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24);
    }

    public void newModel(String modelName) {

        SharedUtils.checkNotNull(modelName);
        LOGGER.info("Creating model");
        model = new DeepSpeechModel(modelName, BEAM_WIDTH);
    }

    public String doInference(String audioFile) {
        long inferenceExecTime = 0;
        String decodedString;

        newModel("this should be the model name");
        LOGGER.info("Extracting audio features ...");

        try (RandomAccessFile wave = new RandomAccessFile(audioFile, "r")) {

            wave.seek(20); char audioFormat = DeepSpeechActivity.readLEChar(wave);
            assert (audioFormat == 1); // 1 is PCM
            // tv_audioFormat.setText("audioFormat=" + (audioFormat == 1 ? "PCM" : "!PCM"));

            wave.seek(22); char numChannels = DeepSpeechActivity.readLEChar(wave);
            assert (numChannels == 1); // MONO
            // tv_numChannels.setText("numChannels=" + (numChannels == 1 ? "MONO" : "!MONO"));

            wave.seek(24); int sampleRate = DeepSpeechActivity.readLEInt(wave);
            assert (sampleRate == model.sampleRate()); // desired sample rate
            // tv_sampleRate.setText("sampleRate=" + (sampleRate == 16000 ? "16kHz" : "!16kHz"));

            wave.seek(34); char bitsPerSample = DeepSpeechActivity.readLEChar(wave);
            assert (bitsPerSample == 16); // 16 bits per sample
            // tv_bitsPerSample.setText("bitsPerSample=" + (bitsPerSample == 16 ? "16-bits" : "!16-bits" ));

            wave.seek(40); int bufferSize = DeepSpeechActivity.readLEInt(wave);
            assert (bufferSize > 0);
            // tv_bufferSize.setText("bufferSize=" + bufferSize);

            wave.seek(44);
            byte[] bytes = new byte[bufferSize];
            wave.readFully(bytes);

            short[] shorts = new short[bytes.length/2];
            // to turn bytes to shorts as either big endian or little endian.
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

            LOGGER.info("Running inference ...");

            long inferenceStartTime = System.currentTimeMillis();

            decodedString = model.stt(shorts, shorts.length);

            inferenceExecTime = System.currentTimeMillis() - inferenceStartTime;

        } catch (FileNotFoundException ex) {
            throw new AlixiaException("File not found", ex);
        } catch (IOException ex) {
            throw new AlixiaException("I/O exception", ex);
        } finally {
            LOGGER.info("Finally...");
        }

        LOGGER.info("Finished! Took " + inferenceExecTime + "ms");
        return decodedString;
    }

    public static void playAudioFile(String audioFile) {
        
        try {
            AudioBytePlayer.playAudioFromFile(audioFile, null);
        } catch (Exception e) {
            throw new AlixiaException("Can't play audio file " + audioFile, e);
        }
    }
}
