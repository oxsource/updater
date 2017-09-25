package oxsource.android.updater.listener;

/**
 * 版本升级校验器
 * Created by peng on 2017/9/23.
 */

public interface UpdateValidator<T> {
    void onStart();

    T onVerify(String string) throws Exception;

    void onFailure(int code, String message);

    void onSuccess(T value);
}