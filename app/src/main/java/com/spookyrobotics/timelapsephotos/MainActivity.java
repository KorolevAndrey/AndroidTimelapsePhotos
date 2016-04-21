package com.spookyrobotics.timelapsephotos;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private Button mStartButton;
    private TextView mLastPhotoText;

    private CameraManager mCameraManager;
    private BroadcastReceiver mTakePhotoReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartButton = (Button) findViewById(R.id.start);
        mLastPhotoText = (TextView) findViewById(R.id.last_photo_time);
        setLastPhotoText("No photo taken");
        mTakePhotoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                takePicture(MainActivity.this);
            }
        };
    }

    private void setupStartPicturesButton() {
        mStartButton.setText("Start Pictures");
        mStartButton.setOnClickListener(getStartCameraClickListener());
    }

    private void setupDisablePicturesButton() {
        mStartButton.setText("Picture Taking in Progress");
        mStartButton.setOnClickListener(getStopCameraClickListener());
    }

    private View.OnClickListener getStartCameraClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getApplicationContext() == null) {
                    return;
                }
                setupDisablePicturesButton();
                Scheduler.scheduleRepeatingPicture(getApplicationContext(), getPackageName());
            }
        };
    }

    private View.OnClickListener getStopCameraClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getApplicationContext() == null) {
                    return;
                }
                setupStartPicturesButton();
                Scheduler.cancelPictureIntent(getApplicationContext());
            }
        };
    }

    @Override
    public void onResume(){
        super.onResume();
        Preview preview = (Preview) findViewById(R.id.camera_preview);
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Storage storage = new Storage(storageDirectory);
        mCameraManager = CameraManager.getInstance(mCamera, preview, storage);
        registerTakePhotoReceiver();
        setupStartPicturesButton();
    }

    private void registerTakePhotoReceiver() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        mLocalBroadcastManager.registerReceiver(mTakePhotoReceiver, Scheduler.getLocalTakePictureIntentFilter());
    }

    private void takePicture(Context context) {
        mCameraManager.takePicture(context);
        setLastPhotoText(mCameraManager.getPictureName());
    }


    private void setLastPhotoText(final String pictureName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLastPhotoText.setText(pictureName);
            }
        });
    }

    @Override
    public void onPause(){
        mCameraManager.stopPreviewAndFreeCamera();
        mLocalBroadcastManager.unregisterReceiver(mTakePhotoReceiver);
        Scheduler.cancelPictureIntent(getApplicationContext());
        super.onPause();
    }

}
