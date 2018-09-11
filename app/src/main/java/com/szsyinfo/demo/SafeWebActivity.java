package com.szsyinfo.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.szsyinfo.demo.utilcode.util.AppUtils;

public class SafeWebActivity extends AppCompatActivity {

    private static final String TAG = "SafeWebActivity";
    private SafeWebView mWebView;
    private LinearLayout mRoot;
    private ProgressBar progressBar;
    private boolean  loadError =false;//默认没有错误

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_web);
        initView();
    }


    private void initView() {
        mRoot = (LinearLayout) findViewById(R.id.activity_safe_web);

        //进度条
        progressBar= (ProgressBar)findViewById(R.id.progressbar);//进度条

        //实际使用时请采用 new 的方式
        mWebView = (SafeWebView) findViewById(R.id.safe_webview);


        //4.2 开启辅助功能崩溃
        mWebView.disableAccessibility(getApplicationContext());
        initWebSettings();
        initListener();
        initinject();
        //mWebView.loadUrl("https://www.jianshu.com/");
        mWebView.loadUrl("file:///android_asset/web/index.html");
        //mWebView.loadUrl("file:///storage/emulated/0/"+ AppUtils.getAppPackageName()+"/web/index.html");
    }

    private void initinject() {
        mWebView.addJavascriptInterface(new NativeInterface(this), "AndroidNative");
    }

    private void initWebSettings() {
        WebSettings webSettings = mWebView.getSettings();
        if (webSettings == null) return;
        //设置字体缩放倍数，默认100
        webSettings.setTextZoom(100);
        // 支持 Js 使用
        webSettings.setJavaScriptEnabled(true);
        // 开启DOM缓存
        webSettings.setDomStorageEnabled(true);
        // 开启数据库缓存
        webSettings.setDatabaseEnabled(true);
        // 支持自动加载图片
        webSettings.setLoadsImagesAutomatically(hasKitkat());
        // 设置 WebView 的缓存模式
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 支持启用缓存模式
        webSettings.setAppCacheEnabled(true);
        // 设置 AppCache 最大缓存值(现在官方已经不提倡使用，已废弃)
        webSettings.setAppCacheMaxSize(8 * 1024 * 1024);
        // Android 私有缓存存储，如果你不调用setAppCachePath方法，WebView将不会产生这个目录
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath());
        // 数据库路径
        if (!hasKitkat()) {
            webSettings.setDatabasePath(getDatabasePath("html").getPath());
        }
        // 关闭密码保存提醒功能
        webSettings.setSavePassword(false);
        // 支持缩放
        webSettings.setSupportZoom(true);
        // 设置 UserAgent 属性
        webSettings.setUserAgentString("");
        // 允许加载本地 html 文件/false
        webSettings.setAllowFileAccess(true);

        // 允许通过 file url 加载的 Javascript 读取其他的本地文件,Android 4.1 之前默认是true，在 Android 4.1 及以后默认是false,也就是禁止
        webSettings.setAllowFileAccessFromFileURLs(false);
        // 允许通过 file url 加载的 Javascript 可以访问其他的源，包括其他的文件和 http，https 等其他的源，
        // Android 4.1 之前默认是true，在 Android 4.1 及以后默认是false,也就是禁止
        // 如果此设置是允许，则 setAllowFileAccessFromFileURLs 不起做用
        webSettings.setAllowUniversalAccessFromFileURLs(false);

        /**
         *  关于缓存目录:
         *
         *  我自测发现以下规律，如果你有涉及到目录操作，需要自己做下验证。
         *  Android 4.4 以下：/data/data/包名/cache
         *  Android 4.4 - 5.0：/data/data/包名/app_webview/cache/
         */
    }

    private void initListener() {
        mWebView.setWebViewClient(new SafeWebActivity.SafeWebViewClient());
        mWebView.setWebChromeClient(new SafeWebActivity.SafeWebChromeClient());
    }

    public class SafeWebViewClient extends WebViewClient {

        /**
         * 当WebView得页面Scale值发生改变时回调
         */
        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
        }

        /**
         * 是否在 WebView 内加载页面
         *
         * @param view
         * @param url
         * @return
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        /**
         * WebView 开始加载页面时回调，一次Frame加载对应一次回调
         *
         * @param view
         * @param url
         * @param favicon
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.v("IE","onPageStarted");
            progressBar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        /**
         * WebView 完成加载页面时回调，一次Frame加载对应一次回调
         *
         * @param view
         * @param url
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
            Log.v("IE","onPageFinished");
             if (loadError){ //加载失败的话，初始化页面加载失败的图，然后替换正在加载的视图页面
                 mWebView.loadUrl("file:///android_asset/web/index.html");
            }
            super.onPageFinished(view, url);
        }

        /**
         * WebView 加载页面资源时会回调，每一个资源产生的一次网络加载，除非本地有当前 url 对应有缓存，否则就会加载。
         *
         * @param view WebView
         * @param url  url
         */
        @Override
        public void onLoadResource(WebView view, String url) {
            Log.v("IE","onLoadResource");
            super.onLoadResource(view, url);
        }

        /**
         * WebView 可以拦截某一次的 request 来返回我们自己加载的数据，这个方法在后面缓存会有很大作用。
         *
         * @param view    WebView
         * @param request 当前产生 request 请求
         * @return WebResourceResponse
         */
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Log.v("IE","shouldInterceptRequest");
            Log.v("IE",request.getUrl().toString().trim());
            return super.shouldInterceptRequest(view, request);
        }

        /**
         * WebView 访问 url 出错
         *
         * @param view
         * @param request
         * @param error
         */
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.v("IE","onReceivedError");
            super.onReceivedError(view, request, error);
            loadError = true;
        }

        /**
         * WebView ssl 访问证书出错，handler.cancel()取消加载，handler.proceed()对然错误也继续加载
         *
         * @param view
         * @param handler
         * @param error
         */
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.v("IE","onReceivedSslError");
            super.onReceivedSslError(view, handler, error);
        }
    }

    public class SafeWebChromeClient extends WebChromeClient {

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.v("IE","onConsoleMessage");
            return super.onConsoleMessage(consoleMessage);
        }

        /**
         * 当前 WebView 加载网页进度
         *
         * @param view
         * @param newProgress
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.v("IE","onProgressChanged");
            progressBar.setProgress(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        /**
         * Js 中调用 alert() 函数，产生的对话框
         *
         * @param view
         * @param url
         * @param message
         * @param result
         * @return
         */
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        /**
         * 处理 Js 中的 Confirm 对话框
         *
         * @param view
         * @param url
         * @param message
         * @param result
         * @return
         */
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            return super.onJsConfirm(view, url, message, result);
        }

        /**
         * 处理 JS 中的 Prompt对话框
         *
         * @param view
         * @param url
         * @param message
         * @param defaultValue
         * @param result
         * @return
         */
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        /**
         * 接收web页面的icon
         *
         * @param view
         * @param icon
         */
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {

            Log.v("IE","onReceivedIcon");
            super.onReceivedIcon(view, icon);
        }

        /**
         * 接收web页面的 Title
         *
         * @param view
         * @param title
         */
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);

            Log.v("IE","onReceivedTitle");

            //判断标题 title 中是否包含有“error”字段，如果包含“error”字段，则设置加载失败，显示加载失败的视图
            if(!TextUtils.isEmpty(title)&&title.toLowerCase().contains("找不到网页")){
                loadError = true;
            }
        }

    }

    @Override
    protected void onResume() {

        Log.v("IE","onResume");

        super.onResume();
        mWebView.onResume();
        mWebView.resumeTimers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
        mWebView.pauseTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRoot != null) {
            mRoot.removeView(mWebView);
        }
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.clearMatches();
            mWebView.clearHistory();
            mWebView.clearSslPreferences();
            mWebView.clearCache(true);
            mWebView.loadUrl("about:blank");
            mWebView.removeAllViews();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mWebView.removeJavascriptInterface("AndroidNative");
            }
            mWebView.destroy();
        }
        mWebView = null;
    }

    private static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= 19;
    }

    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            //最小化，但好没有用
            moveTaskToBack(isFinishing());

            //首先获取当前进程的id，然后杀死该进程。 （建议使用）
            //android.os.Process.killProcess(android.os.Process.myPid());
            //System.exit(0);
            //finish();
        }
    }
}
