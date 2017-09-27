package oxsource.android.updater.async;

import oxsource.android.updater.arch.UpdateController;

/**
 * 下载Runnable
 * Created by peng on 2017/9/26.
 */

public class DownloadRunnable implements Runnable {
    private UpdateController handler;
    private String apkPath;
    private String apkUrl;

    public DownloadRunnable(UpdateController handler, String apkPath, String apkUrl) {
        this.handler = handler;
        this.apkPath = apkPath;
        this.apkUrl = apkUrl;
    }

    @Override
    public void run() {
        if (null != handler) {
            handler.download(apkPath, apkUrl);
        }
    }
}
