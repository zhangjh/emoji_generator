package me.zhangjh.emoji.generator;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

public class LoadingDialog extends Dialog {
    public LoadingDialog(Context context) {
        super(context, R.style.loading_dialog);
        initView();
    }

    private void initView(){
        setContentView(R.layout.activity_loading_dialog);
        setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.alpha = 0.8f;
        getWindow().setAttributes(attributes);
        setCancelable(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.loading_animation);
        animation.setInterpolator(new LinearInterpolator());
        findViewById(R.id.loading_dialog_img).startAnimation(animation);
    }

    @Override
    protected void onStop() {
        super.onStop();
        findViewById(R.id.loading_dialog_img).clearAnimation();
    }

    @Override
    public void onBackPressed() {
    }
}
