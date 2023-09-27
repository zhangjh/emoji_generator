package me.zhangjh.emoji.generator;

import android.content.ContentResolver;
import android.content.Context;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

import me.zhangjh.emoji.generator.entity.ImgItem;

public class MainActivity extends AppCompatActivity {

    private LoadingDialog loadingDialog;

    private Context context;

    private static final Map<Integer, String> IMG_ITEMS = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
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
            // todo: 付费
            runOnUiThread(() -> {
                GooglePayService payService = new GooglePayService(this, (purchase) -> {
                    System.out.println("purchase: " + purchase);
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
                    return null;
                });
                payService.getClient(getApplicationContext());
            });
        });
    }

}