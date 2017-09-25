package oxsource.android.updater;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import oxsource.android.updater.listener.DownloadListener;
import oxsource.android.updater.listener.UpdateValidator;

/**
 * 升级服务
 * Created by peng on 2017/9/23.
 */

public final class UpdateService extends Service {
    public final static String ACTION_SERVICE = "oxsource.android.updater.UpdatePower";
    private final static String TAG = UpdateService.class.getSimpleName();
    public final static String KEY_WHAT = "key_what";
    public final static int WHAT_VERIFY = 1;
    public final static int WHAT_DOWNLOAD = 2;
    public final static int WHAT_INSTALL = 3;
    /*KEY TAG*/
    public final static String KEY_URL = "key_target_url";
    public final static String KEY_PATH = "key_target_path";

    private final Executor threadPool = Executors.newCachedThreadPool();
    private UpdatePower updateHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        updateHandler = new UpdatePower(new NotificationPower(getApplicationContext()));
        log("UpdateService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        int what = intent.getIntExtra(KEY_WHAT, -1);

        switch (what) {
            case WHAT_VERIFY:
                log("UpdateService WHAT_VERIFY");
                String verifyUrl = intent.getStringExtra(KEY_URL);
                threadPool.execute(new UpdatePower.VerifyRunnable(updateHandler, verifyUrl));
                break;
            case WHAT_DOWNLOAD:
                log("UpdateService WHAT_DOWNLOAD");
                String apkUrl = intent.getStringExtra(KEY_URL);
                threadPool.execute(new UpdatePower.DownloadRunnable(updateHandler, apkUrl));
                break;
            case WHAT_INSTALL:
                log("UpdateService WHAT_INSTALL");
                String installPath = intent.getStringExtra(KEY_PATH);
                updateHandler.install(getApplication(), installPath);
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public void onDestroy() {
        log("UpdateService onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static void log(String message) {
        Log.d(TAG, message);
    }

    public static void checkSelfPermission(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            for (int i = 0; i < permissions.length; i++) {
                if (activity.checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    activity.requestPermissions(new String[]{permissions[i]}, 0);
                }
            }
        }
    }

    /**
     * Service意图构建器
     */
    public static class Builder {
        private Intent intent = new Intent(ACTION_SERVICE);

        public Builder what(int what) {
            intent.putExtra(UpdateService.KEY_WHAT, what);
            return this;
        }

        public Builder url(String url) {
            intent.putExtra(UpdateService.KEY_URL, url);
            return this;
        }

        public Builder path(String path) {
            intent.putExtra(UpdateService.KEY_PATH, path);
            return this;
        }

        public Builder validator(UpdateValidator validator) {
            UpdatePower.validator = validator;
            return what(WHAT_VERIFY);
        }

        public Builder download(DownloadListener listener) {
            UpdatePower.dListener = listener;
            return what(WHAT_DOWNLOAD);
        }

        public Intent intent() {
            return intent;
        }

        public void start(Context context) {
            intent.setPackage(context.getPackageName());
            context.startService(intent);
        }
    }
}