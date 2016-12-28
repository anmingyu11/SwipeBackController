package com.amy.swipebackcontroller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;

public class SecondActivity extends Activity {

    private SwipeBackController swipeBackController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bitmap bitmap = Util.bytesToBitmap(getIntent().getByteArrayExtra(MainActivity.EXTRA_CONTENT_BACKGROUND));
        setContentView(R.layout.activity_second);

        final ViewGroup contentView = (ViewGroup) getWindow().getDecorView();

        swipeBackController = SwipeBackController
                .build(this)
                .enableScreenShotBackground(true)
                .setDecorBackgroundDrawable(new BitmapDrawable(bitmap));

        swipeBackController.addHandleXListener(new SwipeBackController.HandleXListener() {
            @Override
            public void handleX(float x, float distanceX, float fractionX) {
                contentView.getChildAt(0).setScaleX(0.95f + 0.05f * fractionX);
                contentView.getChildAt(0).setScaleY(0.95f + 0.05f * fractionX);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (swipeBackController.processEvent(ev)) {
            return true;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_activity_fade_in, R.anim.anim_activity_slide_out_right);
    }

    @Override
    public void finish() {
        super.finish();
    }
}