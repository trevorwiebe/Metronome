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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.ogg.OggExtractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Handler mHandler = new Handler();
    Runnable mRunnable;
    Handler mFlashLightHandler = new Handler();
    Runnable mFlashLightRunnable;
    SimpleExoPlayer mExoPlayer;

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
    private Button mStartBtn;
    private Button mStopBtn;

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
        mStartBtn = (Button) findViewById(R.id.go_btn);
        mStopBtn = (Button) findViewById(R.id.stop_btn);

        mBeatsInMeasure.setText("0");

        mTapToRhythmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long bpm = updateBPM();
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
                beatInMeasure++;
                if(beatInMeasure == 1){
                    playAudio(R.raw.bubble_accent);
                }else{
                    playAudio(R.raw.bubble);
                }
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
                if(beatInMeasure >= Integer.parseInt(mBPMeasureEditText.getText().toString())){
                    beatInMeasure = 0;
                }
                mVibrator.vibrate(notification_time);
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

        setStartBtn();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mFlashLightHandler.removeCallbacksAndMessages(null);
        releasePlayer();
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
    // end of button callbacks

    private long updateBPM() {
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

    private void playAudio(int soundFile) {
        // Create an instance of the ExoPlayer.
        releasePlayer();
        TrackSelector trackSelector = new DefaultTrackSelector();
        LoadControl loadControl = new DefaultLoadControl();
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

        DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(soundFile));
        final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(this);
        try {
            rawResourceDataSource.open(dataSpec);
        } catch (Exception e) {
            Log.e(TAG, "playAudio2: ", e);
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return rawResourceDataSource;
            }
        };

        MediaSource audioSource = new ExtractorMediaSource(rawResourceDataSource.getUri(), factory, OggExtractor.FACTORY, null, null);

        mExoPlayer.prepare(audioSource);

        mExoPlayer.setPlayWhenReady(true);
    }

    private void releasePlayer(){
        if(mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    private void setStartBtn(){
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPauseBtn();
                mStartBtn.setVisibility(View.INVISIBLE);
                mStopBtn.setVisibility(View.VISIBLE);
                resetBeatsPerMinute();
                if(mBPMinuteEditText.getText().toString().equals("0")){
                    return;
                }else if(mBPMinuteEditText.getText().toString().equals("")){
                    return;
                }
                mHandler.postDelayed(mRunnable, delay);
            }
        });
    }

    private void setPauseBtn(){
        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStartBtn();
                mStopBtn.setVisibility(View.INVISIBLE);
                mStartBtn.setVisibility(View.VISIBLE);
                mHandler.removeCallbacksAndMessages(null);
                mFlashLightHandler.removeCallbacksAndMessages(null);
                releasePlayer();
            }
        });
    }
}
