package com.spookyrobotics.timelapsephotos;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private Button mStartButton;
    private int mPhotoCount = -1;
    private TextView mLastPhotoText;

    private CameraManager mCameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartButton = (Button) findViewById(R.id.start);
        mLastPhotoText = (TextView) findViewById(R.id.last_photo_time);
        setLastPhotoText("No photo taken");
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        mStartButton.setText("Start Pictures");
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Camera camera = Camera.open();
                    Preview preview = (Preview) findViewById(R.id.camera_preview);
                    ImageView imageView = (ImageView) findViewById(R.id.last_photo);
                    File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    Storage storage = new Storage(storageDirectory);
                    mCameraManager = new CameraManager(camera, preview, storage);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open camera");
                    return;
                }
                mCameraManager.startCamera();
                mStartButton.setText("Picture Taking in Progress");
                mStartButton.setClickable(false);
                mStartButton.setOnClickListener(null);
                if (getApplicationContext() == null) {
                    return;
                }
                Scheduler.scheduleRepeatingPicture(getApplicationContext(), getPackageName(), new Action<Context>() {
                    @Override
                    public void run(Context context) {
                        takePicture(context);
                    }
                });
            }
        });
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
        Scheduler.cancelPictureIntent(getApplicationContext(), getPackageName());
        super.onPause();
    }

}
