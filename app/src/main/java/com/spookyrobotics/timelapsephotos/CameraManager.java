package com.spookyrobotics.timelapsephotos;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CameraManager {
    private static final String TAG = "CameraManager";
    private static CameraManager sInstance;
    private Camera mCamera;
    private Preview mPreview;
    private Storage mStorage;

    public static CameraManager getInstance(Camera camera, Preview preview, Storage storage){
        if(sInstance == null){
            sInstance = new CameraManager(camera, preview, storage);
        }
        return sInstance;
    }

    private CameraManager(Camera camera, Preview preview, Storage storage) {
        mCamera = camera;
        mPreview = preview;
        if(mPreview == null){
            throw new NullPointerException("Preview cannot be null");
        }
        mStorage = storage;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            throw new IllegalAccessError("Failed to open camera: "+e);
        }

    }


    private void startCamera(final Context context){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(mPreview.getWidth(), mPreview.getHeight());
                parameters.set("orientation", "portrait");
                parameters.setRotation(90);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(parameters);
                mCamera.setDisplayOrientation(90);
                try {
                    mCamera.setPreviewDisplay(mPreview.mHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
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
                                String pictureName = getPictureName();
                                mStorage.saveFile(context, pictureName, bytes);
                            }
                        }
                );

            }
        });
        t.start();
    }

    public void takePicture(final Context context) {
        if(mCamera == null){
            return;
        }
        startCamera(context);

    }

    public void stopPreviewAndFreeCamera() {
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

    public String getPictureName() {
        GregorianCalendar calendar = new GregorianCalendar();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH_mm", Locale.US);
        return formatter.format(calendar.getTime())+".jpg";
    }
}
