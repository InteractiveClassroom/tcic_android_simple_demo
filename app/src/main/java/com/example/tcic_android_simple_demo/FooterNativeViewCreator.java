package com.example.tcic_android_simple_demo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qcloudclass.tcic.NativeViewCreator;
import com.qcloudclass.tcic.TCICManager;

/**
 * Footer Native View 的具体实现
 * 负责创建包含麦克风切换、挂断和扬声器切换按钮的 Footer View
 */
public class FooterNativeViewCreator implements NativeViewCreator, LayoutManager.LayoutChangeListener {

    private static long lastClickTime = 0;
    private static final long CLICK_DEBOUNCE_TIME = 500; // 500ms防抖
    
    // 状态变量
    private boolean isMicrophoneOn = true;
    private boolean isSpeakerOn = true;
    
    // 布局相关变量
    private LinearLayout mainLayout;
    private LinearLayout microphoneLayout;
    private LinearLayout hangupLayout;
    private LinearLayout speakerLayout;
    private Context context;

    @Override
    public View createView(Context context, int id, Object args) {
        this.context = context;
        this.mainLayout = new LinearLayout(context);
        
        // 初始设置为横向布局（默认竖屏状态）
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setGravity(Gravity.CENTER_VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#333333"));
        mainLayout.setPadding(dp2px(context, 16), dp2px(context, 16), dp2px(context, 16), dp2px(context, 16));

        // 创建按钮布局
        createButtonLayouts();
        
        // 注册布局变化监听器
        LayoutManager.addLayoutChangeListener(this);
        
        // 根据当前状态调整布局
        adjustLayoutForOrientation();

        return mainLayout;
    }
    
    @Override
    public void onLayoutChanged(boolean isLandscape) {
        adjustLayoutForOrientation();
    }
    
    /**
     * 创建按钮布局
     */
    private void createButtonLayouts() {
        // 创建麦克风切换按钮
        microphoneLayout = createButtonWithIcon(context,
                android.R.drawable.ic_btn_speak_now, "麦克风", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleMicrophoneClick(v);
                    }
                }
        );
        mainLayout.addView(microphoneLayout);

        // 创建挂断按钮
        hangupLayout = createButtonWithIcon(context,
                android.R.drawable.ic_menu_call, "挂断", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleHangupClick(v);
                    }
                }
        );
        // 挂断按钮使用红色背景
        hangupLayout.setBackgroundColor(Color.parseColor("#FF4444"));
        mainLayout.addView(hangupLayout);

        // 创建扬声器切换按钮
        speakerLayout = createButtonWithIcon(context,
                android.R.drawable.ic_lock_silent_mode_off, "扬声器", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleSpeakerClick(v);
                    }
                }
        );
        mainLayout.addView(speakerLayout);
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
        if (microphoneLayout != null) microphoneLayout.setLayoutParams(equalParams);
        if (hangupLayout != null) hangupLayout.setLayoutParams(equalParams);
        if (speakerLayout != null) speakerLayout.setLayoutParams(equalParams);
        
        // 重新调整按钮内部布局
        adjustButtonInternalLayout(microphoneLayout, isLandscape);
        adjustButtonInternalLayout(hangupLayout, isLandscape);
        adjustButtonInternalLayout(speakerLayout, isLandscape);
        
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
                // 横屏时按钮内部横向布局（图标在左，文字在右）
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(Gravity.CENTER_VERTICAL);
                
                // 调整图标边距
                LinearLayout.LayoutParams iconParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
                iconParams.setMargins(0, 0, dp2px(context, 8), 0);
                icon.setLayoutParams(iconParams);
            } else {
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
     * 处理麦克风按钮点击
     */
    private void handleMicrophoneClick(View v) {
        // 防抖处理
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_DEBOUNCE_TIME) {
            return;
        }
        lastClickTime = currentTime;

        if (isMicrophoneOn) {
            TCICManager.closeMic();
        } else {
            TCICManager.openMic();
        }
        // 切换麦克风状态
        isMicrophoneOn = !isMicrophoneOn;
        String status = isMicrophoneOn ? "开启" : "关闭";
        Toast.makeText(context, "麦克风已" + status, Toast.LENGTH_SHORT).show();

        // 更新按钮颜色
        updateButtonColor(v, isMicrophoneOn);
    }
    
    /**
     * 处理挂断按钮点击
     */
    private void handleHangupClick(View v) {
        // 防抖处理
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_DEBOUNCE_TIME) {
            return;
        }
        lastClickTime = currentTime;

        // 延迟执行挂断操作
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    TCICManager.quitClass();
                    Toast.makeText(context, "通话已挂断", Toast.LENGTH_SHORT).show();
                    TCICManager.closeTCICActivity();
                    // 这里可以添加实际的挂断逻辑
                } catch (Exception e) {
                    Toast.makeText(context, "挂断操作失败", Toast.LENGTH_SHORT).show();
                }
            }
        }, 50);
    }
    
    /**
     * 处理扬声器按钮点击
     */
    private void handleSpeakerClick(View v) {
        // 防抖处理
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_DEBOUNCE_TIME) {
            return;
        }
        lastClickTime = currentTime;

        // 切换扬声器状态
        isSpeakerOn = !isSpeakerOn;
        String status = isSpeakerOn ? "开启" : "关闭";
        Toast.makeText(context, "扬声器已" + status, Toast.LENGTH_SHORT).show();

        // 更新按钮颜色
        updateButtonColor(v, isSpeakerOn);
    }

    // 创建带图标的按钮布局
    private LinearLayout createButtonWithIcon(Context context, int iconResId, String text, View.OnClickListener listener) {
        LinearLayout buttonLayout = new LinearLayout(context);

        // 初始设置为纵向布局（默认竖屏状态）
        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setGravity(Gravity.CENTER);
        buttonLayout.setBackgroundColor(Color.parseColor("#555555"));
        buttonLayout.setPadding(dp2px(context, 8), dp2px(context, 8), dp2px(context, 8), dp2px(context, 8));
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

    // 更新按钮颜色状态
    private void updateButtonColor(View buttonView, boolean isOn) {
        if (buttonView instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) buttonView;
            if (isOn) {
                layout.setBackgroundColor(Color.parseColor("#4CAF50")); // 绿色表示开启
            } else {
                layout.setBackgroundColor(Color.parseColor("#757575")); // 灰色表示关闭
            }
        }
    }

    // dp 转 px 工具
    private int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void disposeView(View view) {
        // 移除布局变化监听器
        LayoutManager.removeLayoutChangeListener(this);
        
        // 清理 View 的点击监听器，避免内存泄漏
        if (view instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) view;
            clearViewListeners(layout);
        }
        
        // 清理引用
        mainLayout = null;
        microphoneLayout = null;
        hangupLayout = null;
        speakerLayout = null;
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