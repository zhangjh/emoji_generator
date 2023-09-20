package me.zhangjh.emoji.emoji.generator;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.zhangjh.emoji.emoji.generator.entity.ImgItem;

public class MainActivity extends AppCompatActivity {

    private LoadingDialog loadingDialog;

    private static Map<String, String> IMG_ITEMS = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void sampleDataInit() {
        IMG_ITEMS.put("https://replicate.delivery/pbxt/J6vOuC0Yj647JRa9YAUMq1vbGKFAiOreQcKuJmHLI0wQuawIA/out-0.png",
                getString(R.string.sample_emoji_tiger));
        IMG_ITEMS.put("https://replicate.delivery/pbxt/a3z81v5vwlKfLq1H5uBqpVmkHalOVup0jSLma9E2UaF3tawIA/out-0.png",
                getString(R.string.sample_emoji_man));
        IMG_ITEMS.put("https://replicate.delivery/pbxt/cNFerMxyBD1UfERJB29hHCGJujf0DShhcDWcaqxlX9aUfVDGB/out-0.png",
                getString(R.string.sample_emoji_llama));
        IMG_ITEMS.put("https://replicate.delivery/pbxt/DKFghOgmkTKVCpwgfIeKkTqMemHQMKtW9yxLYqeLyeonHtGMC/out-0.png",
                getString(R.string.sample_emoji_camera));
    }

    private void init() {
        // 初始化样例容器的展示
        sampleDataInit();
        RecyclerView sampleView = findViewById(R.id.samples);
        sampleView.setVisibility(View.VISIBLE);
        ImageAdapter adapter = new ImageAdapter(this.getApplicationContext());
        List<ImgItem> sampleItems = new ArrayList<>();

        for (Map.Entry<String, String> entry : IMG_ITEMS.entrySet()) {
            String url = entry.getKey();
            String prompt = entry.getValue();
            ImgItem imgItem = new ImgItem(url, prompt);
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
                loadingDialog.show();
                new NetworkRequestTask(getApplicationContext(), imgView1,
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
            // todo: 付费
            String url = imgView1.getTag().toString();
            File directory = getApplicationContext().getDir("download", MODE_PRIVATE);
            if(!directory.exists()) {
                directory.mkdirs();
            }
            Glide.with(this).load(url).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    ContentResolver resolver = getApplicationContext().getContentResolver();
                    try {
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                        File file = new File(directory, System.currentTimeMillis() + ".jpg");
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();
                        }
                        // 插入图片库
                        MediaStore.Images.Media.insertImage(resolver, file.getAbsolutePath(), file.getName(), null);
                        Toast.makeText(getApplicationContext(),  getString(R.string.save_pic_pre) + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

}