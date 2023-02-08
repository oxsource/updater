package oxsource.android.updater.arch;

/**
 * 版本更新信息接口
 * Created by peng on 2017/9/26.
 */

public interface UpdateVersion {
    /**
     * 应用名称
     */
    String name();

    /**
     * 版本号
     */
    int versionCode();

    /**
     * 版本名称
     */
    String versionName();

    /**
     * 文件大小
     */
    String fileSize();

    /**
     * 下载文件名称，APK英文名
     */
    String fileName();

    /**
     * 文件MD5校验
     */
    String fileMd5();

    /**
     * 文件下载路径
     */
    String filePath();

    /**
     * 更新发布时间
     */
    String updateTime();

    /**
     * 更新说明
     */
    String updateDesc();

    /**
     * 是否强制更新
     */
    boolean force();
}