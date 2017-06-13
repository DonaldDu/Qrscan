package com.dhy.qrcode;

import android.content.Intent;
import android.os.Handler;

import com.google.zxing.Result;

public interface IScanUtil {
    Handler getHandler();

    void handleDecode(Result result);

    void drawViewfinder();

    void startActivity(Intent intent);

    void setResult(int result, Intent intent);
}
