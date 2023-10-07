package me.zhangjh.emoji.generator;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class DownloadListener {

    private final Context context;
    private final String url;

    public DownloadListener(Context context, String url) {
        this.url = url;
        this.context = context;
    }

    public void doDownload() {
        Log.d("dowloadPic", "url: " + url);
        if(StringUtils.isEmpty(url)) {
            Toast.makeText(context,  R.string.empty_url, Toast.LENGTH_LONG).show();
            return;
        }
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if(!directory.exists()) {
            directory.mkdirs();
        }
        Glide.with(context).load(url).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                ContentResolver resolver = context.getContentResolver();
                try {
                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                    Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    if(uri != null) {
                        try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                            if (outputStream != null) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            }
                        }
                    }
                    Toast.makeText(context,  context.getString(R.string.save_pic_pre) +
                            contentValues.get(MediaStore.MediaColumns.RELATIVE_PATH) + "/" +
                            contentValues.get(MediaStore.MediaColumns.DISPLAY_NAME), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
