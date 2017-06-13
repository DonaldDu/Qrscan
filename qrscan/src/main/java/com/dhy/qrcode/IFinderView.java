package com.dhy.qrcode;

import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.google.zxing.ResultPoint;

public interface IFinderView {
    void addPossibleResultPoint(ResultPoint point);

    /**
     * note: no need to validate rect, auto validated
     */
    @NonNull
    Rect getDecodeRetangle();
}
