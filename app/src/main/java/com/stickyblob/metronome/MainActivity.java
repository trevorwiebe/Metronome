package com.stickyblob.metronome;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private boolean toggle = false;
    private long timeNow;
    private long timeOld = 0;
    private double totalSeconds;
    private double averageSeconds;
    private int divider = 1;
    boolean shouldSet = false;

    private TextView mTimeTv;
    private TextView mAverageTimeTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimeTv = (TextView) findViewById(R.id.timeTv);
        mAverageTimeTv = (TextView) findViewById(R.id.averageTimeTv);

    }

    public void tapToRhythm(View view) {

        long bpm = testTime();
        mTimeTv.setText(Long.toString(bpm));
    }

    private void toggleFlashLight() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
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
    }

    private long testTime() {
        // get current time in millis
        timeNow = System.currentTimeMillis();
        if(shouldSet) {
            long rawTimeDif = timeNow - timeOld;
            double seconds = rawTimeDif / 1000.00;
            totalSeconds = totalSeconds + seconds;
            averageSeconds = totalSeconds / divider;
            divider++;
        }
        shouldSet = true;
        timeOld = timeNow;
        double bpm = (double) 60 / averageSeconds;
        return Math.round(bpm);
    }
}

