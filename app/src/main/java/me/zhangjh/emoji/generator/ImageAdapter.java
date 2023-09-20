package me.zhangjh.emoji.generator;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import me.zhangjh.emoji.generator.entity.ImgItem;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final Context context;
    private List<ImgItem> imageList = new ArrayList<>();

    public ImageAdapter(Context context) {
        this.context = context;
    }


    public void setImageList(List<ImgItem> imageList) {
        this.imageList = imageList;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageList.get(position).getUrl();
        if(url.startsWith("http")) {
            Glide.with(context).load(url).into(holder.imageView);
        } else {
            int resourceId = Integer.parseInt(url);
            Drawable drawable = ContextCompat.getDrawable(context, resourceId);
            if(drawable instanceof BitmapDrawable) {
                holder.imageView.setImageBitmap(((BitmapDrawable)drawable).getBitmap());
            }
        }
        holder.textView.setText(imageList.get(position).getDesc());
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        private final TextView textView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.sampleImgPrompt);
        }
    }
}
