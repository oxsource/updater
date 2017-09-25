package oxsource.android.updater.listener;

/**
 * 下载更新接口
 * Created by peng on 2017/9/23.
 */

public interface DownloadListener {
    void onStart();

    void onProgress(int current, int total);

    void onFailure(int code, String message);

    void onSuccess(String filePath);
}