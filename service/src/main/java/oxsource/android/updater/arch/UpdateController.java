package oxsource.android.updater.arch;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import androidx.core.content.FileProvider;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import oxsource.android.updater.listener.DownloadListener;
import oxsource.android.updater.listener.UpdateValidator;
import oxsource.android.updater.view.UpdateNotification;

/**
 * 更新控制器
 * Created by peng on 2017/9/23.
 */

public final class UpdateController implements Handler.Callback {
    private static String FILE_PROVIDER_AUTHORITIES = "";
    //整型常量
    private final int WHAT_VERIFY_START = 10;
    private final int WHAT_VERIFY_SUCCESS = 11;
    private final int WHAT_VERIFY_FAILURE = 12;

    private final int WHAT_DOWNLOAD_START = 20;
    private final int WHAT_DOWNLOAD_PROGRESS = 21;
    private final int WHAT_DOWNLOAD_SUCCESS = 22;
    private final int WHAT_DOWNLOAD_FAILURE = 23;

    //字符串常量
    private final static String DOWNLOAD_PATH_DEFAULT = "download";
    private final static String SUFFIX_APK = ".apk";
    private final static String METHOD_POST = "POST";
    private final static String KEY_CHARSET = "Charset";
    private final static String CHARSET_DEFAULT = "UTF-8";
    private final static String MIME_TYPE_APK = "application/vnd.android.package-archive";
    private final static String ERROR_DEFAULT_VALIDATE = "检查版本信息异常";
    private final static String ERROR_DEFAULT_DOWNLOAD = "下载更新异常";
    private final int TIME_OUT_MS = 15 * 1000;
    //监听回调
    private final Handler handler = new Handler(this);
    private UpdateValidator validator;
    private DownloadListener dListener;
    private UpdateNotification notification;

    public static void authority(String authorities) {
        FILE_PROVIDER_AUTHORITIES = authorities;
    }

    public String getTag() {
        return toString();
    }

    public void validator(UpdateValidator validator) {
        this.validator = validator;
    }

    public void downloadListener(DownloadListener dListener) {
        this.dListener = dListener;
    }

    public void notification(UpdateNotification ntf) {
        if (null != notification) {
            notification.cancel();
        }
        notification = ntf;
    }


    public void verify(final String verifyUrl) {
        HttpURLConnection conn = null;
        BufferedInputStream bis = null;
        try {
            notifyHandler(WHAT_VERIFY_START, null);
            //配置远程连接
            URL url = new URL(verifyUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT_MS);
            conn.setConnectTimeout(TIME_OUT_MS);
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
            bos.reset();
            UpdateVersion value = validator.onVerify(bos.toString(CHARSET_DEFAULT));
            notifyHandler(WHAT_VERIFY_SUCCESS, value);
        } catch (Exception e) {
            e.printStackTrace();
            notifyHandler(WHAT_VERIFY_FAILURE, ERROR_DEFAULT_VALIDATE);
        } finally {
            quitHttp(conn);
            quitClose(bis);
        }
    }

    public void download(String apkPath, String apkUrl) {
        HttpURLConnection conn = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        try {
            notifyHandler(WHAT_DOWNLOAD_START, null);
            //配置本地下载路径
            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                throw new IllegalStateException("没有检测到可用的SD卡");
            }
            if (!apkPath.endsWith(SUFFIX_APK)) {
                apkPath += SUFFIX_APK;
            }
            File dPath = new File(Environment.getExternalStorageDirectory(), DOWNLOAD_PATH_DEFAULT);
            File apkFile = new File(dPath, apkPath);
            if (!apkFile.getParentFile().exists()) {
                if (!apkFile.getParentFile().mkdirs()) {
                    int a = 1;
                    System.out.print(a);
                    Log.d("", "mkdirs return failure");
                }
            }
            fos = new FileOutputStream(apkFile);
            //配置远程连接
            URL url = new URL(apkUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT_MS);
            conn.setConnectTimeout(TIME_OUT_MS);
            conn.setRequestProperty(KEY_CHARSET, CHARSET_DEFAULT);
            conn.connect();
            //读取
            bis = new BufferedInputStream(conn.getInputStream());
            byte[] buffer = new byte[10 * 1024];
            final int[] POSITIONS = new int[]{conn.getContentLength(), 0};
            int len;
            long lastReadMs = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                POSITIONS[1] += len;
                long nowReadMs = System.currentTimeMillis();
                int TIME_INTERVAL_MS = 500;
                if ((nowReadMs - lastReadMs) >= TIME_INTERVAL_MS) {
                    notifyHandler(WHAT_DOWNLOAD_PROGRESS, POSITIONS);
                    lastReadMs = nowReadMs;
                }
            }
            notifyHandler(WHAT_DOWNLOAD_SUCCESS, apkFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            notifyHandler(WHAT_DOWNLOAD_FAILURE, ERROR_DEFAULT_DOWNLOAD);
        } finally {
            quitHttp(conn);
            quitClose(bis);
            quitClose(fos);
        }

    }

    public void install(Application context, String path) {
        notification(null);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(path);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITIES, file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(file);
            }
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, MIME_TYPE_APK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                validator.onSuccess((UpdateVersion) message.obj);
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
                if (null != notification) {
                    notification.onProgress(position[1], position[0]);
                }
                break;
            case WHAT_DOWNLOAD_START:
                dListener.onStart();
                if (null != notification) {
                    notification.onStart();
                }
                break;
            case WHAT_DOWNLOAD_FAILURE:
                String error = (String) message.obj;
                dListener.onFailure(-1, error);
                if (null != notification) {
                    notification.onFailure(-1, error);
                }
                break;
            case WHAT_DOWNLOAD_SUCCESS:
                String path = (String) message.obj;
                dListener.onSuccess(path);
                if (null != notification) {
                    notification.onSuccess(path);
                }
                break;
            default:
                finish = false;
                break;
        }
        return finish;
    }
}