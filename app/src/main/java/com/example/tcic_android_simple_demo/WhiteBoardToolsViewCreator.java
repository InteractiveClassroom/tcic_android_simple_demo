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
 * 白板工具栏 View 的具体实现
 * 只在横屏模式下展示，包含退出、画笔、橡皮、画板四个按钮
 */
public class WhiteBoardToolsViewCreator implements NativeViewCreator, LayoutManager.LayoutChangeListener {
    
    private static final String TAG = "WhiteBoardToolsView";
    private static long lastClickTime = 0;
    private static final long CLICK_DEBOUNCE_TIME = 500; // 500ms防抖
    
    // 布局相关变量
    private LinearLayout mainLayout;
    private LinearLayout exitLayout;
    private LinearLayout brushLayout;
    private LinearLayout eraserLayout;
    private LinearLayout boardLayout;
    private Context context;
    
    // 工具状态
    private boolean isBrushSelected = true;  // 默认选中画笔
    private boolean isEraserSelected = false;
    private boolean isBoardVisible = true;   // 默认画板可见

    @Override
    public View createView(Context context, int id, Object args) {
        this.context = context;
        this.mainLayout = new LinearLayout(context);
        
        Log.d(TAG, "=== WhiteBoardToolsViewCreator 创建视图 ===");
        
        // 设置为纵向布局
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setBackgroundColor(Color.parseColor("#2C2C2C")); // 深灰色背景
        mainLayout.setPadding(dp2px(context, 8), dp2px(context, 16), dp2px(context, 8), dp2px(context, 16));

        // 创建按钮布局
        createButtonLayouts();
        
        // 注册布局变化监听器
        LayoutManager.addLayoutChangeListener(this);
        
        // 根据当前状态设置可见性
        updateVisibility();

        return mainLayout;
    }
    
    @Override
    public void onLayoutChanged(boolean isLandscape) {
        Log.d(TAG, "收到布局变化通知: isLandscape = " + isLandscape);
        
        // 延迟更新可见性，避免在屏幕旋转过程中立即操作View
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                updateVisibility();
            }
        }, 200); // 延迟200ms，等待屏幕旋转完成
    }
    
    /**
     * 根据横竖屏状态更新可见性
     */
    private void updateVisibility() {
        if (mainLayout == null || context == null) {
            return;
        }
        
        try {
            boolean isLandscape = LayoutManager.isLandscape();
            Log.d(TAG, "更新可见性: isLandscape = " + isLandscape);
            
            // 只在横屏模式下显示，使用平滑的可见性切换
            if (isLandscape) {
                if (mainLayout.getVisibility() != View.VISIBLE) {
                    mainLayout.setVisibility(View.VISIBLE);
                    // 可以添加淡入动画
                    mainLayout.setAlpha(0f);
                    mainLayout.animate().alpha(1f).setDuration(300).start();
                }
            } else {
                if (mainLayout.getVisibility() != View.GONE) {
                    // 可以添加淡出动画
                    mainLayout.animate().alpha(0f).setDuration(200).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            if (mainLayout != null) {
                                mainLayout.setVisibility(View.GONE);
                            }
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "更新可见性时出错: " + e.getMessage());
            // 降级处理：直接设置可见性，不使用动画
            boolean isLandscape = LayoutManager.isLandscape();
            mainLayout.setVisibility(isLandscape ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * 创建按钮布局
     */
    private void createButtonLayouts() {
        // 纵向等分布局参数
        LinearLayout.LayoutParams equalParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
        );
        equalParams.setMargins(0, dp2px(context, 4), 0, dp2px(context, 4));

        // 创建退出按钮
        exitLayout = createToolButton(context,
                android.R.drawable.ic_menu_close_clear_cancel, "退出", 
                Color.parseColor("#FF4444"), // 红色背景
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleExitClick();
                    }
                }
        );
        exitLayout.setLayoutParams(equalParams);
        mainLayout.addView(exitLayout);

        // 创建画笔按钮
        brushLayout = createToolButton(context,
                android.R.drawable.ic_menu_edit, "画笔",
                isBrushSelected ? Color.parseColor("#4CAF50") : Color.parseColor("#555555"),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleBrushClick();
                    }
                }
        );
        brushLayout.setLayoutParams(equalParams);
        mainLayout.addView(brushLayout);

        // 创建橡皮按钮
        eraserLayout = createToolButton(context,
                android.R.drawable.ic_menu_delete, "橡皮",
                isEraserSelected ? Color.parseColor("#4CAF50") : Color.parseColor("#555555"),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleEraserClick();
                    }
                }
        );
        eraserLayout.setLayoutParams(equalParams);
        mainLayout.addView(eraserLayout);

        // 创建画板按钮
        boardLayout = createToolButton(context,
                android.R.drawable.ic_menu_view, "画板",
                isBoardVisible ? Color.parseColor("#4CAF50") : Color.parseColor("#555555"),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleBoardClick();
                    }
                }
        );
        boardLayout.setLayoutParams(equalParams);
        mainLayout.addView(boardLayout);
    }

    /**
     * 创建工具按钮
     */
    private LinearLayout createToolButton(Context context, int iconResId, String text, 
                                        int backgroundColor, View.OnClickListener listener) {
        LinearLayout buttonLayout = new LinearLayout(context);
        
        // 纵向布局（图标在上，文字在下）
        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setGravity(Gravity.CENTER);
        buttonLayout.setBackgroundColor(backgroundColor);
        buttonLayout.setPadding(dp2px(context, 8), dp2px(context, 12), dp2px(context, 8), dp2px(context, 12));
        buttonLayout.setOnClickListener(listener);

        // 添加图标
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconResId);
        icon.setColorFilter(Color.WHITE);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp2px(context, 28), dp2px(context, 28)
        );
        iconParams.setMargins(0, 0, 0, dp2px(context, 8)); // 下边距
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
        
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textBtn.setLayoutParams(textParams);
        buttonLayout.addView(textBtn);

        return buttonLayout;
    }

    /**
     * 处理退出按钮点击
     */
    private void handleExitClick() {
        if (!checkClickDebounce()) return;
        
        Log.d(TAG, "退出白板工具栏");
        Toast.makeText(context, "退出白板", Toast.LENGTH_SHORT).show();
        
        // 这里可以添加退出白板的逻辑
        // 例如：切换回普通模式，隐藏白板等
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
                        headerBuilderItem.setAnimationType(TCICBuilderItemAnimationType.SLIDE_FROM_TOP);

                        TCICCustomLayoutBuilderItem footerBuilderItem = new TCICCustomLayoutBuilderItem(100, 100);
                        footerBuilderItem.setBuilder(FooterNativeViewCreator::new);
                        footerBuilderItem.setAnimationType(TCICBuilderItemAnimationType.SLIDE_FROM_BOTTOM);

                        // Header 布局
                        customLayoutBuilders.put(CustomLayoutType.HEADER, headerBuilderItem);
                        customLayoutBuilders.put(CustomLayoutType.FOOTER, footerBuilderItem);

                        // 延迟切换布局状态，避免在布局切换过程中立即通知
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                TCICManager.switchLayoutWithCustom("portrait", customLayoutBuilders, true);

                                // 再次延迟更新布局状态，确保布局切换完成
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        LayoutManager.setLandscape(false);
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
        }, 100); //
    }

    /**
     * 处理画笔按钮点击
     */
    private void handleBrushClick() {
        if (!checkClickDebounce()) return;
        
        // 切换画笔状态
        isBrushSelected = true;
        isEraserSelected = false;
        
        Log.d(TAG, "选择画笔工具");
        Toast.makeText(context, "已选择画笔", Toast.LENGTH_SHORT).show();
        
        // 更新按钮颜色
        updateButtonColors();

        TCICManager.setWhiteboardToolType(1);

        // 这里可以添加切换到画笔模式的逻辑
    }

    /**
     * 处理橡皮按钮点击
     */
    private void handleEraserClick() {
        if (!checkClickDebounce()) return;
        
        // 切换橡皮状态
        isBrushSelected = false;
        isEraserSelected = true;
        
        Log.d(TAG, "选择橡皮工具");
        Toast.makeText(context, "已选择橡皮", Toast.LENGTH_SHORT).show();
        
        // 更新按钮颜色
        updateButtonColors();

        TCICManager.setWhiteboardToolType(2);
        
        // 这里可以添加切换到橡皮模式的逻辑
    }

    /**
     * 处理画板按钮点击
     */
    private void handleBoardClick() {
        if (!checkClickDebounce()) return;
        
        // 切换画板可见性
        isBoardVisible = !isBoardVisible;

        Toast.makeText(context, "创建新的画板", Toast.LENGTH_SHORT).show();
        
        // 更新按钮颜色
        updateButtonColors();

        TCICManager.addWhiteboardBoard();
        // 这里可以添加显示/隐藏画板的逻辑
    }

    /**
     * 更新按钮颜色状态
     */
    private void updateButtonColors() {
        if (brushLayout != null) {
            brushLayout.setBackgroundColor(
                isBrushSelected ? Color.parseColor("#4CAF50") : Color.parseColor("#555555")
            );
        }
        
        if (eraserLayout != null) {
            eraserLayout.setBackgroundColor(
                isEraserSelected ? Color.parseColor("#4CAF50") : Color.parseColor("#555555")
            );
        }
        
        if (boardLayout != null) {
            boardLayout.setBackgroundColor(
                isBoardVisible ? Color.parseColor("#4CAF50") : Color.parseColor("#555555")
            );
        }
    }

    /**
     * 防抖检查
     */
    private boolean checkClickDebounce() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_DEBOUNCE_TIME) {
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }

    /**
     * dp 转 px 工具
     */
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
            clearViewListeners(layout);
        }
        
        // 清理引用
        mainLayout = null;
        exitLayout = null;
        brushLayout = null;
        eraserLayout = null;
        boardLayout = null;
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