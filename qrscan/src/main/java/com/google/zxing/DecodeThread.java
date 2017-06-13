package com.google.zxing;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.dhy.qrcode.IScanUtil;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 */
final class DecodeThread extends Thread {

    public static final String BARCODE_BITMAP = "barcode_bitmap";

    private Hashtable<DecodeHintType, Object> hints;
    private CountDownLatch handlerInitLatch;
    private Handler handler;
    private IScanUtil scanUtil;

    /**
     * @param resultPointCallback 屏幕上闪烁的黄点点
     */
    public DecodeThread(IScanUtil scanUtil, @Nullable Vector<BarcodeFormat> decodeFormats, @Nullable String characterSet, ResultPointCallback resultPointCallback) {
        this.scanUtil = scanUtil;
        handlerInitLatch = new CountDownLatch(1);
        hints = new Hashtable<>(3);

        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = new Vector<>();
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        }

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }

        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(scanUtil, hints);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
