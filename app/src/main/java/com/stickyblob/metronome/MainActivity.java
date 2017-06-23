package com.stickyblob.metronome;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private boolean toggle = true;
    private long timeNow;
    private long timeOld = 0;
    private double totalSeconds;
    private double averageSeconds;
    private int divider = 1;
    boolean shouldSet = false;
    private double bpm;


    private EditText mBPMinuteEditText;
    private EditText mBPMeasureEditText;
    private Button mTapToRhythmBtn;

    Vibrator mVibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mBPMinuteEditText = (EditText) findViewById(R.id.bp_minute_et);
        mBPMeasureEditText = (EditText) findViewById(R.id.bp_measure_et);
        mTapToRhythmBtn = (Button) findViewById(R.id.tap_to_rhythm_btn);

        mTapToRhythmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long bpm = testTime();
                if (bpm > 400) {
                    bpm = 0;
                }
                mBPMinuteEditText.setText(Long.toString(bpm));
            }
        });

        mTapToRhythmBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                resetBeatsPerMinute();
                mVibrator.vibrate(25);
                return false;
            }
        });
    }

    // Button Callbacks
    public void plusBPMinuteBtnClicked(View view) {
        long number = Long.parseLong(mBPMinuteEditText.getText().toString());
        long number2 = number + 1;
        mBPMinuteEditText.setText(Long.toString(number2));
    }

    public void minusBPMinuteBtnClicked(View view) {
        long number = Long.parseLong(mBPMinuteEditText.getText().toString());
        if (number == 0) {
            return;
        }
        long number2 = number - 1;
        mBPMinuteEditText.setText(Long.toString(number2));
    }

    public void minusBPMeasureBtnClicked(View view) {

    }

    public void plusBPMeasureBtnClicked(View view) {

    }

    public void startBtnClicked(View view) {
        resetBeatsPerMinute();
        final int delay = 100;
        final Handler h = new Handler();

        h.postDelayed(new Runnable() {
            public void run() {
                toggle = true;
                toggleFlashLight(2);
                Log.d(TAG, "run: here");
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    public void stopBtnClicked(View view) {

    }
    // end of button callbacks


    @TargetApi(Build.VERSION_CODES.M)
    private void toggleFlashLight(int timeOn) {
        toggle = !toggle;
        try {
            CameraManager cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);

            for (String id : cameraManager.getCameraIdList()) {
                if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                    cameraManager.setTorchMode(id, toggle);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "toggleFlashLight: Cannot turn on flashlight", e);
        }
    }

    private long testTime() {
        // get current time in millis
        timeNow = System.currentTimeMillis();
        if (shouldSet) {
            long rawTimeDif = timeNow - timeOld;
            double seconds = rawTimeDif / 1000.00;
            totalSeconds = totalSeconds + seconds;
            averageSeconds = totalSeconds / divider;
            divider++;
        }
        shouldSet = true;
        timeOld = timeNow;
        bpm = (double) 60 / averageSeconds;
        return Math.round(bpm);
    }

    private void resetBeatsPerMinute() {
        divider = 0;
        totalSeconds = 0;
        timeOld = 0;

        shouldSet = false;
    }
}

