package oxsource.android.updater.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import oxsource.android.updater.arch.UpdateVersion;
import oxsource.android.updater.listener.DownloadListener;

/**
 * Created by peng on 2017/9/26.
 */

public class UpdateFragment extends DialogFragment {
    private final String TAG = UpdateFragment.class.getSimpleName();
    private final int PROGRESS_MAX = 100;
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

    //字符串文案
    private final String STR_DOWNLOAD_NOW = "立即更新";
    private final String STR_DOWNLOAD_CANCEL = "取消下载";
    private final String STR_NEXT_TIME = "下次再说";

    private final String STR_DOWNLOAD_AGAIN = "重新下载";

    private UpdateVersion apkVersion;
    private UpdateHandle updateHandle;
    private DownloadListener outDownloadListener;//外部注册的下载监听器

    @Nullable
    @Override
    public View onCreateView(LayoutInflater lf, @Nullable ViewGroup vg, @Nullable Bundle bundle) {
        View view = lf.inflate(R.layout.update_dialog, null, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //
        tvName = (TextView) view.findViewById(R.id.tvName);
        tvName.getPaint().setFakeBoldText(true);
        tvVersion = (TextView) view.findViewById(R.id.tvVersion);
        tvUpdateDescTitle = (TextView) view.findViewById(R.id.tvUpdateDescTitle);
        tvUpdateDesc = (TextView) view.findViewById(R.id.tvUpdateDesc);
        //
        rlDownloadProgress = (RelativeLayout) view.findViewById(R.id.rlDownloadProgress);
        pbDownload = (ProgressBar) view.findViewById(R.id.pbDownload);
        tvPercent = (TextView) view.findViewById(R.id.tvPercent);
        //
        btLeft = (Button) view.findViewById(R.id.btLeft);
        lineBtMiddle = view.findViewById(R.id.lineBtMiddle);
        btRight = (Button) view.findViewById(R.id.btRight);
        initWidgets();
        return view;
    }

    public void setDownloadListener(DownloadListener listener) {
        outDownloadListener = listener;
    }

    private void initWidgets() {
        if (null == apkVersion) {
            setCancelable(false);
            tvName.setText("提示");
            setBottomButton("知道了", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            }, "", null);
            tvVersion.setVisibility(View.GONE);
            tvUpdateDesc.setText("未发现新版本更新信息");
            tvUpdateDescTitle.setVisibility(View.GONE);
        } else {
            setCancelable(false);
            tvName.setText(apkVersion.name());
            //
            StringBuilder sb = new StringBuilder();
            sb.append("新版本号：" + apkVersion.versionName() + "\n");
            sb.append("文件大小：" + apkVersion.fileSize() + "\n");
            sb.append("更新时间：" + apkVersion.updateTime());
            tvVersion.setVisibility(View.VISIBLE);
            tvVersion.setText(sb.toString());
            //
            tvUpdateDescTitle.setVisibility(View.VISIBLE);
            tvUpdateDesc.setText(apkVersion.updateDesc());
            //
            if (apkVersion.force()) {
                setBottomButton(STR_DOWNLOAD_NOW, clkDownload, "", null);
            } else {
                setBottomButton(STR_NEXT_TIME, clkCancel, STR_DOWNLOAD_NOW, clkDownload);
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
        if (progress >= 0 && progress <= PROGRESS_MAX) {
            if (rlDownloadProgress.getVisibility() != View.VISIBLE) {
                rlDownloadProgress.setVisibility(View.VISIBLE);
            }
            tvPercent.setText(String.format("%02d", progress) + "%");
            pbDownload.setProgress(progress);
        } else {
            rlDownloadProgress.setVisibility(View.GONE);
            pbDownload.setMax(PROGRESS_MAX);
        }
    }

    public void setApkVersion(UpdateVersion apkVersion) {
        this.apkVersion = apkVersion;
    }

    /*点击下载*/
    private View.OnClickListener clkDownload = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (null != updateHandle) {
                updateHandle.destroy();
            }
            updateHandle = new UpdateHandle.Builder()
                    .what(UpdateHandle.WHAT_DOWNLOAD)
                    .path(apkVersion.fileName())
                    .url(apkVersion.filePath())
                    .download(downloadListener)
                    .build();
            updateHandle.intent(getContext());
        }
    };

    /*点击取消*/
    private View.OnClickListener clkCancel = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (null != updateHandle) {
                updateHandle.destroy();
                updateHandle = null;
            }
            dismiss();
        }
    };

    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onStart() {
            setDownloadProgress(0);
            setBottomButton(STR_DOWNLOAD_CANCEL, clkCancel, "", null);
            if (null != outDownloadListener) {
                outDownloadListener.onStart();
            }
        }

        @Override
        public void onProgress(int current, int total) {
            int progress = (int) (current / (total / 1.0) * 100);
            setDownloadProgress(progress);
            if (null != outDownloadListener) {
                outDownloadListener.onProgress(current, total);
            }
        }

        @Override
        public void onFailure(int code, String message) {
            Log.d(TAG, message);
            showToast("下载出错！");
            setDownloadProgress(-1);
            setBottomButton(STR_DOWNLOAD_AGAIN, clkDownload, STR_NEXT_TIME, clkCancel);
            if (null != outDownloadListener) {
                outDownloadListener.onFailure(code, message);
            }
        }

        @Override
        public void onSuccess(String filePath) {
            setDownloadProgress(-1);
            setBottomButton("", null, "", null);
            dismiss();
            new UpdateHandle.Builder()
                    .what(UpdateHandle.WHAT_INSTALL)
                    .path(filePath)
                    .build()
                    .intent(getContext());
            if (null != outDownloadListener) {
                outDownloadListener.onSuccess(filePath);
            }
        }
    };

    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}