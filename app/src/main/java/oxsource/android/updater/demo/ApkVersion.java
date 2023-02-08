package oxsource.android.updater.demo;

import android.content.Context;

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
    //下载本地文件名称
    private String fileName;


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
        return fileName;
    }

    @Override
    public String fileSize() {
        return "";
    }

    @Override
    public String fileMd5() {
        return "";
    }

    @Override
    public String filePath() {
        return downloadPath;
    }

    @Override
    public String updateTime() {
        return "";
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

    public static ApkVersion fromJson(Context context, String json) throws Exception {
        JSONObject jbt = new JSONObject(json);
        ApkVersion version = new ApkVersion();
        version.name(jbt.optString("name"));
        version.versionCode(jbt.optInt("version"));
        version.versionName(jbt.optString("versionName"));
        version.updateDesc(jbt.optString("upgradeFeature"));
        version.downloadPath(jbt.optString("downloadUrl"));
        version.force(jbt.optBoolean("forceUpgrade"));
        version.fileName = context.getPackageName() + version.versionCode + ".apk";
        return version;
    }
}