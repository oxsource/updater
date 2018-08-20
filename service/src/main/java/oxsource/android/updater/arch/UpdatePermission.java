package oxsource.android.updater.arch;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 更新权限
 * Created by peng on 2017/9/26.
 */

public class UpdatePermission {
    public static final int REQUEST_DOWNLOAD = 10;

    public static boolean ensureDownload(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        List<String> list = new ArrayList<>(2);
        for (String permission : permissions) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                list.add(permission);
            }
        }
        if (0 == list.size()) return true;
        String[] ps = new String[list.size()];
        list.toArray(ps);
        activity.requestPermissions(ps, REQUEST_DOWNLOAD);
        return false;
    }
}