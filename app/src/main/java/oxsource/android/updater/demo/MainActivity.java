package oxsource.android.updater.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import oxsource.android.updater.UpdateHandle;
import oxsource.android.updater.arch.UpdateController;
import oxsource.android.updater.arch.UpdateVersion;
import oxsource.android.updater.listener.UpdateValidator;
import oxsource.android.updater.view.UpdateFragment;

public class MainActivity extends FragmentActivity {
    private TextView tvInfo;
    private Button btVerify;
    private Button btCancel;

    private UpdateHandle updateHandle;
    private UpdateFragment updateFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateFragment = new UpdateFragment();

        tvInfo = (TextView) findViewById(R.id.tvInfo);
        btVerify = (Button) findViewById(R.id.btVerify);
        btVerify.setOnClickListener(cklVerify);
        btCancel = (Button) findViewById(R.id.btCancel);
        btCancel.setOnClickListener(cklCancel);
        btCancel.setVisibility(View.GONE);
        UpdateController.authority(getPackageName() + ".fileProvider");
    }


    private final View.OnClickListener cklVerify = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (null != updateHandle) {
                updateHandle.destroy();
            }
            updateHandle = new UpdateHandle.Builder()
                    .what(UpdateHandle.WHAT_VERIFY)
                    .url("http://ojnvtxoyg.bkt.clouddn.com/apk_version.json")
                    .validator(validator)
                    .build();
            updateHandle.intent(getBaseContext());
        }
    };

    private final View.OnClickListener cklCancel = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            btVerify.setVisibility(View.VISIBLE);
            if (null != updateHandle) {
                updateHandle.destroy();
                updateHandle = null;
            }
        }
    };

    private final UpdateValidator validator = new UpdateValidator() {
        @Override
        public void onStart() {
            btVerify.setVisibility(View.GONE);
            tvInfo.setText("正在检查更新...");
            btCancel.setVisibility(View.VISIBLE);
        }

        @Override
        public ApkVersion onVerify(String string) throws Exception {
            return ApkVersion.fromJson(getApplicationContext(), string);
        }

        @Override
        public void onFailure(int code, String message) {
            showToast(message);
            tvInfo.setText("检查更新失败");
            btCancel.setVisibility(View.GONE);
            btVerify.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess(UpdateVersion value) {
            tvInfo.setText("");
            btCancel.setVisibility(View.GONE);
            btVerify.setVisibility(View.VISIBLE);
            updateFragment.setApkVersion(value);
            updateFragment.show(getSupportFragmentManager(), "");
        }
    };

    private void showToast(String toast) {
        if (!TextUtils.isEmpty(toast)) {
            Toast.makeText(getBaseContext(), toast, Toast.LENGTH_SHORT).show();
        }
    }
}