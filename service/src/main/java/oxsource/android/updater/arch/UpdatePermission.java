package oxsource.android.updater.arch;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * 更新权限
 * Created by peng on 2017/9/26.
 */

public class UpdatePermission {
    /**
     * 更新授权
     *
     * @param activity
     * @param authorities 7.0+版本FileProvider的authorities
     */
    public static void auth(@NonNull Activity activity, String authorities) {
        UpdateController.FILE_PROVIDER_AUTHORITIES = authorities;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            for (String permission : permissions) {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    activity.requestPermissions(new String[]{permission}, 0);
                }
            }
        }
    }
}