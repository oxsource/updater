package oxsource.android.updater.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import oxsource.android.updater.UpdateService;
import oxsource.android.updater.listener.DownloadListener;
import oxsource.android.updater.listener.UpdateValidator;

public class MainActivity extends Activity {
    private TextView tvInfo;
    private Button btVerify;
    private Button btDownload;
    private Button btCancel;

    private ApkVersion version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvInfo = findViewById(R.id.tvInfo);

        btVerify = findViewById(R.id.btVerify);
        btVerify.setOnClickListener(cklVerify);

        btDownload = findViewById(R.id.btDownload);
        btDownload.setOnClickListener(cklDownload);
        btDownload.setVisibility(View.GONE);

        btCancel = findViewById(R.id.btCancel);
        btCancel.setOnClickListener(cklCancel);
        btCancel.setVisibility(View.GONE);

        UpdateService.checkSelfPermission(this);
    }


    private View.OnClickListener cklVerify = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new UpdateService.Builder()
                    .url("http://ojnvtxoyg.bkt.clouddn.com/apk_version.json")
                    .validator(validator)
                    .start(getBaseContext());
        }
    };

    private View.OnClickListener cklDownload = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (null == version) {
                showToast("未获取到APP版本信息");
                return;
            }
            new UpdateService.Builder()
                    .url(version.downloadPath())
                    .download(downloadListener)
                    .start(getBaseContext());
        }
    };

    private View.OnClickListener cklCancel = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (null == version) {
                btVerify.setVisibility(View.VISIBLE);
            } else {
                btDownload.setVisibility(View.VISIBLE);
            }
            new UpdateService.Builder().validator(null).download(null);
        }
    };

    private UpdateValidator<ApkVersion> validator = new UpdateValidator<ApkVersion>() {
        @Override
        public void onStart() {
            btVerify.setVisibility(View.GONE);
            tvInfo.setText("正在检查更新...");
            btCancel.setVisibility(View.VISIBLE);
        }

        @Override
        public ApkVersion onVerify(String string) throws Exception {
            return ApkVersion.fromJson(string);
        }

        @Override
        public void onFailure(int code, String message) {
            showToast(message);
            btCancel.setVisibility(View.GONE);
            btVerify.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess(ApkVersion value) {
            version = value;
            tvInfo.setText(value.toString());
            btCancel.setVisibility(View.GONE);
            btDownload.setVisibility(View.VISIBLE);
        }
    };

    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onStart() {
            btDownload.setVisibility(View.GONE);
            btCancel.setVisibility(View.VISIBLE);
        }

        @Override
        public void onProgress(int current, int total) {
            tvInfo.setText("下载中：" + current + "/" + total);
        }

        @Override
        public void onFailure(int code, String message) {
            showToast(message);
            btDownload.setVisibility(View.VISIBLE);
            btCancel.setVisibility(View.GONE);
        }

        @Override
        public void onSuccess(String filePath) {
            version = null;
            tvInfo.setText("下载成功，准备安装...");
            btDownload.setVisibility(View.GONE);
            btCancel.setVisibility(View.GONE);
            btVerify.setVisibility(View.VISIBLE);
            new UpdateService.Builder()
                    .path(filePath)
                    .what(UpdateService.WHAT_INSTALL)
                    .start(getBaseContext());
        }
    };

    private void showToast(String toast) {
        if (!TextUtils.isEmpty(toast)) {
            Toast.makeText(getBaseContext(), toast, Toast.LENGTH_SHORT).show();
        }
    }
}