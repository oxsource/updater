package oxsource.android.updater.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import oxsource.android.updater.R;
import oxsource.android.updater.UpdateHandle;
import oxsource.android.updater.arch.UpdatePermission;
import oxsource.android.updater.arch.UpdateVersion;
import oxsource.android.updater.listener.DownloadListener;

/**
 * Created by peng on 2017/9/26.
 */

public class UpdateFragment extends DialogFragment {
    private final String TAG = UpdateFragment.class.getSimpleName();
    //基本信息模块
    private TextView tvName;
    private TextView tvVersion;
    private TextView tvUpdateDescTitle;
    private TextView tvUpdateDesc;
    //下载进度模块
    private RelativeLayout rlDownloadProgress;
    private ProgressBar pbDownload;
    private TextView tvPercent;
    //底部按钮
    private Button btLeft;
    private View lineBtMiddle;
    private Button btRight;

    private static final String STR_NEXT_TIME = "下次再说";

    private UpdateVersion version;
    private UpdateHandle updateHandle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater lf, @Nullable ViewGroup root, @Nullable Bundle bundle) {
        View view = lf.inflate(R.layout.update_dialog, root, false);
        Dialog dialog = getDialog();
        if (null != dialog) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        //
        tvName = view.findViewById(R.id.tvName);
        tvName.getPaint().setFakeBoldText(true);
        tvVersion = view.findViewById(R.id.tvVersion);
        tvUpdateDescTitle = view.findViewById(R.id.tvUpdateDescTitle);
        tvUpdateDesc = view.findViewById(R.id.tvUpdateDesc);
        //
        rlDownloadProgress = view.findViewById(R.id.rlDownloadProgress);
        pbDownload = view.findViewById(R.id.pbDownload);
        tvPercent = view.findViewById(R.id.tvPercent);
        //
        btLeft = view.findViewById(R.id.btLeft);
        lineBtMiddle = view.findViewById(R.id.lineBtMiddle);
        btRight = view.findViewById(R.id.btRight);
        initWidgets();
        return view;
    }

    private void initWidgets() {
        if (null == version) {
            setCancelable(false);
            tvName.setText("提示");
            setBottomButton("知道了", v -> dismiss(), "", null);
            tvVersion.setVisibility(View.GONE);
            tvUpdateDesc.setText("未发现新版本更新信息");
            tvUpdateDescTitle.setVisibility(View.GONE);
        } else {
            setCancelable(false);
            String title = "发现新版本";
            if (!TextUtils.isEmpty(version.getAppName())) {
                title = version.getAppName();
            }
            tvName.setText(title);
            //
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(version.getVersionName())) {
                sb.append("新版本号：").append(version.getVersionName()).append("\n");
            }
            if (!TextUtils.isEmpty(version.getFileSize())) {
                sb.append("文件大小：").append(version.getFileSize()).append("\n");
            }
            if (!TextUtils.isEmpty(version.getUpdateTime())) {
                sb.append("更新时间：").append(version.getUpdateTime());
            }
            String version = sb.toString();
            tvVersion.setVisibility(version.isEmpty() ? View.GONE : View.VISIBLE);
            tvVersion.setText(version);
            //
            tvUpdateDescTitle.setVisibility(View.VISIBLE);
            tvUpdateDesc.setText(this.version.getUpdateDesc());
            //
            String download = "立即更新";
            if (this.version.force()) {
                setBottomButton(download, clkDownload, "", null);
            } else {
                setBottomButton(STR_NEXT_TIME, clkCancel, download, clkDownload);
            }
            //下载进度初始化
            setDownloadProgress(-1);
        }
    }

    /*设置底部按钮*/
    private void setBottomButton(String lText, View.OnClickListener lClk, String rText, View.OnClickListener rClk) {
        lineBtMiddle.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(lText)) {
            btLeft.setVisibility(View.GONE);
            lineBtMiddle.setVisibility(View.GONE);
            btLeft.setText("");
        } else {
            btLeft.setVisibility(View.VISIBLE);
            btLeft.setText(lText);
        }
        btLeft.setOnClickListener(lClk);
        //
        if (TextUtils.isEmpty(rText)) {
            btRight.setVisibility(View.GONE);
            lineBtMiddle.setVisibility(View.GONE);
            btRight.setText("");
        } else {
            btRight.setVisibility(View.VISIBLE);
            btRight.setText(rText);
        }
        btRight.setOnClickListener(rClk);
    }

    /*设置下载进度*/
    private void setDownloadProgress(int progress) {
        int maxProcess = 100;
        if (progress >= 0 && progress <= maxProcess) {
            if (rlDownloadProgress.getVisibility() != View.VISIBLE) {
                rlDownloadProgress.setVisibility(View.VISIBLE);
            }
            String progressText = progress + "%";
            tvPercent.setText(progressText);
            pbDownload.setProgress(progress);
        } else {
            rlDownloadProgress.setVisibility(View.GONE);
            pbDownload.setMax(maxProcess);
        }
    }

    public void setVersion(UpdateVersion version) {
        this.version = version;
    }


    /*点击下载*/
    private final View.OnClickListener clkDownload = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (null != updateHandle) {
                updateHandle.destroy();
            }
            Activity activity = getActivity();
            if (null == activity || activity.isFinishing()) return;
            //确认下载权限
            startDownload();
        }
    };

    //开始下载
    private boolean startDownload() {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) return false;
        //确认下载权限
        if (!UpdatePermission.ensureDownload(activity)) return false;
        String apkName = version.getPackageName() + "_" + version.getVersionName();
        updateHandle = new UpdateHandle.Builder()
                .what(UpdateHandle.WHAT_DOWNLOAD)
                .path(apkName)
                .url(version.getFilePath())
                .download(downloadListener)
                .build();
        updateHandle.intent(activity);
        return true;
    }

    /*点击取消*/
    private final View.OnClickListener clkCancel = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (null != updateHandle) {
                updateHandle.destroy();
                updateHandle = null;
            }
            dismiss();
            if (null != version && version.force()) {
                Activity activity = getActivity();
                if (null == activity) return;
                if (activity.isFinishing() || activity.isDestroyed()) return;
                activity.finish();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (UpdatePermission.REQUEST_DOWNLOAD == requestCode) {
            if (startDownload()) return;
            showToast("未能获取读写权限");
        }
    }

    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onStart() {
            setDownloadProgress(0);
            setBottomButton("取消下载", clkCancel, "", null);
        }

        @Override
        public void onProgress(int current, int total) {
            int progress = (int) (current / total * 100);
            setDownloadProgress(progress);
        }

        @Override
        public void onFailure(int code, String message) {
            Log.d(TAG, message);
            showToast("下载出错！");
            setDownloadProgress(-1);
            setBottomButton("重新下载", clkDownload, STR_NEXT_TIME, clkCancel);
        }

        @Override
        public void onSuccess(String filePath) {
            setDownloadProgress(-1);
            setBottomButton("", null, "", null);
            dismiss();
            if (TextUtils.isEmpty(filePath)) return;
            Activity activity = getActivity();
            if (null == activity || activity.isFinishing()) return;
            //确认安装权限
            new UpdateHandle.Builder()
                    .what(UpdateHandle.WHAT_INSTALL)
                    .path(filePath)
                    .build()
                    .intent(activity);
        }
    };


    private void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) return;
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}