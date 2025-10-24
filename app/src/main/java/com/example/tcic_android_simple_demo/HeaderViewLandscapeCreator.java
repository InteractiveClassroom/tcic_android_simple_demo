package com.example.tcic_android_simple_demo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qcloudclass.tcic.CustomLayoutType;
import com.qcloudclass.tcic.NativeViewCreator;
import com.qcloudclass.tcic.TCICBuilderItemAnimationType;
import com.qcloudclass.tcic.TCICCustomLayoutBuilderItem;
import com.qcloudclass.tcic.TCICManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 横屏模式下的头部视图创建器
 */
public class HeaderViewLandscapeCreator implements NativeViewCreator, LayoutManager.LayoutChangeListener {
    private static final String TAG = "HeaderLandscapeView";
    private static long lastClickTime = 0;
    private static final long CLICK_DEBOUNCE_TIME = 1000; // 1秒防抖
    
    // 布局相关变量
    private LinearLayout mainLayout;
    private LinearLayout titleLayout;
    private LinearLayout settingsLayout;
    private LinearLayout exitLayout;
    private Context context;
    private int viewId;
    private boolean isDisposed = false;

    @Override
    public View createView(Context context, int id, Object args) {
        this.isDisposed = false;
        this.context = context;
        this.viewId = id;
        
        Log.d(TAG, "=== HeaderViewLandscapeCreator 创建横屏视图 ===");
        
        // 创建主布局 - 横屏模式下纵向排列
        mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setBackgroundColor(Color.parseColor("#FF4444"));
        mainLayout.setPadding(dp2px(context, 16), dp2px(context, 16), dp2px(context, 16), dp2px(context, 16));

        // 创建横屏模式下的按钮布局
        createLandscapeButtonLayouts();
        
        // 注册布局变化监听器
        LayoutManager.addLayoutChangeListener(this);
        
        return mainLayout;
    }
    
    @Override
    public void onLayoutChanged(boolean isLandscape) {
        Log.d(TAG, "收到布局变化通知: isLandscape = " + isLandscape);
        // 横屏模式下不需要处理布局变化，因为已经是横屏布局
    }
    
    /**
     * 创建横屏模式下的按钮布局
     */
    private void createLandscapeButtonLayouts() {
        // 创建录制按钮 - 横屏模式
        titleLayout = createLandscapeButtonWithIcon(context,
                android.R.drawable.ic_menu_agenda, "录制", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TCICManager.toggleScreenRecording();
                    }
                }
        );
        mainLayout.addView(titleLayout);

        // 创建旋转按钮 - 横屏模式
        settingsLayout = createLandscapeButtonWithIcon(context,
                android.R.drawable.ic_menu_preferences, "旋转", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleRotationClick(v);
                    }
                }
        );
        mainLayout.addView(settingsLayout);

        // 创建白板按钮 - 横屏模式
        exitLayout = createLandscapeButtonWithIcon(context,
                android.R.drawable.ic_menu_close_clear_cancel, "白板", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleWhiteboardClick(v);
                    }
                }
        );
        mainLayout.addView(exitLayout);
        
        // 设置横屏模式下的布局参数
        setLandscapeLayoutParams();
    }
    
    /**
     * 设置横屏模式下的布局参数
     */
    private void setLandscapeLayoutParams() {
        // 横屏时纵向等分
        LinearLayout.LayoutParams equalParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1.0f
        );
        equalParams.setMargins(0, dp2px(context, 4), 0, dp2px(context, 4));
        
        // 更新所有按钮的布局参数
        if (titleLayout != null) titleLayout.setLayoutParams(equalParams);
        if (settingsLayout != null) settingsLayout.setLayoutParams(equalParams);
        if (exitLayout != null) exitLayout.setLayoutParams(equalParams);
    }
    
    /**
     * 创建横屏模式下的带图标按钮
     */
    private LinearLayout createLandscapeButtonWithIcon(Context context, int iconResId, String text, View.OnClickListener listener) {
        LinearLayout buttonLayout = new LinearLayout(context);

        // 横屏模式：横向布局，图标在左文字在右
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER_VERTICAL);
        buttonLayout.setBackgroundColor(Color.TRANSPARENT);
        buttonLayout.setOnClickListener(listener);

        // 添加图标
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconResId);
        icon.setColorFilter(Color.WHITE);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp2px(context, 24), dp2px(context, 24)
        );
        iconParams.setMargins(0, 0, dp2px(context, 8), 0); // 横屏时右边距
        icon.setLayoutParams(iconParams);
        buttonLayout.addView(icon);

        // 添加文本
        Button textBtn = new Button(context);
        textBtn.setText(text);
        textBtn.setTextColor(Color.WHITE);
        textBtn.setBackgroundColor(Color.TRANSPARENT);
        textBtn.setTextSize(10); // 较小字体
        textBtn.setPadding(0, 0, 0, 0);
        textBtn.setOnClickListener(listener);
        buttonLayout.addView(textBtn);

        return buttonLayout;
    }
    
    /**
     * 处理旋转按钮点击 - 横屏切换到竖屏
     */
    private void handleRotationClick(View v) {
        // 防抖处理，避免快速点击
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_DEBOUNCE_TIME) {
            return;
        }
        lastClickTime = currentTime;

        // 延迟执行旋转操作，避免View ID冲突
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // 横屏切换到竖屏
                    Map<CustomLayoutType, TCICCustomLayoutBuilderItem> customLayoutBuilders = new HashMap<>();

                    TCICCustomLayoutBuilderItem headerBuilderItem = new TCICCustomLayoutBuilderItem(100, 100);
                    headerBuilderItem.setBuilder(HeaderViewCreator::new);
                    headerBuilderItem.setAnimationType(TCICBuilderItemAnimationType.SLIDE_FROM_TOP);


                    TCICCustomLayoutBuilderItem footerBuilderItem = new TCICCustomLayoutBuilderItem(100, 100);
                    footerBuilderItem.setBuilder(FooterNativeViewCreator::new);
                    footerBuilderItem.setAnimationType(TCICBuilderItemAnimationType.SLIDE_FROM_BOTTOM);
                    
                    customLayoutBuilders.put(CustomLayoutType.HEADER, headerBuilderItem);
                    customLayoutBuilders.put(CustomLayoutType.FOOTER, footerBuilderItem);

                    // 延迟切换布局状态，避免在布局切换过程中立即通知
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TCICManager.switchLayoutWithCustom("portrait", customLayoutBuilders);
                            
                            // 再次延迟更新布局状态，确保布局切换完成
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    LayoutManager.setLandscape(false);
                                }
                            }, 100);
                        }
                    }, 50);
                } catch (Exception e) {
                    // 捕获异常，避免崩溃
                    Toast.makeText(context, "旋转操作失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }
        }, 100); // 延迟100ms执行
    }
    
    /**
     * 处理白板按钮点击
     */
    private void handleWhiteboardClick(View v) {
        // 获取横屏模式下的屏幕高度
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int landscapeHeightPx = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);

        // 将像素转换为dp值
        float density = displayMetrics.density;
        int layoutHeight = (int) (landscapeHeightPx / density);

        // 创建自定义布局构建器
        Map<CustomLayoutType, TCICCustomLayoutBuilderItem> customLayoutBuilders = new HashMap<>();

        TCICCustomLayoutBuilderItem whiteBoardBuilderItem = new TCICCustomLayoutBuilderItem(100, layoutHeight);
        // 这里可以添加具体的白板布局逻辑
        whiteBoardBuilderItem.setBuilder(WhiteBoardToolsViewCreator::new);
        whiteBoardBuilderItem.setAnimationType(TCICBuilderItemAnimationType.SLIDE_FROM_RIGHT);
        customLayoutBuilders.put(CustomLayoutType.TOP_RIGHT, whiteBoardBuilderItem);

        TCICManager.snapshotLocalVideo();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                TCICManager.switchLayoutWithCustom("landscape", customLayoutBuilders, false);

                // 再次延迟更新布局状态，确保布局切换完成
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LayoutManager.setLandscape(true);
                    }
                }, 100);
            }
        }, 50);
    }

    // dp 转 px 工具
    private int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void disposeView(View view) {
        Log.d(TAG, "disposeView 被调用" + viewId);
        if(isDisposed) {
            Log.d(TAG, "View " + this.viewId + " already disposed. Ignoring call.");
            return;
        }
        isDisposed = true;
        
        // 移除布局变化监听器
        LayoutManager.removeLayoutChangeListener(this);
        
        // 清理引用
        mainLayout = null;
        titleLayout = null;
        settingsLayout = null;
        exitLayout = null;
        context = null;
    }
}