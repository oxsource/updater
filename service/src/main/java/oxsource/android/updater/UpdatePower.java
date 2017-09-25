package oxsource.android.updater;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import oxsource.android.updater.listener.DownloadListener;
import oxsource.android.updater.listener.UpdateValidator;

/**
 * 更新处理器
 * Created by peng on 2017/9/23.
 */

public final class UpdatePower implements Handler.Callback {
    //整型常量
    private final int WHAT_VERIFY_START = 10;
    private final int WHAT_VERIFY_SUCCESS = 11;
    private final int WHAT_VERIFY_FAILURE = 12;

    private final int WHAT_DOWNLOAD_START = 20;
    private final int WHAT_DOWNLOAD_PROGRESS = 21;
    private final int WHAT_DOWNLOAD_SUCCESS = 22;
    private final int WHAT_DOWNLOAD_FAILURE = 23;

    //字符串常量
    public final static String DIRECTORY_DEFAULT = "DOWNLOADS";
    private final static String METHOD_POST = "POST";
    private final static String KEY_CHARSET = "Charset";
    private final static String CHARSET_DEFAULT = "UTF-8";
    private final static String MIME_TYPE_APK = "application/vnd.android.package-archive";
    private final static String ERROR_DEFAULT_VALIDATE = "检查版本信息异常";
    private final static String ERROR_DEFAULT_DOWNLOAD = "下载更新遗产异常";
    private final int TIME_OUT_MS = 5 * 1000;
    private final int TIME_INTERVAL_MS = 500;
    //监听回调
    private final Handler handler = new Handler(this);
    public static UpdateValidator validator;
    public static DownloadListener dListener;
    private NotificationPower notificationPower;

    UpdatePower(NotificationPower notificationPower) {
        this.notificationPower = notificationPower;
    }

    public void verify(String verifyUrl) {
        HttpURLConnection conn = null;
        BufferedInputStream bis = null;
        try {
            notifyHandler(WHAT_VERIFY_START, null);
            //配置远程连接
            URL url = new URL(verifyUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT_MS);
            conn.setConnectTimeout(TIME_OUT_MS);
            conn.setRequestMethod(METHOD_POST);
            conn.setRequestProperty(KEY_CHARSET, CHARSET_DEFAULT);
            conn.connect();
            //读取远程数据并写入ByteArrayOutputStream
            bis = new BufferedInputStream(conn.getInputStream());
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            byte[] arrays = bos.toByteArray();
            bos.reset();
            Object value = validator.onVerify(new String(arrays, CHARSET_DEFAULT));
            notifyHandler(WHAT_VERIFY_SUCCESS, value);
        } catch (Exception e) {
            String error = null == e ? ERROR_DEFAULT_VALIDATE : e.getMessage();
            notifyHandler(WHAT_VERIFY_FAILURE, error);
        } finally {
            quitHttp(conn);
            quitClose(bis);
        }
    }

    public void download(String apkUrl) {
        HttpURLConnection conn = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        try {
            notifyHandler(WHAT_DOWNLOAD_START, null);
            //配置本地下载路径
            int pos = apkUrl.lastIndexOf("/") + 1;
            File apkPath = downloadFile();
            File apkFile = new File(apkPath, apkUrl.substring(pos));
            if (!apkPath.exists()) {
                apkPath.mkdirs();
            }
            fos = new FileOutputStream(apkFile);
            //配置远程连接
            URL url = new URL(apkUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(METHOD_POST);
            conn.setReadTimeout(TIME_OUT_MS);
            conn.setConnectTimeout(TIME_OUT_MS);
            conn.setRequestProperty(KEY_CHARSET, CHARSET_DEFAULT);
            conn.connect();
            //读取
            bis = new BufferedInputStream(conn.getInputStream());
            byte[] buffer = new byte[10 * 1024];
            final int[] POSITIONS = new int[2];
            POSITIONS[0] = conn.getContentLength();
            POSITIONS[1] = 0;
            int len;
            long lastReadMs = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                POSITIONS[1] += len;
                long nowReadMs = System.currentTimeMillis();
                if ((nowReadMs - lastReadMs) >= TIME_INTERVAL_MS) {
                    notifyHandler(WHAT_DOWNLOAD_PROGRESS, POSITIONS);
                    lastReadMs = nowReadMs;
                }
            }
            notifyHandler(WHAT_DOWNLOAD_SUCCESS, apkFile.getAbsolutePath());
        } catch (Exception e) {
            String error = null == e ? ERROR_DEFAULT_DOWNLOAD : e.getMessage();
            notifyHandler(WHAT_DOWNLOAD_FAILURE, error);
        } finally {
            quitHttp(conn);
            quitClose(bis);
            quitClose(fos);
        }

    }

    public boolean install(Application context, String path) {
        boolean value = false;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(new File(path));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, MIME_TYPE_APK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            value = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public boolean handleMessage(Message message) {
        //版本检查更新回调
        boolean finish = onValidateEvent(message);
        if (finish) {
            return true;
        }
        //下载监听回调
        finish = onDownloadEvent(message);
        return finish;
    }

    //关闭IO流
    private void quitClose(Closeable io) {
        try {
            if (null != io) {
                io.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //断开网络连接
    private void quitHttp(HttpURLConnection conn) {
        try {
            if (null != conn) {
                conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //发送消息
    private void notifyHandler(int what, Object obj) {
        Message msg = Message.obtain(handler, what);
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    //处理验证版本消息
    private boolean onValidateEvent(Message message) {
        if (null == validator) {
            return false;
        }
        boolean finish = true;
        switch (message.what) {
            case WHAT_VERIFY_START:
                validator.onStart();
                break;
            case WHAT_VERIFY_FAILURE:
                validator.onFailure(-1, (String) message.obj);
                break;
            case WHAT_VERIFY_SUCCESS:
                validator.onSuccess(message.obj);
                break;
            default:
                finish = false;
                break;
        }
        return finish;
    }

    //处理下载消息
    private boolean onDownloadEvent(Message message) {
        if (null == dListener) {
            return false;
        }
        boolean finish = true;
        switch (message.what) {
            case WHAT_DOWNLOAD_PROGRESS:
                int[] position = (int[]) message.obj;
                dListener.onProgress(position[1], position[0]);
                notificationPower.onProgress(position[1], position[0]);
                break;
            case WHAT_DOWNLOAD_START:
                dListener.onStart();
                notificationPower.onStart();
                break;
            case WHAT_DOWNLOAD_FAILURE:
                String error = (String) message.obj;
                dListener.onFailure(-1, error);
                notificationPower.onFailure(-1, error);
                break;
            case WHAT_DOWNLOAD_SUCCESS:
                String path = (String) message.obj;
                dListener.onSuccess(path);
                notificationPower.onSuccess(path);
                break;
            default:
                finish = false;
                break;
        }
        return finish;
    }

    //校验Runnable
    static class VerifyRunnable implements Runnable {
        private final String verifyUrl;
        private final UpdatePower handler;

        public VerifyRunnable(UpdatePower handler, String verifyUrl) {
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

    //下载Runnable
    static class DownloadRunnable implements Runnable {
        private final UpdatePower handler;
        private final String apkUrl;

        public DownloadRunnable(UpdatePower handler, String apkUrl) {
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

    private File downloadFile() throws Exception {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            throw new IllegalStateException("没有检测到可用的SD卡");
        }
        File path = Environment.getExternalStorageDirectory();
        return new File(path, DIRECTORY_DEFAULT);
    }
}