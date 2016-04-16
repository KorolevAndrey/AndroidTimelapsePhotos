package com.spookyrobotics.timelapsephotos;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final long ONE_MINUTE = 1000 * 60;
    private Preview mPreview;
    private Camera mCamera;
    private ImageView mImageView;
    private Storage mStorage;
    private Button mStartButton;
    private int mPhotoCount = -1;
    private TextView mLastPhotoText;

    private final Runnable mFutureClick = new Runnable() {
        @Override
        public void run() {
            takePicture();
            startClickLoop();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreview = (Preview) findViewById(R.id.camera_preview);
        mImageView = (ImageView) findViewById(R.id.last_photo);
        mStartButton = (Button) findViewById(R.id.start);
        mLastPhotoText = (TextView) findViewById(R.id.last_photo_time);
        mLastPhotoText.setText("No photo taken");
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mStorage = new Storage(storageDirectory);
        setOnClickListeners();
    }


    private void startClickLoop() {
        Handler mainThread = new Handler(getMainLooper());
        mainThread.postDelayed(mFutureClick, ONE_MINUTE);
    }

    private void setOnClickListeners() {
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
                mStartButton.setText("Take picture");
                mStartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        takePicture();
                    }
                });
            }
        });
    }

    private void takePicture() {
        mCamera.takePicture(
                new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                    }
                },
                new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {

                    }
                },
                new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        mPhotoCount += 1;
                        String pictureName = getPictureName();
                        mStorage.saveFile(MainActivity.this, pictureName, bytes);
                        setLastPhotoText(pictureName);
                        mCamera.startPreview();
                    }
                }
        );
    }

    private void setLastPhotoText(final String pictureName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLastPhotoText.setText(pictureName);
            }
        });
    }

    private String getPictureName() {
        GregorianCalendar calendar = new GregorianCalendar();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH_mm", Locale.US);
        return formatter.format(calendar.getTime())+".jpg";
    }

    private void start(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mCamera = Camera.open();
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setPreviewSize(mPreview.getWidth(), mPreview.getHeight());
                    parameters.set("orientation", "portrait");
                    parameters.setRotation(90);
                    mCamera.setParameters(parameters);
                    mCamera.setDisplayOrientation(90);
                    mPreview.setSurfaceDestroyedListener(new SurfaceDestroyedListener(){
                        @Override
                        public void onSurfaceDestroyed() {
                            stopPreviewAndFreeCamera();
                        }
                    });
                    mCamera.setPreviewDisplay(mPreview.mHolder);
                    mCamera.startPreview();
                    startClickLoop();
                } catch (Exception e){
                    Log.e(TAG,"Failed to open camera: "+e);
                }
            }
        });
        t.start();
    }

    @Override
    public void onPause(){
        Handler mainThread = new Handler(getMainLooper());
        mainThread.removeCallbacks(mFutureClick);
        stopPreviewAndFreeCamera();
        super.onPause();
    }

    private void stopPreviewAndFreeCamera() {
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }



}
