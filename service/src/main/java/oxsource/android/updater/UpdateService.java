package oxsource.android.updater;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import oxsource.android.updater.arch.UpdateController;
import oxsource.android.updater.async.DownloadRunnable;
import oxsource.android.updater.async.VerifyRunnable;
import oxsource.android.updater.view.UpdateNotification;


/**
 * 升级服务
 * Created by peng on 2017/9/23.
 */

public final class UpdateService extends Service {
    private final static String TAG = UpdateService.class.getSimpleName();

    private final Executor threadPool = Executors.newCachedThreadPool();
    private UpdateNotification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        notification = new UpdateNotification(getApplicationContext());
        log("UpdateService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        if (null == intent) {
            return result;
        }
        int what = intent.getIntExtra(UpdateHandle.KEY_WHAT, -1);
        String tag = intent.getStringExtra(UpdateHandle.KEY_CONTROLLER_TAG);
        UpdateController controller = UpdateHandle.obtain(tag);
        switch (what) {
            case UpdateHandle.WHAT_VERIFY:
                log("UpdateService WHAT_VERIFY");
                String verifyUrl = intent.getStringExtra(UpdateHandle.KEY_URL);
                controller.notification(notification);
                threadPool.execute(new VerifyRunnable(controller, verifyUrl));
                break;
            case UpdateHandle.WHAT_DOWNLOAD:
                log("UpdateService WHAT_DOWNLOAD");
                String apkUrl = intent.getStringExtra(UpdateHandle.KEY_URL);
                String apkPah = intent.getStringExtra(UpdateHandle.KEY_PATH);
                controller.notification(notification);
                threadPool.execute(new DownloadRunnable(controller, apkPah, apkUrl));
                break;
            case UpdateHandle.WHAT_INSTALL:
                log("UpdateService WHAT_INSTALL");
                String apkPath = intent.getStringExtra(UpdateHandle.KEY_PATH);
                controller.notification(notification);
                controller.install(getApplication(), apkPath);
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
}