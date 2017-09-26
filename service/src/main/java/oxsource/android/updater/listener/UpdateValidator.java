package oxsource.android.updater.listener;

import oxsource.android.updater.arch.UpdateVersion;

/**
 * 版本升级校验器
 * Created by peng on 2017/9/23.
 */

public interface UpdateValidator {
    void onStart();

    UpdateVersion onVerify(String string) throws Exception;

    void onFailure(int code, String message);

    void onSuccess(UpdateVersion value);
}