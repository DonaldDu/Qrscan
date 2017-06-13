package com.google.zxing;

import com.dhy.qrcode.IFinderView;

public final class ViewfinderResultPointCallback implements ResultPointCallback {

    private final IFinderView viewfinderView;

    public ViewfinderResultPointCallback(IFinderView viewfinderView) {
        this.viewfinderView = viewfinderView;

    }

    public void foundPossibleResultPoint(ResultPoint point) {
        viewfinderView.addPossibleResultPoint(point);

    }

}
