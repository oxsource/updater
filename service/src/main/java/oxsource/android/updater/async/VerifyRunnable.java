package oxsource.android.updater.async;

import oxsource.android.updater.arch.UpdateController;

/**
 * 校验Runnable
 * Created by peng on 2017/9/26.
 */

public class VerifyRunnable implements Runnable {
    private String verifyUrl;
    private UpdateController handler;

    public VerifyRunnable(UpdateController handler, String verifyUrl) {
        this.handler = handler;
        this.verifyUrl = verifyUrl;
    }

    @Override
    public void run() {
        if (null != handler) {
            handler.verify(verifyUrl);
        }
    }
}