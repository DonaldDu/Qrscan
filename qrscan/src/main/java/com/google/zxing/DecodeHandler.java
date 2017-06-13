package com.google.zxing;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.common.HybridBinarizer;
import com.dhy.qrcode.IScanUtil;

import java.util.Hashtable;

/**
 * 进行扫描到的图片进行解码的handler
 *
 * @author tuzhao
 */
public final class DecodeHandler extends Handler {

    private MultiFormatReader multiFormatReader;
    IScanUtil scanUtil;

    public DecodeHandler(IScanUtil scanUtil, Hashtable<DecodeHintType, Object> hints) {
        this.scanUtil = scanUtil;
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case ScanCode.DECODE:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case ScanCode.QUIT:
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        Result rawResult = null;

        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap);
        } catch (ReaderException re) {
            // continue
        } finally {
            multiFormatReader.reset();
        }
        Handler handler = scanUtil.getHandler();
        if (rawResult != null) {
            Message message = Message.obtain(handler, ScanCode.DECODE_SUCC, rawResult);
            message.sendToTarget();
        } else {
            Message message = Message.obtain(handler, ScanCode.DECODE_FAILE);
            message.sendToTarget();
        }
    }
}
