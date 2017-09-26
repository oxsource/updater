package oxsource.android.updater.async;

import oxsource.android.updater.arch.UpdateController;

/**
 * 下载Runnable
 * Created by peng on 2017/9/26.
 */

public class DownloadRunnable implements Runnable {
    private UpdateController handler;
    private String apkUrl;

    public DownloadRunnable(UpdateController handler, String apkUrl) {
        this.apkUrl = apkUrl;
        this.handler = handler;
    }

    @Override
    public void run() {
        if (null != handler) {
            handler.download(apkUrl);
        }
    }
}
