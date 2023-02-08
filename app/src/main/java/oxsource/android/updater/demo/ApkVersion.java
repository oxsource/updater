package oxsource.android.updater.demo;

import org.json.JSONObject;

import oxsource.android.updater.arch.UpdateVersion;

/**
 * APK版本信息
 * Created by peng on 2017/9/25.
 */

public class ApkVersion implements UpdateVersion {
    private String appName;//APP名称
    private String versionName;//版本名称
    private String updateDesc;//更新提示
    private String downloadPath;//文件远程下载地址
    private boolean force;//强制升级
    private String packageName; //包名

    @Override
    public String getAppName() {
        return appName;
    }

    @Override
    public String getVersionName() {
        return versionName;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getFileSize() {
        return "";
    }

    @Override
    public String getFilePath() {
        return downloadPath;
    }

    @Override
    public String getUpdateTime() {
        return "";
    }

    @Override
    public String getUpdateDesc() {
        return updateDesc;
    }

    @Override
    public boolean force() {
        return force;
    }

    public void setAppName(String name) {
        this.appName = name;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setUpdateDesc(String updateDesc) {
        this.updateDesc = updateDesc;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public static ApkVersion fromJson(String json) throws Exception {
        JSONObject jbt = new JSONObject(json);
        ApkVersion version = new ApkVersion();
        version.setAppName(jbt.optString("name"));
        version.setVersionName(jbt.optString("versionName"));
        version.setUpdateDesc(jbt.optString("upgradeFeature"));
        version.setDownloadPath(jbt.optString("downloadUrl"));
        version.setForce(jbt.optBoolean("forceUpgrade"));
        return version;
    }
}