package com.example.demo2.utils;

import java.util.HashMap;

import static java.lang.Math.*;
import static java.lang.Math.log;

public class Utility {
    public static void handleProcedure(Procedure procedure, boolean printStackTrace) {
        try {
            procedure.invoke();
        } catch (Exception e) {

            if (printStackTrace) {
                e.printStackTrace();
            }
        }
    }

    public static class Math {
        public static double offsetTone(double baseFrequency, double frequencyMultiplier) {
            return baseFrequency * pow(2.0, frequencyMultiplier);
        }

        public static double frequencyToAngularFrequency(double freq) {
            return 2 * PI * freq;
        }

        public static double getKeyFrequency(int keyNum) {
            return pow(root(2, 12), keyNum - 49) * 440;
        }

        public static double root(double num, double root) {
            return pow(E, log(num) / root);
        }
    }

    public static class AudioInfo {
        public static final int SAMPLE_RATE = 44100;
        public static final int STARTING_KEY = 16;
        public static final int KEY_FREQUENCY_INCREMENT = 2;
        public static final char[] KEYS = "zxcvbnm,./asdfghjkl;'qwertyuiop[]#".toCharArray();
    }

    public enum Waveform {
        Sine, Square, Saw, Triangle;

        static final int SIZE = 8192;

        private final float[] samples = new float[SIZE];


        static {
            final double FUND_FREQ = 1/(SIZE / (double) Utility.AudioInfo.SAMPLE_RATE); // freq of each wave to produce a complete wave of the desired size
            for (int i = 0; i < SIZE; i++) {
                double t = i / (double) Utility.AudioInfo.SAMPLE_RATE;
                double tDivP = (1d / FUND_FREQ);
                Sine.samples[i] = (float) java.lang.Math.sin(Utility.Math.frequencyToAngularFrequency(FUND_FREQ) * t);
                Square.samples[i] = java.lang.Math.signum(Sine.samples[i]);
                Saw.samples[i] = (float) (2d * (tDivP - java.lang.Math.floor(0.5 + tDivP)));
                Triangle.samples[i] = (float) (2d * java.lang.Math.abs(Saw.samples[i]) - 1d);
            }
        }

        float[] getSamples() {
            return samples;
        }
    }

}

