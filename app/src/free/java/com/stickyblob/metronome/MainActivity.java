package com.stickyblob.metronome;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Handler mHandler = new Handler();
    Runnable mRunnable;
    Handler mFlashLightHandler = new Handler();
    Runnable mFlashLightRunnable;
    MediaPlayer mMediaPlayer;

    private boolean toggle = false;
    private long timeNow;
    private long timeOld = 0;
    private double totalSeconds;
    private double averageSeconds;
    private int divider = 1;
    boolean shouldSet = false;
    private double bpm;
    private long delay = 0;
    private int beatInMeasure = 0;
    private long notification_time;


    private EditText mBPMinuteEditText;
    private EditText mBPMeasureEditText;
    private TextView mBeatsInMeasure;
    private Button mTapToRhythmBtn;

    Vibrator mVibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder adr = new AdRequest.Builder();
        adView.loadAd(adr.build());

        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mBPMinuteEditText = (EditText) findViewById(R.id.bp_minute_et);
        mBPMeasureEditText = (EditText) findViewById(R.id.bp_measure_et);
        mBeatsInMeasure = (TextView) findViewById(R.id.beat_in_measure_tv);
        mTapToRhythmBtn = (Button) findViewById(R.id.tap_to_rhythm_btn);

        String tick = Environment.getExternalStorageDirectory() + "/Metronome-6-22-2017/tickmp3";

        Log.d(TAG, "onCreate: " + tick);


        mBeatsInMeasure.setText("0");

        mTapToRhythmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long bpm = testTime();
                if (bpm > 500) {
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

        mFlashLightRunnable = new Runnable() {
            @Override
            public void run() {
                if(toggle){
                    turnFlashlightOn();
                    toggle = false;
                }else{
                    turnFlashlightOff();
                }
                mFlashLightHandler.postDelayed(mFlashLightRunnable, notification_time);
            }
        };

        mRunnable = new Runnable() {
            @Override
            public void run() {
//                String tick = Environment.getExternalStorageDirectory().getPath() + "/Metronome-6-22-2017/tickmp3";
//                try {
//                    mMediaPlayer = MediaPlayer.create(this, Uri.parse(tick));
//                }catch (Exception e){
//                    Log.e(TAG, "run: ", e);
//                }
                beatInMeasure++;
                mBeatsInMeasure.setText(Integer.toString(beatInMeasure));
                long milli_delay = Long.parseLong(mBPMinuteEditText.getText().toString());
                double some_num = 60.000 / milli_delay;
                double millis = some_num * 1000;
                delay = (int) Math.round(millis);
                notification_time = delay / 10;
                if(notification_time > 100){
                    notification_time = 100;
                }
                toggle = true;
                mFlashLightHandler.postDelayed(mFlashLightRunnable, notification_time);
                Log.d(TAG, "run: notifc time = " + notification_time);
                if(beatInMeasure >= Integer.parseInt(mBPMeasureEditText.getText().toString())){
                    beatInMeasure = 0;
                }
                mVibrator.vibrate(notification_time);
//                toggleFlashLight(2);
                mHandler.postDelayed(this, delay);
            }
        };

        mBPMinuteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    mHandler.removeCallbacksAndMessages(null);
                    delay = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
        mFlashLightHandler.removeCallbacksAndMessages(null);
    }

    // Button Callbacks
    public void plusBPMinuteBtnClicked(View view) {
        if (mBPMinuteEditText.getText().toString().equals("")) {
            mBPMinuteEditText.setText("1");
            return;
        }
        long number = Long.parseLong(mBPMinuteEditText.getText().toString());
        long number2 = number + 1;
        mBPMinuteEditText.setText(Long.toString(number2));
    }

    public void minusBPMinuteBtnClicked(View view) {
        if (mBPMinuteEditText.getText().toString().equals("")) {
            mBPMinuteEditText.setText("0");
            return;
        }
        long number = Long.parseLong(mBPMinuteEditText.getText().toString());
        if (number == 0) {
            return;
        }
        long number2 = number - 1;
        mBPMinuteEditText.setText(Long.toString(number2));
    }

    public void minusBPMeasureBtnClicked(View view) {
        if(mBPMeasureEditText.getText().toString().equals("")){
            mBPMeasureEditText.setText("0");
        }
        long number = Long.parseLong(mBPMeasureEditText.getText().toString());
        if (number == 0) {
            return;
        }
        long number2 = number - 1;
        mBPMeasureEditText.setText(Long.toString(number2));
    }

    public void plusBPMeasureBtnClicked(View view) {
        if(mBPMeasureEditText.getText().toString().equals("")){
            mBPMeasureEditText.setText("1");
            return;
        }
        long number = Long.parseLong(mBPMeasureEditText.getText().toString());
        long number2 = number + 1;
        mBPMeasureEditText.setText(Long.toString(number2));
    }

    public void startBtnClicked(View view) {
        resetBeatsPerMinute();
        if(mBPMinuteEditText.getText().toString().equals("0")){
            return;
        }else if(mBPMinuteEditText.getText().toString().equals("")){
            return;
        }
        mHandler.postDelayed(mRunnable, delay);
    }

    public void stopBtnClicked(View view) {
        mHandler.removeCallbacksAndMessages(null);
        mFlashLightHandler.removeCallbacksAndMessages(null);
    }
    // end of button callbacks



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

    @TargetApi(Build.VERSION_CODES.M)
    private void turnFlashlightOn(){
        try {
            CameraManager cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);

            for (String id : cameraManager.getCameraIdList()) {

                // Turn on the flash if camera has one
                if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {

                    cameraManager.setTorchMode(id, true);
                }
            }

        } catch (Exception e2) {
            Toast.makeText(getApplicationContext(), "Torch Failed: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void turnFlashlightOff(){
        try {
            CameraManager cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);

            for (String id : cameraManager.getCameraIdList()) {

                // Turn on the flash if camera has one
                if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {

                    cameraManager.setTorchMode(id, false);
                }
            }

        } catch (Exception e2) {
            Toast.makeText(getApplicationContext(), "Torch Failed: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
