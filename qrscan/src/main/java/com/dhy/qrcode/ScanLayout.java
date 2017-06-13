package com.dhy.qrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.google.zxing.CameraManager;
import com.google.zxing.CaptureActivityHandler;
import com.google.zxing.Result;

public class ScanLayout extends FrameLayout {
    private AttributeSet attrs;
    private Activity activity;
    private boolean hasSurface;
    private IFinderView finderView;
    private SurfaceView surfaceView;
    private CaptureActivityHandler handler;
    private ScanResultCallback scanResultCallback;

    public ScanLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        CameraManager.init(getContext());
    }

    void initView() {
        if (getChildCount() != 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view instanceof SurfaceView) {
                    surfaceView = (SurfaceView) view;
                } else if (view instanceof IFinderView) {
                    finderView = (IFinderView) view;
                }
            }
        }
        if (surfaceView == null) addView(surfaceView = new SurfaceView(getContext()), 0);
        if (finderView == null) addView((View) (finderView = new FinderView(getContext(), attrs)));
        attrs = null;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    public void onResume(Activity activity) {
        this.activity = activity;
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(callback);
        }
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (!hasSurface) {
                hasSurface = true;
                initCamera(holder);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            hasSurface = false;
        }
    };

    private void initCamera(SurfaceHolder surfaceHolder) {
        CameraManager.setDecodeRect(finderView.getDecodeRetangle());
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        initHandler();
    }

    private void initHandler() {
        if (handler == null) {
            handler = new CaptureActivityHandler(new IScanUtil() {
                @Override
                public Handler getHandler() {
                    return handler;
                }

                @Override
                public void handleDecode(Result result) {
                    if (scanResultCallback != null) {
                        boolean restart = scanResultCallback.handleDecode(result);
                        if (restart) handler.restartPreviewAndDecode();
                    }
                }

                @Override
                public void drawViewfinder() {
                    ((View) finderView).postInvalidate();
                }

                @Override
                public void startActivity(Intent intent) {
                    getContext().startActivity(intent);
                }

                @Override
                public void setResult(int result, Intent intent) {
                    if (activity != null) activity.setResult(result, intent);
                }
            }, finderView);
        }
    }

    public void setScanResultCallback(ScanResultCallback callback) {
        this.scanResultCallback = callback;
    }

    public void restartPreviewAndDecode() {
        if (handler != null) handler.restartPreviewAndDecode();
    }

    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }
}
