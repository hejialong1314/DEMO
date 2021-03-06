package com.szsyinfo.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.szsyinfo.demo.model.ActivityCode;

import static com.szsyinfo.demo.utilcode.util.ActivityUtils.startActivity;
import static com.szsyinfo.demo.utilcode.util.ActivityUtils.startActivityForResult;


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

        //startActivity(new Intent(mContext,SubScanActivity.class));

        //跳转到ActivityA页面
        Intent intent = new Intent(mContext, SubScanActivity.class);

        //发送请求代码
        startActivityForResult((Activity)mContext,intent,ActivityCode.SCan.getCode());
    }

}