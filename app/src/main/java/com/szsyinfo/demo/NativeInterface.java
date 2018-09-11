package com.szsyinfo.demo;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import static com.szsyinfo.demo.utilcode.util.ActivityUtils.startActivity;


public class NativeInterface {

    private Context mContext;

    public NativeInterface(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public void hello() {
        Toast.makeText(mContext, "hello", Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void hello(String params) {
        Toast.makeText(mContext, params, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String getAndroid() {
        Toast.makeText(mContext, "getAndroid", Toast.LENGTH_SHORT).show();
        return "Android data";
    }

    @JavascriptInterface
    public String getAndroidTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    @JavascriptInterface
    public void OpenScan() {



        startActivity(new Intent(mContext,SubScanActivity.class));

    }

}