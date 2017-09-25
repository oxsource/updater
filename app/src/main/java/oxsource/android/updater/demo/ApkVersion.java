package oxsource.android.updater.demo;

import org.json.JSONObject;

/**
 * APK版本信息
 * Created by peng on 2017/9/25.
 */

public class ApkVersion {
    private String name;//APP名称
    private int versionCode;//版本号
    private String versionName;//版本名称
    private String updateDesc;//更新提示
    private String downloadPath;//文件远程下载地址
    private boolean force;//强制升级

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public int versionCode() {
        return versionCode;
    }

    public void versionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String versionName() {
        return versionName;
    }

    public void versionName(String versionName) {
        this.versionName = versionName;
    }

    public String updateDesc() {
        return updateDesc;
    }

    public void updateDesc(String updateDesc) {
        this.updateDesc = updateDesc;
    }

    public String downloadPath() {
        return downloadPath;
    }

    public void downloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public boolean force() {
        return force;
    }

    public void force(boolean force) {
        this.force = force;
    }

    @Override
    public String toString() {
        return "name=" + name +
                ", versionCode=" + versionCode +
                ", versionName=" + versionName +
                ", updateDesc=" + updateDesc +
                ", downloadPath=" + downloadPath;
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