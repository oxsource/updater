package oxsource.android.updater.arch;

/**
 * 版本更新信息接口
 * Created by peng on 2017/9/26.
 */

public interface UpdateVersion {
    /**
     * 应用名称
     *
     * @return
     */
    String name();

    /**
     * 应用名称
     *
     * @return
     */
    String englishName();

    /**
     * 版本号
     *
     * @return
     */
    int versionCode();

    /**
     * 版本名称
     *
     * @return
     */
    String versionName();

    /**
     * 文件大小
     *
     * @return
     */
    String fileSize();

    /**
     * 文件MD5校验
     *
     * @return
     */
    String fileMd5();

    /**
     * 文件下载路径
     *
     * @return
     */
    String filePath();

    /**
     * 更新发布时间
     *
     * @return
     */
    String publishTime();

    /**
     * 更新说明
     *
     * @return
     */
    String updateDesc();

    /**
     * 是否强制更新
     *
     * @return
     */
    boolean force();
}