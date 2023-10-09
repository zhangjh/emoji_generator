package me.zhangjh.emoji.generator;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.zhangjh.emoji.generator.entity.ImgItem;

public class MainActivity extends AppCompatActivity {

    private LoadingDialog loadingDialog;

    private Context context;

    private DownloadListener downloadListener;

    private static final Map<Integer, String> IMG_ITEMS = new HashMap<>();

    private static final int REQUEST_PER_CODE = 101;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bannerAds();
        init();
    }

    private void bannerAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void sampleDataInit() {
        IMG_ITEMS.put(R.drawable.tiger,
                getString(R.string.sample_emoji_tiger));
        IMG_ITEMS.put(R.drawable.llama,
                getString(R.string.sample_emoji_llama));
        IMG_ITEMS.put(R.drawable.man,
                getString(R.string.sample_emoji_man));
        IMG_ITEMS.put(R.drawable.camera,
                getString(R.string.sample_emoji_camera));
    }

    private void init() {
        context = this;
        // 初始化样例容器的展示
        sampleDataInit();
        RecyclerView sampleView = findViewById(R.id.samples);
        sampleView.setVisibility(View.VISIBLE);
        ImageAdapter adapter = new ImageAdapter(this.getApplicationContext());
        List<ImgItem> sampleItems = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : IMG_ITEMS.entrySet()) {
            Integer imgId = entry.getKey();
            String prompt = entry.getValue();
            ImgItem imgItem = new ImgItem(imgId.toString(), prompt);
            sampleItems.add(imgItem);
        }
        adapter.setImageList(sampleItems);
        sampleView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        sampleView.setAdapter(adapter);
        // 隐藏输出结果
        LinearLayout resultView = findViewById(R.id.resultView);
        resultView.setVisibility(View.GONE);
        ImageView imgView1 = findViewById(R.id.imgView1);
        // 初始化输入框监听事件
        loadingDialog = new LoadingDialog(this);
        SearchView searchView = findViewById(R.id.searchView);
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(TextUtils.isEmpty(query)) {
                    return false;
                }
                searchView.clearFocus();
                // 开始生成
                runOnUiThread(() -> loadingDialog.show());
                new NetworkRequestTask(context, imgView1,
                        searchView, sampleView, resultView, loadingDialog)
                        .execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        // 初始分享和下载监听事件
        ImageButton shareBtn = findViewById(R.id.shareBtn);
        ImageButton downloadBtn = findViewById(R.id.downloadBtn);
        shareBtn.setOnClickListener((v) -> {
            String url = imgView1.getTag().toString();
            Uri uri = Uri.parse(url);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(shareIntent);
        });
        downloadBtn.setOnClickListener((v) -> {
            String url = imgView1.getTag().toString();
            if(StringUtils.isNotEmpty(url)) {
                downloadPic(url);
            }
            // todo: 付费
//            runOnUiThread(() -> {
//                GooglePayService payService = new GooglePayService(this, (purchase) -> {
//                    System.out.println("purchase: " + purchase);
//                    doDownload(url);
//                    return null;
//                });
//                payService.getClient(getApplicationContext());
//            });
        });
    }

    private void downloadPic(String url) {
        Log.d("dowloadPic", "url: " + url);
        if(StringUtils.isNotEmpty(url)) {
            downloadListener = new DownloadListener(this, url);
            // 下载到download路径需要请求授权
            if(ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, REQUEST_PER_CODE);
            } else {
                downloadListener.doDownload();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PER_CODE) {
            if(Arrays.stream(grantResults).allMatch(item -> item == PackageManager.PERMISSION_GRANTED)) {
                Log.d("获取授权", "存储权限已授权");
                if(downloadListener != null) {
                    downloadListener.doDownload();
                }
            } else {
                // 授权失败
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    runOnUiThread(() -> startActivity(intent));
                });
            }
        }
    }
}