package oxsource.android.updater.view;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import oxsource.android.updater.R;
import oxsource.android.updater.UpdateHandle;
import oxsource.android.updater.listener.DownloadListener;

/**
 * 通知栏消息展示管理类
 */

public class UpdateNotification implements DownloadListener {
    private final static int TYPE_CHANNEL = 99;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private int mSmallIcon = R.drawable.ic_notification;
    private Context context;

    public UpdateNotification(Context context) {
        this.context = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//        } else {
        mBuilder = new NotificationCompat.Builder(context);
//        }
    }

    public void smallIcon(int smallIconId) {
        mSmallIcon = smallIconId;
        mBuilder.setSmallIcon(mSmallIcon);
    }

    @Override
    public void onStart() {
        mBuilder.setSmallIcon(mSmallIcon);
        mBuilder.setContentTitle("版本更新");
        mBuilder.setContentText("开始下载...");
        mNotificationManager.notify(TYPE_CHANNEL, mBuilder.build());
    }

    @Override
    public void onProgress(int current, int total) {
        mBuilder.setProgress(total, current, false);
        mNotificationManager.notify(TYPE_CHANNEL, mBuilder.build());
    }

    @Override
    public void onFailure(int code, String message) {
        mBuilder.setContentText("下载失败");
        mNotificationManager.notify(TYPE_CHANNEL, mBuilder.build());
    }

    @Override
    public void onSuccess(String filePath) {
        mBuilder.setProgress(0, 0, false);
        mBuilder.setContentText("下载成功，点击安装");
        UpdateHandle handle = new UpdateHandle.Builder()
                .what(UpdateHandle.WHAT_INSTALL)
                .path(filePath)
                .build();
        PendingIntent intent = handle.pendingIntent(context);
        mBuilder.setContentIntent(intent);
        mNotificationManager.notify(TYPE_CHANNEL, mBuilder.build());
    }

    public void cancel() {
        mNotificationManager.cancel(TYPE_CHANNEL);
    }
}