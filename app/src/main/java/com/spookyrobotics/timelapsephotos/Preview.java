package com.spookyrobotics.timelapsephotos;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class Preview extends FrameLayout implements SurfaceHolder.Callback {

    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    private SurfaceDestroyedListener mSurfaceDestroyedListener = null;

    public Preview(Context context) {
        super(context);
        setup(context);
    }

    public Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public Preview(Context context, AttributeSet attributeSet, int style) {
        super(context, attributeSet, style);
        setup(context);
    }

    private void setup(Context context) {
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {;
        requestLayout();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(mSurfaceDestroyedListener != null){
            mSurfaceDestroyedListener.onSurfaceDestroyed();
        }
    }

    public void setSurfaceDestroyedListener(SurfaceDestroyedListener surfaceDestroyedListener) {
        mSurfaceDestroyedListener = surfaceDestroyedListener;
    }
}
