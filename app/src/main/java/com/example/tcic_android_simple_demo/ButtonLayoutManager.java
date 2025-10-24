package com.example.tcic_android_simple_demo;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 按钮布局管理器 - 负责处理横屏和竖屏模式下的按钮布局
 */
public class ButtonLayoutManager {
    private static final String TAG = "ButtonLayoutManager";
    
    private Context context;
    private LinearLayout mainLayout;
    private LinearLayout titleLayout;
    private LinearLayout settingsLayout;
    private LinearLayout exitLayout;
    
    public ButtonLayoutManager(Context context, LinearLayout mainLayout, 
                              LinearLayout titleLayout, LinearLayout settingsLayout, LinearLayout exitLayout) {
        this.context = context;
        this.mainLayout = mainLayout;
        this.titleLayout = titleLayout;
        this.settingsLayout = settingsLayout;
        this.exitLayout = exitLayout;
    }
    
    /**
     * 根据屏幕方向调整主布局
     */
    public void adjustMainLayoutForOrientation(boolean isLandscape) {
        if (mainLayout == null || context == null) {
            return;
        }
        
        Log.d(TAG, "=== 调整主布局 ===");
        Log.d(TAG, "当前横屏状态: " + isLandscape);
        
        // 根据屏幕方向设置布局方向：竖屏时横向布局，横屏时纵向布局
        if (!isLandscape) {
            mainLayout.setOrientation(LinearLayout.HORIZONTAL);
            mainLayout.setGravity(Gravity.CENTER_VERTICAL);
        } else {
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        
        // 设置按钮布局参数
        setButtonLayoutParams(isLandscape);
        
        // 调整按钮内部布局
        adjustButtonInternalLayouts(isLandscape);
        
        // 请求重新布局
        mainLayout.requestLayout();
    }
    
    /**
     * 设置按钮的布局参数
     */
    private void setButtonLayoutParams(boolean isLandscape) {
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
    }
    
    /**
     * 调整所有按钮的内部布局
     */
    private void adjustButtonInternalLayouts(boolean isLandscape) {
        adjustButtonInternalLayout(titleLayout, isLandscape);
        adjustButtonInternalLayout(settingsLayout, isLandscape);
        adjustButtonInternalLayout(exitLayout, isLandscape);
    }
    
    /**
     * 调整单个按钮的内部布局
     */
    private void adjustButtonInternalLayout(LinearLayout buttonLayout, boolean isLandscape) {
        if (buttonLayout == null || buttonLayout.getChildCount() < 2) {
            return;
        }
        
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
    
    /**
     * 创建按钮布局（竖屏模式）
     */
    public static LinearLayout createPortraitButtonLayout(Context context, int iconResId, String text, View.OnClickListener listener) {
        LinearLayout buttonLayout = new LinearLayout(context);
        
        // 竖屏模式：纵向布局，图标在上文字在下
        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setGravity(Gravity.CENTER);
        buttonLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        buttonLayout.setOnClickListener(listener);
        
        // 添加图标
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconResId);
        icon.setColorFilter(android.graphics.Color.WHITE);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp2px(context, 24), dp2px(context, 24)
        );
        iconParams.setMargins(0, 0, 0, dp2px(context, 4)); // 竖屏时下边距
        icon.setLayoutParams(iconParams);
        buttonLayout.addView(icon);
        
        // 添加文本
        android.widget.Button textBtn = new android.widget.Button(context);
        textBtn.setText(text);
        textBtn.setTextColor(android.graphics.Color.WHITE);
        textBtn.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        textBtn.setTextSize(10);
        textBtn.setPadding(0, 0, 0, 0);
        textBtn.setOnClickListener(listener);
        buttonLayout.addView(textBtn);
        
        return buttonLayout;
    }
    
    /**
     * 创建按钮布局（横屏模式）
     */
    public static LinearLayout createLandscapeButtonLayout(Context context, int iconResId, String text, View.OnClickListener listener) {
        LinearLayout buttonLayout = new LinearLayout(context);
        
        // 横屏模式：横向布局，图标在左文字在右
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER_VERTICAL);
        buttonLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        buttonLayout.setOnClickListener(listener);
        
        // 添加图标
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconResId);
        icon.setColorFilter(android.graphics.Color.WHITE);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp2px(context, 24), dp2px(context, 24)
        );
        iconParams.setMargins(0, 0, dp2px(context, 8), 0); // 横屏时右边距
        icon.setLayoutParams(iconParams);
        buttonLayout.addView(icon);
        
        // 添加文本
        android.widget.Button textBtn = new android.widget.Button(context);
        textBtn.setText(text);
        textBtn.setTextColor(android.graphics.Color.WHITE);
        textBtn.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        textBtn.setTextSize(10);
        textBtn.setPadding(0, 0, 0, 0);
        textBtn.setOnClickListener(listener);
        buttonLayout.addView(textBtn);
        
        return buttonLayout;
    }
    
    /**
     * 创建默认按钮布局（根据当前屏幕方向）
     */
    public static LinearLayout createButtonLayout(Context context, int iconResId, String text, View.OnClickListener listener) {
        boolean isLandscape = LayoutManager.isLandscape();
        if (isLandscape) {
            return createLandscapeButtonLayout(context, iconResId, text, listener);
        } else {
            return createPortraitButtonLayout(context, iconResId, text, listener);
        }
    }
    
    // dp 转 px 工具
    private static int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}