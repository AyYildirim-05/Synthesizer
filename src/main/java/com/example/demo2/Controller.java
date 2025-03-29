package com.example.demo2;

import com.example.demo2.utils.Utility;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;



public class Controller {
    private AudioThread auTh;
    private Scene tempScene;
    private Stage tempStage;

    private final Map<MenuButton, String> waveformSelection = new HashMap<>();
    public static final HashMap<Character, Double> KEY_FREQUENCIES = new HashMap<>();

    private final double[] oscillatorFrequencies = new double[3];

    String txt1 = "Sine", txt2 = "Sine", txt3 = "Sine";
    @FXML
    private MenuButton functionChooser1, functionChooser2, functionChooser3;
    @FXML
    private Slider tone1, tone2, tone3, volume1, volume2, volume3;
    @FXML
    private Canvas waveformCanvas;
    private boolean shouldGenerate;
    private int wavePos;
    private Utility.Waveform waveform = Utility.Waveform.Sine;

    private final int NORMALIZER = 6;


    @FXML
    public void initialize() {
        setupMenu(functionChooser1);
        setupMenu(functionChooser2);
        setupMenu(functionChooser3);

        setDefaultMenuSelection(functionChooser1, "Sine");
        setDefaultMenuSelection(functionChooser2, "Sine");
        setDefaultMenuSelection(functionChooser3, "Sine");

        sliderSetUp(tone1,2);
        sliderSetUp(tone2,2);
        sliderSetUp(tone3,2);
        sliderSetUp(volume1,1);
        sliderSetUp(volume2,1);
        sliderSetUp(volume3,1);

        updateSlider(tone1, 0, true);
        updateSlider(tone2, 1, true);
        updateSlider(tone3, 2, true);

        updateSlider(volume1, 0, false);
        updateSlider(volume2, 1, false);
        updateSlider(volume3, 2, false);

        final AudioThread audioThread = new AudioThread(() -> {
            if (!shouldGenerate) {
                return null;
            }
            short[] s = new short[AudioThread.BUFFER_SIZE];
            for (int i = 0; i < AudioThread.BUFFER_SIZE; i++) {
                double mixedSample = 0;

                mixedSample += generateWaveSample(txt1, oscillatorFrequencies[0], wavePos) / NORMALIZER;
                mixedSample += generateWaveSample(txt2, oscillatorFrequencies[1], wavePos) / NORMALIZER;
                mixedSample += generateWaveSample(txt3, oscillatorFrequencies[2], wavePos) / NORMALIZER;

                s[i] = (short) (Short.MAX_VALUE * mixedSample);
                wavePos++;
            }
            return s;
        });
        this.auTh = audioThread;

        for (int i = Utility.AudioInfo.STARTING_KEY, key = 0; i < (Utility.AudioInfo.KEYS).length * Utility.AudioInfo.KEY_FREQUENCY_INCREMENT + Utility.AudioInfo.STARTING_KEY; i += Utility.AudioInfo.KEY_FREQUENCY_INCREMENT, ++key) {
            KEY_FREQUENCIES.put(Utility.AudioInfo.KEYS[key], Utility.Math.getKeyFrequency(i));
        }
    }

    private double generateWaveSample(String waveformType, double frequency, int wavePosition) {
        double tDivP = (wavePosition / (double) Utility.AudioInfo.SAMPLE_RATE) / (1d / frequency);

        switch (waveformType) {
            case "Sine":
                return Math.sin(Utility.Math.frequencyToAngularFrequency(frequency) * wavePosition / Utility.AudioInfo.SAMPLE_RATE);
            case "Square":
                return Math.signum(Math.sin(Utility.Math.frequencyToAngularFrequency(frequency) * wavePosition / Utility.AudioInfo.SAMPLE_RATE));
            case "Saw":
                return 2d * (tDivP - Math.floor(0.5 + tDivP));
            case "Triangle":
                return 2d * Math.abs(2d * (tDivP - Math.floor(0.5 + tDivP))) - 1;
            default:
                throw new RuntimeException("Oscillator is set to unkown waveform");
        }
    }


    private void setupKeyboardListeners() {
        if (tempScene == null) {
            System.err.println("tempScene is not set, cannot set up keyboard listeners");
            return;
        }

        tempScene.setOnKeyPressed(event -> {
            if (!auTh.isRunning()) {
                char key = event.getText().isEmpty() ? '\0' : event.getText().charAt(0);
                if (KEY_FREQUENCIES.containsKey(key)) {
                    double frequency = KEY_FREQUENCIES.get(key); // get frequency from the map

                    oscillatorFrequencies[0] = Utility.Math.offsetTone(frequency, tone1.getValue());
                    oscillatorFrequencies[1] = Utility.Math.offsetTone(frequency, tone2.getValue());
                    oscillatorFrequencies[2] = Utility.Math.offsetTone(frequency, tone3.getValue());
                    System.out.println("initial" + frequency);

                    shouldGenerate = true;
                    auTh.triggerPlayback();
                }
            }
        });

        tempScene.setOnKeyReleased(event -> {
            if (tempScene.getOnKeyPressed() != null) {
                shouldGenerate = false;
            }
        });
    }

    private void updateSlider(Slider slider, int index, boolean isToneSlider) {
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("Slider " + index + " moved to: " + newValue.doubleValue());

            // Update the oscillator frequencies immediately
            if (isToneSlider && tempScene != null) {
                tempScene.setOnKeyPressed(event -> {
                    char key = event.getText().isEmpty() ? '\0' : event.getText().charAt(0);
                    if (KEY_FREQUENCIES.containsKey(key)) {
                        double frequency = KEY_FREQUENCIES.get(key);
                        System.out.println("new" + frequency);
                        oscillatorFrequencies[index] = Utility.Math.offsetTone(frequency, newValue.doubleValue());
                        System.out.println("the new frequency is being called");
                    }
//                    else {
//                        //todo audio increase here
//                    }
                });
            }
        });
    }

    private void closeApplication() {
        if (tempStage == null) {
            System.err.println("tempStage is not set, cannot close application");
            return;
        }

        tempStage.setOnCloseRequest(event -> {
            event.consume();
            auTh.close();
            tempStage.close();
        });
    }

    private void sliderSetUp(Slider slider, int border) {
        slider.setMin(-border);
        slider.setMax(border);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        if (border == 1) {
            slider.setValue(1);
        } else {
            slider.setValue(0);
        }
    }



    private void setupMenu(MenuButton menuButton) {
        for (MenuItem item : menuButton.getItems()) {
            item.setOnAction(event -> {
                menuButton.setText(item.getText());
                waveformSelection.put(menuButton, item.getText());
                if (menuButton == functionChooser1) {
                    txt1 = item.getText();
                } else if (menuButton == functionChooser2) {
                    txt2 = item.getText();
                } else if (menuButton == functionChooser3) {
                    txt3 = item.getText();
                }
                System.out.println(txt1 + " . " + txt2 + " . " + txt3 + " . ");
            });
        }
    }

    private void setDefaultMenuSelection(MenuButton menuButton, String defaultText) {
        for (MenuItem item : menuButton.getItems()) {
            if (item.getText().equals(defaultText)) {
                menuButton.setText(item.getText());
                break;
            }
        }
    }

    public void setScene(Scene scene) {
        this.tempScene = scene;
        System.out.println("temp sce:" + tempScene);
        setupKeyboardListeners();
    }

    public void setStage(Stage stage) {
        this.tempStage = stage;
        System.out.println("temp sta:" + tempStage);
        closeApplication();
    }
}

