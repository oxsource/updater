package oxsource.android.updater.demo;

import org.json.JSONObject;

import oxsource.android.updater.arch.UpdateVersion;

/**
 * APK版本信息
 * Created by peng on 2017/9/25.
 */

public class ApkVersion implements UpdateVersion {
    private String name;//APP名称
    private int versionCode;//版本号
    private String versionName;//版本名称
    private String updateDesc;//更新提示
    private String downloadPath;//文件远程下载地址
    private boolean force;//强制升级

    @Override
    public String name() {
        return name;
    }

    @Override
    public int versionCode() {
        return versionCode;
    }

    @Override
    public String versionName() {
        return versionName;
    }

    @Override
    public String fileName() {
        return "test." + versionName();
    }

    @Override
    public String fileSize() {
        return "8.4M";
    }

    @Override
    public String fileMd5() {
        return "0CA175B9C0F726A831D895E269332461";
    }

    @Override
    public String filePath() {
        return downloadPath;
    }

    @Override
    public String updateTime() {
        return "2017-09-26 18:45:21";
    }

    @Override
    public String updateDesc() {
        return updateDesc;
    }

    @Override
    public boolean force() {
        return force;
    }

    public void name(String name) {
        this.name = name;
    }

    public void versionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void versionName(String versionName) {
        this.versionName = versionName;
    }

    public void updateDesc(String updateDesc) {
        this.updateDesc = updateDesc;
    }

    public void downloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public void force(boolean force) {
        this.force = force;
    }

    public static ApkVersion fromJson(String json) throws Exception {
        JSONObject jbt = new JSONObject(json);
        ApkVersion version = new ApkVersion();
        version.name(jbt.optString("name"));
        version.versionCode(jbt.optInt("versionCode"));
        version.versionName(jbt.optString("versionName"));
        version.updateDesc(jbt.optString("updateDesc"));
        version.downloadPath(jbt.optString("downloadPath"));
        version.force(jbt.optBoolean("force"));
        return version;
    }
}