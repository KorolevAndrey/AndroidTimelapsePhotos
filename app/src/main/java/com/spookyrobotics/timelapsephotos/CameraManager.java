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
    private Camera mCamera;
    private Preview mPreview;
    private Storage mStorage;

    public CameraManager(Camera camera, Preview preview, Storage storage) {
        mCamera = camera;
        mPreview = preview;
        mStorage = storage;
    }


    public void startCamera(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
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
                try {
                    mCamera.setPreviewDisplay(mPreview.mHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();

            }
        });
        t.start();
    }

    public void takePicture(final Context context) {
        if(mCamera == null){
            return;
        }
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
                        stopPreviewAndFreeCamera();
                    }
                }
        );
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
