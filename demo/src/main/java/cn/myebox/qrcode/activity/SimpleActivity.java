package cn.myebox.qrcode.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;
import com.dhy.qrcode.ScanLayout;
import com.dhy.qrcode.ScanResultCallback;

import cn.myebox.qrcode.R;

public class SimpleActivity extends Activity {
    ScanLayout scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finderview_layout);
        scan = (ScanLayout) findViewById(R.id.finderview);
        @SuppressLint("ShowToast")
        final Toast toast = Toast.makeText(this, null, Toast.LENGTH_SHORT);
        scan.setScanResultCallback(new ScanResultCallback() {
            @Override
            public boolean handleDecode(Result result) {
                toast.setText(result.getText());
                toast.show();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        scan.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scan.onPause();
    }
}
