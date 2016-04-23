package com.spookyrobotics.timelapsephotos;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.spookyrobotics.timelapsephotos.functional.Function;

import java.io.File;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private Button mStartButton;
    private TextView mLastPhotoText;

    private CameraManager mCameraManager;
    private BroadcastReceiver mTakePhotoReceiver;
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
        mStartButton.setOnClickListener(getStartCameraClickListener(new Function<Context, Intent>() {
            @Override
            public void call(Context context, Intent intent) {
                takePicture(MainActivity.this);
            }
        }));
    }

    private void setupDisablePicturesButton() {
        mStartButton.setText("Picture Taking in Progress");
        mStartButton.setOnClickListener(getStopCameraClickListener());
    }

    private View.OnClickListener getStartCameraClickListener(final Function<Context, Intent> function) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getApplicationContext() == null) {
                    return;
                }
                setupDisablePicturesButton();
                Scheduler.scheduleRepeatingPicture(getApplicationContext(), getPackageName(), function);
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
        setupStartPicturesButton();
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
        Scheduler.cancelPictureIntent(getApplicationContext());
        super.onPause();
    }

}
