package com.szsyinfo.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.szsyinfo.demo.db.CacheData;
import com.szsyinfo.demo.db.CacheDataDao;
import com.szsyinfo.demo.db.DaoMaster;
import com.szsyinfo.demo.db.DaoSession;
import com.szsyinfo.demo.filedownloader.DownloadFileInfo;
import com.szsyinfo.demo.filedownloader.FileDownloadConfiguration;
import com.szsyinfo.demo.filedownloader.FileDownloader;
import com.szsyinfo.demo.filedownloader.listener.OnDeleteDownloadFileListener;
import com.szsyinfo.demo.filedownloader.listener.OnDownloadFileChangeListener;
import com.szsyinfo.demo.filedownloader.listener.OnFileDownloadStatusListener;
import com.szsyinfo.demo.filedownloader.listener.simple.OnSimpleFileDownloadStatusListener;
import com.szsyinfo.demo.model.AppInfo;
import com.szsyinfo.demo.model.ResponseData;
import com.szsyinfo.demo.utilcode.util.AppUtils;
import com.szsyinfo.demo.utilcode.util.FileUtils;
import com.szsyinfo.demo.utilcode.util.TimeUtils;
import com.szsyinfo.demo.utilcode.util.ToastUtils;
import com.szsyinfo.demo.utilcode.util.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static junit.framework.Assert.assertTrue;


//工具类来自  https://github.com/Blankj/AndroidUtilCode/blob/master/utilcode/README-CN.md
//JSON https://segmentfault.com/a/1190000011212806
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN";
    private String msg = " //Log";

    private ProgressBar pbProgress;
    private CacheDataDao cacheDataDao;//数据操作类
    private CacheData cacheData;//数据
    private AppInfo appInfo_local;//本地
    private AppInfo appInfo_network;//网络

    private String file_url;//下载文件地址 htt://
    private String folder_path;//本地文件夹
    private String file_name;//压缩包的文件名

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        pbProgress = (ProgressBar) findViewById(R.id.pbProgress);

        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(getApplicationContext(), "SQLite.db", null);
        DaoMaster daoMaster = new DaoMaster(devOpenHelper.getWritableDb());
        DaoSession daoSession = daoMaster.newSession();
        cacheDataDao = daoSession.getCacheDataDao();

        folder_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + AppUtils.getAppPackageName();

        CheckUpdate();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                //新建一个Intent(当前Activity, SecondActivity)=====显示Intent
                /*
                Intent intent = new Intent(MainActivity.this, SafeWebActivity.class);
                startActivity(intent);*/
            }
        }, 2000);    //延时2s执行

    }

    /**
     * 检查初始化及更新
     */
    void CheckUpdate() {

        //获取数本地数据
        cacheData = cacheDataDao.queryBuilder().where(CacheDataDao.Properties.Key.eq("app_info"), CacheDataDao.Properties.Channel_name.eq("app")).build().unique();

        if (cacheData != null) {
            Log.d(TAG, "本地：" + cacheData.getValue());
            appInfo_local = JSONObject.parseObject(cacheData.getValue(), AppInfo.class);
        }

        String url = "http://server.szsyinfo.net:8034/tools/submit_ajax.ashx?action=get_app_info&channel_name=app&package_name=com.szsyinfo.demo";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        final Call call = okHttpClient.newCall(request);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = call.execute();
                    ResponseData<AppInfo> model = JSONObject.parseObject(response.body().string(), new TypeReference<ResponseData<AppInfo>>() {
                    });

                    appInfo_network = model.getData();

                    Log.d(TAG, "网络：" + JSONObject.toJSON(appInfo_network));

                    boolean bolUpdate = false;

                    //首一次安装
                    if (appInfo_local == null) {
                        bolUpdate = true;
                    } else if (!appInfo_local.update_time.equals(appInfo_network.update_time)) {
                        bolUpdate = true;
                    }

                    if (bolUpdate) {
                        if (!appInfo_network.file_url.isEmpty()) {
                            file_url = "http://server.szsyinfo.net:8034" + appInfo_network.file_url;
                            GetFile();
                        }
                    } else {
                        Intent intent = new Intent(MainActivity.this, SafeWebActivity.class);
                        startActivity(intent);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void GetFile() {

        msg = " // 开始进入下载环节";
        Log.d(TAG, msg);

        // 1、创建Builder
        FileDownloadConfiguration.Builder builder = new FileDownloadConfiguration.Builder(this);

        // 2.配置Builder
        // 配置下载文件保存的文件夹
        builder.configFileDownloadDir(folder_path + "/zip");

        // 配置同时下载任务数量，如果不配置默认为2
        builder.configDownloadTaskSize(3);

        // 配置失败时尝试重试的次数，如果不配置默认为0不尝试
        builder.configRetryDownloadTimes(5);

        // 开启调试模式，方便查看日志等调试相关，如果不配置默认不开启
        builder.configDebugMode(true);

        // 配置连接网络超时时间，如果不配置默认为15秒
        builder.configConnectTimeout(25000);// 25秒

        // 3、使用配置文件初始化FileDownloader
        FileDownloadConfiguration configuration = builder.build();
        FileDownloader.init(configuration);
        FileDownloader.registerDownloadStatusListener(mOnFileDownloadStatusListener);
        FileDownloader.registerDownloadFileChangeListener(mOnDownloadFileChangeListener);
        //FileDownloader.delete(file_url, true, mOnDeleteDownloadFileListener);
        FileDownloader.start(file_url);
    }

    private OnFileDownloadStatusListener mOnFileDownloadStatusListener = new OnSimpleFileDownloadStatusListener() {
        @Override
        public void onFileDownloadStatusRetrying(DownloadFileInfo downloadFileInfo, int retryTimes) {
            msg = "// 正在重试下载（如果你配置了重试次数，当一旦下载失败时会尝试重试下载），retryTimes是当前第几次重试";
            Log.d(TAG, msg);
        }

        @Override
        public void onFileDownloadStatusWaiting(DownloadFileInfo downloadFileInfo) {
            msg = "// 等待下载（等待其它任务执行完成，或者FileDownloader在忙别的操作）";
            Log.d(TAG, msg);
        }

        @Override
        public void onFileDownloadStatusPreparing(DownloadFileInfo downloadFileInfo) {
            msg = "// 准备中（即，正在连接资源）";
            Log.d(TAG, msg);
        }

        @Override
        public void onFileDownloadStatusPrepared(DownloadFileInfo downloadFileInfo) {
            msg = "// 已准备好（即，已经连接到了资源）";
            Log.d(TAG, msg);
        }

        @Override
        public void onFileDownloadStatusDownloading(DownloadFileInfo downloadFileInfo, float downloadSpeed, long remainingTime) {
            msg = "// 正在下载，downloadSpeed为当前下载速度，单位" + downloadSpeed + "KB/s，remainingTime为预估的剩余时间，" + remainingTime + "秒";
            Log.d(TAG, msg);
            Show(downloadFileInfo);
        }

        @Override
        public void onFileDownloadStatusPaused(DownloadFileInfo downloadFileInfo) {
            msg = "// 下载已被暂停";
            Log.d(TAG, msg);
        }

        @Override
        public void onFileDownloadStatusCompleted(DownloadFileInfo downloadFileInfo) {
            msg = " // 下载完成（整个文件已经全部下载完成）";
            Log.d(TAG, msg);
            file_name = downloadFileInfo.getFileName();
            Install();
        }

        @Override
        public void onFileDownloadStatusFailed(String url, DownloadFileInfo downloadFileInfo, FileDownloadStatusFailReason failReason) {
            msg = "// 下载失败了，详细查看失败原因failReason，有些失败原因你可能必须关心";
            Log.d(TAG, msg);

            String failType = failReason.getType();
            String failUrl = failReason.getUrl();// 或：failUrl = url，url和failReason.getUrl()会是一样的

            if (OnFileDownloadStatusListener.FileDownloadStatusFailReason.TYPE_URL_ILLEGAL.equals(failType)) {
                msg = "// 下载failUrl时出现url错误";
                Log.d(TAG, msg);
            } else if (OnFileDownloadStatusListener.FileDownloadStatusFailReason.TYPE_STORAGE_SPACE_IS_FULL.equals(failType)) {
                msg = "// 下载failUrl时出现本地存储空间不足";
                Log.d(TAG, msg);
            } else if (OnFileDownloadStatusListener.FileDownloadStatusFailReason.TYPE_NETWORK_DENIED.equals(failType)) {
                msg = "// 下载failUrl时出现无法访问网络";
                Log.d(TAG, msg);
            } else if (OnFileDownloadStatusListener.FileDownloadStatusFailReason.TYPE_NETWORK_TIMEOUT.equals(failType)) {
                msg = "// 下载failUrl时出现连接超时";
                Log.d(TAG, msg);
            } else {
                msg = "// 更多错误....";
                Log.d(TAG, msg);
            }

            // 查看详细异常信息
            Throwable failCause = failReason.getCause();// 或：failReason.getOriginalCause()

            // 查看异常描述信息
            String failMsg = failReason.getMessage();// 或：failReason.getOriginalCause().getMessage()
        }
    };

    private OnDownloadFileChangeListener mOnDownloadFileChangeListener = new OnDownloadFileChangeListener() {
        @Override
        public void onDownloadFileCreated(DownloadFileInfo downloadFileInfo) {
            msg = " // 一个新下载文件被创建，也许你需要同步你自己的数据存储，比如在你的业务数据库中增加一条记录";
            Log.d(TAG, msg);
        }

        @Override
        public void onDownloadFileUpdated(DownloadFileInfo downloadFileInfo, Type type) {
            msg = "  // 一个下载文件被更新，也许你需要同步你自己的数据存储，比如在你的业务数据库中更新一条记录";
            Log.d(TAG, msg);
        }

        @Override
        public void onDownloadFileDeleted(DownloadFileInfo downloadFileInfo) {
            msg = " // 一个下载文件被删除，也许你需要同步你自己的数据存储，比如在你的业务数据库中删除一条记录";
            Log.d(TAG, msg);
        }
    };

    private OnDeleteDownloadFileListener mOnDeleteDownloadFileListener = new OnDeleteDownloadFileListener() {

        @Override
        public void onDeleteDownloadFilePrepared(DownloadFileInfo downloadFileNeedDelete) {
            msg = " // 需要删除的下载文件:" + downloadFileNeedDelete.getFileName();
            Log.d(TAG, msg);
        }

        @Override
        public void onDeleteDownloadFileSuccess(DownloadFileInfo downloadFileDeleted) {
            //FileDownloader.start(file_url);
            msg = " // 下载文件已删除:" + downloadFileDeleted.getFileName();
            Log.d(TAG, msg);
        }

        @Override
        public void onDeleteDownloadFileFailed(DownloadFileInfo downloadFileInfo, DeleteDownloadFileFailReason failReason) {

            if (downloadFileInfo == null) {
                msg = " // 需要删除的下载文件，为空的,失败原因 :" + failReason.getMessage();
            } else {

                msg = " // 需要删除的下载文件，失败原因 :" + downloadFileInfo.getFileName() + "," + failReason.getMessage();
            }

            Log.d(TAG, msg);
        }
    };

    void Show(DownloadFileInfo downloadFileInfo) {
        // download progress
        int totalSize = (int) downloadFileInfo.getFileSizeLong();
        int downloaded = (int) downloadFileInfo.getDownloadedSizeLong();
        double rate = (double) totalSize / Integer.MAX_VALUE;
        if (rate > 1.0) {
            totalSize = Integer.MAX_VALUE;
            downloaded = (int) (downloaded / rate);
        }

        pbProgress.setMax(totalSize);
        pbProgress.setProgress(downloaded);

        // file size
        double downloadSize = downloadFileInfo.getDownloadedSizeLong() / 1024f / 1024;
        double fileSize = downloadFileInfo.getFileSizeLong() / 1024f / 1024;

        //tvDownloadSize.setText(((float) (Math.round(downloadSize * 100)) / 100) + "M/");
        //tvTotalSize.setText(((float) (Math.round(fileSize * 100)) / 100) + "M");

        // downloaded percent
        double percent = downloadSize / fileSize * 100;
        //tvPercent.setText(((float) (Math.round(percent * 100)) / 100) + "%");
    }

    /***/
    void Install() {

        msg = "创建Web文件夹...";
        try {
            Log.d(TAG, msg);
            FileUtils.createOrExistsDir(folder_path + "/web");

            msg = "清空Web文件夹...";
            Log.d(TAG, msg);
            FileUtils.deleteAllInDir(folder_path + "/web");//清空

            msg = "解压ZIP置 Web文件夹...";
            Log.d(TAG, msg);
            ZipUtils.unzipFile(folder_path + "/zip/" + file_name, folder_path + "/web");

            if (cacheData == null) {

                cacheData = new CacheData();
                cacheData.setChannel_name("app");
                cacheData.setKey("app_info");
                cacheData.setAdd_time(TimeUtils.getNowString());
                cacheData.setUpdate_time(TimeUtils.getNowString());
                cacheData.setValue(JSONObject.toJSON(appInfo_network).toString());

                cacheDataDao.insert(cacheData);
                msg = "首次安装插入缓存Cache...";
                Log.d(TAG, msg);

            } else if (!appInfo_local.update_time.equals(appInfo_network.update_time)) {

                cacheData.setUpdate_time(TimeUtils.getNowString());
                cacheData.setValue(JSONObject.toJSON(appInfo_network).toString());
                cacheDataDao.update(cacheData);
                msg = "更新缓存Cache...";
                Log.d(TAG, msg);
            }

            Intent intent = new Intent(MainActivity.this, SafeWebActivity.class);
            startActivity(intent);

        } catch (IOException ex) {
            msg = ex.getMessage();
            Log.d(TAG, msg);
        }
    }
}


