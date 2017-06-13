package com.google.zxing;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dhy.qrcode.IFinderView;
import com.dhy.qrcode.IScanUtil;

import java.util.Vector;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class CaptureActivityHandler extends Handler {

    private static final String TAG = "TAG";

    private DecodeThread decodeThread;
    private State state;
    private IScanUtil scanUtil;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    public CaptureActivityHandler(IScanUtil activity, IFinderView viewfinderView) {
        this(activity, null, null, viewfinderView);
    }

    public CaptureActivityHandler(IScanUtil scanUtil, @Nullable Vector<BarcodeFormat> decodeFormats, @Nullable String characterSet, IFinderView viewfinderView) {
        this.scanUtil = scanUtil;
        decodeThread = new DecodeThread(scanUtil, decodeFormats, characterSet, new ViewfinderResultPointCallback(viewfinderView));
        decodeThread.start();
        state = State.SUCCESS;
        // Start ourselves capturing previews and decoding.
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case ScanCode.AUTO_FOCUS:
                //Log.d(TAG, "Got auto-focus message");
                // When one auto focus pass finishes, start another. This is the closest thing to
                // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
                if (state == State.PREVIEW) {
                    CameraManager.get().requestAutoFocus(this, ScanCode.AUTO_FOCUS);
                }
                break;
            case ScanCode.RESTART_PREVIEW:
                Log.d(TAG, "Got restart preview message");
                restartPreviewAndDecode();
                break;
            case ScanCode.DECODE_SUCC:
                Log.d(TAG, "Got decode succeeded message");
                state = State.SUCCESS;
                scanUtil.handleDecode((Result) message.obj);
                break;
            case ScanCode.DECODE_FAILE:
                // We're decoding as fast as possible, so when one decode fails, start another.
                state = State.PREVIEW;
                CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), ScanCode.DECODE);
                break;
            case ScanCode.RETURN_SCAN_RESULT:
                Log.d(TAG, "Got return scan result message");
                scanUtil.setResult(Activity.RESULT_OK, (Intent) message.obj);
                break;
            case ScanCode.LAUNCH_PRODUCT_QUERY:
                Log.d(TAG, "Got product query message");
                String url = (String) message.obj;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                scanUtil.startActivity(intent);
                break;
        }

    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), ScanCode.QUIT);
        quit.sendToTarget();
        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }
        // Be absolutely sure we don't send any queued up messages
        removeMessages(ScanCode.DECODE_SUCC);
        removeMessages(ScanCode.DECODE_FAILE);
    }

    public void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), ScanCode.DECODE);
            CameraManager.get().requestAutoFocus(this, ScanCode.AUTO_FOCUS);
            scanUtil.drawViewfinder();
        }
    }


}
