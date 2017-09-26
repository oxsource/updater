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
    /*授权*/
    public static void auth(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            for (int i = 0; i < permissions.length; i++) {
                if (activity.checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    activity.requestPermissions(new String[]{permissions[i]}, 0);
                }
            }
        }
    }
}