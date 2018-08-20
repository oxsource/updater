package oxsource.android.updater;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import oxsource.android.updater.arch.UpdateController;
import oxsource.android.updater.listener.DownloadListener;
import oxsource.android.updater.listener.UpdateValidator;

/**
 * 更新句柄
 * Created by peng on 2017/9/26.
 */

public class UpdateHandle {
    final static String ACTION_SERVICE = "oxsource.android.updater.UpdatePower";
    public final static String KEY_WHAT = "key_what";
    public final static int WHAT_VERIFY = 1;
    public final static int WHAT_DOWNLOAD = 2;
    public final static int WHAT_INSTALL = 3;
    /*KEY TAG*/
    public final static String KEY_URL = "key_target_url";
    public final static String KEY_PATH = "key_target_path";
    public final static String KEY_CONTROLLER_TAG = "key_controller_tag";

    /*controllers*/
    private final static Map<String, UpdateController> controllers = new HashMap<>();

    static UpdateController obtain(String key) {
        return controllers.get(key);
    }

    private Intent intent;
    private UpdateController controller;

    private UpdateHandle(Builder builder) {
        intent = builder.intent;
        controller = new UpdateController();
        controller.validator(builder.validator);
        controller.downloadListener(builder.listener);
    }

    private void build(Context context) {
        String tag = controller.getTag();
        if (!controllers.containsKey(tag)) {
            controllers.put(tag, controller);
            intent.putExtra(KEY_CONTROLLER_TAG, tag);
            intent.setPackage(context.getPackageName());
        }
    }

    /**
     * 启动service并返回Intent
     */
    public Intent intent(@NonNull Context context) {
        build(context);
        context.startService(intent);
        return intent;
    }

    /**
     * 不启动service并返回Intent
     */
    public PendingIntent pendingIntent(@NonNull Context context) {
        build(context);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    /**
     * 销毁句柄
     */
    public void destroy() {
        controller.validator(null);
        controller.downloadListener(null);
        controller.notification(null);
        controllers.remove(controller.getTag());
    }

    /**
     * 构建类
     */
    public static class Builder {
        private Intent intent = new Intent(ACTION_SERVICE);
        private UpdateValidator validator;
        private DownloadListener listener;

        public Builder what(int what) {
            intent.putExtra(KEY_WHAT, what);
            return this;
        }

        public Builder url(String url) {
            intent.putExtra(KEY_URL, url);
            return this;
        }

        public Builder path(String path) {
            intent.putExtra(KEY_PATH, path);
            return this;
        }

        public Builder validator(UpdateValidator validator) {
            this.validator = validator;
            return this;
        }

        public Builder download(DownloadListener listener) {
            this.listener = listener;
            return this;
        }

        public UpdateHandle build() {
            return new UpdateHandle(this);
        }
    }
}