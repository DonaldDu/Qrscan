package com.dhy.qrcode;

import com.google.zxing.Result;

public interface ScanResultCallback {
    /**
     * @param result the scan result, the code= result.getText()
     * @return continue scan or not, you can call {@link ScanLayout#restartPreviewAndDecode} to continue scan also
     */
    boolean handleDecode(Result result);
}
