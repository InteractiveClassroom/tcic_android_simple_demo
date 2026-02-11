package com.example.tcic_android_simple_demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qcloudclass.tcic.NativeViewCreator;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MainView 自定义 Native View 实现
 * 显示课程封面、老师信息、课程名称、上课时间
 */
public class MainViewNativeViewCreator implements NativeViewCreator {

    private static final String TAG = "MainViewNativeView";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View createView(Context context, int id, Object args) {
        // 根布局 - 使用 FrameLayout 实现背景图 + 内容叠加
        android.widget.FrameLayout rootLayout = new android.widget.FrameLayout(context) {
            @Override
            public boolean onInterceptTouchEvent(android.view.MotionEvent ev) {
                Log.d(TAG, "onInterceptTouchEvent: " + ev.getAction());
                return false;
            }
            
            @Override
            public boolean onTouchEvent(android.view.MotionEvent ev) {
                Log.d(TAG, "onTouchEvent: " + ev.getAction());
                return super.onTouchEvent(ev);
            }
        };
        rootLayout.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        ));
        rootLayout.setOnClickListener(v -> {
            Log.d(TAG, "rootLayout onClick - Native 点击事件触发！");
        });

        // 背景图
        ImageView backgroundImage = new ImageView(context);
        backgroundImage.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        ));
        backgroundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        backgroundImage.setBackgroundColor(Color.parseColor("#333333")); // 占位背景色
        backgroundImage.setClickable(false);
        backgroundImage.setFocusable(false);
        
        // 异步加载网络图片
        loadImageFromUrl(backgroundImage, "https://tcic-prod-1257307760.qcloudclass.com/doc/gqc7lpugu87e0sruvl2d_tiw/thumbnail/1.jpg");
        
        rootLayout.addView(backgroundImage);

        // 半透明遮罩层
        View overlay = new View(context);
        overlay.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        ));
        overlay.setBackgroundColor(Color.parseColor("#40000000"));
        overlay.setClickable(false);
        overlay.setFocusable(false);
        rootLayout.addView(overlay);

        // 内容布局
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER);
        contentLayout.setPadding(dp2px(context, 16), dp2px(context, 16), dp2px(context, 16), dp2px(context, 16));
        contentLayout.setClickable(false);
        contentLayout.setFocusable(false);
        android.widget.FrameLayout.LayoutParams contentParams = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        );
        contentLayout.setLayoutParams(contentParams);

        // 老师名称
        TextView teacherText = createTextView(context, "老师：小张22333", 18, "#D9FFFFFF");
        contentLayout.addView(teacherText);
        addVerticalSpace(contentLayout, context, 18);

        // 课程名称
        TextView courseText = createTextView(context, "腾讯云互动课堂测试", 16, "#D9FFFFFF");
        contentLayout.addView(courseText);
        addVerticalSpace(contentLayout, context, 18);

        // 上课时间
        TextView timeText = createTextView(context, "上课时间: 121312313", 14, "#D9FFFFFF");
        contentLayout.addView(timeText);

        rootLayout.addView(contentLayout);

        return rootLayout;
    }

    private void loadImageFromUrl(ImageView imageView, String imageUrl) {
        executor.execute(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();
                
                mainHandler.post(() -> {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private TextView createTextView(Context context, String text, int textSizeSp, String colorHex) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(textSizeSp);
        textView.setTextColor(Color.parseColor(colorHex));
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    private void addVerticalSpace(LinearLayout layout, Context context, int dpHeight) {
        View space = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp2px(context, dpHeight)
        );
        space.setLayoutParams(params);
        layout.addView(space);
    }

    private int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void disposeView(View view) {
        // 清理资源
    }
}


