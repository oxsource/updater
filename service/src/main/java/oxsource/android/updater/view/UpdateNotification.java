package oxsource.android.updater.view;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;

import oxsource.android.updater.R;
import oxsource.android.updater.UpdateHandle;
import oxsource.android.updater.listener.DownloadListener;

/**
 * 通知栏消息展示管理类
 */

public class UpdateNotification implements DownloadListener {
    private final static int TYPE_CHANNEL = 99;
    private NotificationManager mNotificationManager;
    private Context context;
    private int progress = 0;
    private static int smallIcon = R.drawable.ic_notification;

    public UpdateNotification(Context context) {
        this.context = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void setSmallIcon(@DrawableRes int icon) {
        smallIcon = icon;
    }

    @Override
    public void onStart() {
        progress = 0;
        notify("开始下载...", null);
    }

    @Override
    public void onProgress(int current, int total) {
        progress = current;
        notify("正在下载...", null);
    }

    @Override
    public void onFailure(int code, String message) {
        notify("正在下载...", null);
    }

    @Override
    public void onSuccess(String filePath) {
        UpdateHandle handle = new UpdateHandle.Builder()
                .what(UpdateHandle.WHAT_INSTALL)
                .path(filePath)
                .build();
        PendingIntent intent = handle.pendingIntent(context);
        notify("下载成功，点击安装", intent);
    }

    private void notify(String message, PendingIntent intent) {
        String title = "版本更新";
        Notification notification;
        final int total = 100;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_updater";
            String channelName = "updater";
            //创建渠道
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            //创建builder
            Notification.Builder builder = new Notification.Builder(context, channelId);
            builder.setSmallIcon(smallIcon);
            builder.setContentTitle(title);
            builder.setContentText(message);
            builder.setProgress(total, progress, false);
            if (null != intent) {
                builder.setContentIntent(intent);
            }
            notification = builder.build();
        } else {
            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(smallIcon);
            builder.setContentTitle(title);
            builder.setContentText(message);
            builder.setProgress(total, progress, false);
            if (null != intent) {
                builder.setContentIntent(intent);
            }
            notification = builder.build();
        }
        mNotificationManager.notify(TYPE_CHANNEL, notification);
    }

    public void cancel() {
        mNotificationManager.cancel(TYPE_CHANNEL);
    }
}