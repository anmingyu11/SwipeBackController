package com.amy.swipebackcontroller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;

/**
 * Created by amy on 16-12-28.
 */

public class Util {

    public static Bitmap takeScreenShotWithoutStatusBar(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap screenShot = null;
        try {
            screenShot = view.getDrawingCache();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        Log.d("amy", "status bar height : " + statusBarHeight);
        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        // 去掉标题栏
        Bitmap realScreenShot = Bitmap.createBitmap(screenShot, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return realScreenShot;
    }

    public static Bitmap takeScreenShot(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap screenShot = null;
        try {
            screenShot = view.getDrawingCache();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        Bitmap realScreenShot = Bitmap.createBitmap(screenShot, 0, 0, width, height);

        view.destroyDrawingCache();
        return realScreenShot;
    }

    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static Bitmap bytesToBitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
