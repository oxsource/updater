package oxsource.android.updater.arch;

/**
 * 版本更新信息接口
 * Created by peng on 2017/9/26.
 */

public interface UpdateVersion {
    /**
     * 应用名称
     */
    String getAppName();

    /**
     * 版本名称
     */
    String getVersionName();

    /**
     * 文件大小
     */
    String getFileSize();

    /**
     * 包名
     */
    String getPackageName();

    /**
     * 文件下载路径
     */
    String getFilePath();

    /**
     * 更新发布时间
     */
    String getUpdateTime();

    /**
     * 更新说明
     */
    String getUpdateDesc();

    /**
     * 是否强制更新
     */
    boolean force();
}