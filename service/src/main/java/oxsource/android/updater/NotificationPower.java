package oxsource.android.updater;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import oxsource.android.updater.listener.DownloadListener;

/**
 * 通知栏消息展示管理类
 * Created by wangcheng on 2017/9/25.
 */

public class NotificationPower implements DownloadListener {
    private final static int TYPE_CHANNEL = 99;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private int mSmallIcon = R.drawable.ic_launcher;
    private Context context;

    public NotificationPower(Context context) {
        this.context = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
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
        Intent intent = new UpdateService.Builder()
                .what(UpdateService.WHAT_INSTALL)
                .path(filePath)
                .intent();
        mBuilder.setContentIntent(PendingIntent.getService(context, 0, intent, 0));
        mNotificationManager.notify(TYPE_CHANNEL, mBuilder.build());
    }
}