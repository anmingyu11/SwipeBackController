package com.amy.swipebackcontroller;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private MainActivity mMainActivity;

    public static final String EXTRA_CONTENT_BACKGROUND = "contentBackground";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainActivity = this;
    }

    public void startActivity(View view) {
        Intent intent = new Intent();
        Bitmap bitmap = Util.takeScreenShot(mMainActivity);
        byte[] bytes = Util.bitmapToBytes(bitmap);
        intent.putExtra(EXTRA_CONTENT_BACKGROUND, bytes);
        intent.setClass(MainActivity.this, SecondActivity.class);
        startActivity(intent);
        //如果设置windowTranslucent = true ,此选项会失效，所以需要传一个截图过去。
        overridePendingTransition(R.anim.anim_activity_slide_in_right, R.anim.anim_activity_fade_out);
    }

    public void screenShot(View view) {
        findViewById(R.id.screen_shot).setBackground(new BitmapDrawable(Util.takeScreenShot(this)));
    }
}