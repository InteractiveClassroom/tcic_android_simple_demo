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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qcloudclass.tcic.CustomLayoutType;
import com.qcloudclass.tcic.NativeViewCreator;
import com.qcloudclass.tcic.TCICConfig;
import com.qcloudclass.tcic.TCICCustomLayoutBuilderItem;
import com.qcloudclass.tcic.TCICLayoutComponentConfig;
import com.qcloudclass.tcic.TCICManager;

import java.util.HashMap;
import java.util.Map;
public class HeaderViewCreator implements NativeViewCreator, LayoutManager.LayoutChangeListener {
    private static final String TAG = "HeaderNativeView";
    private static long lastClickTime = 0;
    private static final long CLICK_DEBOUNCE_TIME = 1000; // 1秒防抖
    
    // 布局相关变量
    private LinearLayout mainLayout;
    private LinearLayout titleLayout;
    private LinearLayout settingsLayout;
    private LinearLayout exitLayout;
    private Context context;

    @Override
    public View createView(Context context, int id, Object args) {
        this.context = context;
        this.mainLayout = new LinearLayout(context);
        
        Log.d(TAG, "=== HeaderViewCreator 创建视图 ===");
        
        // 初始设置为横向布局（默认竖屏状态）
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setGravity(Gravity.CENTER_VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#FF4444"));
        mainLayout.setPadding(dp2px(context, 16), dp2px(context, 16), dp2px(context, 16), dp2px(context, 16));

        // 创建按钮布局
        createButtonLayouts();
        
        // 注册布局变化监听器
        LayoutManager.addLayoutChangeListener(this);
        
        // 根据当前状态调整布局
        adjustLayoutForOrientation();
        
        // 10秒后自动切换到横屏（仅在竖屏模式下执行一次）
//        if (!LayoutManager.isLandscape()) {
//            Log.d(TAG, "设置10秒后自动切换到横屏");
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        // 检查View是否仍然有效且Activity未销毁
//                        if (mainLayout != null && mainLayout.getContext() != null &&
//                            !LayoutManager.isLandscape()) {
//                            Log.d(TAG, "执行自动切换到横屏模式");
//                            // 模拟点击旋转按钮，这样使用已有的安全逻辑
//                            handleRotationClick(mainLayout);
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG, "自动旋转失败: " + e.getMessage());
//                    }
//                }
//            }, 10000); // 10秒后执行
//        }

        return mainLayout;
    }
    
    @Override
    public void onLayoutChanged(boolean isLandscape) {
        Log.d(TAG, "收到布局变化通知: isLandscape = " + isLandscape);
        adjustLayoutForOrientation();
    }
    
    /**
     * 创建按钮布局
     */
    private void createButtonLayouts() {
        // 创建录制按钮
        titleLayout = createButtonWithIcon(context,
                android.R.drawable.ic_menu_agenda, "录制", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TCICManager.startScreenRecording();
                    }
                }
        );
        mainLayout.addView(titleLayout);

        // 创建旋转按钮
        settingsLayout = createButtonWithIcon(context,
                android.R.drawable.ic_menu_preferences, "旋转", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleRotationClick(v);
                    }
                }
        );
        mainLayout.addView(settingsLayout);

        // 创建白板按钮
        exitLayout = createButtonWithIcon(context,
                android.R.drawable.ic_menu_close_clear_cancel, "白板", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleWhiteboardClick(v);
                    }
                }
        );
        mainLayout.addView(exitLayout);
    }
    
    /**
     * 根据屏幕方向调整布局
     */
    private void adjustLayoutForOrientation() {
        if (mainLayout == null || context == null) {
            return;
        }
        
        // 获取当前布局状态
        boolean isLandscape = LayoutManager.isLandscape();
        
        Log.d(TAG, "=== 调整布局 ===");
        Log.d(TAG, "当前横屏状态: " + isLandscape);
        
        // 根据屏幕方向设置布局方向：竖屏时横向布局，横屏时纵向布局
        if (!isLandscape) {
            mainLayout.setOrientation(LinearLayout.HORIZONTAL);
            mainLayout.setGravity(Gravity.CENTER_VERTICAL);
        } else {
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        
        // 根据屏幕方向创建等分布局参数
        LinearLayout.LayoutParams equalParams;
        if (!isLandscape) {
            // 竖屏时横向等分
            equalParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
            );
            equalParams.setMargins(dp2px(context, 4), 0, dp2px(context, 4), 0);
        } else {
            // 横屏时纵向等分
            equalParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1.0f
            );
            equalParams.setMargins(0, dp2px(context, 4), 0, dp2px(context, 4));
        }
        
        // 更新所有按钮的布局参数
        if (titleLayout != null) titleLayout.setLayoutParams(equalParams);
        if (settingsLayout != null) settingsLayout.setLayoutParams(equalParams);
        if (exitLayout != null) exitLayout.setLayoutParams(equalParams);
        
        // 重新调整按钮内部布局
        adjustButtonInternalLayout(titleLayout, isLandscape);
        adjustButtonInternalLayout(settingsLayout, isLandscape);
        adjustButtonInternalLayout(exitLayout, isLandscape);
        
        // 请求重新布局
        mainLayout.requestLayout();
    }
    
    /**
     * 调整按钮内部布局
     */
    private void adjustButtonInternalLayout(LinearLayout buttonLayout, boolean isLandscape) {
        if (buttonLayout.getChildCount() >= 2) {
            ImageView icon = (ImageView) buttonLayout.getChildAt(0);
            
            // 根据屏幕方向设置按钮内部布局
            if (isLandscape) {
                Log.d(TAG, "按钮横屏布局: HORIZONTAL方向，图标在左文字在右");
                // 横屏时按钮内部横向布局（图标在左，文字在右）
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(Gravity.CENTER_VERTICAL);
                
                // 调整图标边距
                LinearLayout.LayoutParams iconParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
                iconParams.setMargins(0, 0, dp2px(context, 8), 0);
                icon.setLayoutParams(iconParams);
            } else {
                Log.d(TAG, "按钮竖屏布局: VERTICAL方向，图标在上文字在下");
                // 竖屏时按钮内部纵向布局（图标在上，文字在下）
                buttonLayout.setOrientation(LinearLayout.VERTICAL);
                buttonLayout.setGravity(Gravity.CENTER);
                
                // 调整图标边距
                LinearLayout.LayoutParams iconParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
                iconParams.setMargins(0, 0, 0, dp2px(context, 4));
                icon.setLayoutParams(iconParams);
            }
        }
    }
    
    /**
     * 处理旋转按钮点击
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
                    // 检测当前屏幕方向并切换到相反方向
                    int currentOrientation = context.getResources().getConfiguration().orientation;
                    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        // 当前是横屏，切换到竖屏
                        Map<CustomLayoutType, TCICCustomLayoutBuilderItem> customLayoutBuilders = new HashMap<>();

                        TCICCustomLayoutBuilderItem headerBuilderItem = new TCICCustomLayoutBuilderItem(100, 100);
                        headerBuilderItem.setBuilder(HeaderViewCreator::new);

                        TCICCustomLayoutBuilderItem footerBuilderItem = new TCICCustomLayoutBuilderItem(100, 100);
                        footerBuilderItem.setBuilder(FooterNativeViewCreator::new);
                        // Header 布局
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
                    } else {
                        // 获取横屏模式下的屏幕高度
                        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                        int landscapeHeightPx = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);

                        // 将像素转换为dp值
                        float density = displayMetrics.density;
                        int layoutHeight = (int) (landscapeHeightPx / density);

                        // 创建自定义布局构建器
                        Map<CustomLayoutType, TCICCustomLayoutBuilderItem> customLayoutBuilders = new HashMap<>();

                        TCICCustomLayoutBuilderItem leftBuilderItem = new TCICCustomLayoutBuilderItem(100, layoutHeight);
                        leftBuilderItem.setBuilder(HeaderViewCreator::new);

                        TCICCustomLayoutBuilderItem footerBuilderItem = new TCICCustomLayoutBuilderItem(100, layoutHeight);
                        footerBuilderItem.setBuilder(FooterNativeViewCreator::new);

                        customLayoutBuilders.put(CustomLayoutType.TOP_LEFT, leftBuilderItem);
                        customLayoutBuilders.put(CustomLayoutType.TOP_RIGHT, footerBuilderItem);

                        // 延迟切换布局状态，避免在布局切换过程中立即通知
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 使用便捷API切换布局
                                TCICManager.switchLayoutWithCustom("landscape", customLayoutBuilders);
                                
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
        customLayoutBuilders.put(CustomLayoutType.TOP_RIGHT, whiteBoardBuilderItem);

        TCICManager.snapshotLocalVideo();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                TCICManager.switchLayoutWithCustom("landscape", customLayoutBuilders);

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

    // 创建带图标的按钮布局
    private LinearLayout createButtonWithIcon(Context context, int iconResId, String text, View.OnClickListener listener) {
        LinearLayout buttonLayout = new LinearLayout(context);

        // 初始设置为纵向布局（默认竖屏状态）
        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setGravity(Gravity.CENTER);
        buttonLayout.setBackgroundColor(Color.TRANSPARENT);
        buttonLayout.setOnClickListener(listener);

        // 添加图标
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconResId);
        icon.setColorFilter(Color.WHITE);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp2px(context, 24), dp2px(context, 24)
        );
        // 初始设置为竖屏时的下边距
        iconParams.setMargins(0, 0, 0, dp2px(context, 4));
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

    // dp 转 px 工具
    private int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void disposeView(View view) {
        Log.d(TAG, "disposeView 被调用");
        
        // 移除布局变化监听器
        LayoutManager.removeLayoutChangeListener(this);
        
        // 清理 View 的点击监听器，避免内存泄漏
        if (view instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) view;
//            clearViewListeners(layout);
        }
        
        // 清理引用
        mainLayout = null;
        titleLayout = null;
        settingsLayout = null;
        exitLayout = null;
        context = null;
    }

    /**
     * 递归清理View及其子View的监听器
     */
    private void clearViewListeners(LinearLayout layout) {
        if (layout == null) return;

        // 清理当前View的监听器
        layout.setOnClickListener(null);

        // 递归清理子View的监听器
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof LinearLayout) {
                clearViewListeners((LinearLayout) child);
            } else if (child instanceof Button) {
                child.setOnClickListener(null);
            } else if (child instanceof ImageView) {
                child.setOnClickListener(null);
            }
        }
    }
}
