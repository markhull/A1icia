package com.hulles.alixia.delta.deepspeech;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.hulles.alixia.delta.deepspeech.swig.Metadata;

public class BasicTest {
    private final static String MODELDIR = "/home/hulles/Alixia_Exec/Deepspeech/models/";
    private final static String AUDIODIR = "/home/hulles/Alixia_Exec/Deepspeech/audio/";
    private static final String modelFile = MODELDIR + "output_graph.pbmm";
    private static final String lmFile = MODELDIR + "lm.binary";
    private static final String trieFile = MODELDIR + "trie";
    private static final String wavFile = AUDIODIR + "4507-16021-0012.wav";
    private static final int BEAM_WIDTH = 50;
    private static final float LM_ALPHA = 0.75f;
    private static final float LM_BETA  = 1.85f;

    private static char readLEChar(RandomAccessFile f) throws IOException {
        byte b1 = f.readByte();
        byte b2 = f.readByte();
        return (char)((b2 << 8) | b1);
    }

    private static int readLEInt(RandomAccessFile f) throws IOException {
        byte b1 = f.readByte();
        byte b2 = f.readByte();
        byte b3 = f.readByte();
        byte b4 = f.readByte();
        return ((b1 & 0xFF) | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24);
    }

     public static void loadDeepSpeech_basic() {
        DeepSpeechModel m = new DeepSpeechModel(modelFile, BEAM_WIDTH);
        m.freeModel();
    }

    private static String doSTT(DeepSpeechModel m, boolean extendedMetadata) {
        
        try (RandomAccessFile wave = new RandomAccessFile(wavFile, "r")) {

            wave.seek(20); char audioFormat = BasicTest.readLEChar(wave);
            assert (audioFormat == 1); // 1 is PCM

            wave.seek(22); char numChannels = BasicTest.readLEChar(wave);
            assert (numChannels == 1); // MONO

            wave.seek(24); int sampleRate = BasicTest.readLEInt(wave);
            assert (sampleRate == 16000); // 16000 Hz

            wave.seek(34); char bitsPerSample = BasicTest.readLEChar(wave);
            assert (bitsPerSample == 16); // 16 bits per sample

            wave.seek(40); int bufferSize = BasicTest.readLEInt(wave);
            assert (bufferSize > 0);

            wave.seek(44);
            byte[] bytes = new byte[bufferSize];
            wave.readFully(bytes);

            short[] shorts = new short[bytes.length/2];
            // to turn bytes to shorts as either big endian or little endian.
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

            if (extendedMetadata) {
                return metadataToString(m.sttWithMetadata(shorts, shorts.length));
            }
            return m.stt(shorts, shorts.length);
            
        } catch (FileNotFoundException ex) {

        } catch (IOException ex) {

        } finally {

        }

        return "";
    }

    private static String metadataToString(Metadata m) {
        String retval = "";
        for (int i = 0; i < m.getNum_items(); ++i) {
            retval += m.getItem(i).getCharacter();
        }
        return retval;
    }

    public static void loadDeepSpeech_stt_noLM() {
        DeepSpeechModel m = new DeepSpeechModel(modelFile, BEAM_WIDTH);

        @SuppressWarnings("unused")
        String decoded = doSTT(m, false);
//        assertEquals("she had your dark suit in greasy wash water all year", decoded);
        m.freeModel();
    }

    public static void loadDeepSpeech_stt_withLM() {
        DeepSpeechModel m = new DeepSpeechModel(modelFile, BEAM_WIDTH);
        m.enableDecoderWithLM(lmFile, trieFile, LM_ALPHA, LM_BETA);

        @SuppressWarnings("unused")
        String decoded = doSTT(m, false);
//        assertEquals("she had your dark suit in greasy wash water all year", decoded);
        m.freeModel();
    }

    public static void loadDeepSpeech_sttWithMetadata_noLM() {
        DeepSpeechModel m = new DeepSpeechModel(modelFile, BEAM_WIDTH);

        @SuppressWarnings("unused")
        String decoded = doSTT(m, true);
//        assertEquals("she had your dark suit in greasy wash water all year", decoded);
        m.freeModel();
    }

    public static void loadDeepSpeech_sttWithMetadata_withLM() {
        DeepSpeechModel m = new DeepSpeechModel(modelFile, BEAM_WIDTH);
        m.enableDecoderWithLM(lmFile, trieFile, LM_ALPHA, LM_BETA);

        @SuppressWarnings("unused")
        String decoded = doSTT(m, true);
//        assertEquals("she had your dark suit in greasy wash water all year", decoded);
        m.freeModel();
    }

    public static void main(String[] args) {
        
        loadDeepSpeech_basic();
    }
}
